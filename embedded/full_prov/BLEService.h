#ifndef BLESERVICE_H
#define BLESERVICE_H

#include "Config.h"

class BluetoothService {
public:
    BluetoothService();
    void setup();
    void updateData(const SensorData& sensorData);

private:
    BLEServer* pServer;
    BLECharacteristic* pTemperature1Characteristic;
    BLECharacteristic* pTemperature2Characteristic;
    BLECharacteristic* pLightCharacteristic;
    BLECharacteristic* pSoilMoisture1Characteristic;
    BLECharacteristic* pSoilMoisture2Characteristic;
    BLECharacteristic* pHumidityCharacteristic;
    BLECharacteristic* pResetCharacteristic;
    BLECharacteristic* pEndpointCharacteristic;
    BLECharacteristic* pUpdatePeriodBtCharacteristic;
    BLECharacteristic* pUpdatePeriodWifiCharacteristic;
    static bool deviceConnected;
    static bool oldDeviceConnected;

    class ServerCallbacks : public BLEServerCallbacks {
        void onConnect(BLEServer* pServer) override {
            deviceConnected = true;
        }

        void onDisconnect(BLEServer* pServer) override {
            deviceConnected = false;
        }
    };
    class ResetCallbacks : public BLECharacteristicCallbacks {
        void onWrite(BLECharacteristic* pCharacteristic) override {
            #ifdef DEBUG
            Serial.println("Reset characteristic written");
            #endif
            // Trigger the callback (for now, just print a message)
            Serial.println("Reset callback triggered");
        }
    };
};

bool BluetoothService::deviceConnected = false;
bool BluetoothService::oldDeviceConnected = false;

BluetoothService::BluetoothService() : pServer(nullptr), pTemperature1Characteristic(nullptr), pTemperature2Characteristic(nullptr),
                                       pLightCharacteristic(nullptr), pSoilMoisture1Characteristic(nullptr), pSoilMoisture2Characteristic(nullptr),
                                       pHumidityCharacteristic(nullptr), pResetCharacteristic(nullptr),
                                       pEndpointCharacteristic(nullptr), pUpdatePeriodBtCharacteristic(nullptr), pUpdatePeriodWifiCharacteristic(nullptr) {}

void BluetoothService::setup() {
    // Initialize BLE Device
    BLEDevice::init("BluetoothService");

    // Create the BLE Server
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());

    // Create the BLE Service
    BLEService* pService = pServer->createService(BLE_SERVICE_UUID);

    // Create BLE Characteristics for each sensor data
    pTemperature1Characteristic = pService->createCharacteristic(
        TEMPERATURE1_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pTemperature2Characteristic = pService->createCharacteristic(
        TEMPERATURE2_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pLightCharacteristic = pService->createCharacteristic(
        LIGHT_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pSoilMoisture1Characteristic = pService->createCharacteristic(
        SOILMOISTURE1_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pSoilMoisture2Characteristic = pService->createCharacteristic(
        SOILMOISTURE2_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pHumidityCharacteristic = pService->createCharacteristic(
        HUMIDITY_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_NOTIFY
    );

    pResetCharacteristic = pService->createCharacteristic(
        RESET_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_WRITE
    );
    pResetCharacteristic->setCallbacks(new ResetCallbacks());

    pEndpointCharacteristic = pService->createCharacteristic(
        ENDPOINT_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
    );

    pUpdatePeriodBtCharacteristic = pService->createCharacteristic(
        UPDATE_PERIOD_BT_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
    );

    pUpdatePeriodWifiCharacteristic = pService->createCharacteristic(
        UPDATE_PERIOD_WIFI_CHARACTERISTIC_UUID,
        BLECharacteristic::PROPERTY_READ | BLECharacteristic::PROPERTY_WRITE
    );

    // Add descriptors to the characteristics
    pTemperature1Characteristic->addDescriptor(new BLE2902());
    pTemperature2Characteristic->addDescriptor(new BLE2902());
    pLightCharacteristic->addDescriptor(new BLE2902());
    pSoilMoisture1Characteristic->addDescriptor(new BLE2902());
    pSoilMoisture2Characteristic->addDescriptor(new BLE2902());
    pHumidityCharacteristic->addDescriptor(new BLE2902());
    pResetCharacteristic->addDescriptor(new BLE2902());
    pEndpointCharacteristic->addDescriptor(new BLE2902());
    pUpdatePeriodBtCharacteristic->addDescriptor(new BLE2902());
    pUpdatePeriodWifiCharacteristic->addDescriptor(new BLE2902());

    // Start the service
    pService->start();

    // Start advertising
    BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(BLE_SERVICE_UUID);
    pAdvertising->setScanResponse(false);
    BLEDevice::startAdvertising();

    #ifdef DEBUG
    Serial.println("Bluetooth service setup complete, waiting for client connection...");
    #endif
}

void BluetoothService::updateData(const SensorData& sensorData) {
    if (deviceConnected) {
        if (!isnan(sensorData.temperature1)) {
            float temp1 = sensorData.temperature1;
            pTemperature1Characteristic->setValue(temp1);
            pTemperature1Characteristic->notify();
        }
        
        if (!isnan(sensorData.temperature2)) {
            float temp2 = sensorData.temperature2;
            pTemperature2Characteristic->setValue(temp2);
            pTemperature2Characteristic->notify();
        }

        if (!isnan(sensorData.light)) {
            float light = sensorData.light;
            pLightCharacteristic->setValue(light);
            pLightCharacteristic->notify();
        }

        if (!isnan(sensorData.soilMoisture1)) {
            float soilMoisture1 = sensorData.soilMoisture1;
            pSoilMoisture1Characteristic->setValue(soilMoisture1);
            pSoilMoisture1Characteristic->notify();
        }

        if (!isnan(sensorData.soilMoisture2)) {
            float soilMoisture2 = sensorData.soilMoisture2;
            pSoilMoisture2Characteristic->setValue(soilMoisture2);
            pSoilMoisture2Characteristic->notify();
        }

        if (!isnan(sensorData.humidity)) {
            float humidity = sensorData.humidity;
            pHumidityCharacteristic->setValue(humidity);
            pHumidityCharacteristic->notify();
        }

        #ifdef DEBUG
        Serial.println("Updated sensor data:");
        Serial.print("Temperature1: "); Serial.println(sensorData.temperature1);
        Serial.print("Temperature2: "); Serial.println(sensorData.temperature2);
        Serial.print("Light: "); Serial.println(sensorData.light);
        Serial.print("SoilMoisture1: "); Serial.println(sensorData.soilMoisture1);
        Serial.print("SoilMoisture2: "); Serial.println(sensorData.soilMoisture2);
        Serial.print("Humidity: "); Serial.println(sensorData.humidity);
        #endif
    }
}

#endif
