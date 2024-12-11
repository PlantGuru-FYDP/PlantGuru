const SensorData = require("../models/sensorModel");
const PlantMonitoringService = require('../services/plantMonitoringService');

exports.sensorUpload = async (req, res) => {
  console.log(`Processing sensor upload: ${req.body.length ? 'batch' : 'single'} request`);
  
  try {
    if (req.body.length) {
      for (const data of req.body) {
        const sensorData = new SensorData(data);
        await sensorData.uploadData();
        //await PlantMonitoringService.processNewSensorData(data.plant_id, data);
      }
    } else {
      const sensorData = new SensorData(req.body);
      await sensorData.uploadData();
      //await PlantMonitoringService.processNewSensorData(req.body.plant_id, req.body);
    }

    return res.status(200).send("Successfully uploaded sensor data");
  } catch (err) {
    console.error("Error uploading sensor data:", err);
    return res.status(500).send({ message: err });
  }
};

exports.testSensorUpload = async (req, res) => {
  try {
    if (req.body.length) {
      for (const data of req.body) {
        const body = {
          plant_id: req.plant_id,
          ...data,
        };
        const sensorData = new SensorData(body);
        await sensorData.uploadData();
      }
    } else {
      const body = {
        plant_id: req.plant_id,
        ...req.body,
      };
      const sensorData = new SensorData(body);
      await sensorData.uploadData();
    }

    return res.status(200).send("Successfully uploaded sensor data");
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};
exports.sensorRead = async (req, res) => {
  try {
    const plant_id = req.query.plant_id;
    const [rows] = await SensorData.readData(plant_id);
    return res.status(200).send({ result: rows });
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};

exports.sensorReadSeries = async (req, res) => {
  try {
    const plant_id = req.query.plant_id;
    const time_stamp1 = req.query.time_stamp1;
    const time_stamp2 = req.query.time_stamp2;
    const [rows] = await SensorData.readDataSeries(
      plant_id,
      time_stamp1,
      time_stamp2
    );
    return res.status(200).send({ result: rows });
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};

exports.getLastNSensorReadings = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const n = parseInt(req.query.n);
    const [rows] = await SensorData.getLastNSensorReadings(plant_id, n);
    return res.status(200).send({ result: rows });
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};

exports.getTimeSeriesData = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const start_time = req.query.start_time;
    const end_time = req.query.end_time;
    const granularity = req.query.granularity || 'raw';

    const results = await SensorData.getTimeSeriesData(
      plant_id,
      start_time,
      end_time,
      granularity
    );

    return res.status(200).send({ result: results });
  } catch (err) {
    console.error("Error in getTimeSeriesData:", err);
    return res.status(500).send({ message: err.message });
  }
};

exports.getAnalysis = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const start_time = req.query.start_time;
    const end_time = req.query.end_time;
    const metrics = req.query.metrics ? req.query.metrics.split(',') : ['min', 'max', 'avg'];

    const [rows] = await SensorData.getAnalysis(
      plant_id,
      start_time,
      end_time,
      metrics
    );

    return res.status(200).send({ result: rows[0] });
  } catch (err) {
    return res.status(500).send({ message: err.message });
  }
};

exports.getSensorStats = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const sensor_type = req.query.sensor_type;
    const start_time = req.query.start_time;
    const end_time = req.query.end_time;
    const options = {
      removeOutliers: req.query.remove_outliers === 'true',
      smoothData: req.query.smooth_data === 'true'
    };

    const [rows] = await SensorData.getSensorStats(
      plant_id,
      sensor_type,
      start_time,
      end_time,
      options
    );

    return res.status(200).send({ result: rows[0] });
  } catch (err) {
    return res.status(500).send({ message: err.message });
  }
};
// not used
exports.getSensorTrendline = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const sensor_type = req.query.sensor_type;
    const start_time = req.query.start_time;
    const end_time = req.query.end_time;

    const [rows] = await SensorData.getSensorTrendline(
      plant_id,
      sensor_type,
      start_time,
      end_time
    );

    return res.status(200).send({ result: rows[0] });
  } catch (err) {
    return res.status(500).send({ message: err.message });
  }
};
