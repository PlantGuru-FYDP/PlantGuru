const connection = require("../../db/connection");

class SensorData {
  constructor({
    plant_id,
    ext_temp,
    light,
    humidity,
    soil_temp,
    soil_moisture_1,
    soil_moisture_2,
    time_stamp,
  }) {
    this.plant_id = plant_id;
    this.ext_temp = ext_temp;
    this.light = light;
    this.humidity = humidity;
    this.soil_temp = soil_temp;
    this.soil_moisture_1 = soil_moisture_1;
    this.soil_moisture_2 = soil_moisture_2;
    this.time_stamp = time_stamp;
  }

  static uploadData(values) {
    const cmd =
      "INSERT INTO SensorData (plant_id, ext_temp, light, humidity, soil_temp, soil_moisture_1, soil_moisture_2, time_stamp) VALUES ?";

    return connection.query(cmd, [values]);
  }
  static readData(plant_id, time_stamp) {
    const cmd =
      "Select * from SensorData where plant_id = ? AND time_stamp = ?";
    return connection.query(cmd, [plant_id, time_stamp]);
  }

  static readDataSeries(plant_id, time_stamp1, time_stamp2) {
    const cmd =
      "Select * from SensorData where plant_id = ? AND time_stamp >= ? AND time_stamp <= time_stamp2";
    return connection.query(cmd, [plant_id, time_stamp1, time_stamp2]);
  }
}

module.exports = SensorData;
