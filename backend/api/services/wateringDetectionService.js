const WateringEvent = require("../models/wateringEventModel");
const NotificationService = require("./notificationService");
const DeviceToken = require("../models/deviceTokenModel");
const Plant = require("../models/plantModel");
const SensorData = require("../models/sensorModel");
const NotificationSettings = require('../models/notificationSettingsModel');

class WateringDetectionService {
    constructor() {
        this.MOISTURE_INCREASE_THRESHOLD = 10; // 10% absolute increase
        this.COOLDOWN_HOURS = 24; // 24 hour cooldown between watering events
        this.MAX_EVENT_DURATION_MINS = 30; // Maximum duration to look back for peak
        this.lastWateringTimes = new Map(); // plant_id -> last watering time
    }

    async detectWateringEvent(plant_id, currentData) {
        try {
            // Check cooldown period
            const lastWateringTime = this.lastWateringTimes.get(plant_id);
            if (lastWateringTime) {
                const hoursSinceLastWatering = (new Date(currentData.time_stamp) - lastWateringTime) / (1000 * 60 * 60);
                if (hoursSinceLastWatering < this.COOLDOWN_HOURS) {
                    return; // Still in cooldown period
                }
            }

            // Get previous reading to compare
            const [previousReadings] = await SensorData.getLastNSensorReadings(plant_id, 2);
            if (previousReadings.length < 2) return; // Need at least 2 readings

            const previousReading = previousReadings[1]; // Second most recent reading
            const moistureIncrease = currentData.soil_moisture_1 - previousReading.soil_moisture_1;

            if (moistureIncrease >= this.MOISTURE_INCREASE_THRESHOLD) {
                // Look back up to MAX_EVENT_DURATION_MINS to find peak moisture
                const lookbackTime = new Date(currentData.time_stamp);
                lookbackTime.setMinutes(lookbackTime.getMinutes() - this.MAX_EVENT_DURATION_MINS);

                const [recentReadings] = await SensorData.readDataSeries(
                    plant_id,
                    lookbackTime.toISOString(),
                    currentData.time_stamp
                );

                if (recentReadings.length > 0) {
                    // Find peak moisture and calculate averages
                    const peakMoisture = Math.max(...recentReadings.map(r => r.soil_moisture_1));
                    const avgTemp = recentReadings.reduce((sum, r) => sum + r.ext_temp, 0) / recentReadings.length;
                    const avgMoisture = recentReadings.reduce((sum, r) => sum + r.soil_moisture_1, 0) / recentReadings.length;
                    const duration = Math.round((new Date(currentData.time_stamp) - new Date(recentReadings[0].time_stamp)) / 1000); // in seconds

                    // Create watering event
                    const wateringEvent = new WateringEvent({
                        watering_duration: duration,
                        peak_temp: currentData.ext_temp,
                        peak_moisture: peakMoisture,
                        avg_temp: avgTemp,
                        avg_moisture: avgMoisture,
                        plant_id: plant_id,
                        time_stamp: currentData.time_stamp,
                        volume: null // We can't measure this directly
                    });

                    await wateringEvent.uploadData();
                    await this.sendWateringNotification(plant_id, wateringEvent);

                    // Update cooldown timer
                    this.lastWateringTimes.set(plant_id, new Date(currentData.time_stamp));
                }
            }
        } catch (error) {
            console.error('Error in detectWateringEvent:', error);
        }
    }

    async sendWateringNotification(plant_id, wateringEvent) {
        try {
            const [plantDetails] = await Plant.readDataById(plant_id);
            if (!plantDetails.length) return;

            const plant = plantDetails[0];
            const tokens = await DeviceToken.getTokensByUserId(plant.user_id);
            if (!tokens.length) return;

            const title = `${plant.plant_name} Watering Detected`;
            const body = `Watering event detected (${Math.round(wateringEvent.watering_duration / 60)} minutes)`;

            const data = {
                type: 'WATERING_EVENT',
                plantId: plant_id.toString(),
                wateringDuration: wateringEvent.watering_duration.toString(),
                timestamp: wateringEvent.time_stamp,
                isAutoDetected: 'true'
            };

            await NotificationService.sendNotification(tokens, title, body, data);
        } catch (error) {
            console.error('Error sending watering notification:', error);
        }
    }
}

module.exports = new WateringDetectionService(); 