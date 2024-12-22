import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { 
  Typography, 
  Box, 
  Paper, 
  Grid, 
  List, 
  ListItem, 
  ListItemText,
  ListItemButton,
  Divider,
  Button
} from '@mui/material';
import { Download as DownloadIcon } from '@mui/icons-material';

const documentationSections = {
  'getting-started': {
    title: 'Getting Started',
    content: (
      <>
        <Typography variant="h5" gutterBottom>
          Getting Started with PlantGuru
        </Typography>
        <Typography paragraph>
          Welcome to PlantGuru! This guide will help you understand the system components and get started.
        </Typography>
        <Typography variant="h6" gutterBottom>
          System Overview
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 2 }}>
          PlantGuru consists of three main components:
          <Box component="ul" sx={{ pl: 2 }}>
            <li>Embedded Device - Hardware sensors and microcontroller for data collection</li>
            <li>Android App - Mobile interface for monitoring and control</li>
            <li>Backend Server - Data storage and processing</li>
          </Box>
        </Typography>
      </>
    )
  },
  'embedded-device': {
    title: 'Embedded Device',
    content: (
      <>
        <Typography variant="h5" gutterBottom>
          Embedded Device Setup
        </Typography>
        <Typography variant="h6" gutterBottom>
          Hardware Components
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 2 }}>
          Required components with documentation:
          <Box component="ul" sx={{ pl: 2 }}>
            <li>
              <a href="https://wiki.dfrobot.com/FireBeetle_Board_ESP32_E_SKU_DFR0654" target="_blank" rel="noopener noreferrer">
                Firebeetle 2 ESP32-E Microcontroller
              </a>
            </li>
            <li>
              <a href="https://wiki.dfrobot.com/Waterproof_Capacitive_Soil_Moisture_Sensor_SKU_SEN0308" target="_blank" rel="noopener noreferrer">
                Waterproof Capacitive Soil Moisture Sensor
              </a>
            </li>
            <li>
              <a href="https://wiki.dfrobot.com/Waterproof_DS18B20_Digital_Temperature_Sensor__SKU_DFR0198_" target="_blank" rel="noopener noreferrer">
                Waterproof DS18B20 Digital Temperature Sensor
              </a>
            </li>
            <li>
              <a href="https://cdn-shop.adafruit.com/product-files/161/C2255-001_MFG_PN__HaiWang_MJ5516.pdf" target="_blank" rel="noopener noreferrer">
                Light sensor (photoresistor)
              </a>
            </li>
            <li>
              <a href="https://wiki.dfrobot.com/DHT22_Temperature_and_humidity_module_SKU_SEN0137" target="_blank" rel="noopener noreferrer">
                DHT22 Temperature and humidity module
              </a>
            </li>
            <li>
              <a href="https://digilent.com/reference/pmod/pmodmicrosd/reference-manual" target="_blank" rel="noopener noreferrer">
                SD card module for data logging
              </a>
            </li>
          </Box>
        </Typography>
        <Typography variant="h6" gutterBottom>
          Software Setup
        </Typography>
        <Typography component="div" sx={{ pl: 2 }}>
          1. Arduino IDE Setup:<br/>
          <Box sx={{ pl: 3 }}>
            • Install the ESP32 board in Arduino IDE according to the{' '}
            <a href="https://wiki.dfrobot.com/FireBeetle_Board_ESP32_E_SKU_DFR0654" target="_blank" rel="noopener noreferrer">
              DFRobot guide
            </a><br/>
            • Set Tools {'>'} Partition Scheme to "Huge APP (3MB No OTA/1MB SPIFFS)"
          </Box>
          <br/>
          2. Required Libraries:<br/>
          <Box sx={{ pl: 3 }}>
            • DallasTemperature by Miles Burton and dependencies<br/>
            • DHT22 sensor library by Adafruit and dependencies<br/>
            • ArduinoJson by Benoit Blanchon
          </Box>
          <br/>
          3. Device Setup:<br/>
          <Box sx={{ pl: 3 }}>
            • Upload the BLE_server.ino sketch to your device<br/>
            • Open Tools {'>'} Serial Monitor at 115200 baud to see output<br/>
            • Open index.html in a web browser to connect to the server
          </Box>
        </Typography>
      </>
    )
  },
  'android-app': {
    title: 'Android App',
    content: (
      <>
        <Typography variant="h5" gutterBottom>
          Android Application
        </Typography>
        <Typography variant="h6" gutterBottom>
          Installation
        </Typography>
        <Box sx={{ pl: 2, mb: 3 }}>
          <Button 
            variant="contained" 
            startIcon={<DownloadIcon />}
            href="PlantGuru.apk"
            download
          >
            Download PlantGuru APK
          </Button>
          <Typography variant="caption" display="block" sx={{ mt: 1 }}>
            Download and install the latest version of PlantGuru for Android
          </Typography>
        </Box>
        <Typography variant="h6" gutterBottom>
          Installing APK on Android
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          1. Before Installation:<br/>
          <Box sx={{ pl: 3 }}>
            • Enable "Install from Unknown Sources" in your Android settings<br/>
            • On Android 8 or higher: Go to Settings {'>'} Apps {'>'} Special access {'>'} Install unknown apps<br/>
            • On Android 7 or lower: Go to Settings {'>'} Security {'>'} Unknown sources
          </Box>
          <br/>
          2. Installation Steps:<br/>
          <Box sx={{ pl: 3 }}>
            • Download the PlantGuru APK file<br/>
            • Open your Downloads folder<br/>
            • Tap on PlantGuru.apk to start installation<br/>
            • Review the requested permissions and tap "Install"<br/>
            • Wait for installation to complete and tap "Open"
          </Box>
    
        </Typography>

        <Typography variant="h6" gutterBottom>
          Installing APK on Emulator (No Android Device)
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          If you don't have an Android device, you can run PlantGuru on an emulator:
          <br/><br/>
          1. Setting up Android Emulator:<br/>
          <Box sx={{ pl: 3 }}>
          • Install Android Studio from{' '}
            <a href="https://developer.android.com/studio" target="_blank" rel="noopener noreferrer">
              developer.android.com/studio
            </a><br/>
            • Open Android Studio<br/>
            • Click Tools {'>'} Device Manager<br/>
            • Click "Create Device" button<br/>
            • Select "Pixel 6" or any other modern phone from the list<br/>
            • Click "Next"<br/>
            • Select the latest Android release (API 34 recommended)<br/>
            • Click "Download" next to the release if not already downloaded<br/>
            • Click "Next" and then "Finish"
          </Box>
          <br/>
          2. Installing on Emulator:<br/>
          <Box sx={{ pl: 3 }}>
            • Start the emulator by clicking the play button in Device Manager<br/>
            • Wait for the emulator to fully boot up<br/>
            • Drag and drop the PlantGuru.apk file onto the emulator window<br/>
            • Or use the command line: `adb install path/to/PlantGuru.apk`<br/>
            • The app will install automatically
          </Box>
          <br/>
          3. Emulator Tips:<br/>
          <Box sx={{ pl: 3 }}>
            • Allocate at least 2GB RAM to the emulator for smooth performance<br/>
            • Enable hardware acceleration in Android Studio for better performance<br/>
            • For Bluetooth testing, the emulator supports virtual Bluetooth functionality<br/>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Development Setup
        </Typography>
        <Typography component="div" sx={{ pl: 2 }}>
          1. Prerequisites:<br/>
          <Box sx={{ pl: 3 }}>
            • Install Eclipse Temurin JDK 17 from{' '}
            <a href="https://adoptium.net/temurin/releases/?version=17" target="_blank" rel="noopener noreferrer">
              adoptium.net
            </a><br/>
            • Install Android Studio from{' '}
            <a href="https://developer.android.com/studio" target="_blank" rel="noopener noreferrer">
              developer.android.com/studio
            </a>
          </Box>
          <br/>
          2. Project Setup:<br/>
          <Box sx={{ pl: 3 }}>
            • Clone the repository and navigate to the PlantGuruCompose directory<br/>
            • Open Android Studio<br/>
            • Select "Open" and choose the PlantGuruCompose directory<br/>
            • Wait for the initial project indexing to complete
          </Box>
          <br/>
          3. Gradle Configuration:<br/>
          <Box sx={{ pl: 3 }}>
            • Wait for the automatic Gradle sync to complete (progress shown in bottom bar)<br/>
            • If sync doesn't start automatically, click "Sync Project with Gradle Files" in the toolbar<br/>
            • Once sync is complete, run Gradle build by clicking Build {'>'} Make Project
          </Box>
          <br/>
          4. Build Variant Selection:<br/>
          <Box sx={{ pl: 3 }}>
            • Click Build {'>'} Select Build Variant in the menu<br/>
            • In the Build Variants window (usually at bottom-left), select "production"<br/>
            • This ensures the app connects to the production backend server
          </Box>
          <br/>
          5. Running the App:<br/>
          <Box sx={{ pl: 3 }}>
            • Connect an Android device via USB (enable Developer Options and USB debugging)<br/>
            • Alternatively, create and configure an Android Emulator via Tools {'>'} Device Manager<br/>
            • Click the Run button (green play icon) or press Shift + F10<br/>
            • Select your target device and click OK<br/>
            • Wait for the app to build and install on your device
          </Box>
        </Typography>
      </>
    )
  },
  'backend': {
    title: 'Backend',
    content: (
      <>
        <Typography variant="h5" gutterBottom>
          Backend System
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          All data types specified in the request/response bodies should be valid MySQL types provided as strings, 
          as the API performs direct MySQL insertions without type conversion. For example, integers should be sent 
          as "123", floats as "123.45", and timestamps in MySQL datetime format "YYYY-MM-DD HH:MM:SS".
        </Typography>

        <Typography variant="h6" gutterBottom>
          Authentication
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Sign Up</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: POST /signup<br/>
            • Request Body:
            <pre>{`{
  "name": "string",
  "email": "string",
  "password": "string",
  "address": "string",
  "phoneNumber": "string"
}`}</pre>
            • Response:
            <pre>{`{
  "message": "User created with successfully",
  "user_id": "integer"
}`}</pre>
          </Box>

          <strong>Login</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: POST /login<br/>
            • Request Body:
            <pre>{`{
  "email": "string",
  "password": "string"
}`}</pre>
            • Response:
            <pre>{`{
  "message": "User logged in successfully",
  "token": "string",
  "user_id": "integer"
}`}</pre>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Plants
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Create Plant</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: POST /plantUpload<br/>
            • Request Body:
            <pre>{`{
  "user_id": "integer",
  "plant_name": "string",
  "age": "integer",
  "last_watered": "datetime",
  "next_watering_time": "datetime"
}`}</pre>
            • Response:
            <pre>{`{
  "message": "Plant data uploaded",
  "plant_id": "integer"
}`}</pre>
          </Box>

          <strong>Get Plants</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /plantRead<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- user_id: integer<br/>
            &nbsp;&nbsp;- include_sensors: boolean (optional) - if true, includes latest sensor readings<br/>
            • Response:
            <pre>{`[
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
]`}</pre>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Sensor Data
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Upload Sensor Data</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: POST /sensorUpload<br/>
            • Request Body: Single object or array of:
            <pre>{`{
  "plant_id": "integer",
  "ext_temp": "float",
  "light": "float",
  "humidity": "float",
  "soil_temp": "float",
  "soil_moisture_1": "float",
  "soil_moisture_2": "float",
  "time_stamp": "timestamp"
}`}</pre>
            • Response: "Successfully uploaded sensor data"
          </Box>

          <strong>Get Sensor Reading</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /sensorRead<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- time_stamp: timestamp<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>

          <strong>Get Last N Readings</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /lastNSensorReadings<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- n: integer<br/>
            • Response: Same format as sensorRead
          </Box>

          <strong>Get Time Series Data</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /timeSeriesData<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- start_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- end_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- granularity: string (optional) - one of: "raw", "minute", "hour", "day", "week", "month"<br/>
            &nbsp;&nbsp;- sensor_types: string (optional) - comma-separated list of sensor types to include<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Analysis & Statistics
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Get Sensor Analysis</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /analysis<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- start_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- end_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- metrics: string (optional) - comma-separated list of: "min", "max", "avg"<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>

          <strong>Get Sensor Statistics</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /sensorStats<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- sensor_type: string (one of: "ext_temp", "light", "humidity", "soil_temp", "soil_moisture_1", "soil_moisture_2")<br/>
            &nbsp;&nbsp;- start_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- end_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- remove_outliers: boolean (optional) - removes data points outside 2 standard deviations<br/>
            &nbsp;&nbsp;- smooth_data: boolean (optional) - applies data smoothing<br/>
            • Response:
            <pre>{`{
  "result": {
    "min_value": "float",
    "max_value": "float",
    "avg_value": "float",
    "total_readings": "integer"
  }
}`}</pre>
          </Box>

          <strong>Get Sensor Trendline</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /sensorTrendline<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- sensor_type: string (one of: "ext_temp", "light", "humidity", "soil_temp", "soil_moisture_1", "soil_moisture_2")<br/>
            &nbsp;&nbsp;- start_time: timestamp (ISO8601)<br/>
            &nbsp;&nbsp;- end_time: timestamp (ISO8601)<br/>
            • Response:
            <pre>{`{
  "result": {
    "slope": "float",
    "intercept": "float",
    "min_value": "float",
    "max_value": "float",
    "start_point": "timestamp",
    "end_point": "timestamp"
  }
}`}</pre>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Watering Events
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Upload Watering Event</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: POST /wateringUpload<br/>
            • Request Body: Single object or array of:
            <pre>{`{
  "watering_duration": "integer",
  "peak_temp": "float",
  "peak_moisture": "float",
  "avg_temp": "float",
  "avg_moisture": "float",
  "plant_id": "integer",
  "time_stamp": "timestamp",
  "volume": "float"
}`}</pre>
            • Response: "Successfully uploaded watering event data"
          </Box>

          <strong>Get Watering Events</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /wateringRead<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Device Provisioning
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Provision Device</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: POST /provision<br/>
            • Headers: Authorization: Bearer {'<user_token>'}<br/>
            • Request Body:
            <pre>{`{
  "plant_id": "integer"
}`}</pre>
            • Response:
            <pre>{`{
  "message": "Provisioning successful",
  "token": "string"
}`}</pre>
          </Box>
        </Typography>

        <Typography variant="h6" gutterBottom>
          Insights
        </Typography>
        <Typography component="div" sx={{ pl: 2, mb: 3 }}>
          <strong>Get Sensor Health</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /api/sensorHealth<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            &nbsp;&nbsp;- sensor_type: string (one of: "ext_temp", "light", "humidity", "soil_temp", "soil_moisture_1", "soil_moisture_2")<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>

          <strong>Get Plant Recommendations</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /api/plantRecommendations<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>

          <strong>Get Health Diagnostics</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /api/healthDiagnostics<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>

          <strong>Get Care Schedule</strong>
          <Box sx={{ pl: 2 }}>
            • Endpoint: GET /api/careSchedule<br/>
            • Query Parameters:<br/>
            &nbsp;&nbsp;- plant_id: integer<br/>
            • Response:
            <pre>{`{
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
}`}</pre>
          </Box>
        </Typography>
      </>
    )
  }
};

export default function Documentation() {
  const location = useLocation();
  const navigate = useNavigate();
  const [selectedSection, setSelectedSection] = useState('getting-started');

  useEffect(() => {
    const hash = location.hash.slice(1);
    if (hash && documentationSections[hash]) {
      setSelectedSection(hash);
    } else if (!hash) {
      navigate('#getting-started', { replace: true });
    }
  }, [location.hash]);

  const handleSectionChange = (section) => {
    setSelectedSection(section);
    navigate(`#${section}`, { replace: true });
  };

  return (
    <Box>
      <Typography variant="h3" component="h1" gutterBottom>
        Documentation
      </Typography>
      <Grid container spacing={4}>
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 0 }}>
            <List component="nav">
              {Object.entries(documentationSections).map(([key, section], index) => (
                <Box key={key}>
                  {index > 0 && <Divider />}
                  <ListItemButton
                    selected={selectedSection === key}
                    onClick={() => handleSectionChange(key)}
                  >
                    <ListItemText 
                      primary={section.title}
                      primaryTypographyProps={{
                        fontWeight: selectedSection === key ? 'bold' : 'normal',
                      }}
                    />
                  </ListItemButton>
                </Box>
              ))}
            </List>
          </Paper>
        </Grid>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            {documentationSections[selectedSection].content}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
} 