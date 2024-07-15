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

  static readData(user_id) {
    const cmd = "Select * from Plants where user_id = ?";
    return connection.query(cmd, [user_id]);
  }

  getID() {
    const cmd =
      "Select plant_id from Plants where user_id = ? and plant_name = ?";
    return connection.query(cmd, [this.user_id, this.plant_name]);
  }
}

module.exports = Plant;
