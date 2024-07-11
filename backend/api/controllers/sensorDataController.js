const SensorData = require("../models/sensorModel");

// A lot of similar code between controllers but its fine for now
exports.sensorUpload = async (req, res) => {
  try {
    const values = [];
    if (req.body.length) {
      for (const data of req.body) {
        const sensorData = new SensorData(data);
        values.push(Object.values(sensorData));
      }
    } else {
      const sensorData = new SensorData(req.body);
      values.push(Object.values(sensorData));
    }

    await SensorData.uploadData(values);
    return res.status(200).send("Successfully uploaded sensor data");
  } catch (err) {
    return res.status(500).send({ message: err });
  }
};

// TODO Does this plant_id belong to me? required middleware
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
