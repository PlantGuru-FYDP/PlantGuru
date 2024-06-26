// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
const {
  wateringUpload,
  wateringRead,
  wateringReadSeries,
} = require("../controllers/wateringEventController");

const { body } = require("express-validator");

router.post("/wateringUpload", wateringUpload);

router.get("/wateringRead", wateringRead);

router.get("/sensorReadSeries", wateringReadSeries);

module.exports = router;
