#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <OneWire.h>
#include <DallasTemperature.h>
#include "SDmemory.h"
#include <WiFi.h>
#include "req.h"
#include "DHT.h"
#include "constants.h"

BLEServer* pServer = NULL;
BLECharacteristic* pSensorCharacteristic = NULL;
BLECharacteristic* pWifiCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

OneWire oneWire(DS18S20_Pin);
DallasTemperature sensors(&oneWire);

DHT dht(DHT22_PIN, DHTTYPE);

String wifiSSID = "";
String wifiPASS = "ECE358PROBLEM";

uint32_t SensorUpdate = 0;
uint8_t sensorUpdateInterval = 5 * 1000;
bool saveToSD = true;

SDmemory sdmemory;

SensorData currentData;

#define SERVICE_UUID "19b10000-e8f2-537e-4f6c-d104768a1214"
#define SENSOR_CHARACTERISTIC_UUID "19b10001-e8f2-537e-4f6c-d104768a1214"
#define WIFI_CHARACTERISTIC_UUID "19b10002-e8f2-537e-4f6c-d104768a1214"


class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
  };

  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
  }
};

class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* pWifiCharacteristic) {
    String value = pWifiCharacteristic->getValue();
    if (value.length() > 0) {
      Serial.print("Characteristic event, written: ");
      Serial.println(value.c_str());  // Print the written value

      // Update wifiSSID and wifiPASS variables
      String writtenValue = value.c_str();
      int separatorIndex = writtenValue.indexOf(',');
      if (separatorIndex != -1) {
        wifiSSID = writtenValue.substring(0, separatorIndex);
        wifiPASS = writtenValue.substring(separatorIndex + 1);
      }

      // Print the updated wifiSSID and wifiPASS
      Serial.print("Updated wifiSSID: ");
      Serial.println(wifiSSID);
      Serial.print("Updated wifiPASS: ");
      Serial.println(wifiPASS);

      WiFi.begin(wifiSSID, wifiPASS);
    }
  }
};

void setup() {

  pinMode(lightPin, INPUT);
  pinMode(soilPin1, INPUT);
  pinMode(soilPin2, INPUT);

  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("PlantGuru");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService* pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pSensorCharacteristic = pService->createCharacteristic(
    SENSOR_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_NOTIFY | BLECharacteristic::PROPERTY_INDICATE);
  // Create the WIFI Characteristic
  pWifiCharacteristic = pService->createCharacteristic(
    WIFI_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_WRITE);
  // Register the callback for the WIFI characteristic
  pWifiCharacteristic->setCallbacks(new MyCharacteristicCallbacks());

  // Create a BLE Descriptor
  pSensorCharacteristic->addDescriptor(new BLE2902());
  pWifiCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  Serial.println("Waiting for a client connection to notify...");

  //sd card
  SDmemory sdmemory;

  // Setup the SD card file
  if (!sdmemory.init()) {
    Serial.println("Failed to setup SD memory");
    return;
  }

  WiFi.begin(wifiSSID, wifiPASS);
  Serial.println(WiFi.status());
}

void loop() {
  // notify changed value
  if (deviceConnected) {
    // Create the data string
    String sensorData = String(currentData.temperature1) + ","
       + String(currentData.temperature2) + ","
       + String(currentData.light) + "," 
       + String(currentData.soilMoisture1) + ","
       + String(currentData.soilMoisture2) + ","
       + String(currentData.humidity) + ","
       + String(WiFi.status());

    // Update the characteristic value and notify
    pSensorCharacteristic->setValue(sensorData.c_str());
    pSensorCharacteristic->notify();

    Serial.print("New value notified: ");
    Serial.println(sensorData);

    delay(3000);  // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test I was able to go as low as 3ms
  }

  // disconnecting
  if (!deviceConnected && oldDeviceConnected) {
    Serial.println("Device disconnected.");
    delay(500);                   // give the bluetooth stack the chance to get things ready
    pServer->startAdvertising();  // restart advertising
    Serial.println("Start advertising");
    oldDeviceConnected = deviceConnected;
  }

  // connecting
  if (deviceConnected && !oldDeviceConnected) {
    // do stuff here on connecting
    oldDeviceConnected = deviceConnected;
    Serial.println("Device Connected");
  }

  if ((millis() - currentData.timestamp) >= sensorUpdateInterval) {
    sensors.requestTemperatures();
    float s1 = sensors.getTempCByIndex(0);
    //float s2 = sensors.getTempCByIndex(1); 
    currentData.temperature1 = s1 < 0 ? -1: s1;
    //currentData.temperature2 = s2 < 0 ? -1: s2;
    currentData.light = analogRead(lightPin)/4095;
    currentData.soilMoisture1 = analogRead(soilPin1)/4095;
    currentData.soilMoisture2 = analogRead(soilPin2)/4095;
    currentData.timestamp = millis();

    float s2 = dht.readTemperature(false, true);
    float h1 = dht.readHumidity(true);
  
    currentData.temperature2 = isnan(s2) ? -1 : s2;
    currentData.humidity = isnan(h1) ? -1 : h1;

    if (saveToSD && sdmemory.isSetup() && sdmemory.getRemainingRecords() > 0){
      if (!sdmemory.writeData(currentData)) {
        Serial.println("Failed to write data to SD memory");
      }
    }
  }
}
