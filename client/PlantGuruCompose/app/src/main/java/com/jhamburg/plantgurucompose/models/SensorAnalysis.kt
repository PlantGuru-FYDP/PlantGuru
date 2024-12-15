package com.jhamburg.plantgurucompose.models

data class SensorAnalysis(
    val sensorType: String,
    val analysisType: String,
    val value: Float,
    val timestamp: String
)

data class SensorAnalysisResponse(
    val result: SensorAnalysis
)

data class SensorStatsResponse(
    val result: SensorStatsResult
)

data class SensorStatsResult(
    val minValue: Float,
    val maxValue: Float,
    val avgValue: Float,
    val totalReadings: Int
)

data class SensorTrendlineResponse(
    val result: SensorTrendlineResult
)

data class SensorTrendlineResult(
    val slope: Float,
    val intercept: Float,
    val minValue: Float,
    val maxValue: Float,
    val startPoint: String,
    val endPoint: String
)
