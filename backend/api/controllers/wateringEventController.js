const WateringEvent = require("../models/wateringEventModel");

exports.wateringUpload = async (req, res) => {
  try {
    if (req.body.length) {
      for (const data of req.body) {
        const wateringEvent = new WateringEvent(data);
        await wateringEvent.uploadData();
      }
    } else {
      const wateringEvent = new WateringEvent(req.body);
      await wateringEvent.uploadData();
    }

    return res.status(200).send("Successfully uploaded watering event data");
  } catch (err) {
    return res.status(500).send(err);
  }
};
exports.wateringRead = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const [rows] = await WateringEvent.readData(plant_id);
    return res.status(200).send({ result: rows });
  } catch (err) {
    return res.status(500).send(err.message);
  }
};

exports.wateringReadByPlantIdAndTimestamp = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const time_stamp = req.query.time_stamp;
    const [rows] = await WateringEvent.readDataWithTimestamp(
      plant_id,
      time_stamp
    );
    return res.status(200).send({ result: rows });
  } catch (err) {
    return res.status(500).send(err.message);
  }
};
exports.wateringReadSeries = async (req, res) => {
  try {
    const plant_id = parseInt(req.query.plant_id);
    const time_stamp1 = req.query.time_stamp1;
    const time_stamp2 = req.query.time_stamp2;
    const [rows] = await WateringEvent.readDataSeries(
      plant_id,
      time_stamp1,
      time_stamp2
    );
    return res.status(200).send({ result: rows });
  } catch (err) {
    return res.status(500).send(err.message);
  }
};
