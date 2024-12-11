const WateringEvent = require("../models/wateringEventModel");
const NotificationService = require("../services/notificationService");
const DeviceToken = require("../models/deviceTokenModel");
const Plant = require("../models/plantModel");


async function sendWateringNotification(plantId, wateringData) {
  try {
    console.log(`[Watering Notification] Starting notification process for plant ${plantId}`);
    
    const [plantDetails] = await Plant.readDataById(plantId);
    if (!plantDetails.length) {
      console.log(`[Watering Notification] No plant found for ID ${plantId}`);
      return;
    }
    const plant = plantDetails[0];
    console.log(`[Watering Notification] Found plant: ${plant.plant_name}`);

    const [tokens] = await DeviceToken.getTokensByUserId(plant.user_id);
    if (!tokens.length) {
      console.log(`[Watering Notification] No device tokens found for user ${plant.user_id}`);
      return;
    }
    console.log(`[Watering Notification] Found ${tokens.length} device tokens`);

    const deviceTokens = tokens.map(t => t.token);
    const title = `${plant.plant_name} Watering Event`;
    const body = `Watered for ${wateringData.watering_duration}s with ${wateringData.volume}ml`;
    
    const data = {
      type: 'WATERING_EVENT',
      plantId: plantId.toString(),
      wateringDuration: wateringData.watering_duration.toString(),
      volume: wateringData.volume.toString(),
      timestamp: wateringData.time_stamp
    };

    console.log(`[Watering Notification] Sending notification: ${title} - ${body}`);
    await NotificationService.sendNotification(deviceTokens, title, body, data);
    console.log('[Watering Notification] Successfully sent notification');
  } catch (error) {
    console.error('[Watering Notification] Error sending notification:', error);
  }
}


exports.wateringUpload = async (req, res) => {
  try {
    if (req.body.length) {
      for (const data of req.body) {
        const wateringEvent = new WateringEvent(data);
        await wateringEvent.uploadData();
        await sendWateringNotification(data.plant_id, data);
      }
    } else {
      const wateringEvent = new WateringEvent(req.body);
      await wateringEvent.uploadData();
      await sendWateringNotification(req.body.plant_id, req.body);
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
    return res.status(200).send(rows);
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

// single most recent watering event
exports.getLastWateringEvent = async (req, res) => {
    try {
        const plant_id = parseInt(req.query.plant_id);
        const [rows] = await WateringEvent.readLastEvent(plant_id);
        
        if (rows && rows.length > 0) {
            return res.status(200).send(rows[0]);
        }
        return res.status(404).send({ message: "No watering events found" });
    } catch (err) {
        return res.status(500).send(err.message);
    }
};
