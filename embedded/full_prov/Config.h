#ifndef CONFIG_H
#define CONFIG_H

#include <ArduinoJson.h>
#include <Preferences.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// ==========================================
// Global Preferences Instance
// ==========================================
extern Preferences preferences;

// ==========================================
// Device Mode Configuration
// ==========================================
enum DeviceMode {
    MODE_PROVISION,
    MODE_ACTIVATED
};

#define NO_PROVISIONING false  // Set to true to skip provisioning mode

// Only used when NO_PROVISIONING is true
#define DEFAULT_WIFI_SSID ""
#define DEFAULT_WIFI_PASSWORD ""
#define DEFAULT_PLANT_ID -1 // garlic = 10

// ==========================================
// Server Configuration
// ==========================================
#define USE_PRODUCTION_SERVER true  // Set to true to use production server

#if USE_PRODUCTION_SERVER
    #define PLANTGURU_BASE_URL "http://52.14.140.110:3000"
#else
    #define PLANTGURU_BASE_URL "http://192.168.2.225:3000"
#endif

#define PLANTGURU_SERVER PLANTGURU_BASE_URL
#define PLANTGURU_SENSOR_ENDPOINT PLANTGURU_BASE_URL "/api/sensorUpload"

// ==========================================
// Device Configuration
// ==========================================
#define SERVICE_NAME "GURU_123"
#define SERVICE_UUID "b4df5a1c-3f6b-f4bf-ea4a-820304901a02"
#define SERVICE_KEY "password"
#define POP_KEY "abcd1234"

// ==========================================
// Hardware Pin Configuration
// ==========================================
#define BUTTON2_PIN 27
#define LED_PIN 2
#define DS18S20_Pin D7
#define DHT22_PIN 3
#define LM35_PIN A3
#define LIGHT_PIN A0
#define SOIL_PIN1 A1
#define SOIL_PIN2 A2

// ==========================================
// Timing Configuration
// ==========================================
#define BUTTON_DEBOUNCE_TIME 50
#define BT_UPDATE_INTERVAL 2000
#define WIFI_UPDATE_INTERVAL 20000
#define RESET_BTN_UPDATE_INTERVAL 100
#define RESET_LISTENER_UPDATE_INTERVAL 100
#define SENSOR_UPDATE_INTERVAL 0
#define SENSOR_RECORD_INTERVAL 60000
#define RESTART_DELAY 0

// ==========================================
// Sensor Configuration
// ==========================================
#define USE_LM35 true  // Set to false to use DHT22 instead
#define DHTTYPE DHT22
#define ANALOG_MAX 4095.0

// ==========================================
// BLE Service Configuration
// ==========================================
// Main service UUID
#define BLE_SERVICE_UUID "19b10000-e8f2-537e-4f6c-d104768a1214"

// Sensor characteristics
#define TEMPERATURE1_CHARACTERISTIC_UUID "19b10001-e8f2-537e-4f6c-d104768a1215"
#define TEMPERATURE2_CHARACTERISTIC_UUID "19b10002-e8f2-537e-4f6c-d104768a1216"
#define LIGHT_CHARACTERISTIC_UUID "19b10003-e8f2-537e-4f6c-d104768a1217"
#define SOILMOISTURE1_CHARACTERISTIC_UUID "19b10004-e8f2-537e-4f6c-d104768a1218"
#define SOILMOISTURE2_CHARACTERISTIC_UUID "19b10005-e8f2-537e-4f6c-d104768a1219"
#define HUMIDITY_CHARACTERISTIC_UUID "19b10006-e8f2-537e-4f6c-d104768a1220"

// Settings characteristics
#define RESET_CHARACTERISTIC_UUID "19b10007-e8f2-537e-4f6c-d104768a1221"
#define ENDPOINT_CHARACTERISTIC_UUID "19b10008-e8f2-537e-4f6c-d104768a1222"
#define UPDATE_PERIOD_BT_CHARACTERISTIC_UUID "19b10009-e8f2-537e-4f6c-d104768a1223"
#define UPDATE_PERIOD_WIFI_CHARACTERISTIC_UUID "19b10010-e8f2-537e-4f6c-d104768a1224"

// ==========================================
// Bluetooth Configuration
// ==========================================
#define CONFIG_BLUEDROID_ENABLED 1
#define CONFIG_BT_ENABLED 1
#define CONFIG_BTDM_CTRL_MODE_BLE_ONLY 1
#define CONFIG_BTDM_CTRL_MODE_BR_EDR_ONLY 0
#define CONFIG_BTDM_CTRL_MODE_BTDM 0
#define CONFIG_BT_CLASSIC_ENABLED 0

// ==========================================
// WiFi Provisioning Configuration
// ==========================================
#define WIFI_PROV_SECURITY_VERSION 1
#define WIFI_PROV_SECURITY_FLAG_NONE 0

// ==========================================
// Memory Configuration
// ==========================================
#define BUFFER_SIZE 500  // Maximum number of elements in the circular buffer

// ==========================================
// Data Structures
// ==========================================
// Sensor data structure
class SensorData {
public:
    int plant_id;
    float soilMoisture1;
    float soilMoisture2;
    float temperature1;
    float temperature2;
    float temperature3;
    float humidity;
    float light;
    long timestamp;
    String date;

    SensorData() :
        plant_id(-1), soilMoisture1(NAN), soilMoisture2(NAN), temperature1(NAN),
        temperature2(NAN), temperature3(NAN), humidity(NAN),
        light(NAN), timestamp(-1) {}

    String toJson() {
        StaticJsonDocument<256> doc;
        if (plant_id != -1) doc["plant_id"] = plant_id;
        if (!isnan(soilMoisture1)) doc["soil_moisture_1"] = soilMoisture1;
        if (!isnan(soilMoisture2)) doc["soil_moisture_2"] = soilMoisture2;
        if (!isnan(temperature1)) doc["soil_temp"] = temperature1;
        if (!isnan(temperature2)) doc["ext_temp"] = temperature2;
        if (!isnan(temperature3)) doc["temperature3"] = temperature3;
        if (!isnan(humidity)) doc["humidity"] = humidity;
        if (!isnan(light)) doc["light"] = light;
        if (!date.isEmpty()) doc["time_stamp"] = date;
        String json;
        serializeJson(doc, json);
        return json;
    }
};

// Forward declaration of CircularBuffer
struct CircularBuffer;

// Global circular buffer instance
extern CircularBuffer cb;

#endif // CONFIG_H 