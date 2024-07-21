import pandas as pd
import numpy as np
import pickle


with open('decision_tree', 'rb') as file:
    model = pickle.load(file)

def average_moisture_loss_rate(temp, light, humidity):
    # This function should be defined based on historical data analysis
    # For now, we'll use a simple heuristic for demonstration
    # Example values for demonstration purposes
    base_rate = 0.2  # base moisture loss rate per hour
    temp_factor = (temp - 20) * 0.05  # increase rate with higher temperature
    light_factor = (light / 100) * 0.5  # increase rate with more light
    humidity_factor = (50 - humidity) * 0.02  # decrease rate with higher humidity
    
    # Total moisture loss rate
    moisture_loss_rate = base_rate + temp_factor + light_factor - humidity_factor
    
    return max(moisture_loss_rate, 0.1)  # Ensure it's never zero or negative

def predict_next_watering_time(model, predicted_moisture, current_temp, current_light, current_humidity):
    # Threshold for watering
    moisture_threshold = 35.0
    
    # Get the average moisture loss rate per hour
    moisture_loss_rate = average_moisture_loss_rate(current_temp, current_light, current_humidity)
    
    # Calculate hours until the soil moisture reaches the threshold
    hours_until_watering = (predicted_moisture - moisture_threshold) / moisture_loss_rate
    return hours_until_watering

    # Example usage
predicted_moisture = 59.0
current_temp = 21
current_light = 50
current_humidity = 40

hours_until_watering = predict_next_watering_time(model, predicted_moisture, current_temp, current_light, current_humidity)
print(f"The plant will need to be watered in approximately {hours_until_watering} hours.")
