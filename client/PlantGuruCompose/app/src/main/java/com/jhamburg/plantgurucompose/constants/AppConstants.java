// Copyright 2020 Espressif Systems (Shanghai) PTE LTD
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.jhamburg.plantgurucompose.constants;

public class AppConstants {

    // Keys used to pass data between activities and to store data in SharedPreference.
    public static final String KEY_WIFI_SECURITY_TYPE = "wifi_security";
    public static final String KEY_PROOF_OF_POSSESSION = "proof_of_possession";
    public static final String KEY_WIFI_DEVICE_NAME_PREFIX = "wifi_network_name_prefix";
    public static final String KEY_BLE_DEVICE_NAME_PREFIX = "ble_device_name_prefix";
    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_STATUS_MSG = "status_msg";
    public static final String KEY_WIFI_SSID = "ssid";
    public static final String KEY_WIFI_PASSWORD = "password";
    public static final String KEY_DEVICE_TYPES = "device_types";
    public static final String KEY_SECURITY_TYPE = "security_type";
    public static final String KEY_USER_NAME = "sec2_username";
    public static final String USER_TOKEN_ENDPOINT = "user-token";
    public static final String PLANT_ID_ENDPOINT = "plant-id";

    public static final String ESP_PREFERENCES = "Esp_Preferences";

    public static final String DEVICE_TYPE_SOFTAP = "softap";
    public static final String DEVICE_TYPE_BLE = "ble";
    public static final String DEVICE_TYPE_BOTH = "both";
    public static final String DEVICE_TYPE_DEFAULT = DEVICE_TYPE_BOTH;
    public static final String DEVICE_PREFIX = "GURU";

    public static final int SEC_TYPE_0 = 0;
    public static final int SEC_TYPE_1 = 1;
    public static final int SEC_TYPE_2 = 2;
    public static final int SEC_TYPE_DEFAULT = SEC_TYPE_0;
    public static final String DEFAULT_USER_NAME = "wifiprov";

    public static final String SERVICE_UUID = "90040382-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String PROV_SCAN_CHAR_UUID = "9004ff50-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String PROV_SESSION_CHAR_UUID = "9004ff51-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String PROV_CONFIG_CHAR_UUID = "9004ff52-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String PROTO_VER_CHAR_UUID = "9004ff53-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String PROVISION_TOKEN_CHAR_UUID = "9004ff54-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String PLANT_ID_CHAR_UUID = "9004ff55-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String WIFI_CONFIG_CHAR_UUID = "9004ff56-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String VERIFY_CHAR_UUID = "9004ff57-0aa4-f2bf-f40b-f6fd1c5adfb4";
    public static final String STATUS_CHAR_UUID = "9004ff58-0aa4-f2bf-f40b-f6fd1c5adfb4";

    public static final String PROVISION_TOKEN_ENDPOINT = "provision-token";
    public static final String WIFI_CONFIG_ENDPOINT = "wifi-config";
    public static final String VERIFY_ENDPOINT = "verify";
    public static final String STATUS_ENDPOINT = "status";

}
