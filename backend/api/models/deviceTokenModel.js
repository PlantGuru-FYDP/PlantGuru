const connection = require("../../db/connection");

class DeviceToken {
    constructor(user_id, fcm_token, device_name = null) {
        this.user_id = user_id;
        this.fcm_token = fcm_token;
        this.device_name = device_name;
    }

    async save() {
        const cmd = `
            INSERT INTO DeviceTokens (user_id, fcm_token, device_name)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                device_name = VALUES(device_name),
                last_used = CURRENT_TIMESTAMP
        `;
        return connection.query(cmd, [this.user_id, this.fcm_token, this.device_name]);
    }

    async exists() {
        const cmd = "SELECT token_id FROM DeviceTokens WHERE fcm_token = ?";
        const [rows] = await connection.query(cmd, [this.fcm_token]);
        return rows.length > 0;
    }

    static async getTokensByUserId(user_id) {
        const cmd = "SELECT fcm_token FROM DeviceTokens WHERE user_id = ?";
        try {
            const [rows] = await connection.query(cmd, [user_id]);
            return rows.map(row => row.fcm_token);
        } catch (err) {
            console.error('Error getting tokens:', err);
            throw err;
        }
    }

    static async removeToken(token) {
        const cmd = "DELETE FROM DeviceTokens WHERE fcm_token = ?";
        try {
            console.log(`[DeviceToken] Removing invalid token: ${token}`);
            const [result] = await connection.query(cmd, [token]);
            console.log(`[DeviceToken] Removed token with result:`, result);
            return result;
        } catch (err) {
            console.error('[DeviceToken] Error removing token:', err);
            throw err;
        }
    }

    static async removeTokens(tokens) {
        if (!tokens || tokens.length === 0) return;
        
        const cmd = "DELETE FROM DeviceTokens WHERE fcm_token IN (?)";
        try {
            console.log(`[DeviceToken] Removing ${tokens.length} invalid tokens`);
            const [result] = await connection.query(cmd, [tokens]);
            console.log(`[DeviceToken] Removed tokens with result:`, result);
            return result;
        } catch (err) {
            console.error('[DeviceToken] Error removing tokens:', err);
            throw err;
        }
    }
}

module.exports = DeviceToken; 