#ifndef WIFISERVICE_H
#define WIFISERVICE_H

#include <WiFi.h>
#include <HTTPClient.h>
#include "Memory.h"
// #include "esp_wpa2.h"
#include <esp_wifi.h>
#include "Certificate.h"
#include "TimeService.h"

// Forward declaration of SensorManager class
class SensorManager;

bool postData(const String& url, const String& jsonPayload, int numRetries) {
  Serial.println("Attempting to post data to webserver");
  Serial.println("URL: " + url);
  Serial.println("Payload: " + jsonPayload);

  if(WiFi.status() != WL_CONNECTED) {
    Serial.println("Cannot post: WiFi is disconnected");
    return false;
  }

  if (!isTimeSet()) {
    Serial.println("Cannot post: Time is not set");
    requestTime();
    return false;
  }

  HTTPClient http;
  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  int httpResponseCode = 0;
  while(numRetries-- > 0) {
    httpResponseCode = http.POST(jsonPayload);
    if(httpResponseCode > 0) {
      String response = http.getString();
      Serial.println("HTTP Response code: " + String(httpResponseCode));
      Serial.println("Response: " + response);
      http.end();

      if (httpResponseCode == 200) {
        return true;
      }
    } else {
      Serial.println("Attempt failed, retrying...");
      Serial.println(String(httpResponseCode));
      delay(1000);
    }
  }

  http.end();
  return false;
}

bool postSensorData(const String& url, int numRetries, SensorManager& sensorManager) {
  if (isEmpty(cb)){
    Serial.println("Cannot post: No Data");
    return false;
  }

  SensorData data;
  int maxPerRequest = 10;
  SensorData sendSensorDataBuffer[maxPerRequest];
  int i = 0;
  String json_info = "[";
  
  while (popFront(cb, data) && i < maxPerRequest) {
    sendSensorDataBuffer[i] = data;
    
    time_t unixTimestampSecs = sendSensorDataBuffer[i].timestamp;
    struct tm timeInfo;
    gmtime_r(&unixTimestampSecs, &timeInfo);
    
    char timestampStr[20];
    strftime(timestampStr, sizeof(timestampStr), "%Y-%m-%dT%H:%M:%S", &timeInfo);
    sendSensorDataBuffer[i].date = String(timestampStr);
    
    preferences.begin("device_prefs", true);
    sendSensorDataBuffer[i].plant_id = preferences.getInt("plant_id", -1);
    preferences.end();
    
    if (i != 0) {
      json_info = json_info + "," + sendSensorDataBuffer[i].toJson();
    } else {
      json_info = json_info + sendSensorDataBuffer[i].toJson();
    }
    i++;
  }
  json_info = json_info + "]";

  if (sendSensorDataBuffer[0].plant_id == -1) {
    Serial.println("Cannot post: Invalid plant ID");
    for (int j = i - 1; j >= 0; j--) {
      pushFront(cb, sendSensorDataBuffer[j]);
    }
    return false;
  }

  bool success = postData(url, json_info, numRetries);
  
  if (!success) {
    Serial.println("Failed to post to webserver, reverting buffer");
    for (int j = i - 1; j >= 0; j--) {
      pushFront(cb, sendSensorDataBuffer[j]);
    }
  } else {
    Serial.println("Successfully posted data to webserver, saving buffer state");
    saveBufferState(cb);
  }

  return success;
}

void setupEnterpriseWiFi() {
    Serial.println("\n=== Starting Enterprise WiFi Setup ===");
    
    preferences.begin("device_prefs", true);
    bool isEnterprise = preferences.getBool("is_enterprise", false);
    
    if (isEnterprise) {
        String identity = preferences.getString("enterprise_identity", "");
        String username = preferences.getString("enterprise_username", "");
        String password = preferences.getString("enterprise_password", "");
        String ssid = preferences.getString("wifi_ssid", "");
        
        Serial.println("Retrieved enterprise credentials:");
        Serial.printf("SSID: %s\n", ssid.c_str());
        Serial.printf("Identity length: %d\n", identity.length());
        Serial.printf("Username length: %d\n", username.length());
        Serial.printf("Password length: %d\n", password.length());
        
        if (!identity.isEmpty() && !username.isEmpty() && !password.isEmpty()) {
            Serial.println("All required credentials present, configuring WPA2 Enterprise...");
            
            WiFi.disconnect(true);
            WiFi.mode(WIFI_STA);
            
            // esp_wifi_sta_wpa2_ent_set_identity((uint8_t *)identity.c_str(), identity.length());
            // esp_wifi_sta_wpa2_ent_set_username((uint8_t *)username.c_str(), username.length());
            // esp_wifi_sta_wpa2_ent_set_password((uint8_t *)password.c_str(), password.length());
            
            // esp_wifi_sta_wpa2_ent_set_ca_cert(ca_cert, ca_cert_len);
            
            // esp_wifi_sta_wpa2_ent_enable();
            
            WiFi.begin(ssid.c_str());
            Serial.println("Enterprise WiFi configuration complete, attempting connection...");
        } else {
            Serial.println("ERROR: Missing required enterprise credentials!");
            Serial.printf("Identity present: %s\n", identity.isEmpty() ? "No" : "Yes");
            Serial.printf("Username present: %s\n", username.isEmpty() ? "No" : "Yes");
            Serial.printf("Password present: %s\n", password.isEmpty() ? "No" : "Yes");
        }
    } else {
        Serial.println("Not using enterprise WiFi");
    }
    preferences.end();
    Serial.println("=== Enterprise WiFi Setup Complete ===\n");
}

#endif // WIFISERVICE_H
