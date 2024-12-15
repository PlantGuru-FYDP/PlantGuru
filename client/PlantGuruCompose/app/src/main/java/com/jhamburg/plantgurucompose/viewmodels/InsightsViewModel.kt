package com.jhamburg.plantgurucompose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.models.CareScheduleResponse
import com.jhamburg.plantgurucompose.models.HealthDiagnosticsResponse
import com.jhamburg.plantgurucompose.models.PlantRecommendationsResponse
import com.jhamburg.plantgurucompose.models.SensorHealthResponse
import com.jhamburg.plantgurucompose.repository.InsightsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val insightsRepository: InsightsRepository
) : ViewModel() {
    private val _sensorHealthStates =
        mutableMapOf<String, MutableStateFlow<Result<SensorHealthResponse>?>>(
            "ext_temp" to MutableStateFlow(null),
            "soil_temp" to MutableStateFlow(null),
            "humidity" to MutableStateFlow(null),
            "soil_moisture_1" to MutableStateFlow(null),
            "light" to MutableStateFlow(null)
        )
    val sensorHealthStates: Map<String, StateFlow<Result<SensorHealthResponse>?>> =
        _sensorHealthStates

    private val _recommendations = MutableStateFlow<Result<PlantRecommendationsResponse>?>(null)
    val recommendations: StateFlow<Result<PlantRecommendationsResponse>?> = _recommendations

    private val _healthDiagnostics = MutableStateFlow<Result<HealthDiagnosticsResponse>?>(null)
    val healthDiagnostics: StateFlow<Result<HealthDiagnosticsResponse>?> = _healthDiagnostics

    private val _careSchedule = MutableStateFlow<Result<CareScheduleResponse>?>(null)
    val careSchedule: StateFlow<Result<CareScheduleResponse>?> = _careSchedule

    private val _loading = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val loading: StateFlow<Map<String, Boolean>> = _loading

    private fun setLoading(key: String, isLoading: Boolean) {
        _loading.value = _loading.value.toMutableMap().apply {
            put(key, isLoading)
        }
    }

    fun getSensorHealth(plantId: Int, sensorType: String) {
        viewModelScope.launch {
            try {
                setLoading("sensorHealth_$sensorType", true)
                val response = insightsRepository.getSensorHealth(plantId, sensorType)
                _sensorHealthStates[sensorType]?.value = Result.success(response)
            } catch (e: Exception) {
                _sensorHealthStates[sensorType]?.value = Result.failure(e)
            } finally {
                setLoading("sensorHealth_$sensorType", false)
            }
        }
    }

    fun getPlantRecommendations(plantId: Int) {
        viewModelScope.launch {
            try {
                setLoading("recommendations", true)
                val response = insightsRepository.getPlantRecommendations(plantId)
                _recommendations.value = Result.success(response)
            } catch (e: Exception) {
                _recommendations.value = Result.failure(e)
            } finally {
                setLoading("recommendations", false)
            }
        }
    }

    fun getHealthDiagnostics(plantId: Int) {
        viewModelScope.launch {
            try {
                setLoading("healthDiagnostics", true)
                val response = insightsRepository.getHealthDiagnostics(plantId)
                _healthDiagnostics.value = Result.success(response)
            } catch (e: Exception) {
                _healthDiagnostics.value = Result.failure(e)
            } finally {
                setLoading("healthDiagnostics", false)
            }
        }
    }

    fun getCareSchedule(plantId: Int) {
        viewModelScope.launch {
            try {
                setLoading("careSchedule", true)
                val response = insightsRepository.getCareSchedule(plantId)
                _careSchedule.value = Result.success(response)
            } catch (e: Exception) {
                _careSchedule.value = Result.failure(e)
            } finally {
                setLoading("careSchedule", false)
            }
        }
    }

    // Helper extension to check if specific operation is loading
    fun isLoading(key: String): Boolean = loading.value[key] ?: false
} 