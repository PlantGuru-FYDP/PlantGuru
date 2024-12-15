package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class PlantResponse(
    @SerializedName("plant_id") val plantId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("plant_name") val plantName: String,
    val age: Int,
    @SerializedName("last_watered") val lastWatered: String,
    @SerializedName("next_watering_time") val nextWateringTime: String,
    @SerializedName("ext_temp") val extTemp: Float?,
    val light: Float?,
    val humidity: Float?,
    @SerializedName("soil_temp") val soilTemp: Float?,
    @SerializedName("soil_moisture_1") val soilMoisture1: Float?,
    @SerializedName("soil_moisture_2") val soilMoisture2: Float?,
    @SerializedName("last_sensor_reading") val lastSensorReading: String?,
    @SerializedName("provisioning_status") val provisioningStatus: String?,
    @SerializedName("device_id") val deviceId: String?
)
