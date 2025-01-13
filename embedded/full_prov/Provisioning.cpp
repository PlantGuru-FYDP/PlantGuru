/*
    Provisioning.cpp - Provisioning class for provisioning
    All rights reserved.
 
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.
  
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
  
    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
    
*/

#include "sdkconfig.h"
#include <stdio.h>
#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <esp_err.h>
#include <esp_wifi.h>
#include <esp_event.h>
#include <esp32-hal.h>
#include <esp_wpa2.h>
#if __has_include("qrcode.h")
  #include "qrcode.h"
#endif
#include <ArduinoJson.h>

#include <nvs_flash.h>
#if CONFIG_BLUEDROID_ENABLED
#include "wifi_provisioning/scheme_ble.h"
#endif
#include <wifi_provisioning/scheme_softap.h>
#include <wifi_provisioning/manager.h>
#undef IPADDR_NONE
#include "Provisioning.h"
#if CONFIG_IDF_TARGET_ESP32
#include "SimpleBLE.h"
#endif
#include <HTTPClient.h>
#include "Config.h"

extern Preferences preferences;

bool wifiLowLevelInit(bool persistent);

static esp_err_t enterprise_wifi_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);

#if CONFIG_BLUEDROID_ENABLED
static const uint8_t custom_service_uuid[16] = {  0xb4, 0xdf, 0x5a, 0x1c, 0x3f, 0x6b, 0xf4, 0xbf,
                                                  0xea, 0x4a, 0x82, 0x03, 0x04, 0x90, 0x1a, 0x02, };
#endif

#define SERV_NAME_PREFIX_PROV "GURU_"

static void get_device_service_name(prov_scheme_t prov_scheme, char *service_name, size_t max)
{
    uint8_t eth_mac[6] = {0,0,0,0,0,0};
    if(esp_wifi_get_mac((wifi_interface_t)WIFI_IF_STA, eth_mac) != ESP_OK){
        log_e("esp_wifi_get_mac failed!");
        return;
    }
#if CONFIG_IDF_TARGET_ESP32 && defined(CONFIG_BLUEDROID_ENABLED)
    if(prov_scheme == WIFI_PROV_SCHEME_BLE) {
        snprintf(service_name, max, "%s%02X%02X%02X",SERV_NAME_PREFIX_PROV, eth_mac[3], eth_mac[4], eth_mac[5]);
    } else {
#endif
         snprintf(service_name, max, "%s%02X%02X%02X",SERV_NAME_PREFIX_PROV, eth_mac[3], eth_mac[4], eth_mac[5]);
#if CONFIG_IDF_TARGET_ESP32 && defined(CONFIG_BLUEDROID_ENABLED)
    }
#endif
}

// Custom endpoint handler
static esp_err_t custom_ep_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data)
{
    ProvisioningClass* Provisioning = (ProvisioningClass*) priv_data;
    if (Provisioning == NULL || inbuf == NULL) {
        return ESP_ERR_INVALID_ARG;
    }

    Provisioning->setPlantId((const char*)inbuf);

    // Prepare response
    const char *resp = "Plant ID received";
    *outbuf = (uint8_t *)strdup(resp);
    if (*outbuf == NULL) {
        return ESP_ERR_NO_MEM;
    }
    *outlen = strlen(resp);
    return ESP_OK;
}

// Custom endpoint handler for plant ID
static esp_err_t plant_id_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data)
{
    ProvisioningClass* Provisioning = (ProvisioningClass*) priv_data;
    if (Provisioning == NULL || inbuf == NULL) {
        return ESP_ERR_INVALID_ARG;
    }

    // Parse the JSON data
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, (const char*)inbuf);
    
    if (!error) {
        const char* plantId = doc["plant_id"].as<const char*>();
        if (plantId != nullptr) {
            Provisioning->setPlantId(plantId);
            Serial.println("\n=== Received Plant ID from BLE ===");
            Serial.printf("Plant ID: %s\n", plantId);
            Serial.println("================================\n");
        }
    }

    // Prepare response
    const char *resp = "Plant ID received";
    *outbuf = (uint8_t *)strdup(resp);
    if (*outbuf == NULL) {
        return ESP_ERR_NO_MEM;
    }
    *outlen = strlen(resp);
    return ESP_OK;
}

void ProvisioningClass::generateDeviceId() {
    uint8_t mac[6];
    esp_read_mac(mac, ESP_MAC_WIFI_STA);
    snprintf(device_id, sizeof(device_id), "%02X%02X%02X%02X-%02X%02X",
             mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
}

const char* ProvisioningClass::stateToString(ProvisioningState state) {
    switch (state) {
        case ProvisioningState::PENDING: return "PENDING";
        case ProvisioningState::DEVICE_CONNECTED: return "DEVICE_CONNECTED";
        case ProvisioningState::WIFI_SETUP: return "WIFI_SETUP";
        case ProvisioningState::BACKEND_VERIFIED: return "BACKEND_VERIFIED";
        case ProvisioningState::COMPLETED: return "COMPLETED";
        case ProvisioningState::FAILED: return "FAILED";
        default: return "UNKNOWN";
    }
}

bool ProvisioningClass::updateBackendState(const char* new_state) {
    if (!backend_url || !provision_token[0]) {
        log_e("Backend URL or provision token not set");
        return false;
    }

    HTTPClient http;
    String url = String(backend_url) + "/api/provisioning/status";
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    // Create JSON payload
    StaticJsonDocument<200> doc;
    doc["provision_token"] = provision_token;
    doc["device_id"] = device_id;
    doc["status"] = new_state;

    String payload;
    serializeJson(doc, payload);

    int httpCode = http.POST(payload);
    bool success = (httpCode == 200);
    
    if (!success) {
        log_e("Failed to update backend state: %d", httpCode);
    }

    http.end();
    return success;
}

bool ProvisioningClass::verifyBackendConnection() {
    if (!backend_url || !provision_token[0]) {
        log_e("Backend URL or provision token not set");
        return false;
    }

    if (!verifyWiFiConnection()) {
        log_e("WiFi not connected");
        return false;
    }

    HTTPClient http;
    String url = String(backend_url) + "/api/provisioning/verify";
    http.begin(url);
    http.addHeader("Content-Type", "application/json");

    // Create JSON payload
    StaticJsonDocument<200> doc;
    doc["provision_token"] = provision_token;
    doc["device_id"] = device_id;

    String payload;
    serializeJson(doc, payload);

    int httpCode = http.POST(payload);
    bool success = (httpCode == 200);

    if (!success) {
        log_e("Failed to verify backend connection: %d", httpCode);
    }

    http.end();
    return success;
}

bool ProvisioningClass::setState(ProvisioningState new_state) {
    // Don't update if already in final state
    if (current_state == ProvisioningState::COMPLETED || 
        current_state == ProvisioningState::FAILED) {
        return false;
    }

    // Update backend first if we have a token and URL
    if (backend_url && provision_token[0] != '\0') {
        if (!updateBackendState(stateToString(new_state))) {
            // If backend update fails, move to FAILED state
            current_state = ProvisioningState::FAILED;
            updateBackendState("FAILED");
            return false;
        }
    }

    // Special handling for state transitions
    switch (new_state) {
        case ProvisioningState::DEVICE_CONNECTED:
            if (current_state != ProvisioningState::PENDING) {
                return false;
            }
            break;

        case ProvisioningState::WIFI_SETUP:
            if (current_state != ProvisioningState::DEVICE_CONNECTED) {
                return false;
            }
            break;

        case ProvisioningState::BACKEND_VERIFIED:
            if (current_state != ProvisioningState::WIFI_SETUP) {
                return false;
            }
            if (!verifyBackendConnection()) {
                current_state = ProvisioningState::FAILED;
                updateBackendState("FAILED");
                return false;
            }
            break;

        case ProvisioningState::COMPLETED:
            if (current_state != ProvisioningState::BACKEND_VERIFIED) {
                return false;
            }
            // Save all provisioning data
            saveProvisioningData();
            is_provisioning = false;
            break;

        case ProvisioningState::FAILED:
            // Can transition to FAILED from any state
            clearProvisioningData();
            is_provisioning = false;
            break;

        default:
            break;
    }

    current_state = new_state;
    return true;
}

void ProvisioningClass::setProvisionToken(const char* token) {
    if (token && strlen(token) < sizeof(provision_token)) {
        strncpy(provision_token, token, sizeof(provision_token) - 1);
        provision_token[sizeof(provision_token) - 1] = '\0';
        saveProvisioningData();
    }
}

void ProvisioningClass::setPlantId(const char* id) {
    if (id && strlen(id) < sizeof(plant_id)) {
        strncpy(plant_id, id, sizeof(plant_id) - 1);
        plant_id[sizeof(plant_id) - 1] = '\0';
        
        // Store in preferences
        preferences.begin("device_prefs", false);
        preferences.putInt("plant_id", atoi(plant_id));
        preferences.end();
    }
}

void ProvisioningClass::setUserToken(const char* token) {
    if (token && strlen(token) < sizeof(user_token)) {
        strncpy(user_token, token, sizeof(user_token) - 1);
        user_token[sizeof(user_token) - 1] = '\0';
        
        // Store in preferences
        preferences.begin("device_prefs", false);
        preferences.putString("user_token", user_token);
        preferences.end();
    }
}

void ProvisioningClass::beginProvision(
    prov_scheme_t prov_scheme,
    scheme_handler_t scheme_handler,
    wifi_prov_security_t security,
    const char *pop,
    const char *service_name,
    const char *service_key,
    uint8_t *uuid,
    bool reset_provisioned,
    const char* backend_endpoint
) {
    backend_url = backend_endpoint;
    is_provisioning = true;
    
    // Load stored values from preferences
    if (!reset_provisioned && loadProvisioningData()) {
        if (current_state == ProvisioningState::COMPLETED) {
            log_i("Device already provisioned");
            is_provisioning = false;
            return;
        }
    }

    // Start in PENDING state
    current_state = ProvisioningState::PENDING;
    wifi_state = WiFiSetupState::NOT_STARTED;
    
    // Original provisioning logic
    bool provisioned = false;
    static char service_name_temp[32];
    
    // Initialize WiFi provisioning manager configuration
    wifi_prov_mgr_config_t config = {};
    
    if (prov_scheme == WIFI_PROV_SCHEME_BLE) {
        config.scheme = wifi_prov_scheme_ble;
    }

    // Set up event handlers
    if (scheme_handler == WIFI_PROV_SCHEME_HANDLER_NONE) {
        wifi_prov_event_handler_t scheme_event_handler = WIFI_PROV_EVENT_HANDLER_NONE;
        memcpy(&config.scheme_event_handler, &scheme_event_handler, sizeof(wifi_prov_event_handler_t));
#if CONFIG_BLUEDROID_ENABLED
    } else if (scheme_handler == WIFI_PROV_SCHEME_HANDLER_FREE_BTDM) {
        wifi_prov_event_handler_t scheme_event_handler = WIFI_PROV_SCHEME_BLE_EVENT_HANDLER_FREE_BTDM;
        memcpy(&config.scheme_event_handler, &scheme_event_handler, sizeof(wifi_prov_event_handler_t));
    } else if (scheme_handler == WIFI_PROV_SCHEME_HANDLER_FREE_BT) {
        wifi_prov_event_handler_t scheme_event_handler = WIFI_PROV_SCHEME_BLE_EVENT_HANDLER_FREE_BT;
        memcpy(&config.scheme_event_handler, &scheme_event_handler, sizeof(wifi_prov_event_handler_t));
    } else if (scheme_handler == WIFI_PROV_SCHEME_HANDLER_FREE_BLE) {
        wifi_prov_event_handler_t scheme_event_handler = WIFI_PROV_SCHEME_BLE_EVENT_HANDLER_FREE_BLE;
        memcpy(&config.scheme_event_handler, &scheme_event_handler, sizeof(wifi_prov_event_handler_t));
#endif
    } else {
        log_e("Unknown scheme handler!");
        return;
    }
    
    config.app_event_handler.event_cb = NULL;
    config.app_event_handler.user_data = NULL;
    
    // Initialize WiFi
    wifiLowLevelInit(true);
    
    // Initialize the provisioning manager
    esp_err_t err = wifi_prov_mgr_init(config);
    if (err != ESP_OK) {
        log_e("wifi_prov_mgr_init failed! %d", err);
        return;
    }
    
    // Handle reset provisioning if requested
    if (reset_provisioned) {
        log_i("Resetting provisioned data.");
        wifi_prov_mgr_reset_provisioning();
        clearProvisioningData();
        clearWiFiCredentials();
    } else if (wifi_prov_mgr_is_provisioned(&provisioned) != ESP_OK) {
        log_e("wifi_prov_mgr_is_provisioned failed!");
        wifi_prov_mgr_deinit();
        return;
    }
    
    if (!provisioned) {
        // Set up BLE service parameters first
        service_key = NULL;
        if (uuid == NULL) {
            uuid = (uint8_t *)custom_service_uuid;
        }
        if (service_name == NULL) {
            get_device_service_name(prov_scheme, service_name_temp, 32);
            service_name = (const char *)service_name_temp;
        }

#if CONFIG_BLUEDROID_ENABLED
        if (prov_scheme == WIFI_PROV_SCHEME_BLE) {
            wifi_prov_scheme_ble_set_service_uuid(uuid);
        }
#endif

        // Create endpoints before starting provisioning
        if (wifi_prov_mgr_endpoint_create("provision-token") != ESP_OK ||
            wifi_prov_mgr_endpoint_create("plant-id") != ESP_OK ||
            wifi_prov_mgr_endpoint_create("wifi-config") != ESP_OK ||
            wifi_prov_mgr_endpoint_create("status") != ESP_OK ||
            wifi_prov_mgr_endpoint_create("enterprise-wifi") != ESP_OK) {
            log_e("Failed to create endpoints");
            wifi_prov_mgr_deinit();
            return;
        }

        // Start provisioning
        err = wifi_prov_mgr_start_provisioning(security, pop, service_name, service_key);
        if (err != ESP_OK) {
            log_e("wifi_prov_mgr_start_provisioning failed: %d", err);
            wifi_prov_mgr_deinit();
            return;
        }

        // Register handlers after starting provisioning
        if (wifi_prov_mgr_endpoint_register("provision-token", provision_token_handler, this) != ESP_OK ||
            wifi_prov_mgr_endpoint_register("plant-id", plant_id_handler, this) != ESP_OK ||
            wifi_prov_mgr_endpoint_register("wifi-config", wifi_config_handler, this) != ESP_OK ||
            wifi_prov_mgr_endpoint_register("status", status_handler, this) != ESP_OK ||
            wifi_prov_mgr_endpoint_register("enterprise-wifi", enterprise_wifi_handler, this) != ESP_OK) {
            log_e("Failed to register handlers");
            wifi_prov_mgr_deinit();
            return;
        }

        log_i("Provisioning Started. Waiting for connection...");
    } else {
        log_i("Already Provisioned");
        if (loadProvisioningData()) {
            log_i("Loaded existing provisioning data");
            current_state = ProvisioningState::COMPLETED;
        }
#if ARDUHAL_LOG_LEVEL >= ARDUHAL_LOG_LEVEL_INFO
        static wifi_config_t conf;
        esp_wifi_get_config((wifi_interface_t)WIFI_IF_STA, &conf);
        log_i("Attempting connect to AP: %s", conf.sta.ssid);
#endif
        esp_wifi_start();
        wifi_prov_mgr_deinit();
        WiFi.begin();
    }
}

// Copied from IDF example
void ProvisioningClass::printQR(const char *name, const char *pop, const char *transport)
{
    if (!name || !transport) {
        log_w("Cannot generate QR code payload. Data missing.");
        return;
    }
    char payload[150] = {0};
    if (pop) {
        snprintf(payload, sizeof(payload), "{\"ver\":\"%s\",\"name\":\"%s\"" \
                    ",\"pop\":\"%s\",\"transport\":\"%s\"}",
                    "v1", name, pop, transport);
    } else {
        snprintf(payload, sizeof(payload), "{\"ver\":\"%s\",\"name\":\"%s\"" \
                    ",\"transport\":\"%s\"}",
                    "v1", name, transport);
    }
#if __has_include("qrcode.h")
    log_i("Scan this QR code from the provisioning application for Provisioning.");
    esp_qrcode_config_t cfg = ESP_QRCODE_CONFIG_DEFAULT();
    esp_qrcode_generate(&cfg, payload);
#else
    log_i("If QR code is not visible, copy paste the below URL in a browser.\n%s?data=%s", "https://espressif.github.io/esp-jumpstart/qrcode.html", payload);
    log_i("If you are using Arduino as IDF component, install ESP Rainmaker:\nhttps://github.com/espressif/esp-rainmaker");
#endif
}

bool ProvisioningClass::loadProvisioningData() {
    preferences.begin("device_prefs", true);
    bool hasData = preferences.getBool("has_data", false);
    if (hasData) {
        preferences.getString("prov_token", provision_token, sizeof(provision_token));
        preferences.getString("plant_id", plant_id, sizeof(plant_id));
        preferences.getString("user_token", user_token, sizeof(user_token));
        current_state = static_cast<ProvisioningState>(preferences.getInt("state", static_cast<int>(ProvisioningState::PENDING)));
        wifi_state = static_cast<WiFiSetupState>(preferences.getInt("wifi_state", static_cast<int>(WiFiSetupState::NOT_STARTED)));
    }
    preferences.end();
    return hasData;
}

void ProvisioningClass::clearProvisioningData() {
    preferences.begin("device_prefs", false);
    preferences.remove("has_data");
    preferences.remove("prov_token");
    preferences.remove("plant_id");
    preferences.remove("user_token");
    preferences.remove("state");
    preferences.remove("wifi_state");
    preferences.remove("verified");
    preferences.end();
    
    // Reset all member variables
    provision_token[0] = '\0';
    plant_id[0] = '\0';
    user_token[0] = '\0';
    current_state = ProvisioningState::PENDING;
    wifi_state = WiFiSetupState::NOT_STARTED;
    is_provisioning = false;
}

void ProvisioningClass::clearWiFiCredentials() {
    WiFi.disconnect(true);  // true parameter erases stored credentials
    esp_wifi_restore();     // Reset WiFi settings to factory defaults
}

// Endpoint handler implementations

static esp_err_t enterprise_wifi_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data) {
    Serial.println("\n=== Enterprise WiFi Handler Called ===");
    Serial.printf("Session ID: %u\n", session_id);
    Serial.printf("Input length: %d\n", inlen);
    
    if (!inbuf || !outbuf || !outlen) {
        Serial.println("Invalid arguments received");
        return ESP_ERR_INVALID_ARG;
    }

    // Parse the JSON data
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, (const char*)inbuf);
    
    if (!error) {
        const char* identity = doc["identity"] | "";
        const char* username = doc["username"] | "";
        const char* password = doc["password"] | "";
        const char* ssid = doc["ssid"] | "";
        bool isEnterprise = doc["isEnterprise"] | false;
        
        Serial.println("\n=== Enterprise WiFi Config Details ===");
        Serial.printf("SSID: %s\n", ssid);
        Serial.printf("Is Enterprise: %s\n", isEnterprise ? "true" : "false");
        Serial.printf("Identity Length: %d\n", strlen(identity));
        Serial.printf("Username Length: %d\n", strlen(username));
        Serial.printf("Password Length: %d\n", strlen(password));
        
        // Save enterprise credentials to preferences
        preferences.begin("device_prefs", false);
        preferences.putString("enterprise_identity", identity);
        preferences.putString("enterprise_username", username);
        preferences.putString("enterprise_password", password);
        preferences.putString("wifi_ssid", ssid);
        preferences.putBool("is_enterprise", isEnterprise);
        preferences.end();

        Serial.println("Enterprise credentials saved to preferences");
        
        // Verify saved data
        preferences.begin("device_prefs", true);
        bool savedIsEnterprise = preferences.getBool("is_enterprise", false);
        String savedSsid = preferences.getString("wifi_ssid", "");
        String savedIdentity = preferences.getString("enterprise_identity", "");
        String savedUsername = preferences.getString("enterprise_username", "");
        
        Serial.println("\n=== Verifying Saved Credentials ===");
        Serial.printf("Saved SSID: %s\n", savedSsid.c_str());
        Serial.printf("Saved Is Enterprise: %s\n", savedIsEnterprise ? "true" : "false");
        Serial.printf("Saved Identity Length: %d\n", savedIdentity.length());
        Serial.printf("Saved Username Length: %d\n", savedUsername.length());
        preferences.end();

        Serial.println("=== Enterprise WiFi Handler Complete ===\n");

        // Prepare success response
        const char *resp = "Enterprise config received and saved";
        *outbuf = (uint8_t *)strdup(resp);
        if (*outbuf == NULL) {
            return ESP_ERR_NO_MEM;
        }
        *outlen = strlen(resp);
        return ESP_OK;
    }

    Serial.println("Failed to parse enterprise config JSON");
    // Error response
    const char *resp = "Failed to parse enterprise config";
    *outbuf = (uint8_t *)strdup(resp);
    if (*outbuf == NULL) {
        return ESP_ERR_NO_MEM;
    }
    *outlen = strlen(resp);
    return ESP_OK;
}

static esp_err_t provision_token_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data) {
    Serial.println("\n=== Provision Token Handler Called ===");
    Serial.printf("Session ID: %u\n", session_id);
    Serial.printf("Input length: %d\n", inlen);
    
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, (const char*)inbuf);
    
    if (!error) {
        const char* token = doc["provision_token"];
        if (token) {
            Serial.println("Parsing provision token successful");
            preferences.begin("device_prefs");
            preferences.putString("provision_token", token);
            String deviceId = preferences.getString("device_id", "");
            preferences.end();

            Serial.printf("Stored token: %s\n", token);
            Serial.printf("Device ID: %s\n", deviceId.c_str());
            Serial.println("Token stored successfully - waiting for WiFi credentials");
        } else {
            Serial.println("Error: No provision token in payload");
        }
    } else {
        Serial.printf("JSON parse error: %s\n", error.c_str());
    }

    Serial.println("=== Provision Token Handler Complete ===\n");
    const char *resp = "Provision token received";
    *outbuf = (uint8_t *)strdup(resp);
    if (*outbuf == NULL) {
        return ESP_ERR_NO_MEM;
    }
    *outlen = strlen(resp);
    return ESP_OK;
}

static esp_err_t wifi_config_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data) {
    if (!inbuf || !outbuf || !outlen) {
        return ESP_ERR_INVALID_ARG;
    }

    Serial.println("\n=== WiFi Config Handler Called ===");
    
    // Parse the JSON data
    StaticJsonDocument<512> doc;
    DeserializationError error = deserializeJson(doc, (const char*)inbuf);
    
    if (!error) {
        const char* ssid = doc["ssid"];
        const char* password = doc["password"];
        bool isEnterprise = doc["isEnterprise"] | false;
        
        Serial.printf("Received WiFi config - SSID: %s, Enterprise: %s\n", 
                     ssid,  // ssid is already const char*
                     isEnterprise ? "Yes" : "No"
        );
        
        if (isEnterprise) {
            const char* identity = doc["identity"] | "";
            
            // Save enterprise credentials to preferences
            preferences.begin("device_prefs", false);
            preferences.putString("enterprise_identity", identity);
            preferences.putString("enterprise_password", password);  // Use the password field for enterprise password
            preferences.putBool("is_enterprise", true);
            preferences.putString("wifi_ssid", ssid);
            preferences.end();

            // For enterprise networks, set up WPA2 Enterprise
            WiFi.disconnect(true);
            WiFi.mode(WIFI_STA);
            
            Serial.println("\n=== Setting up Enterprise WiFi ===");
            Serial.printf("Identity length: %d\n", strlen(identity));
            Serial.printf("Password length: %d\n", strlen(password));
            
            esp_wifi_sta_wpa2_ent_set_identity((uint8_t *)identity, strlen(identity));
            esp_wifi_sta_wpa2_ent_set_username((uint8_t *)identity, strlen(identity));  // Use identity for username
            esp_wifi_sta_wpa2_ent_set_password((uint8_t *)password, strlen(password));
            
            esp_wifi_sta_wpa2_ent_enable();

            Serial.println("\n=== Enterprise WiFi Config Complete ===");
            Serial.printf("SSID: %s\n", ssid);
            Serial.printf("Identity: %s\n", identity);
            Serial.println("Password: [hidden]");
            Serial.println("WPA2 Enterprise enabled and configured");
            Serial.println("=====================================\n");
        } else {
            // Regular WiFi credentials
            preferences.begin("device_prefs", false);
            preferences.putString("wifi_ssid", ssid);
            preferences.putString("wifi_password", password);
            preferences.putBool("is_enterprise", false);
            preferences.end();

            Serial.println("\nReceived Wi-Fi credentials");
            Serial.printf("SSID: %s\n", ssid);
            Serial.printf("Password length: %d\n", strlen(password));
            Serial.printf("First few chars of password: %.3s...\n", password);
            Serial.println("Attempting to save credentials...");
            Serial.println("Credentials saved to preferences");
        }

        const char *resp = "WiFi config received";
        *outbuf = (uint8_t *)strdup(resp);
        if (*outbuf == NULL) {
            return ESP_ERR_NO_MEM;
        }
        *outlen = strlen(resp);
        return ESP_OK;
    }

    const char *resp = "Failed to parse WiFi config";
    *outbuf = (uint8_t *)strdup(resp);
    if (*outbuf == NULL) {
        return ESP_ERR_NO_MEM;
    }
    *outlen = strlen(resp);
    return ESP_OK;
}

void handle_wifi_connected() {
    Serial.println("\n=== WiFi Connected - Starting Verification ===");
    
    if (WiFi.status() == WL_CONNECTED) {
        preferences.begin("device_prefs");
        String token = preferences.getString("provision_token", "");
        String deviceId = preferences.getString("device_id", "");
        Serial.printf("Loaded from preferences:\n  Token: %s\n  Device ID: %s\n", token.c_str(), deviceId.c_str());
        preferences.end();

        if (token.length() > 0 && deviceId.length() > 0) {
            HTTPClient http;
            String url = String(PLANTGURU_SERVER) + "/api/provision/verify";
            Serial.printf("Making verification request to: %s\n", url.c_str());
            http.begin(url);
            http.addHeader("Content-Type", "application/json");

            // Create verification payload
            StaticJsonDocument<200> doc;
            doc["provision_token"] = token;
            doc["device_id"] = deviceId;
            String payload;
            serializeJson(doc, payload);
            Serial.printf("Request payload: %s\n", payload.c_str());

            // Try verification up to 3 times
            bool success = false;
            int plantId = -1;
            int httpCode = 0;
            
            for (int i = 0; i < 3 && !success; i++) {
                Serial.printf("\nVerification attempt %d/3...\n", i + 1);
                httpCode = http.POST(payload);
                Serial.printf("HTTP Response code: %d\n", httpCode);
                
                if (httpCode == 200) {
                    String response = http.getString();
                    Serial.printf("Response body: %s\n", response.c_str());
                    
                    StaticJsonDocument<200> respDoc;
                    DeserializationError error = deserializeJson(respDoc, response);
                    Serial.printf("JSON parse result: %s\n", error.c_str());
                    
                    if (!error) {
                        // Debug print all fields in response
                        Serial.println("Response fields:");
                        for (JsonPair kv : respDoc.as<JsonObject>()) {
                            Serial.printf("  %s: ", kv.key().c_str());
                            if (kv.value().is<const char*>()) {
                                Serial.println(kv.value().as<const char*>());
                            } else if (kv.value().is<int>()) {
                                Serial.println(kv.value().as<int>());
                            } else {
                                Serial.println("[unknown type]");
                            }
                        }
                        
                        plantId = respDoc["plant_id"].as<int>();
                        Serial.printf("Extracted plant_id: %d\n", plantId);
                        
                        if (plantId > 0) {
                            Serial.printf("Successfully parsed valid plant_id: %d\n", plantId);
                            success = true;
                        } else {
                            Serial.println("Invalid plant_id received (zero or negative)");
                            success = false;
                        }
                    } else {
                        Serial.printf("Failed to parse response. Error: %s\n", error.c_str());
                        success = false;
                    }
                }
                if (!success && i < 2) {
                    Serial.println("Verification failed, retrying in 1s...");
                    delay(1000);
                }
            }

            http.end();
            
            Serial.printf("\nVerification final result: %s\n", success ? "SUCCESS" : "FAILED");
            Serial.printf("Final plant_id value: %d\n", plantId);

            if (success && plantId > 0) {
                Serial.println("Verification successful! Saving plant ID...");
                preferences.begin("device_prefs");
                preferences.putInt("plant_id", plantId);
                preferences.putBool("verified", true);
                preferences.end();
                
                Serial.printf("\n=== Device Successfully Verified ===\n");
                Serial.printf("Plant ID: %d\n", plantId);
                Serial.println("====================================\n");
            } else {
                Serial.println("\n=== Verification Failed ===");
                Serial.printf("Last HTTP code: %d\n", httpCode);
                Serial.printf("Final plant_id: %d\n", plantId);
                Serial.println("=========================\n");
                
                preferences.begin("device_prefs");
                preferences.putBool("verified", false);
                preferences.end();
            }
        } else {
            Serial.println("Missing token or device ID - cannot verify");
        }
    } else {
        Serial.println("WiFi not connected - cannot verify");
    }
    
    Serial.println("=== Verification Process Complete ===\n");
}

esp_err_t status_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data) {
    ProvisioningClass* Provisioning = (ProvisioningClass*) priv_data;
    if (Provisioning == NULL) {
        return ESP_ERR_INVALID_ARG;
    }

    // Create JSON response with current status
    StaticJsonDocument<200> doc;
    doc["state"] = Provisioning->stateToString(Provisioning->getState());
    doc["wifi_connected"] = WiFi.status() == WL_CONNECTED;
    doc["device_id"] = Provisioning->getDeviceId();
    
    String response;
    serializeJson(doc, response);

    // Prepare response buffer
    *outbuf = (uint8_t *)strdup(response.c_str());
    if (*outbuf == NULL) {
        return ESP_ERR_NO_MEM;
    }
    *outlen = response.length();
    return ESP_OK;
}

void ProvisioningClass::saveProvisioningData() {
    preferences.begin("device_prefs", false);
    preferences.putBool("has_data", true);
    preferences.putString("prov_token", provision_token);
    preferences.putString("plant_id", plant_id);
    preferences.putString("user_token", user_token);
    preferences.putInt("state", static_cast<int>(current_state));
    preferences.putInt("wifi_state", static_cast<int>(wifi_state));
    preferences.end();
}

bool ProvisioningClass::verifyWiFiConnection() {
    if (WiFi.status() != WL_CONNECTED) {
        return false;
    }

    // Try to ping the backend server to verify internet connectivity
    HTTPClient http;
    String url = String(backend_url) + "/api/ping";
    http.begin(url);
    
    int httpCode = http.GET();
    bool success = (httpCode == 200);
    
    http.end();
    return success;
}

// Add this new endpoint handler for enterprise WiFi

ProvisioningClass Provisioning;

bool setupWiFiConnection() {
    Serial.println("\n=== Setting up WiFi Connection ===");
    
    preferences.begin("device_prefs", true);
    bool isEnterprise = preferences.getBool("is_enterprise", false);
    String ssid = preferences.getString("wifi_ssid", "");
    
    Serial.printf("Retrieved SSID: %s\n", ssid.c_str());
    Serial.printf("Is Enterprise Network: %s\n", isEnterprise ? "true" : "false");
    
    if (isEnterprise) {
        String identity = preferences.getString("enterprise_identity", "");
        String username = preferences.getString("enterprise_username", "");
        String password = preferences.getString("enterprise_password", "");
        
        Serial.println("\n=== Enterprise WiFi Details ===");
        Serial.printf("Identity Length: %d\n", identity.length());
        Serial.printf("Username Length: %d\n", username.length());
        Serial.printf("Password Length: %d\n", password.length());
        
        WiFi.disconnect(true);
        WiFi.mode(WIFI_STA);
        
        Serial.println("Setting enterprise credentials...");
        esp_wifi_sta_wpa2_ent_set_identity((uint8_t *)identity.c_str(), identity.length());
        esp_wifi_sta_wpa2_ent_set_username((uint8_t *)username.c_str(), username.length());
        esp_wifi_sta_wpa2_ent_set_password((uint8_t *)password.c_str(), password.length());
        
        esp_wifi_sta_wpa2_ent_enable();
        Serial.println("Enterprise WiFi enabled");
        
        WiFi.begin(ssid.c_str());
    } else {
        String password = preferences.getString("wifi_password", "");
        Serial.println("\n=== Standard WiFi Details ===");
        Serial.printf("Password Length: %d\n", password.length());
        
        WiFi.begin(ssid.c_str(), password.c_str());
    }
    preferences.end();
    
    Serial.println("\nAttempting WiFi connection...");
    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 20) {
        delay(500);
        Serial.print(".");
        attempts++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\nWiFi connected successfully!");
        Serial.printf("IP address: %s\n", WiFi.localIP().toString().c_str());
        return true;
    }
    
    Serial.printf("\nWiFi connection failed after %d attempts\n", attempts);
    Serial.printf("Last WiFi status: %d\n", WiFi.status());
    return false;
}


