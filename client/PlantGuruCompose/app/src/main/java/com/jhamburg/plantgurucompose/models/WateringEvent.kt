package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class WateringEvent(
    @SerializedName("watering_id") val wateringId: Int,
    @SerializedName("watering_duration") val wateringDuration: Int,
    @SerializedName("peak_temp") val peakTemp: Float,
    @SerializedName("peak_moisture") val peakMoisture: Float,
    @SerializedName("avg_temp") val avgTemp: Float,
    @SerializedName("avg_moisture") val avgMoisture: Float,
    @SerializedName("plant_id") val plantId: Int,
    @SerializedName("time_stamp") val timeStamp: String,
    val volume: Float
)