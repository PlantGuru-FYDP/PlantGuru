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
    sun_light FLOAT,
    ext_temp FLOAT,
    soil_temp FLOAT,
    soil_moisture_top FLOAT,
    soil_moisture_bottom FLOAT,
    time DATETIME NOT NULL,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE
);
