const jwt = require("jsonwebtoken");
const connection = require("../../db/connection");
const { v4: uuidv4 } = require('uuid');
require("dotenv").config();

const VALID_TRANSITIONS = {
    'PENDING': ['BACKEND_VERIFIED', 'FAILED'],
    'BACKEND_VERIFIED': ['COMPLETED', 'FAILED'],
    'COMPLETED': [],
    'FAILED': []
};

class ProvisioningController {
    static async validateStateTransition(currentState, newState) {
        console.log(`[Provisioning] Validating state transition: ${currentState} -> ${newState}`);
        if (!VALID_TRANSITIONS[currentState]?.includes(newState)) {
            console.error(`[Provisioning] Invalid state transition from ${currentState} to ${newState}`);
            throw new Error(`Invalid state transition from ${currentState} to ${newState}`);
        }
        console.log(`[Provisioning] State transition valid`);
        return true;
    }

    static async updateProvisioningStatus(req, res) {
        const { provision_token, device_id, status } = req.body;
        console.log('\n=== Provisioning Status Update ===');
        console.log('Request body:', req.body);
        
        if (!provision_token || !status) {
            console.error('[Provisioning] Missing required fields');
            return res.status(400).json({ message: "provision_token and status are required" });
        }

        try {
            console.log(`[Provisioning] Fetching record for token: ${provision_token}`);
            const [existing] = await connection.query(
                'SELECT * FROM DeviceProvisioning WHERE provision_token = ? AND status NOT IN ("COMPLETED", "FAILED") AND expires_at > NOW()',
                [provision_token]
            );

            if (!existing.length) {
                console.error('[Provisioning] No valid provisioning record found');
                return res.status(404).json({ message: "Invalid, expired, or completed provisioning token" });
            }

            console.log(`[Provisioning] Current status: ${existing[0].status}`);
            console.log(`[Provisioning] Requested status: ${status}`);

            try {
                await this.validateStateTransition(existing[0].status, status);
            } catch (error) {
                console.error('[Provisioning] State transition validation failed:', error.message);
                return res.status(400).json({ message: error.message });
            }

            switch (status) {
                case 'DEVICE_CONNECTED':
                    if (!device_id) {
                        console.error('[Provisioning] Missing device_id for DEVICE_CONNECTED state');
                        return res.status(400).json({ message: "device_id is required for DEVICE_CONNECTED state" });
                    }
                    console.log(`[Provisioning] Device connected: ${device_id}`);
                    break;

                case 'WIFI_SETUP':
                    console.log('[Provisioning] WiFi setup completed');
                    break;

                case 'BACKEND_VERIFIED':
                    console.log('[Provisioning] Backend verification successful');
                    break;

                case 'COMPLETED':
                    console.log('[Provisioning] Provisioning completed successfully');
                    break;

                case 'FAILED':
                    const failureReason = req.body.reason || 'Unknown failure';
                    console.error(`[Provisioning] Failed - Reason: ${failureReason}`);
                    break;
            }

            console.log('[Provisioning] Updating record in database');
            await connection.query(
                `UPDATE DeviceProvisioning 
                 SET status = ?, 
                     device_id = COALESCE(?, device_id)
                 WHERE provision_token = ?`,
                [status, device_id, provision_token]
            );

            console.log('[Provisioning] Status update successful');
            return res.status(200).json({ 
                message: "Status updated",
                status: status
            });
        } catch (err) {
            console.error('[Provisioning] Error updating status:', err);
            return res.status(500).json({ message: "Internal server error" });
        }
    }

    static async getProvisioningStatus(req, res) {
        const { provision_token } = req.params;
        
        if (!provision_token) {
            return res.status(400).json({ message: "provision_token is required" });
        }

        try {
            const [result] = await connection.query(
                `SELECT dp.status, dp.device_id, dp.plant_id, dp.expires_at,
                        TIMESTAMPDIFF(SECOND, NOW(), dp.expires_at) as seconds_until_expiry
                 FROM DeviceProvisioning dp
                 WHERE dp.provision_token = ? AND dp.expires_at > NOW()`,
                [provision_token]
            );

            if (!result.length) {
                return res.status(404).json({ message: "Provisioning token not found or expired" });
            }

            const response = { ...result[0] };
            if (response.seconds_until_expiry < 300) {
                response.warning = "Provisioning will expire soon";
            }
            delete response.seconds_until_expiry;

            return res.status(200).json(response);
        } catch (err) {
            console.error('Error getting provisioning status:', err);
            return res.status(500).json({ message: "Internal server error" });
        }
    }

    static async verifyDeviceConnection(req, res) {
        const { provision_token, device_id } = req.body;
        console.log('\n=== Device Verification Request ===');
        console.log('Request body:', req.body);
        
        if (!provision_token || !device_id) {
            console.error('[Provisioning] Missing required fields');
            return res.status(400).json({ message: "provision_token and device_id are required" });
        }

        try {
            console.log(`[Provisioning] Checking if device ${device_id} is already provisioned`);
            const [existingDevice] = await connection.query(
                'SELECT * FROM DeviceProvisioning WHERE device_id = ? AND status IN ("BACKEND_VERIFIED", "COMPLETED")',
                [device_id]
            );

            if (existingDevice.length > 0) {
                console.log(`[Provisioning] Device ${device_id} already provisioned. Invalidating old provisioning.`);
                await connection.query(
                    'UPDATE DeviceProvisioning SET status = "FAILED" WHERE device_id = ?',
                    [device_id]
                );
            }

            console.log(`[Provisioning] Looking up token: ${provision_token}`);
            const [existing] = await connection.query(
                'SELECT * FROM DeviceProvisioning WHERE provision_token = ?',
                [provision_token]
            );

            console.log(`[Provisioning] Found record with plant_id: ${existing[0].plant_id}`);
            console.log('[Provisioning] Updating status to BACKEND_VERIFIED');
            
            await connection.query(
                'UPDATE DeviceProvisioning SET status = "BACKEND_VERIFIED", device_id = ? WHERE provision_token = ?',
                [device_id, provision_token]
            );

            console.log('[Provisioning] Verification successful');
            return res.status(200).json({
                message: "Device backend connection verified",
                status: "BACKEND_VERIFIED",
                plant_id: existing[0].plant_id,
                debug_info: {
                    device_id: device_id,
                    token: provision_token,
                    original_status: existing[0].status
                }
            });
        } catch (err) {
            console.error('[Provisioning] Error during verification:', err);
            return res.status(500).json({ 
                message: "Internal server error",
                error: err.message
            });
        }
    }

    static async getProvisioningToken(req, res) {
        try {
            const { plant_id } = req.params;
            
            if (!plant_id) {
                console.error('[Provisioning] No plant ID provided');
                return res.status(400).json({ message: "Plant ID is required" });
            }

            const [plantData] = await connection.query(
                'SELECT user_id FROM Plants WHERE plant_id = ?',
                [plant_id]
            );

            if (!plantData.length) {
                console.error(`[Provisioning] Plant ${plant_id} not found`);
                return res.status(404).json({ message: "Plant not found" });
            }

            const user_id = plantData[0].user_id;

            const [existingToken] = await connection.query(
                `SELECT provision_token, status, expires_at 
                 FROM DeviceProvisioning 
                 WHERE plant_id = ?`,
                [plant_id]
            );

            if (existingToken.length > 0) {
                console.log(`[Provisioning] Found existing token for plant ${plant_id}`);
                return res.status(200).json({
                    provision_token: existingToken[0].provision_token,
                    status: existingToken[0].status
                });
            }

            const provisionToken = uuidv4();
            await connection.query(
                `INSERT INTO DeviceProvisioning 
                (provision_token, user_id, plant_id, status, expires_at) 
                VALUES (?, ?, ?, 'PENDING', DATE_ADD(NOW(), INTERVAL 1 HOUR))`,
                [provisionToken, user_id, plant_id]
            );

            console.log(`[Provisioning] Created new token for plant ${plant_id}`);
            return res.status(200).json({
                provision_token: provisionToken,
                status: 'PENDING'
            });
        } catch (err) {
            console.error('[Provisioning] Error getting token:', err);
            return res.status(500).json({ 
                message: "Internal server error",
                error: err.message 
            });
        }
    }
}

module.exports = ProvisioningController;
