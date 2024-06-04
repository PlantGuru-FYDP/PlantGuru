#include <WiFi.h>
#include <HTTPClient.h>
#include "constants.h"

bool postSensorData(const String& url, SensorData& data, int numRetries) {
  if(WiFi.status() != WL_CONNECTED) {
    Serial.println("WiFi Disconnected");
    return false;
  }

  HTTPClient http;
  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  int httpResponseCode = 0;
  while(numRetries-- > 0) {
    httpResponseCode = http.POST(data.toJson());
    if(httpResponseCode > 0) {
      String response = http.getString();
      Serial.println("HTTP Response code: " + String(httpResponseCode));
      Serial.println("Response: " + response);
      http.end();
      return true;
    } else {
      Serial.println("Attempt failed, retrying...");
      delay(1000); // Wait a second before retrying
    }
  }

  http.end();
  return false;
}