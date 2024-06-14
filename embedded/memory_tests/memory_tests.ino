#include <Preferences.h>

Preferences preferences;

// ****************************
// Buffer saving
// ****************************8
typedef struct {
  char name[20];
  int age;
  float height;
} Person;

// Define the maximum number of elements in the buffer
const int BUFFER_SIZE = 5;

// Circular buffer structure
struct CircularBuffer {
  Person buffer[BUFFER_SIZE];
  int head;
  int tail;
  int count;
};

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
void pushBack(CircularBuffer &cb, const Person &person) {
  if (!isFull(cb)) {
    cb.buffer[cb.tail] = person;
    cb.tail = (cb.tail + 1) % BUFFER_SIZE;
    cb.count++;
    Serial.printf("Pushed to back: %s\n", person.name);
  } else {
    Serial.println("Buffer is full. Cannot push to back.");
  }
}

// Push an element to the front of the buffer
void pushFront(CircularBuffer &cb, const Person &person) {
  if (!isFull(cb)) {
    cb.head = (cb.head - 1 + BUFFER_SIZE) % BUFFER_SIZE;
    cb.buffer[cb.head] = person;
    cb.count++;
    Serial.printf("Pushed to front: %s\n", person.name);
  } else {
    Serial.println("Buffer is full. Cannot push to front.");
  }
}

// Pop an element from the front of the buffer
Person popFront(CircularBuffer &cb) {
  Person person = {0};
  if (!isEmpty(cb)) {
    person = cb.buffer[cb.head];
    cb.head = (cb.head + 1) % BUFFER_SIZE;
    cb.count--;
    Serial.printf("Popped from front: %s\n", person.name);
  } else {
    Serial.println("Buffer is empty. Cannot pop from front.");
  }
  return person;
}

// Pop an element from the back of the buffer
Person popBack(CircularBuffer &cb) {
  Person person = {0};
  if (!isEmpty(cb)) {
    cb.tail = (cb.tail - 1 + BUFFER_SIZE) % BUFFER_SIZE;
    person = cb.buffer[cb.tail];
    cb.count--;
    Serial.printf("Popped from back: %s\n", person.name);
  } else {
    Serial.println("Buffer is empty. Cannot pop from back.");
  }
  return person;
}

// Save the state of the buffer to non-volatile memory
void saveBufferState(const CircularBuffer &cb) {
  preferences.begin("buffer-state", false);
  preferences.putBytes("circularBuffer", &cb, sizeof(cb));
  preferences.end();
  Serial.println("Buffer state saved to NVS.");
}

// Load the state of the buffer from non-volatile memory
void loadBufferState(CircularBuffer &cb) {
  preferences.begin("buffer-state", true);
  preferences.getBytes("circularBuffer", &cb, sizeof(cb));
  preferences.end();
  Serial.println("Buffer state loaded from NVS.");
}

//*******************
// CONFIG SAVING
// ******************

struct WiFiConfig {
  char ssid[32];
  char password[64];
};

// Define a struct for the configuration data
struct Config {
  WiFiConfig wifiConfig;
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

  void setWiFiConfig(const char* ssid, const char* password) {
    // Set the WiFi configuration
    strncpy(config.wifiConfig.ssid, ssid, sizeof(config.wifiConfig.ssid));
    strncpy(config.wifiConfig.password, password, sizeof(config.wifiConfig.password));
    saveConfig();
  }

  WiFiConfig getWiFiConfig() const {
    // Return the WiFi configuration
    return config.wifiConfig;
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

void setup() {
  Serial.begin(115200);

  // Initialize the circular buffer
  CircularBuffer cb;
  initCircularBuffer(cb);

  // Create Person objects
  Person alice = {"Alice", 30, 5.5};
  Person bob = {"Bob", 25, 6.0};
  Person charlie = {"Charlie", 28, 5.8};

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
    Person p = popFront(cb);
    Serial.printf("Name: %s, Age: %d, Height: %.2f\n", p.name, p.age, p.height);
  }

  
  // Create an instance of ConfigManager
  ConfigManager configManager;

  // Set and get WiFi configuration
  configManager.setWiFiConfig("MySSID", "MyPassword");
  WiFiConfig wifiConfig = configManager.getWiFiConfig();
  Serial.printf("WiFi SSID: %s, Password: %s\n", wifiConfig.ssid, wifiConfig.password);

  // Set and get user token
  configManager.setUserToken("1234567890abcdef");
  Serial.printf("User Token: %s\n", configManager.getUserToken());

  // Increment and get the number of resets
  configManager.incrementNumResets();
  Serial.printf("Number of Resets: %u\n", configManager.getNumResets());
}

void loop() {
  // Not used in this example
}
