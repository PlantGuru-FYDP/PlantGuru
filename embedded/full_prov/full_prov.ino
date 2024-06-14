#include <WiFi.h>
#include "Provisioning.h"
#include <Preferences.h>
#include <Arduino.h>
#include "Scheduling.h"
#include "SensorService.h"
#include "WifiService.h"
#include "Memory.h"
#include "BLEService.h"

#define BUTTON2_PIN 27
#define LED_PIN 2

#define BT_UPDATE_INTERVAL 2000
#define WIFI_UPDATE_INTERVAL 20000
#define RESET_BTN_UPDATE_INTERVAL 100
#define RESET_LISTENER_UPDATE_INTERVAL 100
#define SENSOR_UPDATE_INTERVAL 0

#define PLANTGURU_SERVER "http://18.191.162.227:3000/test"

enum DeviceMode {
    MODE_PROVISION,
    MODE_ACTIVATED,
};

SensorManager sensorManager;
Scheduler scheduler;

DeviceMode mode = MODE_PROVISION;
bool beginRestart = false;
const char* service_name = "GURU_123"; // Name of your device

void SysProvEvent(arduino_event_t *sys_event) {
    switch (sys_event->event_id) {
        case ARDUINO_EVENT_WIFI_STA_GOT_IP:
            Serial.print("\nConnected IP address : ");
            Serial.println(IPAddress(sys_event->event_info.got_ip.ip_info.ip.addr));
            break;
        case ARDUINO_EVENT_WIFI_STA_DISCONNECTED:
            Serial.println("\nDisconnected. Connecting to the AP again... ");
            break;
        case ARDUINO_EVENT_PROV_START:
            Serial.println("\nProvisioning started\nGive Credentials of your access point using smartphone app");
            break;
        case ARDUINO_EVENT_PROV_CRED_RECV: {
            Serial.println("\nReceived Wi-Fi credentials");
            Serial.print("\tSSID : ");
            Serial.println((const char *) sys_event->event_info.prov_cred_recv.ssid);
            Serial.print("\tPassword : ");
            Serial.println((char const *) sys_event->event_info.prov_cred_recv.password);

            // Save the Wi-Fi credentials
            preferences.begin("device_prefs");
            preferences.putString("wifi_ssid", (const char *) sys_event->event_info.prov_cred_recv.ssid);
            preferences.putString("wifi_password", (const char *) sys_event->event_info.prov_cred_recv.password);
            preferences.end();
            break;
        }
        case ARDUINO_EVENT_PROV_CRED_FAIL: {
            Serial.println("\nProvisioning failed!\nPlease reset to factory and retry provisioning\n");
            if (sys_event->event_info.prov_fail_reason == WIFI_PROV_STA_AUTH_ERROR)
                Serial.println("\nWi-Fi AP password incorrect");
            else
                Serial.println("\nWi-Fi AP not found....Add API \"nvs_flash_erase()\" before beginProvision()");
            ESP.restart();
            break;
        }
        case ARDUINO_EVENT_PROV_CRED_SUCCESS:
            Serial.println("\nProvisioning Successful");
            break;
        case ARDUINO_EVENT_PROV_END:
            Serial.println("\nProvisioning Ends");
            beginRestart = true;
            break;
        default:
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

void setup() {

  pinMode(BUTTON2_PIN, INPUT_PULLUP);
  sensorManager.setupBeforeSerial();

  Serial.begin(115200);

  sensorManager.setupAfterSerial();
  preferences.begin("device_prefs", false);
  mode = (DeviceMode)preferences.getUInt("device_mode", MODE_PROVISION);

  Serial.print("Device mode: ");
  Serial.println(mode == MODE_PROVISION ? "Provision" : mode == MODE_ACTIVATED ? "Activated" : "Unknown");

  pinMode(LED_PIN, OUTPUT);

  printWiFiCredentials();

  switch (mode) {
    case MODE_PROVISION: {
      Serial.println("Beginning Provisioning Setup");
      WiFi.onEvent(SysProvEvent);

      Serial.println("Starting WiFi Provisioning...");
      uint8_t uuid[16] = {0xb4, 0xdf, 0x5a, 0x1c, 0x3f, 0x6b, 0xf4, 0xbf,
              0xea, 0x4a, 0x82, 0x03, 0x04, 0x90, 0x1a, 0x02 };
      // user token is the key to switch to provision mode
      // get token from preferences
      String pop = "abcd1234"; //preferences.getString("user_token", "abcd1234");
      Serial.printf("User token: %s\n", pop.c_str());
      Provisioning.beginProvision(WIFI_PROV_SCHEME_BLE, WIFI_PROV_SCHEME_HANDLER_FREE_BTDM, WIFI_PROV_SECURITY_0, pop.c_str(), service_name, NULL, uuid, true);
      
      // Memory
      initCircularBuffer(cb);
      saveBufferState(cb);

      scheduler.add([]() {
        if (beginRestart) {
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
      WiFi.begin(preferences.getString("wifi_ssid", "No SSID").c_str(), preferences.getString("wifi_password", "No Password").c_str());
      preferences.end();
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
      scheduler.add([&]() { sensorManager.run(); }, SENSOR_UPDATE_INTERVAL);
      
      scheduler.add([&]() { postSensorData(PLANTGURU_SERVER, test, 1); }, WIFI_UPDATE_INTERVAL);
      break;
    }
    default: {
      Serial.println("No mode found. Not setting up!");
      Serial.printf("Device mode: %d\n", mode);
      break;
    }
  }

  scheduler.add([]() {
    if (digitalRead(BUTTON2_PIN) == LOW) {
      preferences.begin("device_prefs");
      preferences.clear();
      preferences.end();

      initCircularBuffer(cb);
      saveBufferState(cb);
      //preferences.putUInt("device_mode", MODE_PROVISION);
      Serial.println("Preferences cleared!");
      ESP.restart();
    }
  }, RESET_BTN_UPDATE_INTERVAL);
}

void loop() {
  scheduler.run();

  if (WiFi.status() == WL_CONNECTED) {
    if (!isTimeSet()) {
      requestTime();
    } else {
      //Serial.printf("Time: %d\n", getUnixTime());
    }
  }
}
