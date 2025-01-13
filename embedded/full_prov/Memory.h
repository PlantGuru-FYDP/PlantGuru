#ifndef MEMORY_H
#define MEMORY_H

#include "Config.h"

// Circular buffer structure
struct CircularBuffer {
  SensorData buffer[BUFFER_SIZE];
  int head;
  int tail;
  int count;
};

// Function declarations
void initCircularBuffer(CircularBuffer &cb);
bool isFull(const CircularBuffer &cb);
bool isEmpty(const CircularBuffer &cb);
void pushBack(CircularBuffer &cb, const SensorData &sensorData);
void pushFront(CircularBuffer &cb, const SensorData &sensorData);
bool popFront(CircularBuffer &cb, SensorData &sensorData);
bool popBack(CircularBuffer &cb, SensorData &sensorData);
void saveBufferState(const CircularBuffer &cb);
void loadBufferState(CircularBuffer &cb);

// Global circular buffer instance
extern CircularBuffer cb;

#endif