const connection = require("../../db/connection");

class Plant {
  constructor(user_id, plant_name, age, last_watered, next_watering_time) {
    this.user_id = user_id;
    this.plant_name = plant_name;
    this.age = age;
    this.last_watered = last_watered;
    this.next_watering_time = next_watering_time;
  }
  uploadData() {
    const cmd =
      "INSERT INTO Plants (user_id, plant_name, age, last_watered, next_watering_time) VALUES (?, ?, ?, ?, ?)";
    return connection.query(cmd, [
      this.user_id,
      this.plant_name,
      this.age,
      this.last_watered,
      this.next_watering_time,
    ]);
  }

  static async readDataWithSensors(user_id) {
    const cmd = `
      SELECT 
        p.*,
        s.ext_temp,
        s.light,
        s.humidity,
        s.soil_temp,
        s.soil_moisture_1,
        s.soil_moisture_2,
        DATE_FORMAT(s.time_stamp, '%Y-%m-%dT%H:%i:%s.000Z') as last_sensor_reading,
        dp.status as provisioning_status,
        dp.device_id
      FROM Plants p
      LEFT JOIN (
        SELECT s1.*
        FROM SensorData s1
        INNER JOIN (
          SELECT plant_id, MAX(sensor_id) as max_sensor_id
          FROM SensorData
          GROUP BY plant_id
        ) s2
        ON s1.plant_id = s2.plant_id AND s1.sensor_id = s2.max_sensor_id
      ) s ON p.plant_id = s.plant_id
      LEFT JOIN DeletedPlants d ON p.plant_id = d.plant_id
      LEFT JOIN DeviceProvisioning dp ON p.plant_id = dp.plant_id
      WHERE p.user_id = ? AND d.plant_id IS NULL`;
    
    return connection.query(cmd, [user_id]);
  }

  static readData(user_id) {
    const cmd = `
      SELECT p.* 
      FROM Plants p
      LEFT JOIN DeletedPlants d ON p.plant_id = d.plant_id
      WHERE p.user_id = ? AND d.plant_id IS NULL`;
    return connection.query(cmd, [user_id]);
  }

  getID() {
    const cmd =
      "Select plant_id from Plants where user_id = ? and plant_name = ?";
    return connection.query(cmd, [this.user_id, this.plant_name]);
  }

  static async softDelete(plant_id) {
    const cmd = "INSERT INTO DeletedPlants (plant_id) VALUES (?)";
    return connection.query(cmd, [plant_id]);
  }

  static async readDataById(plant_id) {
    const cmd = `
      SELECT p.* 
      FROM Plants p
      LEFT JOIN DeletedPlants d ON p.plant_id = d.plant_id
      WHERE p.plant_id = ? AND d.plant_id IS NULL`;
    return connection.query(cmd, [plant_id]);
  }

  static async updatePlantName(plant_id, new_name) {
    const cmd = "UPDATE Plants SET plant_name = ? WHERE plant_id = ?";
    return connection.query(cmd, [new_name, plant_id]);
  }

  static async updatePlant(plant_id, updates) {
    const validFields = ['plant_name', 'age', 'last_watered', 'next_watering_time'];
    const updateFields = [];
    const updateValues = [];

    for (const [key, value] of Object.entries(updates)) {
      if (validFields.includes(key) && value !== undefined && value !== null) {
        updateFields.push(`${key} = ?`);
        updateValues.push(value);
      }
    }

    if (updateFields.length === 0) return;

    updateValues.push(plant_id);
    const cmd = `UPDATE Plants SET ${updateFields.join(', ')} WHERE plant_id = ?`;
    return connection.query(cmd, updateValues);
  }
}

module.exports = Plant;
