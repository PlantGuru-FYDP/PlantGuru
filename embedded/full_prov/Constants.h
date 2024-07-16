#include <ArduinoJson.h>
#include <string.h>

#ifndef Constants_h
#define Constants_h

Preferences preferences;

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
    if (!isnan(temperature3)) doc["temperature3"] = temperature3;
    if (!isnan(humidity)) doc["humidity"] = humidity;
    if (!isnan(light)) doc["light"] = light;
    if (timestamp != -1) doc["time_stamp"] = date;
    String json;
    serializeJson(doc, json);
    return json;
  }
};

#endif