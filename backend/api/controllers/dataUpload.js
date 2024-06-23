const conn = require("../../db/connection");

exports.sensorUpload = (req, res) => {
  const values = [];

  // bad code, need to refactor
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

  uploadData(res, cmd, values);
};

exports.wateringUpload = (req, res) => {
  const values = [];

  for (let i = 0; i < req.body.length; i++) {
    const {
      watering_duration,
      peak_temp,
      peak_moisture,
      avg_temp,
      avg_moisture,
      plant_id,
      time_stamp,
      volume,
    } = req.body[i];

    values.push([
      watering_duration,
      peak_temp,
      peak_moisture,
      avg_temp,
      avg_moisture,
      plant_id,
      time_stamp,
      volume,
    ]);
  }
  const cmd =
    "INSERT INTO WateringEvent (watering_duration, peak_temp, peak_moisture, avg_temp, avg_moisture, plant_id, time_stamp, volume) VALUES ?";
  uploadData(res, cmd, values);
};

exports.plantUpload = (req, res) => {
  const { user_id, plant_name, age, last_watered, next_watering_time } =
    req.body;

  const values = [user_id, plant_name, age, last_watered, next_watering_time];
  const cmd =
    "INSERT INTO Plants (user_id, plant_name, age, last_watered, next_watering_time) VALUES (?)";
  uploadData(res, cmd, values);
};

// Helper function to upload data to the db
function uploadData(res, cmd, values) {
  console.log(values);
  conn.query(cmd, [values], (err) => {
    if (err) {
      console.error(err);
      return res.status(400).send("Failed to upload data with error: " + err);
    }
    return res.status(200).send("Data uploaded successfully");
  });
}
