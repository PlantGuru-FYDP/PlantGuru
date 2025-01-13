/*
    Provisioning.h - Base class for provisioning support
    All right reserved.
 
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

#ifndef Provisioning_h
#define Provisioning_h

#include "Config.h"
#include "WiFi.h"
#include "wifi_provisioning/manager.h"
#include <Preferences.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// Provisioning states
enum class ProvisioningState {
    PENDING,
    DEVICE_CONNECTED,
    WIFI_SETUP,
    BACKEND_VERIFIED,
    COMPLETED,
    FAILED
};

// WiFi setup states
enum class WiFiSetupState {
    NOT_STARTED,
    CONNECTING,
    CONNECTED,
    FAILED
};

// Provisioning scheme types
typedef enum {
    WIFI_PROV_SCHEME_SOFTAP,
#if CONFIG_BLUEDROID_ENABLED
    WIFI_PROV_SCHEME_BLE,
#endif
    WIFI_PROV_SCHEME_MAX
} prov_scheme_t;

// Provisioning scheme handlers
typedef enum {
    WIFI_PROV_SCHEME_HANDLER_NONE,
#if CONFIG_BLUEDROID_ENABLED
    WIFI_PROV_SCHEME_HANDLER_FREE_BTDM,
    WIFI_PROV_SCHEME_HANDLER_FREE_BLE,
    WIFI_PROV_SCHEME_HANDLER_FREE_BT,
#endif
    WIFI_PROV_SCHEME_HANDLER_MAX
} scheme_handler_t;

// Endpoint handlers forward declarations
static esp_err_t provision_token_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
static esp_err_t plant_id_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
static esp_err_t wifi_config_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
static esp_err_t verify_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
static esp_err_t status_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);

// Add this near the top of the file with other function declarations
extern void handle_wifi_connected();

// Provisioning class 
class ProvisioningClass {  
    private:
        Preferences preferences;
        char provision_token[37];  // UUID is 36 chars + null terminator
        char device_id[37];       // Using UUID format for device ID
        char plant_id[16];        // Enough space for plant ID string
        char user_token[16];      // Storage for user token
        ProvisioningState current_state;
        WiFiSetupState wifi_state;
        const char* backend_url;  // Backend URL for state updates
        bool is_provisioning;     // Flag to track if provisioning is in progress

        // State management
        bool updateBackendState(const char* new_state);
        bool verifyBackendConnection();
        void generateDeviceId();
        const char* stateToString(ProvisioningState state);
        
        // WiFi management
        bool setupWiFiConnection(const char* ssid, const char* password);
        bool verifyWiFiConnection();
        void clearWiFiCredentials();
        
        // Storage management
        void saveProvisioningData();
        bool loadProvisioningData();
        void clearProvisioningData();

        // Friend declarations for endpoint handlers
        friend esp_err_t provision_token_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
        friend esp_err_t plant_id_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
        friend esp_err_t wifi_config_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
        friend esp_err_t verify_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);
        friend esp_err_t status_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data);

    public:
        ProvisioningClass() : 
            current_state(ProvisioningState::PENDING),
            wifi_state(WiFiSetupState::NOT_STARTED),
            is_provisioning(false) {
            generateDeviceId();
            provision_token[0] = '\0';
            plant_id[0] = '\0';
            user_token[0] = '\0';
        }

        void beginProvision(
            prov_scheme_t prov_scheme,
            scheme_handler_t scheme_handler,
            wifi_prov_security_t security,
            const char *pop,
            const char *service_name,
            const char *service_key,
            uint8_t *uuid,
            bool reset_provisioned,
            const char* backend_endpoint = nullptr
        );

        void printQR(const char *name, const char *pop, const char *transport);
        
        // State management
        bool setState(ProvisioningState new_state);
        ProvisioningState getState() const { return current_state; }
        WiFiSetupState getWiFiState() const { return wifi_state; }
        bool isProvisioning() const { return is_provisioning; }
        
        // Token management
        const char* getProvisionToken() const { return provision_token; }
        void setProvisionToken(const char* token);
        const char* getUserToken() const { return user_token; }
        void setUserToken(const char* token);
        
        // Plant ID management
        const char* getPlantId() const { return plant_id; }
        void setPlantId(const char* id);
        
        // Device ID management
        const char* getDeviceId() const { return device_id; }

        // Provisioning control
        void cancelProvisioning();
        void resetProvisioning();
        bool isProvisioningComplete() const {
            return current_state == ProvisioningState::COMPLETED;
        }
};

extern ProvisioningClass Provisioning;

#endif
