// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
let {
  sensorUpload,
  wateringUpload,
  plantUpload,
} = require("../controllers/dataUpload");

// const { body } = require("express-validator");

router.post("/sensorUpload", sensorUpload);

router.post("/wateringEventUpload", wateringUpload);

router.post("/plantUpload", plantUpload);

module.exports = router;
