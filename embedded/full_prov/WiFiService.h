#include <WiFi.h>
#include <HTTPClient.h>
#include "Memory.h"
#include <time.h>

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

  if (isEmpty(cb)){
    Serial.println("Cannot post: No Data");
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
  String json_info = "[";
  while (popFront(cb, data) && i < maxPerRequest) {
    sendSensorDataBuffer[i] = data;
    //Do conversion here 
    time_t unixTimestampSecs = sendSensorDataBuffer[i].timestamp;

    struct tm timeInfo;
    gmtime_r(&unixTimestampSecs, &timeInfo); // Convert to UTC time

    char timestampStr[20];
    strftime(timestampStr, sizeof(timestampStr), "%Y-%m-%dT%H:%M:%S", &timeInfo);
    sendSensorDataBuffer[i].date = String(timestampStr);
    
    //Placeholder hard coded, remove when there's a proper way to pass in the plant_id
    sendSensorDataBuffer[i].plant_id = 7;
    if (i != 0){
      json_info = json_info + "," + sendSensorDataBuffer[i].toJson();
    }else{
      json_info = json_info + sendSensorDataBuffer[i].toJson();
    }
    i++;
  }
  json_info = json_info + "]";
  Serial.println(json_info);
  while(numRetries-- > 0) {
    httpResponseCode = http.POST(json_info);
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
      Serial.println(String(httpResponseCode));
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
