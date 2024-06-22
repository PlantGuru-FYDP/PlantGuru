-- Create the database
CREATE DATABASE IF NOT EXISTS PlantGuruDB;
USE PlantGuruDB;

-- Create the Users table
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    address TEXT,
    phone_number VARCHAR(20),
    number_of_plants INT DEFAULT 0
);

-- Create the Plants table
CREATE TABLE Plants (
    plant_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    plant_name VARCHAR(255) NOT NULL,
    age INT,
    last_watered DATETIME,
    next_watering_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create the SensorData table
CREATE TABLE SensorData (
    sensor_id INT AUTO_INCREMENT PRIMARY KEY,
    plant_id INT,
    ext_temp FLOAT,
    humidity FLOAT,
    light FLOAT,
    soil_temp FLOAT,
    soil_moisture_1 FLOAT,
    soil_moisture_2 FLOAT,
    time_stamp timestamp NOT NULL,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE
);

-- Create the WateringEvent table
CREATE TABLE WateringEvent (
    watering_id INT AUTO_INCREMENT PRIMARY KEY,
    watering_duration INT,
    peak_temp FLOAT,
    peak_moisture FLOAT,
    avg_temp FLOAT, 
    avg_moisture FLOAT, 
    plant_id INT,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE
);