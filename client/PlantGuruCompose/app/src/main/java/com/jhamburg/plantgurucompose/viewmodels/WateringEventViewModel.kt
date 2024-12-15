package com.jhamburg.plantgurucompose.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.models.WateringEvent
import com.jhamburg.plantgurucompose.repository.WateringEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WateringEventViewModel @Inject constructor(private val wateringEventRepository: WateringEventRepository) :
    ViewModel() {

    private val _wateringEvents = MutableStateFlow<List<WateringEvent>>(emptyList())
    val wateringEvents: StateFlow<List<WateringEvent>> get() = _wateringEvents

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    private val _lastWateringEvent = MutableStateFlow<WateringEvent?>(null)
    val lastWateringEvent: StateFlow<WateringEvent?> get() = _lastWateringEvent

    private val _timeSeriesEvents = MutableStateFlow<List<WateringEvent>>(emptyList())
    val timeSeriesEvents: StateFlow<List<WateringEvent>> get() = _timeSeriesEvents

    private val _timeSeriesLoading = MutableStateFlow(false)
    val timeSeriesLoading: StateFlow<Boolean> get() = _timeSeriesLoading

    fun getWateringEvents(plantId: Int) {
        Log.d("WateringEventViewModel", "Getting watering events for plant $plantId")
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = wateringEventRepository.getWateringEvents(plantId)
                Log.d("WateringEventViewModel", "Received watering events response: $response")

                // Handle both array and {result: []} formats
                val events = when {
                    response is List<*> -> {
                        Log.d("WateringEventViewModel", "Processing list response")
                        response
                    }

                    response is Map<*, *> && response["result"] is List<*> -> {
                        Log.d("WateringEventViewModel", "Processing map response with result key")
                        (response["result"] as List<WateringEvent>)
                    }

                    else -> {
                        Log.w(
                            "WateringEventViewModel",
                            "Unexpected response format, defaulting to empty list"
                        )
                        emptyList()
                    }
                }

                _wateringEvents.value = events
                Log.d("WateringEventViewModel", "Updated watering events. Count: ${events.size}")
                if (events.isNotEmpty()) {
                    Log.d("WateringEventViewModel", "First event: ${events.first()}")
                    Log.d("WateringEventViewModel", "Last event: ${events.last()}")
                }
            } catch (e: Exception) {
                Log.e("WateringEventViewModel", "Error loading watering events", e)
                _error.value = e.message
            } finally {
                _loading.value = false
                Log.d("WateringEventViewModel", "Finished loading watering events")
            }
        }
    }

    fun getLastWateringEvent(plantId: Int) {
        Log.d("WateringEventViewModel", "Getting last watering event for plant $plantId")
        viewModelScope.launch {
            try {
                val event = wateringEventRepository.getLastWateringEvent(plantId)
                _lastWateringEvent.value = event
                Log.d("WateringEventViewModel", "Updated last watering event: $event")
            } catch (e: Exception) {
                Log.e("WateringEventViewModel", "Error loading last watering event", e)
                _error.value = e.message
            }
        }
    }

    fun getWateringEventSeries(plantId: Int, startTime: String, endTime: String) {
        Log.d("WateringEventViewModel", "Getting watering event series for plant $plantId")
        Log.d("WateringEventViewModel", "Time range: $startTime to $endTime")

        viewModelScope.launch {
            _timeSeriesLoading.value = true
            try {
                val events = wateringEventRepository.getWateringEventSeries(
                    plantId,
                    startTime,
                    endTime
                )
                _timeSeriesEvents.value = events
                Log.d(
                    "WateringEventViewModel",
                    "Updated watering events series. Count: ${events.size}"
                )
                if (events.isNotEmpty()) {
                    Log.d("WateringEventViewModel", "First event: ${events.first()}")
                    Log.d("WateringEventViewModel", "Last event: ${events.last()}")
                } else {
                    Log.d("WateringEventViewModel", "No watering events found in time range")
                }
            } catch (e: Exception) {
                Log.e("WateringEventViewModel", "Error loading watering events series", e)
                _error.value = e.message
            } finally {
                _timeSeriesLoading.value = false
                Log.d("WateringEventViewModel", "Finished loading watering events series")
            }
        }
    }
}
