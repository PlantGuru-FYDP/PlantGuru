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

def create_plant(user_id, plant_name):
    plant_data = {
        "user_id": user_id,
        "plant_name": plant_name,
        "age": 90,
        "last_watered": datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
        "next_watering_time": (datetime.now() + timedelta(days=14)).strftime('%Y-%m-%d %H:%M:%S')
    }
    
    print(plant_data)
    try:
        response = requests.post(f"{BASE_URL}/api/plantUpload", json=plant_data)
        print(f"Response status: {response.status_code}")
        print(f"Response text: {response.text}")
        
        if response.status_code != 200:
            raise Exception(f"Failed to create plant: {response.text}")
            
        response_data = response.json()
        return response_data['plant_id']
    except Exception as e:
        print(f"Error creating plant: {str(e)}")
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
    
    user_id = 1
    plant1_id = create_plant(user_id, "Monstera")
    plant2_id = create_plant(user_id, "Peace Lily")
    
    # Initial conditions for each plant
    plants_initial = {
        plant1_id: {
            'ext_temp': 22.0,
            'humidity': 60.0,
            'light': 500.0,
            'soil_temp': 20.0,
            'soil_moisture_1': 85.0,
            'soil_moisture_2': 85.0
        },
        plant2_id: {
            'ext_temp': 24.0,
            'humidity': 70.0,
            'light': 400.0,
            'soil_temp': 22.0,
            'soil_moisture_1': 85.0,
            'soil_moisture_2': 85.0
        }
    }
    
    # Generate 3 months of data
    start_date = datetime.now() - timedelta(days=90)
    end_date = datetime.now()
    
    for plant_id, current_reading in plants_initial.items():
        current_date = start_date
        moisture_threshold = 40.0  # Water when moisture drops below this
        recent_readings = []
        batch = []
        
        while current_date <= end_date:
            # Add random time interval (4-6 minutes)
            current_date += timedelta(minutes=random.randint(4, 6))
            if current_date > end_date:
                break
                
            # Generate next reading
            current_reading = generate_next_reading(current_reading)
            reading = {**current_reading, 
                      'plant_id': plant_id,
                      'time_stamp': current_date.isoformat()}
            
            batch.append(reading)
            recent_readings.append(current_reading)
            
            # Keep recent readings window at 12 readings (about 1 hour)
            if len(recent_readings) > 12:
                recent_readings.pop(0)
            
            # Upload in batches of 50 readings
            if len(batch) >= 50:
                requests.post(f"{BASE_URL}/api/sensorUpload", json=batch)
                batch = []
            
            # Check if watering is needed
            if current_reading['soil_moisture_1'] < moisture_threshold:
                create_watering_event(plant_id, current_date, recent_readings)
                # Reset moisture levels after watering
                current_reading['soil_moisture_1'] = 100
        
        print(f"Completed data generation for plant {plant_id}")

if __name__ == "__main__":
    main() 