#include <WiFi.h>
#include "Provisioning.h"
#include <Preferences.h>
#include <Arduino.h>
#include "Scheduling.h"
#include "SensorService.h"
#include "WifiService.h"
#include "Memory.h"
#include "BLEService.h"
#include "Config.h"
#include <HTTPClient.h>
// #include <esp_wpa2.h>

#define BUTTON2_PIN 27
#define BUTTON_DEBOUNCE_TIME 50
#define LED_PIN 2

#define BT_UPDATE_INTERVAL 2000
#define WIFI_UPDATE_INTERVAL 20000
#define RESET_BTN_UPDATE_INTERVAL 100
#define RESET_LISTENER_UPDATE_INTERVAL 100
#define SENSOR_UPDATE_INTERVAL 0
#define SENSOR_RECORD_INTERVAL 60000
#define RESTART_DELAY 0




SensorManager sensorManager;
Scheduler scheduler;

DeviceMode mode = MODE_PROVISION;
bool beginRestart = false;
unsigned long restartTime = 0;

#define SERVICE_UUID "b4df5a1c-3f6b-f4bf-ea4a-820304901a02"
#define PROV_UUID "b4df5a1c-3f6b-f4bf-ea4a-820304901a02"

const char* pop = "abcd1234";
const char* uuid = "b4df5a1c-3f6b-f4bf-ea4a-820304901a02";
const char* service_name = "GURU_123";
const char* service_key = "password"; 

void handle_wifi_connected();

void SysProvEvent(arduino_event_t *sys_event) {
    bool verified = false;
    
    switch (sys_event->event_id) {
        case ARDUINO_EVENT_WIFI_STA_GOT_IP:
            Serial.print("\nConnected IP address : ");
            Serial.println(IPAddress(sys_event->event_info.got_ip.ip_info.ip.addr));
            Serial.println("WiFi connection successful!");
            handle_wifi_connected();
            break;
        case ARDUINO_EVENT_WIFI_STA_DISCONNECTED:
            Serial.println("\nDisconnected from WiFi");
            Serial.printf("Disconnect reason: %d\n", sys_event->event_info.wifi_sta_disconnected.reason);
            Serial.println("Attempting to reconnect...");
            break;
        case ARDUINO_EVENT_PROV_START:
            Serial.println("\nProvisioning started");
            Serial.println("Waiting for app to send credentials...");
            break;
        case ARDUINO_EVENT_PROV_CRED_RECV: {
            Serial.println("\nReceived Wi-Fi credentials");
            Serial.printf("SSID: %s\n", (const char *) sys_event->event_info.prov_cred_recv.ssid);
            Serial.println("Attempting to save credentials...");

            // Save the Wi-Fi credentials
            preferences.begin("device_prefs");
            preferences.putString("wifi_ssid", (const char *) sys_event->event_info.prov_cred_recv.ssid);
            preferences.putString("wifi_password", (const char *) sys_event->event_info.prov_cred_recv.password);
            preferences.end();
            
            Serial.println("Credentials saved to preferences");
            break;
        }
        case ARDUINO_EVENT_PROV_CRED_FAIL: {
            Serial.println("\nProvisioning failed!");
            if (sys_event->event_info.prov_fail_reason == WIFI_PROV_STA_AUTH_ERROR) {
                Serial.println("WiFi authentication failed - check password");
            } else {
                Serial.println("WiFi network not found - check SSID");
            }
            break;
        }
        case ARDUINO_EVENT_PROV_CRED_SUCCESS:
            Serial.println("\nProvisioning successful!");
            Serial.println("WiFi credentials applied successfully");
            break;
        case ARDUINO_EVENT_PROV_END:
            Serial.println("\nProvisioning complete");
            preferences.begin("device_prefs");
            verified = preferences.getBool("verified", false);
            preferences.end();
            
            if (verified) {
                Serial.println("Device verified with backend - scheduling restart");
                preferences.begin("device_prefs");
                preferences.putUInt("device_mode", MODE_ACTIVATED);
                preferences.end();
                restartTime = millis();  // Set restart timer
                beginRestart = true;
            } else {
                Serial.println("Device not verified with backend - staying in provisioning mode");
                ESP.restart();
            }
            break;
        default:
            // These events are expected and can be ignored:
            // 1: BLE_GAP_EVENT_CONNECTED
            // 3: BLE_GAP_EVENT_DISCONNECT
            // 4: BLE_GATTS_EVENT_WRITE
            // 34: ARDUINO_EVENT_WIFI_STA_START
            if (sys_event->event_id != 1 && 
                sys_event->event_id != 3 && 
                sys_event->event_id != 4 && 
                sys_event->event_id != 34) {
                Serial.printf("\nUnhandled provisioning event: %d\n", sys_event->event_id);
            }
            break;
    }
}

void printWiFiCredentials() {
    preferences.begin("device_prefs");
    String ssid = preferences.getString("wifi_ssid", "No SSID");
    String password = preferences.getString("wifi_password", "No Password");
    preferences.end();
    Serial.println("Stored WiFi Credentials:");
    Serial.print("SSID: ");
    Serial.println(ssid);
    Serial.print("Password: ");
    Serial.println(password);
}

String test = "test";

unsigned long lastButtonPress = 0;
bool lastButtonState = HIGH;

void setup() {

  pinMode(BUTTON2_PIN, INPUT_PULLUP);
  sensorManager.setupBeforeSerial();

  Serial.begin(115200);

  sensorManager.setupAfterSerial();
  preferences.begin("device_prefs", false);
  
  #if NO_PROVISIONING
    mode = MODE_ACTIVATED;
    // Force save the credentials
    preferences.putString("wifi_ssid", DEFAULT_WIFI_SSID);
    preferences.putString("wifi_password", DEFAULT_WIFI_PASSWORD);
    preferences.putInt("plant_id", DEFAULT_PLANT_ID);
  #else
    // Load the device mode from preferences
    mode = (DeviceMode)preferences.getUInt("device_mode", MODE_PROVISION);
  #endif

  preferences.end();

  Serial.print("Device mode: ");
  Serial.println(mode == MODE_PROVISION ? "Provision" : mode == MODE_ACTIVATED ? "Activated" : "Unknown");

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, mode == MODE_PROVISION ? HIGH : LOW);  // LED ON in provisioning mode, OFF in activated mode

  printWiFiCredentials();

  switch (mode) {
    case MODE_PROVISION: {
      Serial.println("Starting provisioning mode setup...");
      Serial.println("Initializing WiFi for provisioning...");
      WiFi.onEvent(SysProvEvent);

      // Generate a unique device ID
      uint8_t mac[6];
      #ifndef esp_read_mac(mac, ESP_IF_WIFI_STA)
        #define esp_read_mac(mac, ESP_IF_WIFI_STA) WiFi.macAddress(mac)
      esp_read_mac(mac, ESP_MAC_WIFI_STA);
      #endif
      char deviceId[13];
      snprintf(deviceId, sizeof(deviceId), "%02X%02X%02X%02X%02X%02X", 
               mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
      
      preferences.begin("device_prefs");
      preferences.putString("device_id", deviceId);
      preferences.end();

      Serial.printf("Generated Device ID: %s\n", deviceId);

      // Start provisioning as before...
      Serial.println("Starting WiFi Provisioning...");
      uint8_t uuid[16];
      const char *uuid_str = SERVICE_UUID;
      for(int i = 0; i < 16; i++) {
        sscanf(uuid_str + (i * 2), "%2hhx", &uuid[i]);
      }

      String pop = "abcd1234";
      Serial.printf("User token: %s\n", pop.c_str());
      Provisioning.beginProvision(
          WIFI_PROV_SCHEME_BLE,
          WIFI_PROV_SCHEME_HANDLER_FREE_BTDM,
          WIFI_PROV_SECURITY_0,
          pop.c_str(),
          service_name,
          service_key,
          uuid,
          true,
          PLANTGURU_SERVER
      );
      
      // Memory
      initCircularBuffer(cb);
      saveBufferState(cb);

      scheduler.add([]() {
        if (beginRestart && millis() - restartTime >= RESTART_DELAY) {
            Serial.println("Restarting device after successful provisioning...");
            preferences.begin("device_prefs");
            preferences.putUInt("device_mode", MODE_ACTIVATED);
            preferences.end();
            ESP.restart();
        }
      }, RESET_LISTENER_UPDATE_INTERVAL);
      break;
    }
    case MODE_ACTIVATED: {
      Serial.println("Beginning Regular Setup");
      preferences.begin("device_prefs");
      bool isEnterprise = preferences.getBool("is_enterprise", false);
      preferences.end();
      
      if (isEnterprise) {
          setupEnterpriseWiFi();
      } else {
          preferences.begin("device_prefs");
          WiFi.begin(preferences.getString("wifi_ssid", "No SSID").c_str(), 
                    preferences.getString("wifi_password", "No Password").c_str());
          preferences.end();
      }
      
      Serial.println("Connecting to WiFi...");
      if (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Connected to wifi");
      } else {
        Serial.println("Could not connect to wifi");
      }

      // Memory
      loadBufferState(cb);

      // Scheduled tasks
      scheduler.add([&]() { sensorManager.run(); }, SENSOR_UPDATE_INTERVAL);  // Fast sensor readings
      scheduler.add([&]() { sensorManager.recordToBuffer(); }, SENSOR_RECORD_INTERVAL);  // Record every minute
      
      // get plant id from preferences
      int plant_id = preferences.getInt("plant_id", -1);
      scheduler.add([&]() {
        postSensorData(PLANTGURU_SENSOR_ENDPOINT, 3, sensorManager);
      }, WIFI_UPDATE_INTERVAL);
      break;
    }
    default: {
      Serial.println("No mode found. Not setting up!");
      Serial.printf("Device mode: %d\n", mode);
      break;
    }
  }

  scheduler.add([]() {
    // Read current button state
    bool currentButtonState = digitalRead(BUTTON2_PIN);
    
    // Check if button state changed from HIGH to LOW (button press)
    if (currentButtonState == LOW && lastButtonState == HIGH) {
        // Debounce check
        if (millis() - lastButtonPress > BUTTON_DEBOUNCE_TIME) {
            Serial.println("Reset button pressed!");
            
            // Clear all preferences namespaces
            preferences.begin("device_prefs", false);
            preferences.clear();
            // Explicitly clear enterprise settings
            preferences.putBool("is_enterprise", false);
            preferences.putString("enterprise_identity", "");
            preferences.putString("enterprise_username", "");
            preferences.putString("enterprise_password", "");
            preferences.end();

            // Initialize an empty buffer
            initCircularBuffer(cb);
            saveBufferState(cb);

            Serial.println("All preferences cleared!");
            delay(100);  // Small delay to ensure serial prints
            ESP.restart();  // Hard restart the device
        }
        lastButtonPress = millis();
    }
    lastButtonState = currentButtonState;
  }, RESET_BTN_UPDATE_INTERVAL);
}

void loop() {
    scheduler.run();

    if (WiFi.status() == WL_CONNECTED) {
        static unsigned long lastDebugPrint = 0;
        if (millis() - lastDebugPrint > 5000) {  // Print every 5 seconds
            preferences.begin("device_prefs", true);
            int plantId = preferences.getInt("plant_id", -1);
            preferences.end();
            
            if (plantId != -1) {
                Serial.printf("Current Plant ID: %d\n", plantId);
            }
            lastDebugPrint = millis();
        }
        
        if (!isTimeSet()) {
            requestTime();
        } else {
            //Serial.printf("Time: %d\n", getUnixTime());
        }
    }
}
