const conn = require("../../db/connection");

exports.sensorUpload = (req, res) => {
  const {
    plant_id,
    ext_temp,
    light,
    humidity,
    soil_temp,
    soil_moisture_1,
    soil_moisture_2,
    time,
  } = req.body;
  conn.query(
    "INSERT INTO sensorData (plant_id, ext_temp, light, humidity,  soil_temp, soil_moisture_1, soil_moisture_2, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)",
    [
      plant_id,
      ext_temp,
      light,
      humidity,
      soil_temp,
      soil_moisture_1,
      soil_moisture_2,
      time,
    ],
    (err) => {
      if (err) {
        console.log(err);
        return res.status(400).send("Failed to upload data with error:" + err);
      }
      return res.status(200).send("Data upload done");
    }
  );
};

exports.PlantUpload = (req, res) => {};
