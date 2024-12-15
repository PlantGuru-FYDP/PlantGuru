package com.jhamburg.plantgurucompose.models

data class ProvisioningStatusUpdate(
    val provision_token: String,
    val device_id: String?,
    val status: String,
    val plant_name: String?,
    val age: Int?,
    val reason: String? = null
)

data class ProvisioningStatus(
    val status: String,
    val device_id: String?,
    val plant_id: Int?,
    val warning: String?,
    val message: String?,
    val seconds_until_expiry: Int?
)

data class ProvisioningStatusResponse(
    val status: String,
    val message: String
)

data class ProvisioningVerifyRequest(
    val provision_token: String,
    val device_id: String
)

// Enum for provisioning states that matches backend
enum class ProvisioningStateType {
    PENDING,
    DEVICE_CONNECTED,
    WIFI_SETUP,
    BACKEND_VERIFIED,
    COMPLETED,
    FAILED
}

data class ProvisioningTokenResponse(
    val provision_token: String,
    val status: String
)