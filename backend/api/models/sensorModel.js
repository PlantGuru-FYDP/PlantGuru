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

  uploadData() {
    const cmd =
      "INSERT INTO SensorData (plant_id, ext_temp, light, humidity, soil_temp, soil_moisture_1, soil_moisture_2, time_stamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    return connection.query(cmd, [
      this.plant_id,
      this.ext_temp,
      this.light,
      this.humidity,
      this.soil_temp,
      this.soil_moisture_1,
      this.soil_moisture_2,
      this.time_stamp,
    ]);
  }
  static readData(plant_id) {
    const cmd = "Select * from SensorData where plant_id = ?";
    return connection.query(cmd, [plant_id]);
  }

  static readDataSeries(plant_id, time_stamp1, time_stamp2) {
    const cmd =
      "Select * from SensorData where plant_id = ? AND time_stamp >= ? AND time_stamp <= time_stamp2";
    return connection.query(cmd, [plant_id, time_stamp1, time_stamp2]);
  }
  static getLastNSensorReadings(plant_id, n) {
    const cmd =
      "Select * from SensorData where plant_id = ? ORDER BY time_stamp DESC LIMIT ?";
    return connection.query(cmd, [plant_id, n]);
  }

  static readLatestData(plant_id) {
    const cmd =
      "Select * from SensorData where plant_id = ? ORDER BY time_stamp DESC LIMIT 1";
    return connection.query(cmd, [plant_id]);
  }

  static async getTimeSeriesData(plant_id, start_time, end_time, granularity = 'raw', columns = []) {
    const validColumns = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2', 'time_stamp'];
    
    // Validate columns
    if (columns.length > 0) {
      const invalidColumns = columns.filter(col => !validColumns.includes(col));
      if (invalidColumns.length > 0) {
        throw new Error(`Invalid columns: ${invalidColumns.join(', ')}`);
      }
    }
    
    let selectColumns = columns.length > 0 ? columns.join(', ') : '*';
    let groupByClause = '';
    let timeFormat = '';
    
    switch (granularity) {
      case 'minute':
        timeFormat = "DATE_FORMAT(time_stamp, '%Y-%m-%d %H:%i:00')";
        break;
      case 'hour':
        timeFormat = "DATE_FORMAT(time_stamp, '%Y-%m-%d %H:00:00')";
        break;
      case 'day':
        timeFormat = "DATE_FORMAT(time_stamp, '%Y-%m-%d 00:00:00')";
        break;
      case 'week':
        timeFormat = "DATE_FORMAT(time_stamp, '%Y-%U-1')";
        break;
      case 'month':
        timeFormat = "DATE_FORMAT(time_stamp, '%Y-%m-01')";
        break;
      default:
        return this.readDataSeries(plant_id, start_time, end_time);
    }

    if (granularity !== 'raw') {
      selectColumns = `
        ${timeFormat} as time_period,
        AVG(ext_temp) as ext_temp,
        AVG(light) as light,
        AVG(humidity) as humidity,
        AVG(soil_temp) as soil_temp,
        AVG(soil_moisture_1) as soil_moisture_1,
        AVG(soil_moisture_2) as soil_moisture_2
      `;
      groupByClause = `GROUP BY time_period`;
    }

    const cmd = `
      SELECT ${selectColumns}
      FROM SensorData 
      WHERE plant_id = ? 
      AND time_stamp >= ? 
      AND time_stamp <= ?
      ${groupByClause}
      ORDER BY time_stamp
    `;

    return connection.query(cmd, [plant_id, start_time, end_time]);
  }

  static async getAnalysis(plant_id, start_time, end_time, metrics = ['min', 'max', 'avg']) {
    const validColumns = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2'];
    const aggregations = [];
    
    if (metrics.includes('min')) {
      validColumns.forEach(col => aggregations.push(`MIN(${col}) as min_${col}`));
    }
    if (metrics.includes('max')) {
      validColumns.forEach(col => aggregations.push(`MAX(${col}) as max_${col}`));
    }
    if (metrics.includes('avg')) {
      validColumns.forEach(col => aggregations.push(`AVG(${col}) as avg_${col}`));
    }

    const cmd = `
      SELECT ${aggregations.join(', ')}
      FROM SensorData 
      WHERE plant_id = ? 
      AND time_stamp >= ? 
      AND time_stamp <= ?
    `;

    return connection.query(cmd, [plant_id, start_time, end_time]);
  }
}

module.exports = SensorData;
