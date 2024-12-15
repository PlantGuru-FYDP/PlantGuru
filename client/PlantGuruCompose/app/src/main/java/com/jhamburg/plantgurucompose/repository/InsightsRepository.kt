package com.jhamburg.plantgurucompose.repository

import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.models.CareScheduleResponse
import com.jhamburg.plantgurucompose.models.HealthDiagnosticsResponse
import com.jhamburg.plantgurucompose.models.PlantRecommendationsResponse
import com.jhamburg.plantgurucompose.models.SensorHealthResponse
import javax.inject.Inject

class InsightsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getSensorHealth(plantId: Int, sensorType: String): SensorHealthResponse {
        return apiService.getSensorHealth(plantId, sensorType)
    }

    suspend fun getPlantRecommendations(plantId: Int): PlantRecommendationsResponse {
        return apiService.getPlantRecommendations(plantId)
    }

    suspend fun getHealthDiagnostics(plantId: Int): HealthDiagnosticsResponse {
        return apiService.getHealthDiagnostics(plantId)
    }

    suspend fun getCareSchedule(plantId: Int): CareScheduleResponse {
        return apiService.getCareSchedule(plantId)
    }
} 