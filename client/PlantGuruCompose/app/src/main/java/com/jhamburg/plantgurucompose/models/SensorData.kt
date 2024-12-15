package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class SensorData(
    @SerializedName("sensor_id") val sensorId: Int,
    @SerializedName("plant_id") val plantId: Int,
    @SerializedName("ext_temp") val extTemp: Float,
    val humidity: Float,
    val light: Float,
    @SerializedName("soil_temp") val soilTemp: Float,
    @SerializedName("soil_moisture_1") val soilMoisture1: Float,
    @SerializedName("soil_moisture_2") val soilMoisture2: Float,
    @SerializedName("time_stamp") val timeStamp: String,
    val confidence: Double = 0.0
)

data class SensorDataResponse(
    @SerializedName("result") val result: List<SensorData>
)
