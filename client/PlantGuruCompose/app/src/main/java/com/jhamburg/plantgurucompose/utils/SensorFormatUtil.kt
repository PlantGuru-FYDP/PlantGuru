package com.jhamburg.plantgurucompose.utils

import com.jhamburg.plantgurucompose.models.SensorType

object SensorFormatUtil {
    fun formatValue(
        value: Float,
        range: Float,
        sensorType: SensorType,
        includeUnits: Boolean = true
    ): String {
        val format = if (range < 2f) "%.1f" else "%.0f"
        val formattedNumber = format.format(value)

        if (!includeUnits) return formattedNumber

        return when (sensorType) {
            SensorType.EXTERNAL_TEMP, SensorType.SOIL_TEMP ->
                "$formattedNumber°C"

            SensorType.SOIL_MOISTURE, SensorType.HUMIDITY, SensorType.LIGHT ->
                "$formattedNumber%"

            else -> formattedNumber
        }
    }

    fun formatValueCompact(value: Float, range: Float, sensorType: SensorType): String {
        val format = if (range < 2f) "%.1f" else "%.0f"
        val formattedNumber = format.format(value)

        return when (sensorType) {
            SensorType.EXTERNAL_TEMP, SensorType.SOIL_TEMP ->
                "$formattedNumber°"

            SensorType.SOIL_MOISTURE, SensorType.HUMIDITY, SensorType.LIGHT ->
                "$formattedNumber%"

            else -> formattedNumber
        }
    }
} 