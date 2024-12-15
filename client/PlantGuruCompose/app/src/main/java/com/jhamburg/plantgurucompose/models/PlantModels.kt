package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class PlantCreateRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("plant_name") val plantName: String,
    val age: Int,
    @SerializedName("last_watered") val lastWatered: String,
    @SerializedName("next_watering_time") val nextWateringTime: String
)

data class PlantCreateResponse(
    val message: String,
    val plant_id: Int?,
    val provision_token: String
) 