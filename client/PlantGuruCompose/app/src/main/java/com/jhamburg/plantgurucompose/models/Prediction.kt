package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class Prediction(
    @SerializedName("Next watering time") val hoursToNextWatering: Float
)