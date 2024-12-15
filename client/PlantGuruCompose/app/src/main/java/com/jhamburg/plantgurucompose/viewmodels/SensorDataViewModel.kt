package com.jhamburg.plantgurucompose.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.models.*
import com.jhamburg.plantgurucompose.repository.SensorDataRepository
import com.jhamburg.plantgurucompose.repository.WateringEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorDataViewModel @Inject constructor(
    private val sensorDataRepository: SensorDataRepository,
    private val wateringEventRepository: WateringEventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SensorDataState())
    val state: StateFlow<SensorDataState> = _state

    private val _sensorData = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorData: StateFlow<List<SensorData>> = _sensorData

    private val _currentSensorData = MutableStateFlow<SensorData?>(null)
    val currentSensorData: StateFlow<SensorData?> = _currentSensorData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentJob: Job? = null

    fun getTimeSeriesData(
        plantId: Int,
        startTime: String,
        endTime: String,
        granularity: Int = 0
    ) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                _loading.value = true
                _state.update { it.copy(loadingState = LoadingState.Loading) }

                val data = sensorDataRepository.getTimeSeriesData(plantId, startTime, endTime, granularity)
                val events = wateringEventRepository.getWateringEventSeries(plantId, startTime, endTime)
                
                _sensorData.value = data.map { timeSeries ->
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
                }

                _state.update { currentState -> 
                    currentState.copy(
                        sensorData = _sensorData.value,
                        wateringEvents = events,
                        loadingState = LoadingState.Success
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
                _state.update { currentState -> 
                    currentState.copy(
                        loadingState = LoadingState.Error(e.message ?: "Unknown error"),
                        error = e.message
                    )
                }
            } finally {
                _loading.value = false
            }
        }
    }

    fun getLastNSensorReadings(plantId: Int, n: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val data = sensorDataRepository.getLastNSensorReadings(plantId, n)
                _currentSensorData.value = data.firstOrNull()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
    }
}