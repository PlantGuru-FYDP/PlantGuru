package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.SerializedName


data class TimeSeriesData(
    @SerializedName("time_period") val timeStamp: String,
    @SerializedName("ext_temp") val ext_temp: Float,
    val humidity: Float,
    val light: Float,
    @SerializedName("soil_temp") val soil_temp: Float,
    @SerializedName("soil_moisture_1") val soil_moisture_1: Float,
    @SerializedName("soil_moisture_2") val soil_moisture_2: Float,
    @SerializedName("data_points") val dataPoints: Int
)

data class TimeSeriesResponse(
    @SerializedName("result") val result: List<TimeSeriesData>
) 