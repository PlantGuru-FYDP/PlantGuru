#include <ArduinoJson.h> 

#define lightPin A0
#define soilPin1 A1
#define soilPin2 A2
#define DHT22_PIN D2
#define DS18S20_Pin D7
#define SD_PIN D6

#define DHTTYPE DHT22

#define ANALOG_MAX 4095.0

#ifndef CONSTANTS_H
#define CONSTANTS_H

class SensorData {
public:
  float soilMoisture1;
  float soilMoisture2;
  float temperature1;
  float temperature2;
  float temperature3;
  float humidity;
  float light;
  long timestamp;

  SensorData() :
    soilMoisture1(NAN), soilMoisture2(NAN), temperature1(NAN),
    temperature2(NAN), temperature3(NAN), humidity(NAN),
    light(NAN), timestamp(-1) {}

  String toJson() {
    StaticJsonDocument<256> doc;
    if (!isnan(soilMoisture1)) doc["soilMoisture1"] = soilMoisture1;
    if (!isnan(soilMoisture2)) doc["soilMoisture2"] = soilMoisture2;
    if (!isnan(temperature1)) doc["temperature1"] = temperature1;
    if (!isnan(temperature2)) doc["temperature2"] = temperature2;
    if (!isnan(temperature3)) doc["temperature3"] = temperature3;
    if (!isnan(humidity)) doc["humidity"] = humidity;
    if (!isnan(light)) doc["light"] = light;
    if (timestamp != -1) doc["timestamp"] = timestamp;
    String json;
    serializeJson(doc, json);
    return json;
  }
};

#endif