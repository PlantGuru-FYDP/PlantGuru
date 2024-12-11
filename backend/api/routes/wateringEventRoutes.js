// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
const {
  wateringUpload,
  wateringRead,
  wateringReadByPlantIdAndTimestamp,
  wateringReadSeries,
  getLastWateringEvent,
} = require("../controllers/wateringEventController");

const { body, query } = require("express-validator");

router.post("/wateringUpload", wateringUpload);

router.get("/wateringRead", wateringRead);

// not used atm
router.get(
  "/wateringReadByPlantIdAndTimestamp",
  wateringReadByPlantIdAndTimestamp
);

router.get("/wateringReadSeries", [
    query('plant_id').isInt(),
    query('time_stamp1').isISO8601(),
    query('time_stamp2').isISO8601()
], wateringReadSeries);

router.get("/lastWateringEvent", getLastWateringEvent);

module.exports = router;
