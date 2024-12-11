const connection = require("../../db/connection");

class DeviceToken {
    constructor(user_id, fcm_token, device_name) {
        this.user_id = user_id;
        this.fcm_token = fcm_token;
        this.device_name = device_name;
    }

    async exists() {
        const cmd = "SELECT COUNT(*) as count FROM DeviceTokens WHERE fcm_token = ?";
        const [rows] = await connection.query(cmd, [this.fcm_token]);
        return rows[0].count > 0;
    }

    async save() {
        const cmd = `
            INSERT INTO DeviceTokens (user_id, fcm_token, device_name) 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                user_id = VALUES(user_id),
                device_name = VALUES(device_name)
        `;
        return connection.query(cmd, [this.user_id, this.fcm_token, this.device_name]);
    }

    static async getTokensByUserId(user_id) {
        const cmd = "SELECT fcm_token FROM DeviceTokens WHERE user_id = ?";
        const [rows] = await connection.query(cmd, [user_id]);
        return rows.map(row => row.fcm_token);
    }

    static async removeToken(token) {
        const cmd = "DELETE FROM DeviceTokens WHERE fcm_token = ?";
        return connection.query(cmd, [token]);
    }
}

module.exports = DeviceToken; 