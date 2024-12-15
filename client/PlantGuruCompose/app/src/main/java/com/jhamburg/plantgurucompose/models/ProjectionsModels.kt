package com.jhamburg.plantgurucompose.models

data class ProjectionsResponse(
    val plant_id: Int,
    val sensor_type: String,
    val granularity: Int,
    val num_points: Int,
    val last_reading: LastReading,
    val historicalData: List<TimeSeriesData>,
    val projections: List<Projection>
)

data class LastReading(
    val value: Double,
    val timestamp: String
)

data class Projection(
    val value: Double,
    val timestamp: String,
    val confidence: Double
) 