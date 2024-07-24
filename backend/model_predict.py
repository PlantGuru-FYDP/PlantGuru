import numpy as np
import pickle
import random


def soil_moisture_predict(input_arr):

    with open('decision_tree', 'rb') as file:
        model = pickle.load(file)
    # input_arr should be array like -> [ext_temp, humidity, light, current soil_moisture]
    predicted_moisture = model.predict(input_arr)[0]
    predicted_moisture = input_arr[0][3] - round(random.uniform(0.8, 1.2), 2)

    return predicted_moisture

def average_moisture_loss_rate(temp, light, humidity):
    # This function should be defined based on historical data analysis
    # For now, we'll use a simple heuristic for demonstration
    # Example values for demonstration purposes
    base_rate = 0.1  # base moisture loss rate per hour
    temp_factor = (temp - 20) * 0.02  # increase rate with higher temperature
    light_factor = (light / 100) * 0.1  # increase rate with more light
    humidity_factor = (50 - humidity) * 0.02  # decrease rate with higher humidity
    
    # Total moisture loss rate
    moisture_loss_rate = base_rate + temp_factor + light_factor - humidity_factor
    
    print(f"moisture loss rate: {max(moisture_loss_rate, 0.1)}")
    return max(moisture_loss_rate, 0.08)  # Ensure it's never zero or negative

def predict_next_watering_time(predicted_moisture, current_temp, current_light, current_humidity):
    # Threshold for watering
    moisture_threshold = 50
    
    # Get the average moisture loss rate per hour
    moisture_loss_rate = average_moisture_loss_rate(current_temp, current_light, current_humidity)
    
    # Calculate hours until the soil moisture reaches the threshold
    hours_until_watering = (predicted_moisture - moisture_threshold) / moisture_loss_rate
    return hours_until_watering

# Example usage
current_temp = 24
current_light = 80
current_humidity = 45
current_soil_moisture = 60

input_arr = []
input_arr = np.array([current_temp, current_humidity, current_light, current_soil_moisture])
input_arr = input_arr.reshape(1,-1)

<<<<<<< Updated upstream
predicted_moisture = soil_moisture_predict(input_arr)
print(predicted_moisture)
=======
predicted_moisture = soil_moisture_predict(model, input_arr)
print(f"predicted moisture: {predicted_moisture}")
>>>>>>> Stashed changes

hours_until_watering = predict_next_watering_time(predicted_moisture, current_temp, current_light, current_humidity)
print(f"The plant will need to be watered in approximately {hours_until_watering} hours.")

