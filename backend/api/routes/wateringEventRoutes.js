// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
const {
  wateringUpload,
  wateringRead,
  wateringReadByPlantIdAndTimestamp,
  wateringReadSeries,
} = require("../controllers/wateringEventController");

const { body } = require("express-validator");

router.post("/wateringUpload", wateringUpload);

router.get("/wateringRead", wateringRead);

// not used atm
router.get(
  "/wateringReadByPlantIdAndTimestamp",
  wateringReadByPlantIdAndTimestamp
);

router.get("/sensorReadSeries", wateringReadSeries);

module.exports = router;
