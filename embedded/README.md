# PlantGuru Embedded

This is an arduino project which uses the Firebeetle 2 ESP32-E Microcontroller to 
collect data from soil moisture, soil temp, air temp, and light, and the interfaces to collect the data.

# Resources
[Firebeetle 2 ESP32-E](https://wiki.dfrobot.com/FireBeetle_Board_ESP32_E_SKU_DFR0654)

[Soil moisture](https://wiki.dfrobot.com/Waterproof_Capacitive_Soil_Moisture_Sensor_SKU_SEN0308)

[SD card module](https://digilent.com/reference/pmod/pmodmicrosd/reference-manual)

[Soil temperature](https://wiki.dfrobot.com/Waterproof_DS18B20_Digital_Temperature_Sensor__SKU_DFR0198_)

[Light sensor (photoresistor)](https://cdn-shop.adafruit.com/product-files/161/C2255-001_MFG_PN__HaiWang_MJ5516.pdf)

[Air temperature](https://wiki.dfrobot.com/DFRobot_LM35_Linear_Temperature_Sensor__SKU_DFR0023_)

[Air temp & humidity](https://wiki.dfrobot.com/DHT22_Temperature_and_humidity_module_SKU_SEN0137)

## Setup
1. Install the ESP32 board in the Arduino IDE according to the [DFRobot guide](https://wiki.dfrobot.com/FireBeetle_Board_ESP32_E_SKU_DFR0654)
2. Open BLE_server.ino sketch in the Arduino IDE
3. Install the DallasTemperature library in he Arduino IDE
4. Install the DHT22 library in the Arduino IDE
5. Open index.html in a web browser to connect to the server