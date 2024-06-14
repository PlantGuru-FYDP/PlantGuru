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

#include "WiFi.h"
#include "wifi_provisioning/manager.h"
//Select the scheme using which you want to provision
typedef enum {
    WIFI_PROV_SCHEME_SOFTAP,
#if CONFIG_BLUEDROID_ENABLED
    WIFI_PROV_SCHEME_BLE,
#endif
    WIFI_PROV_SCHEME_MAX
} prov_scheme_t;

typedef enum {
    WIFI_PROV_SCHEME_HANDLER_NONE,
#if CONFIG_BLUEDROID_ENABLED
    WIFI_PROV_SCHEME_HANDLER_FREE_BTDM,
    WIFI_PROV_SCHEME_HANDLER_FREE_BLE,
    WIFI_PROV_SCHEME_HANDLER_FREE_BT,
#endif
    WIFI_PROV_SCHEME_HANDLER_MAX
} scheme_handler_t;

//Provisioning class 
class ProvisioningClass
{  
    public:
        void beginProvision(prov_scheme_t prov_scheme, scheme_handler_t scheme_handler, wifi_prov_security_t security, const char *pop, const char *service_name, const char *service_key, uint8_t *uuid, bool reset_provisioned);
        void printQR(const char *name, const char *pop, const char *transport);
        const char* getUserToken() const;
        void setUserToken(const char* token);
        char user_token[64];

};

extern ProvisioningClass Provisioning;

#endif
