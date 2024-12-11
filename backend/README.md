# Plant Care API Documentation

> **Important Note**: All data types specified in the request/response bodies should be valid MySQL types provided as strings, as the API performs direct MySQL insertions without type conversion. For example, integers should be sent as "123", floats as "123.45", and timestamps in MySQL datetime format "YYYY-MM-DD HH:MM:SS".

## Authentication

### Sign Up
- **Endpoint**: `POST /signup`
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
- **Endpoint**: `POST /login`
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
- **Endpoint**: `POST /plantUpload`
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
- **Endpoint**: `GET /plantRead`
- **Query Parameters**: 
  - `user_id`: integer
  - `include_sensors`: boolean (optional) - if true, includes latest sensor readings
- **Response**: Array of plant objects with optional sensor data
  ```json
  [
    {
      "plant_id": "integer",
      "user_id": "integer",
      "plant_name": "string",
      "age": "integer",
      "last_watered": "datetime",
      "next_watering_time": "datetime",
      // Only included if include_sensors=true
      "ext_temp": "float | null",
      "light": "float | null",
      "humidity": "float | null",
      "soil_temp": "float | null",
      "soil_moisture_1": "float | null",
      "soil_moisture_2": "float | null",
      "last_sensor_reading": "datetime | null"
    }
  ]
  ```

## Sensor Data

### Upload Sensor Data
- **Endpoint**: `POST /sensorUpload`
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
- **Endpoint**: `GET /sensorRead`
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
- **Endpoint**: `GET /lastNSensorReadings`
- **Query Parameters**:
  - `plant_id`: integer
  - `n`: integer
- **Response**: Same format as sensorRead

### Get Time Series Data
- **Endpoint**: `GET /timeSeriesData`
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
- **Endpoint**: `GET /analysis`
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

### Get Sensor Statistics
- **Endpoint**: `GET /sensorStats`
- **Query Parameters**:
  - `plant_id`: integer
  - `sensor_type`: string (one of: "ext_temp", "light", "humidity", "soil_temp", "soil_moisture_1", "soil_moisture_2")
  - `start_time`: timestamp (ISO8601)
  - `end_time`: timestamp (ISO8601)
  - `remove_outliers`: boolean (optional) - removes data points outside 2 standard deviations
  - `smooth_data`: boolean (optional) - applies data smoothing
- **Response**:
  ```json
  {
    "result": {
      "min_value": "float",
      "max_value": "float",
      "avg_value": "float",
      "total_readings": "integer"
    }
  }
  ```

### Get Sensor Trendline
- **Endpoint**: `GET /sensorTrendline`
- **Query Parameters**:
  - `plant_id`: integer
  - `sensor_type`: string (one of: "ext_temp", "light", "humidity", "soil_temp", "soil_moisture_1", "soil_moisture_2")
  - `start_time`: timestamp (ISO8601)
  - `end_time`: timestamp (ISO8601)
- **Response**:
  ```json
  {
    "result": {
      "slope": "float",
      "intercept": "float",
      "min_value": "float",
      "max_value": "float",
      "start_point": "timestamp",
      "end_point": "timestamp"
    }
  }
  ```

## Watering Events

### Upload Watering Event
- **Endpoint**: `POST /wateringUpload`
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
- **Endpoint**: `GET /wateringRead`
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
- **Endpoint**: `POST /provision`
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

## Insights

The Insights API provides analytics and recommendations for plant care based on sensor data.

### Get Sensor Health
Evaluates how ideal each sensor reading of a plant currently is. Uses historical information and context about specific plant, time
and season to determine if sensors are currently high or low. light is low for this time of day

- **Endpoint**: `GET /api/sensorHealth`
- **Query Parameters**:
  - `plant_id`: integer
  - `sensor_type`: string (one of: "ext_temp", "light", "humidity", "soil_temp", "soil_moisture_1", "soil_moisture_2")
- **Response**:
  ```json
  {
    "status": "string", // One of: "GOOD", "WARNING_LOW", "WARNING_HIGH", "CRITICAL", "UNKNOWN"
    "current_value": {
      "value": "float",
      "unit": "string",
      "timestamp": "timestamp"
    },
    "optimal_range": {
      "min": "float",
      "max": "float",
      "unit": "string"
    },
    "historical_context": {
      "min": "float",
      "max": "float", 
      "avg": "float",
      "readings": "integer"
    },
    "sensor_type": "string",
    "plant_id": "integer"
  }
  ```

### Get Plant Recommendations
Provides actionable care recommendations based on current sensor readings. More of a high level overview of what things would be better
for the plant, i.e. put in more sunlight, move to warmer area, water more frequently.

- **Endpoint**: `GET /api/plantRecommendations`
- **Query Parameters**:
  - `plant_id`: integer
- **Response**:
  ```json
  {
    "plant_id": "integer",
    "timestamp": "timestamp",
    "recommendations": [
      {
        "type": "string",
        "priority": "string", // "HIGH" or "MEDIUM"
        "message": "string"
      }
    ],
    "last_watering": {
      // Last watering event data or null
    }
  }
  ```

### Get Health Diagnostics
Provides a comprehensive health assessment of the plant, including a long term rating for each sensor, as well as watering frequency. The health score is calculated based on multiple sensor readings and watering over a longer period and should correlate to the plant's current health.

- **Endpoint**: `GET /api/healthDiagnostics`
- **Query Parameters**:
  - `plant_id`: integer
- **Response**:
  ```json
  {
    "overall_health": "string", // One of: "EXCELLENT", "GOOD", "FAIR", "POOR", "CRITICAL"
    "health_score": "integer", // 0-100
    "stress_indicators": {
      "water_stress": "boolean",
      "heat_stress": "boolean",
      "light_stress": "boolean",
      "humidity_stress": "boolean"
    },
    "sensor_health": {
      // Health status for each sensor
    },
    "alerts": [
      {
        "type": "string",
        "severity": "string",
        "message": "string"
      }
    ],
    "latest_readings": {
      "temperature": "float",
      "humidity": "float",
      "light": "float",
      "soil_moisture": "float"
    }
  }
  ```

### Get Care Schedule
Generates a personalized care schedule with upcoming tasks and recent actions. Mainly about timing out how often the plant should be watered for now.

- **Endpoint**: `GET /api/careSchedule`
- **Query Parameters**:
  - `plant_id`: integer
- **Response**:
  ```json
  {
    "plant_id": "integer",
    "timestamp": "timestamp",
    "next_actions": [
      {
        "type": "string",
        "due_in_hours": "integer",
        "priority": "string", // "HIGH" or "MEDIUM"
        "details": "string"
      }
    ],
    "recent_actions": [
      {
        "type": "string",
        "timestamp": "timestamp",
        "details": "string"
      }
    ],
    "sensor_context": {
      "soil_moisture": "float",
      "temperature": "float",
      "light": "float",
      "humidity": "float"
    }
  }
  ```
