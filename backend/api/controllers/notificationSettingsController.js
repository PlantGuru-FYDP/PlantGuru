const NotificationSettings = require('../models/notificationSettingsModel');
const Plant = require('../models/plantModel');
const { DEFAULT_USER_SETTINGS, DEFAULT_PLANT_SETTINGS } = require('../constants/defaultSettings');

exports.getUserSettings = async (req, res) => {
    console.log(`[getUserSettings] Getting settings for user_id: ${req.user_id}`);
    try {
        const userId = req.user_id;
        let settings = await NotificationSettings.getUserSettings(userId);
        
        if (!settings || settings.length === 0 || 
            settings[0].email_notifications === null || 
            settings[0].email_digests === null || 
            settings[0].digests_frequency === null) {
            
            console.log(`[getUserSettings] Invalid settings found for user_id: ${userId}, updating with defaults`);
            const defaultSettings = {
                userId: userId,
                email: settings?.[0]?.email ?? null,
                ...DEFAULT_USER_SETTINGS
            };
            
            await NotificationSettings.updateUserSettings(userId, defaultSettings);
            settings = await NotificationSettings.getUserSettings(userId);
            console.log(`[getUserSettings] Updated settings with defaults for user_id: ${userId}`);
        }
        
        const sanitizedSettings = {
            ...settings[0],
            emailNotifications: settings[0].email_notifications ?? false,
            emailDigests: settings[0].email_digests ?? false,
            digestsFrequency: settings[0].digests_frequency ?? 'INSTANT'
        };
        
        console.log(`[getUserSettings] Returning settings for user_id: ${userId}:`, sanitizedSettings);
        return res.status(200).json(sanitizedSettings);
    } catch (err) {
        console.error('[getUserSettings] Error:', err);
        return res.status(500).json({ message: err.message });
    }
};

exports.updateUserSettings = async (req, res) => {
    console.log(`[updateUserSettings] Updating settings for user_id: ${req.user_id}`);
    console.log('[updateUserSettings] Request body:', req.body);
    try {
        const userId = req.user_id;
        const settings = req.body;
        
        await NotificationSettings.updateUserSettings(userId, {
            emailNotifications: settings.email_notifications,
            emailDigests: settings.email_digests,
            email: settings.email,
            digestsFrequency: settings.digests_frequency || 'INSTANT'
        });
        
        const result = await NotificationSettings.getUserSettings(userId);
        
        const sanitizedResult = {
            ...result[0],
            emailNotifications: Boolean(result[0].email_notifications),
            emailDigests: Boolean(result[0].email_digests),
            digestsFrequency: result[0].digests_frequency || 'INSTANT'
        };
        
        console.log(`[updateUserSettings] Settings updated for user_id: ${userId}:`, sanitizedResult);
        return res.status(200).json(sanitizedResult);
    } catch (err) {
        console.error('[updateUserSettings] Error:', err);
        return res.status(500).json({ message: err.message });
    }
};

exports.getPlantSettings = async (req, res) => {
    console.log(`[getPlantSettings] Getting settings for plant_id: ${req.params.plantId}`);
    try {
        const plantId = req.params.plantId;
        
        const [plant] = await Plant.readDataById(plantId);
        if (!plant || plant.length === 0 || plant[0].user_id !== req.user_id) {
            console.log(`[getPlantSettings] Unauthorized access attempt for plant_id: ${plantId} by user_id: ${req.user_id}`);
            return res.status(403).json({ message: 'Unauthorized access to plant settings' });
        }
        
        let settings = await NotificationSettings.getPlantSettings(plantId);
        
        if (!settings || settings.length === 0) {
            console.log(`[getPlantSettings] No settings found for plant_id: ${plantId}, creating defaults`);
            const defaultSettings = {
                plantId: plantId,
                ...DEFAULT_PLANT_SETTINGS
            };
            
            await NotificationSettings.createPlantSettings(plantId, defaultSettings);
            settings = await NotificationSettings.getPlantSettings(plantId);
            console.log(`[getPlantSettings] Created default settings for plant_id: ${plantId}`);
        }

        const sanitizedSettings = {
            ...settings[0],
            extTempNotifications: settings[0].extTempNotifications ?? true,
            humidityNotifications: settings[0].humidityNotifications ?? true,
            lightNotifications: settings[0].lightNotifications ?? true,
            soilTempNotifications: settings[0].soilTempNotifications ?? true,
            soilMoistureNotifications: settings[0].soilMoistureNotifications ?? true,
            wateringReminderEnabled: settings[0].wateringReminderEnabled ?? true,
            wateringEventNotifications: settings[0].wateringEventNotifications ?? true,
            healthStatusNotifications: settings[0].healthStatusNotifications ?? true,
            criticalAlertsOnly: settings[0].criticalAlertsOnly ?? false,
            wateringReminderFrequency: settings[0].wateringReminderFrequency ?? 'SMART',
            healthCheckFrequency: settings[0].healthCheckFrequency ?? 'DAILY',
            wateringReminderTime: settings[0].wateringReminderTime ?? '09:00'
        };
        
        console.log(`[getPlantSettings] Returning settings for plant_id: ${plantId}:`, sanitizedSettings);
        return res.status(200).json(sanitizedSettings);
    } catch (err) {
        console.error('[getPlantSettings] Error:', err);
        return res.status(500).json({ message: err.message });
    }
};

exports.updatePlantSettings = async (req, res) => {
    console.log(`[updatePlantSettings] Updating settings for plant_id: ${req.params.plantId}`);
    console.log('[updatePlantSettings] Request body:', req.body);
    try {
        const plantId = req.params.plantId;
        
        const [plant] = await Plant.readDataById(plantId);
        if (!plant || plant.length === 0 || plant[0].user_id !== req.user_id) {
            console.log(`[updatePlantSettings] Unauthorized access attempt for plant_id: ${plantId} by user_id: ${req.user_id}`);
            return res.status(403).json({ message: 'Unauthorized access to plant settings' });
        }
        
        const settings = req.body;
        
        await NotificationSettings.updatePlantSettings(plantId, {
            extTempNotifications: settings.ext_temp_notifications,
            extTempMin: settings.ext_temp_min,
            extTempMax: settings.ext_temp_max,
            humidityNotifications: settings.humidity_notifications,
            humidityMin: settings.humidity_min,
            humidityMax: settings.humidity_max,
            lightNotifications: settings.light_notifications,
            lightMin: settings.light_min,
            lightMax: settings.light_max,
            soilTempNotifications: settings.soil_temp_notifications,
            soilTempMin: settings.soil_temp_min,
            soilTempMax: settings.soil_temp_max,
            soilMoistureNotifications: settings.soil_moisture_notifications,
            soilMoistureMin: settings.soil_moisture_min,
            soilMoistureMax: settings.soil_moisture_max,
            wateringReminderEnabled: settings.watering_reminder_enabled,
            wateringReminderFrequency: settings.watering_reminder_frequency,
            wateringReminderInterval: settings.watering_reminder_interval,
            wateringReminderTime: settings.watering_reminder_time,
            wateringEventNotifications: settings.watering_event_notifications,
            healthStatusNotifications: settings.health_status_notifications,
            healthCheckFrequency: settings.health_check_frequency,
            criticalAlertsOnly: settings.critical_alerts_only
        });
        
        const result = await NotificationSettings.getPlantSettings(plantId);
        
        const sanitizedResult = {
            ...result[0],
            extTempNotifications: Boolean(result[0].ext_temp_notifications),
            humidityNotifications: Boolean(result[0].humidity_notifications),
            lightNotifications: Boolean(result[0].light_notifications),
            soilTempNotifications: Boolean(result[0].soil_temp_notifications),
            soilMoistureNotifications: Boolean(result[0].soil_moisture_notifications),
            wateringReminderEnabled: Boolean(result[0].watering_reminder_enabled),
            wateringEventNotifications: Boolean(result[0].watering_event_notifications),
            healthStatusNotifications: Boolean(result[0].health_status_notifications),
            criticalAlertsOnly: Boolean(result[0].critical_alerts_only),
            extTempMin: result[0].ext_temp_min,
            extTempMax: result[0].ext_temp_max,
            humidityMin: result[0].humidity_min,
            humidityMax: result[0].humidity_max,
            wateringReminderFrequency: result[0].watering_reminder_frequency || 'SMART',
            wateringReminderInterval: result[0].watering_reminder_interval,
            wateringReminderTime: result[0].watering_reminder_time || '09:00',
            healthCheckFrequency: result[0].health_check_frequency || 'DAILY'
        };
        
        console.log(`[updatePlantSettings] Settings updated for plant_id: ${plantId}:`, sanitizedResult);
        return res.status(200).json(sanitizedResult);
    } catch (err) {
        console.error('[updatePlantSettings] Error:', err);
        return res.status(500).json({ message: err.message });
    }
}; 