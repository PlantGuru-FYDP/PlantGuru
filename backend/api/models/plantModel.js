const connection = require("../../db/connection");

class Plant {
  constructor(
    plant_id,
    user_id,
    plant_name,
    age,
    last_watered,
    next_watering_time
  ) {
    this.plant_id = plant_id;
    this.user_id = user_id;
    this.plant_name = plant_name;
    this.age = age;
    this.last_watered = last_watered;
    this.next_watering_time = next_watering_time;
  }
  static uploadData(values) {
    const cmd =
      "INSERT INTO Plants (user_id, plant_name, age, last_watered, next_watering_time) VALUES ?";
    return connection.query(cmd, [values]);
  }

  static readData(user_id) {
    const cmd = "Select * from Plants where user_id = ?";
    return connection.query(cmd, [user_id]);
  }
}

module.exports = Plant;
