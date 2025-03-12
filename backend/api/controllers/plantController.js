let Plant = require("../models/plantModel");
const NotificationSettings = require('../models/notificationSettingsModel');
const { DEFAULT_PLANT_SETTINGS } = require('../constants/defaultSettings');
const { v4: uuidv4 } = require('uuid');
const connection = require('../../db/connection'); 

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

    const provisionToken = uuidv4();
    await connection.query(
      `INSERT INTO DeviceProvisioning 
      (provision_token, user_id, plant_id, status, expires_at) 
      VALUES (?, ?, ?, 'PENDING', DATE_ADD(NOW(), INTERVAL 1 HOUR))`,
      [provisionToken, user_id, plant_id]
    );

    return res.status(200).send({
      message: "Plant data uploaded",
      plant_id: plant_id,
      provision_token: provisionToken
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

exports.updatePlantName = async (req, res) => {
    try {
        const { plant_id, plant_name } = req.body;
        
        if (!plant_id || !plant_name) {
            return res.status(400).send({ message: "Plant ID and name are required" });
        }

        await Plant.updatePlantName(plant_id, plant_name);
        return res.status(200).send({ message: "Plant name updated successfully" });
    } catch (err) {
        console.error('Error in updatePlantName:', err);
        return res.status(500).send({ message: err.message || 'Internal server error' });
    }
};

exports.updatePlant = async (req, res) => {
    try {
        const { plant_id, ...updates } = req.body;
        
        if (!plant_id) {
            return res.status(400).send({ message: "Plant ID is required" });
        }

        if (Object.keys(updates).length === 0) {
            return res.status(400).send({ message: "No update fields provided" });
        }

        await Plant.updatePlant(plant_id, updates);
        return res.status(200).send({ message: "Plant updated successfully" });
    } catch (err) {
        console.error('Error in updatePlant:', err);
        return res.status(500).send({ message: err.message || 'Internal server error' });
    }
};

exports.getAllPlants = async (req, res) => {
  try {
    const [rows] = await Plant.readAllPlants();
    return res.status(200).send(rows);
  } catch (err) {
    console.error('Error fetching all plants:', err);
    return res.status(500).send({ message: err.message });
  }
};
