let express = require("express");
let { model } = require("../controllers/mlControlller");
let router = express.Router();

// Needs plant_id passed as query parameter
router.get("/model", model);

module.exports = router;
