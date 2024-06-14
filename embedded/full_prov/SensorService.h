#include <OneWire.h>
#include <DallasTemperature.h>
#include "DHT.h"
#include <ArduinoJson.h>
#include "Constants.h"
#include "Memory.h"
#include "WifiService.h"

#define DS18S20_Pin 2
#define DHT22_PIN 3
#define lightPin A0
#define soilPin1 A1
#define soilPin2 A2

#define DHTTYPE DHT22
#define ANALOG_MAX 1023

OneWire oneWire(DS18S20_Pin);
DallasTemperature sensors(&oneWire);
DHT dht(DHT22_PIN, DHTTYPE);

class SensorManager {
private:
  SensorData currentData;

public:
  SensorManager()
    : currentData() {}

  void setupBeforeSerial() {
    pinMode(lightPin, INPUT);
    pinMode(soilPin1, INPUT);
    pinMode(soilPin2, INPUT);
  }

  void setupAfterSerial() {
    sensors.begin();
    dht.begin();
  }

  void run() {
    if (isTimeSet()) {
      updateSensorData();
      Serial.printf("T1 = %.2f, T2 = %.2f, L = %.2f, S1 = %.2f, S2 = %.2f, H = %.2f\n",
                    currentData.temperature1, currentData.temperature2, currentData.light,
                    currentData.soilMoisture1, currentData.soilMoisture2, currentData.humidity);
      
      pushBack(cb, currentData);
      saveBufferState(cb);
    } else {
      requestTime();
    }
  }

private:
  void updateSensorData() {
    sensors.requestTemperatures();
    float s1 = sensors.getTempCByIndex(0);
    currentData.temperature1 = s1 < 0 ? -1 : s1;

    currentData.soilMoisture1 = (1 - analogRead(soilPin1) / (float)ANALOG_MAX) * 100;
    currentData.soilMoisture2 = (1 - analogRead(soilPin2) / (float)ANALOG_MAX) * 100;

    currentData.light = (analogRead(lightPin) / (float)ANALOG_MAX) * 100;

    float s2 = dht.readTemperature();
    currentData.temperature2 = isnan(s2) ? -1 : s2;

    float h1 = dht.readHumidity();
    currentData.humidity = isnan(h1) ? -1 : h1;

    currentData.timestamp = getUnixTime();
  }
};
