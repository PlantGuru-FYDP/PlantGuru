#ifndef SENSORSERVICE_H
#define SENSORSERVICE_H

#include <OneWire.h>
#include <DallasTemperature.h>
#include "DHT.h"
#include <ArduinoJson.h>
#include "Config.h"
#include "Memory.h"
#include "TimeService.h"

OneWire oneWire(DS18S20_Pin);
DallasTemperature sensors(&oneWire);

#if !USE_LM35
DHT dht(DHT22_PIN, DHTTYPE);
#endif

class SensorManager {
private:
  SensorData currentData;
  int sampleCount;
  float runningAvgTemperature1;
  float runningAvgTemperature2;
  float runningAvgHumidity;
  float runningAvgLight;
  float runningAvgSoilMoisture1;
  float runningAvgSoilMoisture2;
  bool validTemperature1;
  bool validTemperature2;
  bool validHumidity;
  bool validLight;
  bool validSoilMoisture1;
  bool validSoilMoisture2;

  void updateRunningAverage(float& runningAvg, int& count, float newValue, bool& validFlag) {
    if (!isnan(newValue) && newValue >= 0) {
      runningAvg = ((runningAvg * count) + newValue) / (count + 1);
      count++;
      validFlag = true;
    }
  }

public:
  SensorManager()
    : currentData(), sampleCount(0), runningAvgTemperature1(0), runningAvgTemperature2(0),
      runningAvgHumidity(0), runningAvgLight(0), runningAvgSoilMoisture1(0), runningAvgSoilMoisture2(0),
      validTemperature1(false), validTemperature2(false), validHumidity(false), validLight(false),
      validSoilMoisture1(false), validSoilMoisture2(false) {}

  void run() {
    if (isTimeSet()) {
      updateSensorData();
    } else {
      requestTime();
    }
  }

  void recordToBuffer() {
    Serial.printf("T1 = %.2f, T2 = %.2f, L = %.2f, S1 = %.2f, S2 = %.2f, H = %.2f\n",
                  currentData.temperature1, currentData.temperature2, currentData.light,
                  currentData.soilMoisture1, currentData.soilMoisture2, currentData.humidity);
    
    pushBack(cb, currentData);
    saveBufferState(cb);
    
    // Reset averages after recording to start fresh for next interval
    resetAverages();
  }

  void setupBeforeSerial() {
    pinMode(LIGHT_PIN, INPUT);
    pinMode(SOIL_PIN1, INPUT);
    pinMode(SOIL_PIN2, INPUT);
    #if USE_LM35
    pinMode(LM35_PIN, INPUT);
    #endif
  }

  void setupAfterSerial() {
    sensors.begin();
    #if !USE_LM35
    dht.begin();
    #endif
  }

  String getCurrentDataJson() {
    return currentData.toJson();
  }

  void updateSensorData() {
    Serial.println("\n=== Sensor Data Update ===");
    
    // DS18B20 Temperature Sensor
    sensors.requestTemperatures();
    float s1 = sensors.getTempCByIndex(0);
    Serial.printf("DS18B20 Raw Temperature: %.2f°C\n", s1);
    updateRunningAverage(runningAvgTemperature1, sampleCount, s1, validTemperature1);
    currentData.temperature1 = validTemperature1 ? runningAvgTemperature1 : NAN;
    Serial.printf("DS18B20 Running Average: %.2f°C (Valid: %s)\n", 
                 currentData.temperature1, 
                 validTemperature1 ? "Yes" : "No");

    // Soil Moisture Sensor 1
    float rawSoil1 = analogRead(SOIL_PIN1);
    float soilMoisture1 = (1 - rawSoil1 / (float)ANALOG_MAX) * 100;
    Serial.printf("Soil Moisture 1 Raw: %.0f, Calculated: %.1f%%\n", rawSoil1, soilMoisture1);
    updateRunningAverage(runningAvgSoilMoisture1, sampleCount, soilMoisture1, validSoilMoisture1);
    currentData.soilMoisture1 = validSoilMoisture1 ? runningAvgSoilMoisture1 : NAN;
    Serial.printf("Soil Moisture 1 Running Average: %.1f%% (Valid: %s)\n", 
                 currentData.soilMoisture1, 
                 validSoilMoisture1 ? "Yes" : "No");

    // Soil Moisture Sensor 2
    float rawSoil2 = analogRead(SOIL_PIN2);
    float soilMoisture2 = (1 - rawSoil2 / (float)ANALOG_MAX) * 100;
    Serial.printf("Soil Moisture 2 Raw: %.0f, Calculated: %.1f%%\n", rawSoil2, soilMoisture2);
    updateRunningAverage(runningAvgSoilMoisture2, sampleCount, soilMoisture2, validSoilMoisture2);
    currentData.soilMoisture2 = validSoilMoisture2 ? runningAvgSoilMoisture2 : NAN;
    Serial.printf("Soil Moisture 2 Running Average: %.1f%% (Valid: %s)\n", 
                 currentData.soilMoisture2, 
                 validSoilMoisture2 ? "Yes" : "No");

    // Light Sensor
    float rawLight = analogRead(LIGHT_PIN);
    float light = (rawLight / (float)ANALOG_MAX) * 100;
    Serial.printf("Light Raw: %.0f, Calculated: %.1f%%\n", rawLight, light);
    updateRunningAverage(runningAvgLight, sampleCount, light, validLight);
    currentData.light = validLight ? runningAvgLight : NAN;
    Serial.printf("Light Running Average: %.1f%% (Valid: %s)\n", 
                 currentData.light, 
                 validLight ? "Yes" : "No");

    #if USE_LM35
    // LM35 Temperature Sensor
    float voltage = analogRead(LM35_PIN);
    Serial.printf("LM35 Raw Reading: %.2f\n", voltage);
    updateRunningAverage(runningAvgTemperature2, sampleCount, voltage, validTemperature2);
    currentData.temperature2 = validTemperature2 ? runningAvgTemperature2 : NAN;
    currentData.humidity = -1;
    Serial.printf("LM35 Running Average: %.2f°C (Valid: %s)\n", 
                 currentData.temperature2, 
                 validTemperature2 ? "Yes" : "No");
    #else
    // DHT Temperature & Humidity Sensor
    float s2 = dht.readTemperature();
    float h1 = dht.readHumidity();
    Serial.printf("DHT Raw Temperature: %.2f°C, Raw Humidity: %.1f%%\n", s2, h1);
    
    updateRunningAverage(runningAvgTemperature2, sampleCount, s2, validTemperature2);
    currentData.temperature2 = validTemperature2 ? runningAvgTemperature2 : NAN;
    Serial.printf("DHT Temperature Running Average: %.2f°C (Valid: %s)\n", 
                 currentData.temperature2, 
                 validTemperature2 ? "Yes" : "No");

    updateRunningAverage(runningAvgHumidity, sampleCount, h1, validHumidity);
    currentData.humidity = validHumidity ? runningAvgHumidity : NAN;
    Serial.printf("DHT Humidity Running Average: %.1f%% (Valid: %s)\n", 
                 currentData.humidity, 
                 validHumidity ? "Yes" : "No");
    #endif

    currentData.timestamp = getUnixTime();
    Serial.printf("Timestamp: %lu\n", currentData.timestamp);
    Serial.printf("Sample Count: %d\n", sampleCount);
    Serial.println("=== End Sensor Update ===\n");
}

  void resetAverages() {
    sampleCount = 0;
    runningAvgTemperature1 = 0;
    runningAvgTemperature2 = 0;
    runningAvgHumidity = 0;
    runningAvgLight = 0;
    runningAvgSoilMoisture1 = 0;
    runningAvgSoilMoisture2 = 0;
    validTemperature1 = false;
    validTemperature2 = false;
    validHumidity = false;
    validLight = false;
    validSoilMoisture1 = false;
    validSoilMoisture2 = false;
  }
};

#endif
