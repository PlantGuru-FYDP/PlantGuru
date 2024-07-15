class WateringEvent {
  constructor(
    watering_duration,
    peak_temp,
    peak_moisture,
    avg_temp,
    avg_moisture,
    plant_id,
    time_stamp,
    volume
  ) {
    this.watering_duration = watering_duration;
    this.peak_temp = peak_temp;
    this.peak_moisture = peak_moisture;
    this.avg_temp = avg_temp;
    this.avg_moisture = avg_moisture;
    this.plant_id = plant_id;
    this.time_stamp = time_stamp;
    this.volume = volume;
  }

  static uploadData(values) {
    const cmd =
      "INSERT INTO WateringEvent (watering_duration, peak_temp, peak_moisture, avg_temp, avg_moisture, plant_id, time_stamp, volume) VALUES ?";

    connection.query(cmd, [values]);
  }

  static readData(plant_id) {
    const cmd = "Select * from WateringEvent where plant_id = ?";
    return connection.query(cmd, [plant_id]);
  }

  static readDataWithTimestamp(plant_id, time_stamp) {
    const cmd =
      "Select * from WateringEvent where plant_id = ? AND time_stamp = ?";
    return connection.query(cmd, [plant_id, time_stamp]);
  }

  static readDataSeries(plant_id, time_stamp1, time_stamp2) {
    const cmd =
      "Select * from WateringEvent where plant_id = ? AND time_stamp >= ? AND time_stamp <= time_stamp2";
    return connection.query(cmd, [plant_id, time_stamp1, time_stamp2]);
  }
}

module.exports = WateringEvent;
