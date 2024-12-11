const NotificationService = require('../services/notificationService');
const DeviceToken = require('../models/deviceTokenModel');

exports.sendPlantAlert = async (req, res) => {
    try {
        const { user_id, plant_name, message, notification_type } = req.body;

        const tokens = await DeviceToken.getTokensByUserId(user_id);
        if (!tokens.length) {
            return res.status(404).send({ message: 'No device tokens found for user' });
        }

        const result = await NotificationService.sendNotification(
            tokens,
            `Alert for ${plant_name}`,
            message,
            {
                type: notification_type,
                plant_name,
                timestamp: new Date().toISOString()
            }
        );

        if (result.failure > 0) {
            result.results.forEach((response, index) => {
                if (response.error?.code === 'messaging/invalid-registration-token') {
                    DeviceToken.removeToken(tokens[index]).catch(console.error);
                }
            });
        }

        return res.status(200).send({
            message: 'Notification sent successfully',
            result
        });
    } catch (error) {
        console.error('Error in sendPlantAlert:', error);
        return res.status(500).send({ message: error.message });
    }
};

exports.registerDevice = async (req, res) => {
    try {
        const { fcm_token, device_name } = req.body;
        const user_id = req.user_id;

        const deviceToken = new DeviceToken(user_id, fcm_token, device_name);
        
        if (await deviceToken.exists()) {
            return res.status(200).send({
                message: 'Device already registered'
            });
        }

        await deviceToken.save();

        return res.status(200).send({
            message: 'Device registered successfully'
        });
    } catch (error) {
        console.error('Error in registerDevice:', error);
        return res.status(500).send({ message: error.message });
    }
}; 