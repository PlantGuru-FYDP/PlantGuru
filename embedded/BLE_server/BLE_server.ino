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
#include "wifi.h"


#define DEBUG

BLEServer* pServer = NULL;
BLECharacteristic* pSensorCharacteristic = NULL;
BLECharacteristic* pWifiCharacteristic = NULL;
BLECharacteristic* pSettingsCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

OneWire oneWire(DS18S20_Pin);
DallasTemperature sensors(&oneWire);

DHT dht(DHT22_PIN, DHTTYPE);

String wifiSSID = "";
String wifiPASS = "ECE358PROBLEM";
String wifiPOSTUrl = "none";

bool sendWifiData = false;

uint32_t lastSensorUpdate = 0;
uint32_t lastWifiUpload = 0;
uint32_t lastBluetoothUpdate = 0;

uint16_t bluetoothUpdatePeriod = 3 * 1000;
uint16_t sensorUpdatePeriod = 5 * 1000;
uint16_t wifiUploadPeriod = 3 * 1000;
bool saveToSD = false;


SDmemory sdmemory;

SensorData currentData;

#define SERVICE_UUID "19b10000-e8f2-537e-4f6c-d104768a1214"
#define SENSOR_CHARACTERISTIC_UUID "19b10001-e8f2-537e-4f6c-d104768a1214"
#define WIFI_CHARACTERISTIC_UUID "19b10002-e8f2-537e-4f6c-d104768a1214"
#define SETTINGS_CHARACTERISTIC_UUID "19b10003-e8f2-537e-4f6c-d104768a1214"


class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
  };

  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
  }
};

class MyCharacteristicCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* pCharacteristic) {
    std::string value = pCharacteristic->getValue();
    if (value.length() > 0) {
      #ifdef DEBUG
      Serial.print("Received data from client: ");
      Serial.println(value.c_str());  // Print the written value
      #endif

      // Handle WiFi settings
      if (pCharacteristic == pWifiCharacteristic) {
        String writtenValue = value.c_str();
        int separatorIndex = writtenValue.indexOf(',');
        if (separatorIndex != -1) {
          wifiSSID = writtenValue.substring(0, separatorIndex);
          wifiPASS = writtenValue.substring(separatorIndex + 1);
        }

        #ifdef DEBUG
        Serial.print("Updated wifiSSID: ");
        Serial.println(wifiSSID);
        Serial.print("Updated wifiPASS: ");
        Serial.println(wifiPASS);
        #endif

        WiFi.begin(wifiSSID, wifiPASS);
      }

      // Handle Settings
      if (pCharacteristic == pSettingsCharacteristic) {
        String writtenValue = value.c_str();
        int separatorIndex1 = writtenValue.indexOf(',');
        int separatorIndex2 = writtenValue.indexOf(',', separatorIndex1 + 1);
        int separatorIndex3 = writtenValue.indexOf(',', separatorIndex2 + 1);
        if (separatorIndex1 != -1 && separatorIndex2 != -1 && separatorIndex3 != -1) {
          bluetoothUpdatePeriod = writtenValue.substring(0, separatorIndex1).toInt();
          wifiUploadPeriod = writtenValue.substring(separatorIndex1 + 1, separatorIndex2).toInt();
          sensorUpdatePeriod = writtenValue.substring(separatorIndex2 + 1, separatorIndex3).toInt();
          saveToSD = writtenValue.substring(separatorIndex3 + 1).toInt();

          #ifdef DEBUG
          Serial.print("Updated bluetoothUpdatePeriod: ");
          Serial.println(bluetoothUpdatePeriod);
          Serial.print("Updated wifiUploadPeriod: ");
          Serial.println(wifiUploadPeriod);
          Serial.print("Updated sensorRecordingPeriod: ");
          Serial.println(sensorUpdatePeriod);
          Serial.print("Updated saveToSD: ");
          #endif
        }
      }
    }
  }
};

void setup() {

  pinMode(lightPin, INPUT);
  pinMode(soilPin1, INPUT);
  pinMode(soilPin2, INPUT);

  Serial.begin(115200);

  sensors.begin();
  dht.begin();

  // Create the BLE Device
  BLEDevice::init("PlantGuru 2");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService* pService = pServer->createService(SERVICE_UUID);

  pSensorCharacteristic = pService->createCharacteristic(
    SENSOR_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_NOTIFY | BLECharacteristic::PROPERTY_INDICATE);

  pWifiCharacteristic = pService->createCharacteristic(
    WIFI_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_WRITE);
  pWifiCharacteristic->setCallbacks(new MyCharacteristicCallbacks());

  pSettingsCharacteristic = pService->createCharacteristic(
    SETTINGS_CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_WRITE);
  pSettingsCharacteristic->setCallbacks(new MyCharacteristicCallbacks());

  // Create a BLE Descriptor
  pSensorCharacteristic->addDescriptor(new BLE2902());
  pWifiCharacteristic->addDescriptor(new BLE2902());
  pSettingsCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();

  #ifdef DEBUG
  Serial.println("Waiting for a client connection to notify...");
  #endif

  //sd card
  SDmemory sdmemory;

  // Setup the SD card file
  if (saveToSD && !sdmemory.init()) {
    Serial.println("Failed to setup SD memory");
    return;
  }

  WiFi.begin(wifiSSID, wifiPASS);
  Serial.println(WiFi.status());
}

// Update Methods
void bluetoothUpdate() {
  // Create the data string
  String sensorData = String(round(currentData.temperature1 * 10) / 10) + ","
      + String(round(currentData.temperature2 * 10) / 10) + ","
      + String(round(currentData.light * 10) / 10) + "," 
      + String(round(currentData.soilMoisture1 * 10) / 10) + ","
      + String(round(currentData.soilMoisture2 * 10) / 10) + ","
      + String(round(currentData.humidity * 10) / 10) + ","
      + String(WiFi.status());

  // Update the characteristic value and notify
  pSensorCharacteristic->setValue(sensorData.c_str());
  pSensorCharacteristic->notify();

  #ifdef DEBUG
  Serial.print("Sent sensor data values: ");
  Serial.println(sensorData);
  #endif
}

void sensorUpdate() {
  // Temperature 1
  sensors.requestTemperatures();
  float s1 = sensors.getTempCByIndex(0);
  currentData.temperature1 = s1 < 0 ? -1: s1;

  // Soil Moisture
  currentData.soilMoisture1 = (1-analogRead(soilPin1)/ANALOG_MAX) * 100;
  currentData.soilMoisture2 = (1-analogRead(soilPin2)/ANALOG_MAX) * 100;

  // Light
  currentData.light = (analogRead(lightPin)/ANALOG_MAX) *100;

  // Temperature 2 & Humidity
  float s2 = dht.readTemperature();
  currentData.temperature2 = isnan(s2) ? -1 : s2;
  
  float h1 = dht.readHumidity();
  currentData.humidity = isnan(h1) ? -1 : h1;

  // Time
  currentData.timestamp = millis();

  #ifdef DEBUG
  Serial.printf("time: %d T1 = %.2f, T2 = %.2f, L = %.2f, S1 = %.2f, S2 = %.2f, H = %.2f\n",
    currentData.timestamp, currentData.temperature1, currentData.temperature2, currentData.light, currentData.soilMoisture1, currentData.soilMoisture2, currentData.humidity);
  #endif

  if (saveToSD && sdmemory.isSetup() && sdmemory.getRemainingRecords() > 0){
    if (!sdmemory.writeData(currentData)) {
      Serial.println("Failed to write data to SD memory");
    }
  }
}

void loop() {
  // Bluetooth
  if (deviceConnected && (millis() - lastBluetoothUpdate) >= bluetoothUpdatePeriod) {
    bluetoothUpdate();
    lastBluetoothUpdate = millis();
  }

  // Sensors
  if ((millis() - lastSensorUpdate) >= sensorUpdatePeriod) {
    sensorUpdate();
    lastSensorUpdate = millis();
  }

  // Wifi
  if ((millis() - lastWifiUpload) >= wifiUploadPeriod) {
    lastWifiUpload = millis();
    if (sendWifiData) {
      postSensorData(wifiPOSTUrl, currentData, 3);
    }
  }

  // disconnecting
  if (!deviceConnected && oldDeviceConnected) {
    #ifdef DEBUG
    Serial.println("Device disconnected.");
    #endif

    delay(500);                   // give the bluetooth stack the chance to get things ready
    pServer->startAdvertising();  // restart advertising

    #ifdef DEBUG
    Serial.println("Start advertising");
    #endif

    oldDeviceConnected = deviceConnected;
  }

  // connecting
  if (deviceConnected && !oldDeviceConnected) {
    oldDeviceConnected = deviceConnected;

    #ifdef DEBUG
    Serial.println("Device Connected");
    #endif
  }

}
