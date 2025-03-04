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

-- Primary time-series index for fast time-based lookups
CREATE INDEX idx_sensor_plant_time ON SensorData (plant_id, time_stamp DESC);

-- Sensor-specific indexes for analytics queries
CREATE INDEX idx_sensor_ext_temp ON SensorData (plant_id, ext_temp, time_stamp);
CREATE INDEX idx_sensor_humidity ON SensorData (plant_id, humidity, time_stamp);
CREATE INDEX idx_sensor_light ON SensorData (plant_id, light, time_stamp);
CREATE INDEX idx_sensor_soil_temp ON SensorData (plant_id, soil_temp, time_stamp);
CREATE INDEX idx_sensor_moisture ON SensorData (plant_id, soil_moisture_1, soil_moisture_2, time_stamp);
CREATE INDEX idx_sensor_soil_moisture_1 ON SensorData (plant_id, soil_moisture_1, time_stamp);
CREATE INDEX idx_sensor_soil_moisture_2 ON SensorData (plant_id, soil_moisture_2, time_stamp);

-- Watering events index
CREATE INDEX idx_watering_plant_time ON WateringEvent (plant_id, time_stamp DESC);

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

CREATE TABLE DeviceProvisioning (
    provision_token VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    plant_id INT NOT NULL,
    status ENUM('PENDING', 'DEVICE_CONNECTED', 'WIFI_SETUP', 'BACKEND_VERIFIED', 'COMPLETED', 'FAILED') NOT NULL,
    device_id VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id),
    UNIQUE KEY unique_plant_device (plant_id)
);

-- Create MoisturePredictions table for storing next dry threshold crossing
CREATE TABLE MoisturePredictions (
    prediction_id INT AUTO_INCREMENT PRIMARY KEY,
    plant_id INT NOT NULL,
    predicted_dry_time TIMESTAMP NOT NULL,
    current_moisture FLOAT NOT NULL,
    prediction_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE,
    INDEX idx_plant_predictions (plant_id, prediction_created_at DESC)
);

-- Create HourlyMoisturePredictions table for storing hourly predictions
CREATE TABLE HourlyMoisturePredictions (
    hourly_prediction_id INT AUTO_INCREMENT PRIMARY KEY,
    plant_id INT NOT NULL,
    predicted_moisture FLOAT NOT NULL,
    prediction_for_time TIMESTAMP NOT NULL,
    prediction_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plant_id) REFERENCES Plants(plant_id) ON DELETE CASCADE,
    INDEX idx_plant_hourly_predictions (plant_id, prediction_for_time),
    INDEX idx_prediction_created (prediction_created_at DESC)
);