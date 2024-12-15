package com.jhamburg.plantgurucompose.models

sealed class LoadingState {
    object Loading : LoadingState()
    object Success : LoadingState()
    data class Error(val message: String) : LoadingState()
}

data class SensorDataState(
    val sensorData: List<SensorData> = emptyList(),
    val wateringEvents: List<WateringEvent> = emptyList(),
    val loadingState: LoadingState = LoadingState.Success,
    val error: String? = null
)

data class ProjectionState(
    val historicalData: List<SensorData> = emptyList(),
    val projections: ProjectionsResponse? = null,
    val loadingState: LoadingState = LoadingState.Success,
    val error: String? = null
) 