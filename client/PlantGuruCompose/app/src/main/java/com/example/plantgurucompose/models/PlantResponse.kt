package com.example.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class PlantResponse(
    @SerializedName("plant_id") val plantId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("plant_name") val plantName: String,
    val age: Int,
    @SerializedName("last_watered") val lastWatered: String,
    @SerializedName("next_watering_time") val nextWateringTime: String
)
