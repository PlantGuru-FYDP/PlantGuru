package com.jhamburg.plantgurucompose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.models.LoadingState
import com.jhamburg.plantgurucompose.models.ProjectionState
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.repository.ProjectionsRepository
import com.jhamburg.plantgurucompose.repository.SensorDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ProjectionsViewModel @Inject constructor(
    private val projectionsRepository: ProjectionsRepository,
    private val sensorDataRepository: SensorDataRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ProjectionState())
    val state: StateFlow<ProjectionState> = _state

    private var currentJob: Job? = null

    fun getProjections(
        plantId: Int,
        sensorType: String,
        numPoints: Int = 24,
        granularityMinutes: Int = 60
    ) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _state.update { it.copy(loadingState = LoadingState.Loading) }
                
                val endTime = LocalDateTime.now(ZoneId.of("UTC"))
                val startTime = endTime.minusMinutes((numPoints * granularityMinutes).toLong())

                val historical = sensorDataRepository.getTimeSeriesData(
                    plantId,
                    startTime.toString() + "Z",
                    endTime.toString() + "Z",
                    granularityMinutes
                )

                val projected = projectionsRepository.getProjections(
                    plantId,
                    sensorType,
                    numPoints,
                    granularityMinutes
                )

                _state.update { currentState ->
                    currentState.copy(
                        historicalData = historical.map { timeSeries ->
                            SensorData(
                                sensorId = 0,
                                plantId = plantId,
                                extTemp = timeSeries.ext_temp,
                                humidity = timeSeries.humidity,
                                light = timeSeries.light,
                                soilTemp = timeSeries.soil_temp,
                                soilMoisture1 = timeSeries.soil_moisture_1,
                                soilMoisture2 = timeSeries.soil_moisture_2,
                                timeStamp = timeSeries.timeStamp
                            )
                        },
                        projections = projected,
                        loadingState = LoadingState.Success
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        loadingState = LoadingState.Error(e.message ?: "Unknown error"),
                        error = e.message
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
} 