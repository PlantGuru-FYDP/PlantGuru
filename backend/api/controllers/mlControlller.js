const ModelingService = require('../services/modelingService');

exports.model = async (req, res) => {
  try {
    const { plant_id } = req.query;
    if (!plant_id) {
      return res.status(400).send({ message: "plant_id is required" });
    }

    // Regular prediction flow
    const predictedDryTime = await ModelingService.getNextDryTime(plant_id);
    if (!predictedDryTime) {
      return res.status(404).send({ message: "No predictions available for this plant" });
    }

    // Calculate hours until watering
    const hoursUntilWatering = Math.max(0, (new Date(predictedDryTime) - new Date()) / (1000 * 60 * 60));

    // Get hourly predictions for more detailed data
    const hourlyPredictions = await ModelingService.getHourlyPredictions(plant_id);

    return res.status(200).send({ 
      next_watering_in_hours: Math.round(hoursUntilWatering * 10) / 10,
      predicted_dry_time: predictedDryTime,
      hourly_predictions: hourlyPredictions
    });
  } catch (error) {
    console.error('Error in model endpoint:', error);
    return res.status(500).send({ message: "Internal server error" });
  }
};

function soil_moisture_predict(input_arr) {
  avg_moisture = (input_arr[0][3] + input_arr[0][4]) / 2
  return avg_moisture - Math.round((Math.random() * 0.4 + 0.8) * 100) / 100;
}

function average_moisture_loss_rate(temp, light, humidity) {
  // This function should be defined based on historical data analysis
  // For now, we'll use a simple heuristic for demonstration
  // Example values for demonstration purposes
  base_rate = 0.1 
  temp_factor = (temp - 20) * 0.02 
  light_factor = (light / 100) * 0.1 
  humidity_factor = (50 - humidity) * 0.02 

  // Total moisture loss rate
  const moisture_loss_rate =
    base_rate + temp_factor + light_factor - humidity_factor;

  return Math.max(moisture_loss_rate, 0.08); // Ensure it's never zero or negative
}

function predict_next_watering_time(
  predicted_moisture,
  current_temp,
  current_light,
  current_humidity
) {
  // Threshold for watering
  const moisture_threshold = 50.0;

  // Get the average moisture loss rate per hour
  const moisture_loss_rate = average_moisture_loss_rate(
    current_temp,
    current_light,
    current_humidity
  );

  // Calculate hours until the soil moisture reaches the threshold
  const hours_until_watering =
    (predicted_moisture - moisture_threshold) / moisture_loss_rate;
  return hours_until_watering;
}
