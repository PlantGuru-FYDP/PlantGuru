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
  int plant_id;
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
    if (!isnan(plant_id)) doc["plant_id"] = plant_id;
    if (!isnan(soilMoisture1)) doc["soil_moisture_1"] = soilMoisture1;
    if (!isnan(soilMoisture2)) doc["soil_moisture_2"] = soilMoisture2;
    if (!isnan(temperature1)) doc["soil_temp"] = temperature1;
    if (!isnan(temperature2)) doc["ext_temp"] = temperature2;
    if (!isnan(temperature3)) doc["temperature_3"] = temperature3;
    if (!isnan(humidity)) doc["humidity"] = humidity;
    if (!isnan(light)) doc["light"] = light;
    if (timestamp != -1) doc["time_stamp"] = timestamp;
    String json;
    serializeJson(doc, json);
    return json;
  }
};

#endif