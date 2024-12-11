const express = require("express");
const router = express.Router();
const { query } = require("express-validator");
const { getProjections } = require("../controllers/projectionsController");

router.get("/projections", [
    query('plant_id').isInt(),
    query('sensor_type').isString(),
    query('num_points').isInt().default(24),
    query('granularity').isInt().default(60)
], getProjections);

module.exports = router; 