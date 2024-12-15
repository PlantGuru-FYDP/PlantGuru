package com.jhamburg.plantgurucompose.models

data class RegisterDeviceRequest(
    val fcm_token: String,
    val device_name: String? = null
)
