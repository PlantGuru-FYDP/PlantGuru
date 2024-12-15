const express = require("express");
const router = express.Router();
const ProvisioningController = require("../controllers/provisioningController");
const { tokenVerify } = require("../middlewares/tokenVerify");

// Add route to get provisioning token
router.get("/provision/token/:plant_id",
    tokenVerify,
    ProvisioningController.getProvisioningToken
);

// Update provisioning status
router.post("/provision/status",
    ProvisioningController.updateProvisioningStatus
);

// Get provisioning status
router.get("/provision/:provision_token",
    ProvisioningController.getProvisioningStatus
);

// Verify device backend connection
router.post("/provision/verify",
    ProvisioningController.verifyDeviceConnection
);

module.exports = router;
