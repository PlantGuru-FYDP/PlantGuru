// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
let {
  sensorUpload,
  sensorRead,
  sensorReadSeries,
  testSensorUpload,
  getLastNSensorReadings,
  getTimeSeriesData,
  getAnalysis,
  getSensorStats,
  getSensorTrendline
} = require("../controllers/sensorDataController");

let { plantTokenVerify } = require("../middlewares/plantTokenVerify");
const { body, query } = require("express-validator");

router.post("/sensorUpload", sensorUpload);

router.post("/testSensorUpload", plantTokenVerify, testSensorUpload);

router.get("/sensorRead", sensorRead);

router.get("/sensorReadSeries", sensorReadSeries);

router.get("/lastNSensorReadings", getLastNSensorReadings);

router.get("/timeSeriesData", [
  query('plant_id').isInt(),
  query('start_time').isISO8601(),
  query('end_time').isISO8601(),
  query('granularity').optional().isInt().default(0),
  query('sensor_types').optional().isString()
], getTimeSeriesData);

router.get("/analysis", [
  query('plant_id').isInt(),
  query('start_time').isISO8601(),
  query('end_time').isISO8601(),
  query('metrics').optional().isString()
], getAnalysis);

router.get("/sensorStats", [
  query('plant_id').isInt(),
  query('sensor_type').isString(),
  query('start_time').isISO8601(),
  query('end_time').isISO8601(),
  query('remove_outliers').optional().isBoolean(),
  query('smooth_data').optional().isBoolean()
], getSensorStats);

router.get("/sensorTrendline", [
  query('plant_id').isInt(),
  query('sensor_type').isString(),
  query('start_time').isISO8601(),
  query('end_time').isISO8601()
], getSensorTrendline);

module.exports = router;
