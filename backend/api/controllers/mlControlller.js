let SensorData = require("../models/sensorModel");

exports.model = async (req, res) => {
  try {
    const plant_id = req.query.plant_id;

    const [rows] = await SensorData.readLatestData(plant_id);
    const current_temp = rows[0].ext_temp;
    const current_light = rows[0].light;
    const current_humidity = rows[0].humidity;
    const current_soil_moisture_1 = rows[0].soil_moisture_1;
    const current_soil_moisture_2 = rows[0].soil_moisture_2;

    const input_arr = [
      current_temp,
      current_humidity,
      current_light,
      current_soil_moisture_1,
      current_soil_moisture_2,
    ];
    console.log(input_arr);
    const predicted_moisture = soil_moisture_predict([input_arr]);

    const hours_until_watering = predict_next_watering_time(
      predicted_moisture,
      current_temp,
      current_light,
      current_humidity
    );

    return res.status(200).send({ "Next watering time": hours_until_watering });
  } catch (err) {
    return res.status(500).send({ message: "Internal server error" + err });
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
