package com.jhamburg.plantgurucompose.repository

import android.util.Log
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.models.Prediction

class PredictionRepository(private val apiService: ApiService) {
    suspend fun predictNextWatering(plantId: Int): Prediction {
        return try {
            val response = apiService.predictNextWatering(plantId)
            Log.d("PredictionRepository", "Predict next watering response: $response")
            response
        } catch (e: Exception) {
            Log.e("PredictionRepository", "Predict next watering error: ${e.message}")
            Prediction(
                0f,
            )
        }
    }
}
