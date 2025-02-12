package com.jhamburg.plantgurucompose.models

enum class SensorType(val label: String, val apiName: String, val unit: String = "") {
    SOIL_MOISTURE("Soil Moisture", "soil_moisture_1", "%"),
    SOIL_TEMP("Soil Temp", "soil_temp", "°C"),//deg C
    EXTERNAL_TEMP("Air Temp", "ext_temp", "°C"),
    HUMIDITY("Humidity", "humidity", "%"),
    LIGHT("Light", "light", "%");

    companion object {
        fun fromString(value: String): SensorType {
            return values().find { it.apiName == value } ?: SOIL_MOISTURE
        }
    }
} 