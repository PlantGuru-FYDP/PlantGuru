const DEFAULT_USER_SETTINGS = {
    emailNotifications: false,
    emailDigests: false,
    digestsFrequency: 'WEEKLY'
};

const DEFAULT_PLANT_SETTINGS = {
    extTempNotifications: false,
    extTempMin: 15,
    extTempMax: 30,
    humidityNotifications: false,
    humidityMin: 40,
    humidityMax: 80,
    lightNotifications: false,
    lightMin: 100,
    lightMax: 2000,
    soilTempNotifications: false,
    soilTempMin: 15,
    soilTempMax: 25,
    soilMoistureNotifications: false,
    soilMoistureMin: 20,
    soilMoistureMax: 80,
    wateringReminderEnabled: false,
    wateringReminderFrequency: 'SMART',
    wateringReminderInterval: null,
    wateringReminderTime: '09:00',
    wateringEventNotifications: false,
    healthStatusNotifications: false,
    healthCheckFrequency: 'DAILY',
    criticalAlertsOnly: false
};

module.exports = {
    DEFAULT_USER_SETTINGS,
    DEFAULT_PLANT_SETTINGS
}; 