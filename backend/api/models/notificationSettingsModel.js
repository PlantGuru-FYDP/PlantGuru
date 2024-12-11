const connection = require("../../db/connection");

class NotificationSettings {
    static async getUserSettings(userId) {
        console.log(`[getUserSettings] Fetching settings for user_id: ${userId}`);
        const cmd = "SELECT * FROM UserNotificationSettings WHERE user_id = ?";
        try {
            const [rows] = await connection.query(cmd, [userId]);
            console.log(`[getUserSettings] Found settings for user_id: ${userId}:`, rows);
            return rows;
        } catch (err) {
            console.error('[getUserSettings] Error:', err);
            throw err;
        }
    }

    static async updateUserSettings(userId, settings) {
        console.log(`[updateUserSettings] Updating settings for user_id: ${userId}`, settings);
        const cmd = `
            INSERT INTO UserNotificationSettings SET user_id = ?, 
                email_notifications = ?,
                email_digests = ?,
                email = ?,
                digests_frequency = ?
            ON DUPLICATE KEY UPDATE
                email_notifications = VALUES(email_notifications),
                email_digests = VALUES(email_digests),
                email = VALUES(email),
                digests_frequency = VALUES(digests_frequency)
        `;
        
        try {
            await connection.query(cmd, [
                userId,
                settings.emailNotifications,
                settings.emailDigests,
                settings.email,
                settings.digestsFrequency
            ]);
            console.log(`[updateUserSettings] Successfully updated settings for user_id: ${userId}`);
            return this.getUserSettings(userId);
        } catch (err) {
            console.error('[updateUserSettings] Error:', err);
            throw err;
        }
    }

    static async getPlantSettings(plantId) {
        console.log(`[getPlantSettings] Fetching settings for plant_id: ${plantId}`);
        const cmd = "SELECT * FROM PlantNotificationSettings WHERE plant_id = ?";
        try {
            const [rows] = await connection.query(cmd, [plantId]);
            console.log(`[getPlantSettings] Found settings for plant_id: ${plantId}:`, rows);
            return rows;
        } catch (err) {
            console.error('[getPlantSettings] Error:', err);
            throw err;
        }
    }

    static async updatePlantSettings(plantId, settings) {
        console.log(`[updatePlantSettings] Updating settings for plant_id: ${plantId}`, settings);
        const cmd = `
            INSERT INTO PlantNotificationSettings SET plant_id = ?, 
                ext_temp_notifications = ?,
                ext_temp_min = ?,
                ext_temp_max = ?,
                humidity_notifications = ?,
                humidity_min = ?,
                humidity_max = ?,
                light_notifications = ?,
                light_min = ?,
                light_max = ?,
                soil_temp_notifications = ?,
                soil_temp_min = ?,
                soil_temp_max = ?,
                soil_moisture_notifications = ?,
                soil_moisture_min = ?,
                soil_moisture_max = ?,
                watering_reminder_enabled = ?,
                watering_reminder_frequency = ?,
                watering_reminder_interval = ?,
                watering_reminder_time = ?,
                watering_event_notifications = ?,
                health_status_notifications = ?,
                health_check_frequency = ?,
                critical_alerts_only = ?
            ON DUPLICATE KEY UPDATE
                ext_temp_notifications = VALUES(ext_temp_notifications),
                ext_temp_min = VALUES(ext_temp_min),
                ext_temp_max = VALUES(ext_temp_max),
                humidity_notifications = VALUES(humidity_notifications),
                humidity_min = VALUES(humidity_min),
                humidity_max = VALUES(humidity_max),
                light_notifications = VALUES(light_notifications),
                light_min = VALUES(light_min),
                light_max = VALUES(light_max),
                soil_temp_notifications = VALUES(soil_temp_notifications),
                soil_temp_min = VALUES(soil_temp_min),
                soil_temp_max = VALUES(soil_temp_max),
                soil_moisture_notifications = VALUES(soil_moisture_notifications),
                soil_moisture_min = VALUES(soil_moisture_min),
                soil_moisture_max = VALUES(soil_moisture_max),
                watering_reminder_enabled = VALUES(watering_reminder_enabled),
                watering_reminder_frequency = VALUES(watering_reminder_frequency),
                watering_reminder_interval = VALUES(watering_reminder_interval),
                watering_reminder_time = VALUES(watering_reminder_time),
                watering_event_notifications = VALUES(watering_event_notifications),
                health_status_notifications = VALUES(health_status_notifications),
                health_check_frequency = VALUES(health_check_frequency),
                critical_alerts_only = VALUES(critical_alerts_only)
        `;
        
        try {
            await connection.query(cmd, [
                plantId,
                settings.extTempNotifications,
                settings.extTempMin,
                settings.extTempMax,
                settings.humidityNotifications,
                settings.humidityMin,
                settings.humidityMax,
                settings.lightNotifications,
                settings.lightMin,
                settings.lightMax,
                settings.soilTempNotifications,
                settings.soilTempMin,
                settings.soilTempMax,
                settings.soilMoistureNotifications,
                settings.soilMoistureMin,
                settings.soilMoistureMax,
                settings.wateringReminderEnabled,
                settings.wateringReminderFrequency,
                settings.wateringReminderInterval,
                settings.wateringReminderTime,
                settings.wateringEventNotifications,
                settings.healthStatusNotifications,
                settings.healthCheckFrequency,
                settings.criticalAlertsOnly
            ]);
            console.log(`[updatePlantSettings] Successfully updated settings for plant_id: ${plantId}`);
            return this.getPlantSettings(plantId);
        } catch (err) {
            console.error('[updatePlantSettings] Error:', err);
            throw err;
        }
    }

    static async createUserSettings(userId, settings) {
        console.log(`[createUserSettings] Creating settings for user_id: ${userId}`, settings);
        const cmd = `
            INSERT INTO UserNotificationSettings (
                user_id, 
                email_notifications, 
                email_digests, 
                email, 
                digests_frequency
            ) VALUES (?, ?, ?, ?, ?)
        `;
        
        try {
            await connection.query(cmd, [
                userId,
                settings.emailNotifications,
                settings.emailDigests,
                settings.email,
                settings.digestsFrequency
            ]);
            console.log(`[createUserSettings] Successfully created settings for user_id: ${userId}`);
            return this.getUserSettings(userId);
        } catch (err) {
            console.error('[createUserSettings] Error:', err);
            throw err;
        }
    }

    static async createPlantSettings(plantId, settings) {
        console.log(`[createPlantSettings] Creating settings for plant_id: ${plantId}`, settings);
        const cmd = `
            INSERT INTO PlantNotificationSettings (
                plant_id,
                ext_temp_notifications,
                ext_temp_min,
                ext_temp_max,
                humidity_notifications,
                humidity_min,
                humidity_max,
                light_notifications,
                light_min,
                light_max,
                soil_temp_notifications,
                soil_temp_min,
                soil_temp_max,
                soil_moisture_notifications,
                soil_moisture_min,
                soil_moisture_max,
                watering_reminder_enabled,
                watering_reminder_frequency,
                watering_reminder_interval,
                watering_reminder_time,
                watering_event_notifications,
                health_status_notifications,
                health_check_frequency,
                critical_alerts_only
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `;
        
        try {
            await connection.query(cmd, [
                plantId,
                settings.extTempNotifications,
                settings.extTempMin,
                settings.extTempMax,
                settings.humidityNotifications,
                settings.humidityMin,
                settings.humidityMax,
                settings.lightNotifications,
                settings.lightMin,
                settings.lightMax,
                settings.soilTempNotifications,
                settings.soilTempMin,
                settings.soilTempMax,
                settings.soilMoistureNotifications,
                settings.soilMoistureMin,
                settings.soilMoistureMax,
                settings.wateringReminderEnabled,
                settings.wateringReminderFrequency,
                settings.wateringReminderInterval,
                settings.wateringReminderTime,
                settings.wateringEventNotifications,
                settings.healthStatusNotifications,
                settings.healthCheckFrequency,
                settings.criticalAlertsOnly
            ]);
            console.log(`[createPlantSettings] Successfully created settings for plant_id: ${plantId}`);
            return this.getPlantSettings(plantId);
        } catch (err) {
            console.error('[createPlantSettings] Error:', err);
            throw err;
        }
    }
}

module.exports = NotificationSettings;