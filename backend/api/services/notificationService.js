const { messaging } = require('./firebaseService');
const DeviceToken = require('../models/deviceTokenModel');

class NotificationService {
    async sendNotification(tokens, title, body, data = {}) {
        try {
            const tokenArray = Array.isArray(tokens) ? tokens : [tokens];
            const invalidTokens = [];
            
            const promises = tokenArray.map(async token => {
                try {
                    const message = {
                        token: token,
                        notification: {
                            title,
                            body
                        },
                        android: {
                            notification: {
                                clickAction: 'FLUTTER_NOTIFICATION_CLICK'
                            }
                        },
                        apns: {
                            payload: {
                                aps: {
                                    'mutable-content': 1
                                }
                            }
                        },
                        data: {
                            ...data,
                            click_action: 'FLUTTER_NOTIFICATION_CLICK'
                        }
                    };
                    
                    const response = await messaging.send(message);
                    return { success: true, response };
                } catch (error) {
                    if (error.errorInfo?.code === 'messaging/registration-token-not-registered') {
                        invalidTokens.push(token);
                        console.log(`Invalid token found: ${token}`);
                        return { success: false, token };
                    }
                    throw error;
                }
            });

            const results = await Promise.all(promises);
            
            if (invalidTokens.length > 0) {
                console.log(`Cleaning up ${invalidTokens.length} invalid tokens`);
                try {
                    await DeviceToken.removeTokens(invalidTokens);
                } catch (error) {
                    console.error('Error cleaning up invalid tokens:', error);
                }
            }

            const successfulSends = results.filter(r => r.success).length;
            console.log(`Successfully sent ${successfulSends} out of ${tokenArray.length} messages`);
            
            return results;
        } catch (error) {
            console.error('Error sending notification:', error);
            throw error;
        }
    }
}

module.exports = new NotificationService(); 