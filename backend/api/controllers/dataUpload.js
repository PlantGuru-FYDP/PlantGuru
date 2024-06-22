const conn = require("../../db/connection");

exports.sensorUpload = (req, res) => {
  const values = [];

  for (let i = 0; i < req.body.length; i++) {
    const {
      plant_id,
      ext_temp,
      light,
      humidity,
      soil_temp,
      soil_moisture_1,
      soil_moisture_2,
      time_stamp,
    } = req.body[i];

    values.push([
      plant_id,
      ext_temp,
      light,
      humidity,
      soil_temp,
      soil_moisture_1,
      soil_moisture_2,
      time_stamp,
    ]);
  }

  const cmd =
    "INSERT INTO SensorData (plant_id, ext_temp, light, humidity, soil_temp, soil_moisture_1, soil_moisture_2, time_stamp) VALUES ?";

  conn.query(cmd, [values], (err) => {
    if (err) {
      console.error(err);
      return res.status(400).send("Failed to upload data with error: " + err);
    }
    return res.status(200).send("Data uploaded successfully");
  });
};

// need to implement
exports.PlantUpload = (req, res) => {};
