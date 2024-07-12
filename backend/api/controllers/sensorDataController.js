const SensorData = require("../models/sensorModel");

exports.sensorUpload = async (req, res) => {
  try {
    if (req.body.length) {
      for (const data of req.body) {
        const sensorData = new SensorData(data);
        await sensorData.uploadData();
      }
    } else {
      const sensorData = new SensorData(req.body);
      await sensorData.uploadData();
    }

    return res.status(200).send("Successfully uploaded sensor data");
  } catch (err) {
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
    const time_stamp = req.query.time_stamp;
    const [rows] = await SensorData.readData(plant_id, time_stamp);
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
