#include <Preferences.h>
#include "Constants.h"

#ifndef MEMORY_H
#define MEMORY_H

// ****************************
// Buffer saving
// ****************************8

// Define the maximum number of elements in the buffer
const int BUFFER_SIZE = 500;

// Circular buffer structure
typedef struct {
  SensorData buffer[BUFFER_SIZE];
  int head;
  int tail;
  int count;
} CircularBuffer;

// Initialize the circular buffer
void initCircularBuffer(CircularBuffer &cb) {
  cb.head = 0;
  cb.tail = 0;
  cb.count = 0;
  Serial.println("Circular buffer initialized.");
}

// Check if the buffer is full
bool isFull(const CircularBuffer &cb) {
  return cb.count == BUFFER_SIZE;
}

// Check if the buffer is empty
bool isEmpty(const CircularBuffer &cb) {
  return cb.count == 0;
}

// Push an element to the back of the buffer
void pushBack(CircularBuffer &cb, const SensorData &sensorData) {
  cb.buffer[cb.tail] = sensorData;
  cb.tail = (cb.tail + 1) % BUFFER_SIZE;
  if (!isFull(cb)) {
    cb.count++;
  } else {
    Serial.println("Buffer is full. Data has been overwritten.");
  }
}

// Push an element to the front of the buffer
void pushFront(CircularBuffer &cb, const SensorData &sensorData) {
  cb.head = (cb.head - 1 + BUFFER_SIZE) % BUFFER_SIZE;
  cb.buffer[cb.head] = sensorData;
  if (!isFull(cb)) {
    cb.count++;
  } else {
    Serial.println("Buffer is full. Data has been overwritten.");
  }
}

// Pop an element from the front of the buffer
bool popFront(CircularBuffer &cb, SensorData &sensorData) {
  if (!isEmpty(cb)) {
    sensorData = cb.buffer[cb.head];
    cb.head = (cb.head + 1) % BUFFER_SIZE;
    cb.count--;
    return true;
  } else {
    Serial.println("Buffer is empty. Cannot pop from front.");
    return false;
  }
}

bool popBack(CircularBuffer &cb, SensorData &sensorData) {
  if (!isEmpty(cb)) {
    cb.tail = (cb.tail - 1 + BUFFER_SIZE) % BUFFER_SIZE;
    sensorData = cb.buffer[cb.tail];
    cb.count--;
    return true;
  } else {
    Serial.println("Buffer is empty. Cannot pop from back.");
    return false;
  }
}

// Save the state of the buffer to non-volatile memory
void saveBufferState(const CircularBuffer &cb) {
  //preferences.begin("buffer-state", false);
  preferences.putBytes("circularBuffer", &cb, sizeof(cb));
  preferences.end();
  Serial.print("Buffer state saved to NVS. Buffer size: ");
  Serial.println(cb.count);
}

// Load the state of the buffer from non-volatile memory
void loadBufferState(CircularBuffer &cb) {
  //preferences.begin("buffer-state", true);
  preferences.getBytes("circularBuffer", &cb, sizeof(cb));
  preferences.end();
  Serial.print("Buffer state loaded from NVS. Buffer size: ");
  Serial.println(cb.count);
}

//*******************
// CONFIG SAVING
// ******************

// Define a struct for the configuration data
struct Config {
  char userToken[64];
  unsigned int numResets;
};

class ConfigManager {
private:
  Preferences preferences;
  Config config;

public:
  ConfigManager() {
    // Load the configuration on startup
    loadConfig();
  }

  void setUserToken(const char* token) {
    // Set the user token
    strncpy(config.userToken, token, sizeof(config.userToken));
    saveConfig();
  }

  const char* getUserToken() const {
    // Return the user token
    return config.userToken;
  }

  void incrementNumResets() {
    // Increment the number of resets
    config.numResets++;
    saveConfig();
  }

  unsigned int getNumResets() const {
    // Return the number of resets
    return config.numResets;
  }

private:
  void saveConfig() {
    // Save the configuration to non-volatile storage
    preferences.begin("app-config", false);
    preferences.putBytes("config", &config, sizeof(config));
    preferences.end();
  }

  void loadConfig() {
    // Load the configuration from non-volatile storage
    preferences.begin("app-config", true);
    preferences.getBytes("config", &config, sizeof(config));
    preferences.end();
  }
};

CircularBuffer cb;

void setupConfig() {
  /*
  Serial.begin(115200);

  // Initialize the circular buffer
  
  initCircularBuffer(cb);

  // Create SensorData objects
  SensorData alice = {"Alice", 30, 5.5};
  SensorData bob = {"Bob", 25, 6.0};
  SensorData charlie = {"Charlie", 28, 5.8};

  // Push some test data to the buffer
  pushBack(cb, alice);
  pushBack(cb, bob);
  pushFront(cb, charlie);
  
  // Save the state of the buffer
  saveBufferState(cb);

  // Clear the buffer to demonstrate loading
  initCircularBuffer(cb);

  // Load the state of the buffer
  loadBufferState(cb);

  // Pop and print elements from the buffer
  while (!isEmpty(cb)) {
    SensorData p = popFront(cb);
    Serial.printf("Name: %s, Age: %d, Height: %.2f\n", p.name, p.age, p.height);
  }

  
  // Create an instance of ConfigManager
  ConfigManager configManager;

  // Set and get user token
  configManager.setUserToken("1234567890abcdef");
  Serial.printf("User Token: %s\n", configManager.getUserToken());

  // Increment and get the number of resets
  configManager.incrementNumResets();
  Serial.printf("Number of Resets: %u\n", configManager.getNumResets());
  */
}

#endif