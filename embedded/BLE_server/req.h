#include <WiFi.h>

const char* ntpServer = "pool.ntp.org";  //Get the time from the network time server
const long gmtOffset_sec = 28800;      //UTC time is used here, China is in the UTC+8 time zone, which is 8*60*60
const int daylightOffset_sec = 0;      //Use daylight saving time daylightOffset_sec = 3600, otherwise it is equal to 0

// Method that will connect to the ntp server to set the system time, if not already
// set, if already set it just returns the system time
// all in unix time
bool getNtpTime()
{
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  struct tm *info;
  return getLocalTime(info);
}