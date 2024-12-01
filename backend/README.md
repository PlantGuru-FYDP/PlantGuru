# Plant Care API Documentation

## Authentication

### Sign Up
- **Endpoint**: `POST /auth/signup`
- **Request Body**:
  ```json
  {
    "name": "string",
    "email": "string",
    "password": "string",
    "address": "string",
    "phoneNumber": "string"
  }
  ```
- **Response**:
  ```json
  {
    "message": "User created with successfully",
    "user_id": "integer"
  }
  ```

### Login
- **Endpoint**: `POST /auth/login`
- **Request Body**:
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Response**:
  ```json
  {
    "message": "User logged in successfully",
    "token": "string",
    "user_id": "integer"
  }
  ```

## Plants

### Create Plant
- **Endpoint**: `POST /plant/plantUpload`
- **Request Body**:
  ```json
  {
    "user_id": "integer",
    "plant_name": "string",
    "age": "integer",
    "last_watered": "datetime",
    "next_watering_time": "datetime"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Plant data uploaded",
    "plant_id": "integer"
  }
  ```

### Get Plants
- **Endpoint**: `GET /plant/plantRead`
- **Query Parameters**: 
  - `user_id`: integer
- **Response**: Array of plant objects

## Sensor Data

### Upload Sensor Data
- **Endpoint**: `POST /sensor/sensorUpload`
- **Request Body**: Single object or array of:
  ```json
  {
    "plant_id": "integer",
    "ext_temp": "float",
    "light": "float",
    "humidity": "float",
    "soil_temp": "float",
    "soil_moisture_1": "float",
    "soil_moisture_2": "float",
    "time_stamp": "timestamp"
  }
  ```
- **Response**: `"Successfully uploaded sensor data"`

### Get Sensor Reading
- **Endpoint**: `GET /sensor/sensorRead`
- **Query Parameters**:
  - `plant_id`: integer
  - `time_stamp`: timestamp
- **Response**:
  ```json
  {
    "result": [
      {
        "sensor_id": "integer",
        "plant_id": "integer",
        "ext_temp": "float",
        "light": "float",
        "humidity": "float",
        "soil_temp": "float",
        "soil_moisture_1": "float",
        "soil_moisture_2": "float",
        "time_stamp": "timestamp"
      }
    ]
  }
  ```

### Get Last N Readings
- **Endpoint**: `GET /sensor/lastNSensorReadings`
- **Query Parameters**:
  - `plant_id`: integer
  - `n`: integer
- **Response**: Same format as sensorRead

### Get Time Series Data
- **Endpoint**: `GET /sensor/timeSeriesData`
- **Query Parameters**:
  - `plant_id`: integer
  - `start_time`: timestamp (ISO8601)
  - `end_time`: timestamp (ISO8601)
  - `granularity`: string (optional) - one of: "raw", "minute", "hour", "day", "week", "month"
  - `sensor_types`: string (optional) - comma-separated list of sensor types to include
- **Response**:
  ```json
  {
    "result": [
      {
        "time_period": "timestamp",
        "ext_temp": "float",
        "light": "float",
        "humidity": "float",
        "soil_temp": "float",
        "soil_moisture_1": "float",
        "soil_moisture_2": "float"
      }
    ]
  }
  ```

### Get Sensor Analysis
- **Endpoint**: `GET /sensor/analysis`
- **Query Parameters**:
  - `plant_id`: integer
  - `start_time`: timestamp (ISO8601)
  - `end_time`: timestamp (ISO8601)
  - `metrics`: string (optional) - comma-separated list of: "min", "max", "avg"
- **Response**:
  ```json
  {
    "result": {
      "min_ext_temp": "float",
      "max_ext_temp": "float",
      "avg_ext_temp": "float",
      "min_light": "float",
      "max_light": "float",
      "avg_light": "float",
      "min_humidity": "float",
      "max_humidity": "float",
      "avg_humidity": "float",
      "min_soil_temp": "float",
      "max_soil_temp": "float",
      "avg_soil_temp": "float",
      "min_soil_moisture_1": "float",
      "max_soil_moisture_1": "float",
      "avg_soil_moisture_1": "float",
      "min_soil_moisture_2": "float",
      "max_soil_moisture_2": "float",
      "avg_soil_moisture_2": "float"
    }
  }
  ```

## Watering Events

### Upload Watering Event
- **Endpoint**: `POST /watering/wateringUpload`
- **Request Body**: Single object or array of:
  ```json
  {
    "watering_duration": "integer",
    "peak_temp": "float",
    "peak_moisture": "float",
    "avg_temp": "float",
    "avg_moisture": "float",
    "plant_id": "integer",
    "time_stamp": "timestamp",
    "volume": "float"
  }
  ```
- **Response**: `"Successfully uploaded watering event data"`

### Get Watering Events
- **Endpoint**: `GET /watering/wateringRead`
- **Query Parameters**:
  - `plant_id`: integer
- **Response**:
  ```json
  {
    "result": [
      {
        "watering_id": "integer",
        "watering_duration": "integer",
        "peak_temp": "float",
        "peak_moisture": "float",
        "avg_temp": "float",
        "avg_moisture": "float",
        "plant_id": "integer",
        "time_stamp": "timestamp",
        "volume": "float"
      }
    ]
  }
  ```

## Device Provisioning

### Provision Device
- **Endpoint**: `POST /provisioning/provision`
- **Headers**: `Authorization: Bearer <user_token>`
- **Request Body**:
  ```json
  {
    "plant_id": "integer"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Provisioning successful",
    "token": "string"
  }
  ```
