// Routes relating to uploading data to the db
let express = require("express");
let router = express.Router();
let { sensorUpload, plantUpload } = require("../controllers/dataUpload");
// const { body } = require("express-validator");

router.post(
  "/sensorUpload",
  // [
  //   body("plant_id").notEmpty(),
  //   body("sun_light").notEmpty(),
  //   body("ext_temp").notEmpty(),
  //   body("soil_temp").notEmpty(),
  //   body("soil_moisture_top").notEmpty(),
  //   body("soil_moisture_bottom").notEmpty(),
  //   body("time").notEmpty(),
  // ],
  sensorUpload
);

// router.post(
//   "/plantUplaod",
//   [
//     ,// body("plant_id").notEmpty(),
//     // body("sun_light").notEmpty(),
//     // body("ext_temp").notEmpty(),
//     // body("soil_temp").notEmpty(),
//     // body("soil_moisture_top").notEmpty(),
//     // body("soil_moisture_bottom").notEmpty(),
//     // body("time").notEmpty(),
//   ],
//   plantUpload
// );
