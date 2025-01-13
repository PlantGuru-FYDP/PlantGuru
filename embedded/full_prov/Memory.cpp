#include "Memory.h"

// Define the global circular buffer instance
CircularBuffer cb;

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
  preferences.putBytes("circularBuffer", &cb, sizeof(cb));
  preferences.end();
  Serial.print("Buffer state saved to NVS. Buffer size: ");
  Serial.println(cb.count);
}

// Load the state of the buffer from non-volatile memory
void loadBufferState(CircularBuffer &cb) {
  preferences.getBytes("circularBuffer", &cb, sizeof(cb));
  preferences.end();
  Serial.print("Buffer state loaded from NVS. Buffer size: ");
  Serial.println(cb.count);
}