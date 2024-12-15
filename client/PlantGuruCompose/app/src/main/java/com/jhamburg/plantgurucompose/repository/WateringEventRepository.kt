package com.jhamburg.plantgurucompose.repository

import android.util.Log
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.models.WateringEvent

class WateringEventRepository(private val apiService: ApiService) {
    suspend fun getWateringEvents(plantId: Int): List<WateringEvent> {
        return try {
            val response = apiService.wateringRead(plantId)
            Log.d("WateringEventRepository", "Get watering events response: $response")
            if (response.isEmpty()) {
                Log.w("WateringEventRepository", "No watering events found for plant $plantId")
            }
            response
        } catch (e: Exception) {
            Log.e("WateringEventRepository", "Get watering events error: ${e.message}")
            throw e
        }
    }

    suspend fun getLastWateringEvent(plantId: Int): WateringEvent? {
        return try {
            val response = apiService.getLastWateringEvent(plantId)
            Log.d("WateringEventRepository", "Get last watering event response: $response")
            response
        } catch (e: Exception) {
            Log.e("WateringEventRepository", "Get last watering event error: ${e.message}")
            null
        }
    }

    suspend fun getWateringEventSeries(
        plantId: Int,
        startTime: String,
        endTime: String
    ): List<WateringEvent> {
        return try {
            val response = apiService.getWateringEventSeries(plantId, startTime, endTime)
            Log.d("WateringEventRepository", "Get watering events series response: $response")
            response.result ?: emptyList()
        } catch (e: Exception) {
            Log.e("WateringEventRepository", "Get watering events series error: ${e.message}")
            emptyList()
        }
    }
}