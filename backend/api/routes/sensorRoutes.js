// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
let {
  sensorUpload,
  sensorRead,
  sensorReadSeries,
  testSensorUpload,
} = require("../controllers/sensorDataController");

let { plantTokenVerify } = require("../middlewares/plantTokenVerify");
const { body } = require("express-validator");

router.post("/sensorUpload", sensorUpload);

router.post("/testSensorUpload", plantTokenVerify, testSensorUpload);

router.get("/sensorRead", sensorRead);

router.get("/sensorReadSeries", sensorReadSeries);

module.exports = router;
