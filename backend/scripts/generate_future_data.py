import requests
import datetime
import random
import numpy as np
from datetime import datetime, timedelta
import time

BASE_URL = 'http://localhost:3000'

def check_server():
    try:
        response = requests.get(f"{BASE_URL}/test")
        return response.status_code == 200
    except requests.ConnectionError:
        return False

def get_plants():
    try:
        response = requests.get(f"{BASE_URL}/api/plantRead?user_id=1")
        if response.status_code == 200:
            # Filter out plant_id 1 from the response
            return [plant for plant in response.json() if plant['plant_id'] != 1]
        else:
            raise Exception("Failed to get plants")
    except Exception as e:
        print(f"Error getting plants: {str(e)}")
        raise

def generate_next_reading(previous, moisture_decrease=0.02):
    return {
        'ext_temp': round(previous['ext_temp'] + np.random.normal(0, 0.1), 2),
        'humidity': round(max(min(previous['humidity'] + np.random.normal(0, 0.2), 100), 0), 2),
        'light': round(max(min(previous['light'] + np.random.normal(0, 5), 1000), 0), 2),
        'soil_temp': round(previous['soil_temp'] + np.random.normal(0, 0.05), 2),
        'soil_moisture_1': round(max(min(previous['soil_moisture_1'] + np.random.normal(-moisture_decrease, 0.01), 100), 0), 2),
        'soil_moisture_2': round(max(min(previous['soil_moisture_2'] + np.random.normal(-moisture_decrease, 0.01), 100), 0), 2)
    }

def create_watering_event(plant_id, timestamp, recent_readings):
    event_data = {
        "watering_duration": random.randint(30, 120),
        "peak_temp": round(max(float(d['ext_temp']) for d in recent_readings), 2),
        "peak_moisture": round(max(float(d['soil_moisture_1']) for d in recent_readings), 2),
        "avg_temp": round(sum(float(d['ext_temp']) for d in recent_readings) / len(recent_readings), 2),
        "avg_moisture": round(sum(float(d['soil_moisture_1']) for d in recent_readings) / len(recent_readings), 2),
        "plant_id": plant_id,
        "time_stamp": timestamp.strftime('%Y-%m-%d %H:%M:%S'),
        "volume": round(random.uniform(0.2, 0.5), 2)
    }
    
    requests.post(f"{BASE_URL}/api/wateringUpload", json=event_data)

def main():
    # Check if server is running
    print("Checking server connection...")
    retries = 0
    while not check_server() and retries < 3:
        print(f"Server not responding, retrying... ({retries + 1}/3)")
        time.sleep(2)
        retries += 1
    
    if retries == 3:
        raise Exception("Could not connect to server")

    print("Server connection established")
    
    # Use fixed start date and calculate end date
    start_date = datetime.strptime("2024-12-02 11:37:30", '%Y-%m-%d %H:%M:%S')
    end_date = start_date + timedelta(days=90)
    plants = get_plants()
    
    print(f"Generating future data from {start_date} to {end_date}")
    
    # Initial conditions for continuing data generation
    plants_current = {
        plant['plant_id']: {
            'ext_temp': 22.0,
            'humidity': 60.0,
            'light': 500.0,
            'soil_temp': 20.0,
            'soil_moisture_1': 85.0,
            'soil_moisture_2': 85.0
        } for plant in plants
    }
    
    for plant_id, current_reading in plants_current.items():
        current_date = start_date
        moisture_threshold = 40.0
        recent_readings = []
        batch = []
        
        print(f"Generating future data for plant {plant_id}")
        
        while current_date <= end_date:
            # Add random time interval (4-6 minutes)
            current_date += timedelta(minutes=random.randint(4, 6))
            if current_date > end_date:
                break
                
            # Generate next reading
            current_reading = generate_next_reading(current_reading)
            reading = {**current_reading, 
                      'plant_id': plant_id,
                      'time_stamp': current_date.strftime('%Y-%m-%d %H:%M:%S')}
            
            batch.append(reading)
            recent_readings.append(current_reading)
            
            # Keep recent readings window at 12 readings (about 1 hour)
            if len(recent_readings) > 12:
                recent_readings.pop(0)
            
            # Upload in batches of 50 readings
            if len(batch) >= 50:
                requests.post(f"{BASE_URL}/api/sensorUpload", json=batch)
                batch = []
                print(f"Uploaded batch of readings for plant {plant_id}, current date: {current_date}")
            
            # Check if watering is needed
            if current_reading['soil_moisture_1'] < moisture_threshold:
                create_watering_event(plant_id, current_date, recent_readings)
                # Reset moisture levels after watering
                current_reading['soil_moisture_1'] = 100
                current_reading['soil_moisture_2'] = 100
        
        # Upload any remaining readings
        if batch:
            requests.post(f"{BASE_URL}/api/sensorUpload", json=batch)
        
        print(f"Completed future data generation for plant {plant_id}")

if __name__ == "__main__":
    main() 