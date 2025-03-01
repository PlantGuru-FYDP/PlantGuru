/*
Plant Health Check Notification Logic:
1. Checks each sensor type for the plant
2. For each sensor with a valid reading:
   - Gets the current value and status
   - Formats into a human-readable message
3. Sends a notification with:
   - Title: "Health Update: {plant_name}"
   - Message: List of sensor readings and their status
   - Data: Includes structured data for mobile app handling
*/

const NotificationService = require('./notificationService');
const NotificationSettings = require('../models/notificationSettingsModel');
const Plant = require('../models/plantModel');
const SensorData = require('../models/sensorModel');
const DeviceToken = require('../models/deviceTokenModel');
const User = require('../models/userModel');
const EmailService = require('./mailService');
const { getSensorHealth, getHealthDiagnostics } = require('../controllers/insightsController');

class PlantHealthCheckService {
    async checkPlantHealth(plant_id) {
        try {
            const settings = await NotificationSettings.getPlantSettings(plant_id);
            if (!settings?.length || !settings[0].health_status_notifications) {
                return;
            }

            const [plant] = await Plant.readDataById(plant_id);
            const emailId = await User.getUserEmail(plant[0].user_id);
            const plantName = plant[0].plant_name;
            
            if (!plant?.length) {
                return;
            }
            
            const lastSensorData = await SensorData.getLastNSensorReadings(plant_id, 1);
            if (!lastSensorData?.length) {
                return;
            }

            const soil_moisture = lastSensorData[0].soil_moisture_1;
            const threshold = settings[0].soil_moisture_min;
            if (soil_moisture < threshold) {
                await EmailService.sendMoistureAlert(threshold, soil_moisture, emailId, plantName);
            }
            
            const tokens = await DeviceToken.getTokensByUserId(plant[0].user_id);
            if (!tokens?.length) {
                return;
            }

            const sensorTypes = ['ext_temp', 'humidity', 'light', 'soil_moisture_1', 'soil_moisture_2'];
            const healthIssues = [];

            const healthDiagnostics = await getHealthDiagnostics(
                { query: { plant_id } },
                { status: () => ({ send: (data) => data }) }
            );

            for (const sensorType of sensorTypes) {
                const healthResult = await getSensorHealth(
                    { query: { plant_id, sensor_type: sensorType } },
                    { status: () => ({ send: (data) => data }) }
                );
                
                if (healthResult?.immediate_status) {
                    healthIssues.push({
                        type: sensorType,
                        status: healthResult.immediate_status.status,
                        value: healthResult.immediate_status.current_value.value,
                        unit: healthResult.immediate_status.current_value.unit
                    });
                }
            }

            if (healthIssues.length > 0) {
                const title = `Health Update: ${plant[0].plant_name}`;
                
                const healthScore = healthDiagnostics?.health_score || 0;
                const overallHealth = healthDiagnostics?.overall_health || 'UNKNOWN';
                
                const message = this.formatHealthMessage(healthIssues, {
                    overall_health: overallHealth,
                    health_score: healthScore
                });

                const notificationData = {
                    type: 'HEALTH_UPDATE',
                    plant_id: plant_id.toString(),
                    timestamp: new Date().toISOString(),
                    issues: JSON.stringify(healthIssues),
                    overall_health: overallHealth.toString(),
                    health_score: healthScore.toString()
                };

                await NotificationService.sendNotification(
                    tokens,
                    title,
                    message,
                    notificationData
                );
            }
        } catch (error) {
            console.error(`Error checking health for plant ${plant_id}:`, error);
            throw error;
        }
    }

    formatHealthMessage(issues, healthDiagnostics) {
        const score = Number(healthDiagnostics.health_score) || 0;
        const overallHealthMsg = `Overall Health: ${healthDiagnostics.overall_health} (${score}%)\n`;
        
        if (issues.length === 0) return overallHealthMsg + "All parameters are within normal range";

        return overallHealthMsg + issues.map(issue => {
            const status = issue.status === 'GOOD' ? 'Normal' :
                          issue.status === 'CRITICAL' ? 'Critical' : 'Warning';
            
            return `${this.formatSensorName(issue.type)}: ${issue.value}${issue.unit} (${status})`;
        }).join('\n');
    }

    formatSensorName(sensorType) {
        const names = {
            'ext_temp': 'Temperature',
            'humidity': 'Humidity',
            'light': 'Light level',
            'soil_moisture_1': 'Soil moisture',
            'soil_moisture_2': 'Soil moisture'
        };
        return names[sensorType] || sensorType;
    }
}

module.exports = new PlantHealthCheckService();