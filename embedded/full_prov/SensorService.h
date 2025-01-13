#ifndef SENSORSERVICE_H
#define SENSORSERVICE_H

#include <OneWire.h>
#include <DallasTemperature.h>
#include "DHT.h"
#include <ArduinoJson.h>
#include "Config.h"
#include "Memory.h"
#include "TimeService.h"

#define DS18S20_Pin D7
#define DHT22_PIN 3
#define LM35_PIN A3
#define lightPin A0
#define soilPin1 A1
#define soilPin2 A2

#define DHTTYPE DHT22
#define ANALOG_MAX 4095.0

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
  }

  void setupBeforeSerial() {
    pinMode(lightPin, INPUT);
    pinMode(soilPin1, INPUT);
    pinMode(soilPin2, INPUT);
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
    sensors.requestTemperatures();
    float s1 = sensors.getTempCByIndex(0);
    updateRunningAverage(runningAvgTemperature1, sampleCount, s1, validTemperature1);
    currentData.temperature1 = validTemperature1 ? runningAvgTemperature1 : NAN;

    float soilMoisture1 = (1 - analogRead(soilPin1) / (float)ANALOG_MAX) * 100;
    updateRunningAverage(runningAvgSoilMoisture1, sampleCount, soilMoisture1, validSoilMoisture1);
    currentData.soilMoisture1 = validSoilMoisture1 ? runningAvgSoilMoisture1 : NAN;

    float soilMoisture2 = (1 - analogRead(soilPin2) / (float)ANALOG_MAX) * 100;
    updateRunningAverage(runningAvgSoilMoisture2, sampleCount, soilMoisture2, validSoilMoisture2);
    currentData.soilMoisture2 = validSoilMoisture2 ? runningAvgSoilMoisture2 : NAN;

    float light = (analogRead(lightPin) / (float)ANALOG_MAX) * 100;
    updateRunningAverage(runningAvgLight, sampleCount, light, validLight);
    currentData.light = validLight ? runningAvgLight : NAN;

    #if USE_LM35
    float voltage = analogRead(LM35_PIN);
    updateRunningAverage(runningAvgTemperature2, sampleCount, voltage, validTemperature2);
    currentData.temperature2 = validTemperature2 ? runningAvgTemperature2 : NAN;
    currentData.humidity = -1;
    #else
    float s2 = dht.readTemperature();
    updateRunningAverage(runningAvgTemperature2, sampleCount, s2, validTemperature2);
    currentData.temperature2 = validTemperature2 ? runningAvgTemperature2 : NAN;

    float h1 = dht.readHumidity();
    updateRunningAverage(runningAvgHumidity, sampleCount, h1, validHumidity);
    currentData.humidity = validHumidity ? runningAvgHumidity : NAN;
    #endif

    currentData.timestamp = getUnixTime();
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
