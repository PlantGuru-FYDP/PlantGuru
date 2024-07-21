let express = require("express");
let router = express.Router();
let { provision } = require("../controllers/provisioningController");

let { body } = require("express-validator");
let { tokenVerify } = require("../middlewares/tokenVerify");
let { verifyPlantWithUser } = require("../middlewares/verifyPlantWithUser");

router.post(
  "/provision",
  tokenVerify,
  [body("plant_id").notEmpty().withMessage("Plant ID is required")],
  verifyPlantWithUser,
  provision
);

module.exports = router;
