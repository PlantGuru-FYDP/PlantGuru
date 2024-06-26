// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
let {
  sensorUpload,
  sensorRead,
  sensorReadSeries,
} = require("../controllers/sensorDataController");

// TODO middleware checks to make sure the data is fine
const { body } = require("express-validator");

router.post("/sensorUpload", sensorUpload);

router.get("/sensorRead", sensorRead);

router.get("/sensorReadSeries", sensorReadSeries);

module.exports = router;
