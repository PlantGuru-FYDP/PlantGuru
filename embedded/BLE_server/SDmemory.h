/*
 * Connect the SD card to the following pins:
 *
 * SD Card | ESP32
 *    D2       -
 *    D3       SS
 *    CMD      MOSI
 *    VSS      GND
 *    VDD      3.3V
 *    CLK      SCK
 *    VSS      GND
 *    D0       MISO
 *    D1       -
 */
#include "FS.h"
#include "SD.h"
#include "SPI.h"
#include "constants.h"

void createDir(fs::FS &fs, const char * path){
    Serial.printf("Creating Dir: %s\n", path);
    if(fs.mkdir(path)){
        Serial.println("Dir created");
    } else {
        Serial.println("mkdir failed");
    }
}

void removeDir(fs::FS &fs, const char * path){
    Serial.printf("Removing Dir: %s\n", path);
    if(fs.rmdir(path)){
        Serial.println("Dir removed");
    } else {
        Serial.println("rmdir failed");
    }
}

void readFile(fs::FS &fs, const char * path){
    Serial.printf("Reading file: %s\n", path);

    File file = fs.open(path);
    if(!file){
        Serial.println("Failed to open file for reading");
        return;
    }

    Serial.print("Read from file: ");
    while(file.available()){
        Serial.write(file.read());
    }
    file.close();
}

bool writeFile(fs::FS &fs, const char * path, const char * message){
    Serial.printf("Writing file: %s\n", path);

    File file = fs.open(path, FILE_WRITE);
    if(!file){
        return false;
    }
    bool r = false;
    if(file.print(message)){
        r = true;
    }
    file.close();
    return r;
}


class SensorData {
  public:
    int soilMoisture1 = -1;
    int soilMoisture2 = -1;
    float temperature1 = -1.0;
    float temperature2 = -1.0;
    float temperature3 = -1.0;
    float humidity = -1.0;
    float light = -1.0;
    int timestamp = -1;
};

class SDmemory {
  private:
    const char* filename = "/data.csv"; // File name to store sensor data
    const int FLOAT_PRE_DECIMAL = 2;
    const int FLOAT_POST_DECIMAL = 4;
    const int INT_CHARS = 4;
    const int TIMESTAMP_CHARS = 10;
    const int NUMFIELDS = 8;

  public:
    // Creates the file if it doesn't exist and makes sure the connection is good
    bool init() {
      if(!SD.begin(SD_PIN, SPI, 4000000,"/sd",5)){
      return false;
      }
      if (SD.cardType() == CARD_NONE) {
      return false;
      }
      if (!SD.exists(filename)) {
        writeFile(SD, filename, "soilMoisture1,soilMoisture2,temperature1,temperature2,temperature3,humidity,light,timestamp\n");
      }
      return SD.exists(filename);
    }

    // Returns true if the setup was successful
    bool isSetup() {
      return SD.exists(filename);
    }

    // Returns the number of records stored on the SD card
    int getNumRecords() {
      File file = SD.open(filename);
      int numRecords = -1;
      while (file.available()) {
        file.readStringUntil('\n');
        numRecords++;
      }
      file.close();
      return numRecords;
    }

    // Calculates the maximum number of records that can be stored on the SD card
    int getMaxRecords() {
      uint64_t cardSize = SD.cardSize();
      int recordSize = NUMFIELDS * (
        5*(FLOAT_PRE_DECIMAL + 1 + FLOAT_POST_DECIMAL) +
        2*(INT_CHARS) +
        TIMESTAMP_CHARS + 
        7
      );
      int maxRecords = cardSize / recordSize;
      return maxRecords;
    }

    // Returns the number of records that can still be stored on the SD card
    int getRemainingRecords() {
      int numRecords = getNumRecords();
      int maxRecords = getMaxRecords();
      int remainingRecords = maxRecords - numRecords;
      return remainingRecords;
    }

    // Appends a single record to the file
    bool writeData(SensorData data) {
      File file = SD.open(filename, FILE_APPEND);
      if (!file) {
        return false;
      }
      file.print(data.soilMoisture1);
      file.print(",");
      file.print(data.soilMoisture2);
      file.print(",");
      file.print(data.temperature1, FLOAT_PRE_DECIMAL);
      file.print(",");
      file.print(data.temperature2, FLOAT_PRE_DECIMAL);
      file.print(",");
      file.print(data.temperature3, FLOAT_PRE_DECIMAL);
      file.print(",");
      file.print(data.humidity, FLOAT_PRE_DECIMAL);
      file.print(",");
      file.print(data.light, FLOAT_PRE_DECIMAL);
      file.print(",");
      file.print(data.timestamp);
      file.println();
      file.close();
      return true;
    }

    // Reads a single record at the given index
    bool readSingleData(int index, SensorData& data) {
      File file = SD.open(filename);
      int currentIndex = 0;
      if (!file.available()){
        return false;
      }
      file.readStringUntil('\n');
      while (file.available()) {
        String line = file.readStringUntil('\n');
        if (currentIndex == index+1) {
          parseData(line, data);
          break;
        }
        currentIndex++;
      }
      file.close();
      return true;
    }

    // Reads all records from the file
    bool readAllData(SensorData*& data, int& numRecords) {
      numRecords = getNumRecords();
      data = new SensorData[numRecords];
      File file = SD.open(filename);
      int currentIndex = 0;
      if (!file.available()) {
        return false;
      }
      file.readStringUntil('\n');
      while (file.available()) {
        String line = file.readStringUntil('\n');
        parseData(line, data[currentIndex]);
        currentIndex++;
      }
      file.close();
      return true;
    }

    // Reads records within the given timestamp range
    bool readDataRange(int start, int end, SensorData*& data, int& numRecords) {
      if (end > getNumRecords()-1 || start > end || start <0 || end<0) {
        return false;
      }
      numRecords = end - start + 1;
      data = new SensorData[numRecords];
      File file = SD.open(filename);
      int currentIndex = 0;
      if (!file.available()) {
        return false;
      }
      file.readStringUntil('\n');
      while (file.available()) {
        String line = file.readStringUntil('\n');
        if (currentIndex >= start && currentIndex <= end) {
          parseData(line, data[currentIndex - start]);
        }
        currentIndex++;
      }
      file.close();
      return true;
    }

    // Clears all data in the file
    bool clearData() {
      return SD.remove(filename);
    }

  private:
    // Helper function to parse a line of data and populate the SensorData object
    void parseData(String line, SensorData& data) {
      int commaIndex = line.indexOf(',');
      data.soilMoisture1 = line.substring(0, commaIndex).toInt();
      line = line.substring(commaIndex + 1);
      commaIndex = line.indexOf(',');
      data.soilMoisture2 = line.substring(0, commaIndex).toInt();
      line = line.substring(commaIndex + 1);
      commaIndex = line.indexOf(',');
      data.temperature1 = line.substring(0, commaIndex).toFloat();
      line = line.substring(commaIndex + 1);
      commaIndex = line.indexOf(',');
      data.temperature2 = line.substring(0, commaIndex).toFloat();
      line = line.substring(commaIndex + 1);
      commaIndex = line.indexOf(',');
      data.temperature3 = line.substring(0, commaIndex).toFloat();
      line = line.substring(commaIndex + 1);
      commaIndex = line.indexOf(',');
      data.humidity = line.substring(0, commaIndex).toFloat();
      line = line.substring(commaIndex + 1);
      commaIndex = line.indexOf(',');
      data.light = line.substring(0, commaIndex).toFloat();
      line = line.substring(commaIndex + 1);
      data.timestamp = line.toInt();
    }
  };

void setup2() {
  Serial.begin(115200);
  Serial.println("Starting tests!!");
  // Tests for every single method for the sdmemory class
  // with prints to the serial monitor and early exiting if a functionality prevents further testing
  // dummy data are created here to test the functionality of the class

  // Create an instance of the SDmemory class
  SDmemory sdmemory;

  // Setup the SD card file
  if (!sdmemory.init()) {
    Serial.println("Failed to setup SD memory");
    return;
  }

  // Test the getNumRecords method
  int numRecords = sdmemory.getNumRecords();
  Serial.printf("Number of records: %d\n", numRecords);

  // Test the getMaxRecords method
  int maxRecords = sdmemory.getMaxRecords();
  Serial.printf("Max number of records: %d\n", maxRecords);

  // Test the getRemainingRecords method
  int remainingRecords = sdmemory.getRemainingRecords();
  Serial.printf("Remaining records: %d\n", remainingRecords);

  // Create a dummy SensorData object
  SensorData data;
  data.soilMoisture1 = 100;
  data.soilMoisture2 = 200;
  data.temperature1 = 25.0;
  data.temperature2 = 26.0;
  data.temperature3 = 27.0;
  data.humidity = 50.0;
  data.light = 100.0;
  data.timestamp = 1630431602;

  // Test the writeData method
  if (!sdmemory.writeData(data)) {
    Serial.println("Failed to write data");
    return;
  }

  // Test the getNumRecords method
  numRecords = sdmemory.getNumRecords();
  Serial.printf("Number of records: %d\n", numRecords);

  // Test the readSingleData method
  Serial.println("Single data:");
  SensorData readData;
  if (sdmemory.readSingleData(0, readData)) {
    Serial.printf("Read data: %d %d %.2f %.2f %.2f %.2f %.2f %d\n",
                  readData.soilMoisture1,
                  readData.soilMoisture2,
                  readData.temperature1,
                  readData.temperature2,
                  readData.temperature3,
                  readData.humidity,
                  readData.light,
                  readData.timestamp);
  } else {
    Serial.println("Failed to read single data");
  }

  // Test the readAllData method
  Serial.println("all data:");
  SensorData* allData;
  if (sdmemory.readAllData(allData, numRecords)) {
    for (int i = 0; i < numRecords; i++) {
      Serial.printf("Read data: %d %d %.2f %.2f %.2f %.2f %.2f %d\n",
                    allData[i].soilMoisture1,
                    allData[i].soilMoisture2,
                    allData[i].temperature1,
                    allData[i].temperature2,
                    allData[i].temperature3,
                    allData[i].humidity,
                    allData[i].light,
                    allData[i].timestamp);
    }
    delete[] allData; // Free the memory allocated for allData
  } else {
    Serial.println("Failed to read all data");
  }

  // Test the readDataRange method
  Serial.println("date range 0-600:");
  SensorData* rangeData;
  if (sdmemory.readDataRange(0, 2, rangeData, numRecords)) {
    Serial.printf("Number of records: %d\n", numRecords);
    for (int i = 0; i < numRecords; i++) {
      Serial.printf("Read data: %d %d %.2f %.2f %.2f %.2f %.2f %d\n",
                    rangeData[i].soilMoisture1,
                    rangeData[i].soilMoisture2,
                    rangeData[i].temperature1,
                    rangeData[i].temperature2,
                    rangeData[i].temperature3,
                    rangeData[i].humidity,
                    rangeData[i].light,
                    rangeData[i].timestamp);
    }
    delete[] rangeData; // Free the memory allocated for rangeData
  } else {
    Serial.println("Failed to read data range");
  }
}

void loop2() {

}