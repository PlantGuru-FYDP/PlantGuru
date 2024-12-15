package com.jhamburg.plantgurucompose.models

data class SensorHealthResponse(
    val immediate_status: ImmediateStatus,
    val sensor_type: String,
    val plant_id: Int
)

data class ImmediateStatus(
    val status: String,
    val current_value: CurrentValue,
    val optimal_range: OptimalRange
)

data class CurrentValue(
    val value: Double,
    val unit: String,
    val timestamp: String
)

data class OptimalRange(
    val min: Double,
    val max: Double,
    val unit: String
)

data class HistoricalContext(
    val min: Double,
    val max: Double,
    val avg: Double,
    val readings: Int
)

data class PlantRecommendationsResponse(
    val plant_id: String,
    val timestamp: String,
    val current_issues: List<Issue>,
    val long_term_issues: List<Issue>,
    val last_watering: WateringEvent?,
    val recommendations: List<Recommendation>?

)

data class Recommendation(
    val type: String,
    val status: String,
    val value: Int,
    val message: String,
    val priority: String?,
)

data class Issue(
    val type: String,
    val status: String,
    val value: Int,
    val message: String,
    val priority: String?,
)

data class HealthDiagnosticsResponse(
    val overall_health: String,
    val health_score: Int,
    val stress_indicators: StressIndicators,
    val sensor_health: Map<String, String>,
    val alerts: List<Alert>,
    val timestamp: String,
    val latest_readings: LatestReadings
)

data class StressIndicators(
    val water_stress: Boolean,
    val heat_stress: Boolean,
    val light_stress: Boolean,
    val humidity_stress: Boolean
)

data class Alert(
    val type: String,
    val severity: String,
    val message: String
)

data class LatestReadings(
    val temperature: Double,
    val humidity: Double,
    val light: Double,
    val soil_moisture: Double
)

data class CareScheduleResponse(
    val plant_id: Int,
    val timestamp: String,
    val next_actions: List<CareAction>,
    val recent_actions: List<RecentAction>,
    val sensor_context: SensorContext
)

data class CareAction(
    val type: String,
    val due_in_hours: Int,
    val priority: String,
    val details: String
)

data class RecentAction(
    val type: String,
    val timestamp: String,
    val details: String
)

data class SensorContext(
    val soil_moisture: Double,
    val temperature: Double,
    val light: Double,
    val humidity: Double
) 