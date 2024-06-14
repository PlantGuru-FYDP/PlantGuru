#include <WiFi.h>
#include <HTTPClient.h>
#include "Memory.h"

#define ntpServer "pool.ntp.org"

#ifndef WIFISERVICE_H
#define WIFISERVICE_H

bool isTimeSet() {
  time_t now;
  struct tm timeinfo;
  if(!getLocalTime(&timeinfo)){
    return false;
  }
  return true;
}

bool requestTime() {
  configTime(0, 0, ntpServer);
  return isTimeSet();
}

time_t getUnixTime() {
  struct tm timeinfo;
  if(!getLocalTime(&timeinfo)){
    return 0;
  }
  return mktime(&timeinfo);
}

bool postSensorData(const String& url, String& postData, int numRetries) {
  Serial.println("Attemping to post data to webserver");

  if(WiFi.status() != WL_CONNECTED) {
    Serial.println("Cannot post: WiFi is disconnected");
    return false;
  }

  if (!isTimeSet()) {
    Serial.println("Cannot post: Time is not set");
    requestTime();
    return false;
  }

  SensorData data;

  HTTPClient http;
  http.begin(url);
  http.addHeader("Content-Type", "application/json");

  int httpResponseCode = 0;
  int maxPerRequest = 10;
  SensorData sendSensorDataBuffer[maxPerRequest];
  int i = 0;
  while (popFront(cb, data) && i < maxPerRequest) {
    sendSensorDataBuffer[i] = data;
    i++;
  }
  while(numRetries-- > 0) {
    httpResponseCode = http.POST(postData);
    if(httpResponseCode > 0) {
      String response = http.getString();
      Serial.println("HTTP Response code: " + String(httpResponseCode));
      Serial.println("Response: " + response);
      http.end();

      if (httpResponseCode == 200) {
        Serial.println("Sucessfully posted data to webserver, deleting data from buffer");
        saveBufferState(cb);
        return true;
      }
    } else {
      Serial.println("Attempt failed, retrying...");
      delay(1000); // Wait a second before retrying
    }
  }

  Serial.println("Failed to post to webserver, reverting buffer");

  // add back
  for (int j = maxPerRequest; j >= 0; j--) {
    pushFront(cb, sendSensorDataBuffer[j]);
  }

  http.end();
  return false;
}

#endif
