const NotificationService = require('./notificationService');
const EmailService = require('./mailService');
const DeviceToken = require('../models/deviceTokenModel');
const SensorData = require('../models/sensorModel');
const Plant = require('../models/plantModel');
const User = require('../models/userModel');
class PlantMonitoringService {
    async checkPlantConditions(plantId, sensorData) {
        const alerts = [];

        const [user] = await connection.query(`
            SELECT u.email 
            FROM Users u 
            JOIN Plants p ON u.user_id = p.user_id 
            WHERE p.plant_id = ?
        `, [plantId]);
        const userEmail = user?.[0]?.email;

        const avgMoisture = (sensorData.soil_moisture_1 + sensorData.soil_moisture_2) / 2;
        if (avgMoisture < 30) {
            alerts.push({
                type: 'MOISTURE_LOW',
                priority: 'HIGH',
                message: 'Critical: Soil moisture is very low'
            });
            await EmailService.sendMoistureAlert(userEmail, plantId);
        } else if (avgMoisture < 45) {
            alerts.push({
                type: 'MOISTURE_WARNING',
                priority: 'MEDIUM',
                message: 'Warning: Soil moisture is getting low'
            });
        }

        if (sensorData.ext_temp > 30) {
            alerts.push({
                type: 'TEMPERATURE_HIGH',
                priority: 'HIGH',
                message: 'Critical: Temperature is too high'
            });
        }

        const hour = new Date(sensorData.time_stamp).getHours();
        if (hour >= 6 && hour <= 18 && sensorData.light < 1000) {
            alerts.push({
                type: 'LIGHT_LOW',
                priority: 'MEDIUM',
                message: 'Warning: Light levels are low during daylight hours'
            });
        }
        return alerts;
    }

    async processNewSensorData(plantId, sensorData) {
        try {
            const [plantDetails] = await Plant.readData(plantId);
            if (!plantDetails.length) return;
            
            const plant = plantDetails[0];
            const alerts = await this.checkPlantConditions(plantId, sensorData);

            console.log('Notifications disabled - would have sent:', {
                plantId,
                alerts,
                plant: plant.plant_name
            });

        } catch (error) {
            console.error('Error in processNewSensorData:', error);
        }
    }
}

module.exports = new PlantMonitoringService(); 