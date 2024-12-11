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
    phone_number VARCHAR(20)
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

-- Create the DeletedPlants table
CREATE TABLE DeletedPlants (
    deleted_id INT AUTO_INCREMENT PRIMARY KEY,
    plant_id INT,
    deleted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE
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
    time_stamp timestamp NOT NULL,
    volume FLOAT,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE
);

CREATE TABLE DeviceTokens (
    token_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    device_name VARCHAR(100),
    last_used TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    UNIQUE KEY unique_token (fcm_token)
);

-- Create UserNotificationSettings table
CREATE TABLE UserNotificationSettings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    email_notifications BOOLEAN DEFAULT false,
    email_digests BOOLEAN DEFAULT false,
    email VARCHAR(255),
    digests_frequency VARCHAR(20) DEFAULT 'INSTANT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_settings (user_id)
);

-- Create PlantNotificationSettings table
CREATE TABLE PlantNotificationSettings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    plant_id INT NOT NULL,
    ext_temp_notifications BOOLEAN DEFAULT true,
    ext_temp_min FLOAT,
    ext_temp_max FLOAT,
    humidity_notifications BOOLEAN DEFAULT true,
    humidity_min FLOAT,
    humidity_max FLOAT,
    light_notifications BOOLEAN DEFAULT true,
    light_min FLOAT,
    light_max FLOAT,
    soil_temp_notifications BOOLEAN DEFAULT true,
    soil_temp_min FLOAT,
    soil_temp_max FLOAT,
    soil_moisture_notifications BOOLEAN DEFAULT true,
    soil_moisture_min FLOAT,
    soil_moisture_max FLOAT,
    watering_reminder_enabled BOOLEAN DEFAULT true,
    watering_reminder_frequency VARCHAR(20) DEFAULT 'SMART',
    watering_reminder_interval INT,
    watering_reminder_time TIME,
    watering_event_notifications BOOLEAN DEFAULT true,
    health_status_notifications BOOLEAN DEFAULT true,
    health_check_frequency VARCHAR(20) DEFAULT 'DAILY',
    critical_alerts_only BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE,
    UNIQUE KEY unique_plant_settings (plant_id)
);