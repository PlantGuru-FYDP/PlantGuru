const express = require("express");
const router = express.Router();
const { query } = require("express-validator");
const { 
  getSensorHealth, 
  getPlantRecommendations,
  getHealthDiagnostics,
  getCareSchedule,
} = require("../controllers/insightsController");

router.get("/sensorHealth", [
  query('plant_id').isInt(),
  query('sensor_type').isString().isIn([
    'ext_temp',
    'light',
    'humidity',
    'soil_temp',
    'soil_moisture_1',
    'soil_moisture_2'
  ])
], getSensorHealth);

router.get("/plantRecommendations", [
  query('plant_id').isInt()
], getPlantRecommendations);

router.get("/healthDiagnostics", [
  query('plant_id').isInt()
], getHealthDiagnostics);

router.get("/careSchedule", [
  query('plant_id').isInt()
], getCareSchedule);

module.exports = router; 