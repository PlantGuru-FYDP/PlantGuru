let Plant = require("../models/plantModel");
const NotificationSettings = require('../models/notificationSettingsModel');
const { DEFAULT_PLANT_SETTINGS } = require('../constants/defaultSettings');

exports.plantRead = async (req, res) => {
  try {
    const user_id = req.query.user_id;
    const includeSensors = req.query.include_sensors === 'true';

    let data;
    if (includeSensors) {
      [data] = await Plant.readDataWithSensors(user_id);
    } else {
      [data] = await Plant.readData(user_id);
    }

    return res.status(200).send(data);
  } catch (err) {
    console.error('Error in plantRead:', err);
    return res.status(500).send({ message: err.message || 'Internal server error' });
  }
};

exports.plantUpload = async (req, res) => {
  try {
    let { user_id, plant_name, age, last_watered, next_watering_time } =
      req.body;
    const plant = new Plant(
      user_id,
      plant_name,
      age,
      last_watered,
      next_watering_time
    );

    let checkPlant = await plant.getID();
    if (checkPlant[0].length > 0) {
      return res.status(400).send({ message: "Plant already exists!" });
    }

    await plant.uploadData();
    let id = await plant.getID();
    const plant_id = id[0][0].plant_id;

    const defaultPlantSettings = {
      plantId: plant_id,
      ...DEFAULT_PLANT_SETTINGS
    };

    await NotificationSettings.createPlantSettings(plant_id, defaultPlantSettings);
    console.log(`[plantUpload] Created default notification settings for plant_id: ${plant_id}`);

    return res.status(200).send({
      message: "Plant data uploaded",
      plant_id: plant_id,
    });
  } catch (err) {
    console.error('[plantUpload] Error:', err);
    return res.status(500).send({ message: err });
  }
};

exports.plantDelete = async (req, res) => {
  try {
    const plant_id = req.query.plant_id;
    
    if (!plant_id) {
      return res.status(400).send({ message: "Plant ID is required" });
    }

    await Plant.softDelete(plant_id);
    return res.status(200).send({ message: "Plant successfully deleted" });
  } catch (err) {
    console.error('Error in plantDelete:', err);
    return res.status(500).send({ message: err.message || 'Internal server error' });
  }
};
