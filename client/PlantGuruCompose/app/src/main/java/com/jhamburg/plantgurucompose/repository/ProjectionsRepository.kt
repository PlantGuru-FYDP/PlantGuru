package com.jhamburg.plantgurucompose.repository

import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.models.ProjectionsResponse
import javax.inject.Inject

class ProjectionsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getProjections(
        plantId: Int,
        sensorType: String,
        numPoints: Int,
        granularity: Int
    ): ProjectionsResponse {
        return apiService.getProjections(plantId, sensorType, numPoints, granularity)
    }
} 