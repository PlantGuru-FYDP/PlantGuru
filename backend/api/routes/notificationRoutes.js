const express = require('express');
const router = express.Router();
const { body } = require('express-validator');
const { sendPlantAlert, registerDevice } = require('../controllers/notificationController');
const { tokenVerify } = require('../middlewares/tokenVerify');
const NotificationService = require('../services/notificationService');

router.post('/send-plant-alert',
  [
    body('user_id').isInt(),
    body('plant_name').isString(),
    body('message').isString(),
    body('notification_type').isString()
  ],
  sendPlantAlert
);

router.post('/register-device',
    tokenVerify,
  [
    body('fcm_token').isString(),
    body('device_name').optional().isString()
  ],
  registerDevice
);

router.post('/test-notification',
    tokenVerify,
    [
        body('fcm_token').isString(),
    ],
    async (req, res) => {
        try {
            const { fcm_token } = req.body;
            
            const result = await NotificationService.sendNotification(
                fcm_token,
                'Test Notification',
                'This is a test notification from Plant Guru!',
                {
                    type: 'TEST',
                    timestamp: new Date().toISOString()
                }
            );

            res.status(200).json({
                success: true,
                result
            });
        } catch (error) {
            console.error('Test notification error:', error);
            res.status(500).json({
                success: false,
                error: error.message
            });
        }
    }
);

module.exports = router; 