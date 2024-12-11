const connection = require("../../db/connection");
const TIME_SETTINGS = require("../../constants/timeSettings");

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
      "SELECT *, time_stamp as time_period FROM SensorData WHERE plant_id = ? AND time_stamp >= ? AND time_stamp <= ?";
    return connection.query(cmd, [plant_id, time_stamp1, time_stamp2]);
  }
  static getLastNSensorReadings(plant_id, n) {
    console.log(`DEBUG: Fetching last ${n} readings for plant ${plant_id}`);
    
    const cmd =
      "Select * from SensorData where plant_id = ? ORDER BY time_stamp DESC LIMIT ?";
    
    return connection.query(cmd, [plant_id, n])
        .then(results => {
            console.log("DEBUG: Number of readings retrieved:", results.length);
            console.log("DEBUG: Sample moisture values:", 
                results.slice(0, 3).map(r => ({
                    moisture1: r.soil_moisture_1,
                    moisture2: r.soil_moisture_2,
                    timestamp: r.time_stamp
                }))
            );
            return results;
        });
  }

  static readLatestData(plant_id) {
    const cmd =
      "Select * from SensorData where plant_id = ? ORDER BY time_stamp DESC LIMIT 1";
    return connection.query(cmd, [plant_id]);
  }

  static determineGranularity(startTime, endTime) {
    const diffInHours = (new Date(endTime) - new Date(startTime)) / (1000 * 60 * 60);
    
    const { GRANULARITY_THRESHOLDS, GRANULARITIES } = TIME_SETTINGS;
    
    if (diffInHours <= GRANULARITY_THRESHOLDS.RAW) {
        return GRANULARITIES.RAW;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.MINUTE_5) {
        return GRANULARITIES.MINUTE_5;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.MINUTE_15) {
        return GRANULARITIES.MINUTE_15;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.MINUTE_30) {
        return GRANULARITIES.MINUTE_30;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.HOUR) {
        return GRANULARITIES.HOUR;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.HOUR_12) {
        return GRANULARITIES.HOUR_12;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.DAY) {
        return GRANULARITIES.DAY;
    } else if (diffInHours <= GRANULARITY_THRESHOLDS.WEEK) {
        return GRANULARITIES.WEEK;
    } else {
        return GRANULARITIES.MONTH;
    }
  }

  static async getTimeSeriesData(plant_id, start_time, end_time, granularity = 0) {
    const startDate = new Date(start_time);
    const endDate = new Date(end_time);
    
    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
        throw new Error('Invalid date format');
    }

    // Convert ISO dates to MySQL format
    const mysqlStartDate = startDate.toISOString().slice(0, 19).replace('T', ' ');
    const mysqlEndDate = endDate.toISOString().slice(0, 19).replace('T', ' ');

    if (granularity === 0) {
        const [rawData] = await this.readDataSeries(plant_id, startDate.toISOString(), endDate.toISOString());
        return rawData.map(row => ({
            time_period: row.time_stamp,
            ext_temp: row.ext_temp,
            light: row.light,
            humidity: row.humidity,
            soil_temp: row.soil_temp,
            soil_moisture_1: row.soil_moisture_1,
            soil_moisture_2: row.soil_moisture_2,
            data_points: 1
        }));
    }

    const cmd = `
        WITH sensor_data AS (
            SELECT 
                DATE_FORMAT(
                    DATE_SUB(time_stamp, 
                        INTERVAL (
                            TIMESTAMPDIFF(MINUTE, 
                                DATE('1970-01-01'), 
                                time_stamp
                            ) % ?
                        ) MINUTE
                    ),
                    '%Y-%m-%d %H:%i:00'
                ) as period,
                ROUND(AVG(ext_temp), 2) as ext_temp,
                ROUND(AVG(light), 2) as light,
                ROUND(AVG(humidity), 2) as humidity,
                ROUND(AVG(soil_temp), 2) as soil_temp,
                ROUND(AVG(soil_moisture_1), 2) as soil_moisture_1,
                ROUND(AVG(soil_moisture_2), 2) as soil_moisture_2,
                COUNT(*) as data_points
            FROM SensorData
            WHERE plant_id = ?
                AND time_stamp >= ?
                AND time_stamp <= ?
            GROUP BY period
            ORDER BY period ASC
        )
        SELECT 
            period as time_period,
            ext_temp,
            light,
            humidity,
            soil_temp,
            soil_moisture_1,
            soil_moisture_2,
            data_points
        FROM sensor_data
    `;

    try {
        const [results] = await connection.query(cmd, [
            granularity,
            plant_id,
            mysqlStartDate,
            mysqlEndDate
        ]);
        return results;
    } catch (error) {
        console.error("DEBUG: Query error:", error);
        throw error;
    }
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

  static async getSensorStats(plant_id, sensor_type, start_time, end_time, options = {}) {
    const validSensors = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2'];
    if (!validSensors.includes(sensor_type)) {
      throw new Error('Invalid sensor type');
    }

    let whereClause = 'WHERE plant_id = ? AND time_stamp >= ? AND time_stamp <= ?';
    let params = [plant_id, start_time, end_time];

    if (options.removeOutliers) {
      whereClause += ` AND ${sensor_type} BETWEEN 
        (SELECT AVG(${sensor_type}) - 2 * STDDEV(${sensor_type}) 
         FROM SensorData WHERE plant_id = ? AND time_stamp >= ? AND time_stamp <= ?)
        AND 
        (SELECT AVG(${sensor_type}) + 2 * STDDEV(${sensor_type})
         FROM SensorData WHERE plant_id = ? AND time_stamp >= ? AND time_stamp <= ?)`;
      params = [...params, plant_id, start_time, end_time, plant_id, start_time, end_time];
    }

    const cmd = `
      SELECT 
        MIN(${sensor_type}) as min_value,
        MAX(${sensor_type}) as max_value,
        AVG(${sensor_type}) as avg_value,
        COUNT(*) as total_readings
      FROM SensorData
      ${whereClause}
    `;

    return connection.query(cmd, params);
  }

  static async getSensorTrendline(plant_id, sensor_type, start_time, end_time) {
    const validSensors = ['ext_temp', 'light', 'humidity', 'soil_temp', 'soil_moisture_1', 'soil_moisture_2'];
    if (!validSensors.includes(sensor_type)) {
      throw new Error('Invalid sensor type');
    }

    const cmd = `
      SELECT 
        (
          (COUNT(*) * SUM(UNIX_TIMESTAMP(time_stamp) * ${sensor_type}) - 
           SUM(UNIX_TIMESTAMP(time_stamp)) * SUM(${sensor_type})) /
          (COUNT(*) * SUM(POWER(UNIX_TIMESTAMP(time_stamp), 2)) - 
           POWER(SUM(UNIX_TIMESTAMP(time_stamp)), 2))
        ) as slope,
        (
          (SUM(${sensor_type}) * SUM(POWER(UNIX_TIMESTAMP(time_stamp), 2)) - 
           SUM(UNIX_TIMESTAMP(time_stamp)) * SUM(UNIX_TIMESTAMP(time_stamp) * ${sensor_type})) /
          (COUNT(*) * SUM(POWER(UNIX_TIMESTAMP(time_stamp), 2)) - 
           POWER(SUM(UNIX_TIMESTAMP(time_stamp)), 2))
        ) as intercept,
        MIN(${sensor_type}) as min_value,
        MAX(${sensor_type}) as max_value,
        MIN(time_stamp) as start_point,
        MAX(time_stamp) as end_point
      FROM SensorData
      WHERE plant_id = ? 
      AND time_stamp >= ? 
      AND time_stamp <= ?
    `;

    return connection.query(cmd, [plant_id, start_time, end_time]);
  }

  static async readFirstData(plant_id) {
    const query = `
        SELECT *
        FROM SensorData
        WHERE plant_id = ?
        ORDER BY time_stamp ASC
        LIMIT 1
    `;
    return await connection.query(query, [plant_id]);
  }
}

module.exports = SensorData;
