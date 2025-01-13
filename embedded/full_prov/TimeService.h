#ifndef TIMESERVICE_H
#define TIMESERVICE_H

#include <time.h>

#define ntpServer "time.windows.com"

bool isTimeSet() {
  time_t now;
  struct tm timeinfo;
  if(!getLocalTime(&timeinfo)){
    return false;
  }
  return true;
}

bool requestTime() {
  configTime(0, 0, ntpServer);
  return isTimeSet();
}

time_t getUnixTime() {
  struct tm timeinfo;
  if(!getLocalTime(&timeinfo)){
    return 0;
  }
  return mktime(&timeinfo);
}

#endif // TIMESERVICE_H 