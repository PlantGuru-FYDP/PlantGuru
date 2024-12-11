const express = require('express');
const router = express.Router();
const { body } = require('express-validator');
const { tokenVerify } = require('../middlewares/tokenVerify');
const {
    getUserSettings,
    updateUserSettings,
    getPlantSettings,
    updatePlantSettings
} = require('../controllers/notificationSettingsController');

router.get('/user-notifications-settings', tokenVerify, getUserSettings);

router.put('/user-notifications-settings', [
    tokenVerify,
    body('emailNotifications').isBoolean(),
    body('emailDigests').isBoolean(),
    body('email').optional().isEmail(),
    body('digestsFrequency').isIn(['INSTANT', 'HOURLY', 'DAILY'])
], updateUserSettings);

router.get('/plant-notifications-settings/:plantId', tokenVerify, getPlantSettings);

router.put('/plant-notifications-settings/:plantId', [
    tokenVerify,
    body('extTempNotifications').optional().isBoolean(),
    body('extTempMin').optional().isFloat(),
    body('extTempMax').optional().isFloat(),
    body('humidityNotifications').optional().isBoolean(),
    body('humidityMin').optional().isFloat(),
    body('humidityMax').optional().isFloat(),
    body('lightNotifications').optional().isBoolean(),
    body('lightMin').optional().isFloat(),
    body('lightMax').optional().isFloat(),
    body('soilTempNotifications').optional().isBoolean(),
    body('soilTempMin').optional().isFloat(),
    body('soilTempMax').optional().isFloat(),
    body('soilMoistureNotifications').optional().isBoolean(),
    body('soilMoistureMin').optional().isFloat(),
    body('soilMoistureMax').optional().isFloat(),
    body('wateringReminderEnabled').optional().isBoolean(),
    body('wateringReminderFrequency').optional().isIn(['SMART', 'DAILY', 'CUSTOM']),
    body('wateringReminderInterval').optional().isInt(),
    body('wateringReminderTime').optional().matches(/^([01]\d|2[0-3]):([0-5]\d)$/),
    body('wateringEventNotifications').optional().isBoolean(),
    body('healthStatusNotifications').optional().isBoolean(),
    body('healthCheckFrequency').optional().isIn(['HOURLY', 'DAILY', 'WEEKLY']),
    body('criticalAlertsOnly').optional().isBoolean()
], updatePlantSettings);

module.exports = router; 