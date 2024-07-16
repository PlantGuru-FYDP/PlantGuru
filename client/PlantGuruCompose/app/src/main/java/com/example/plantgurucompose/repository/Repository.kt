package com.example.plantgurucompose.repository

import android.util.Log
import com.example.plantgurucompose.api.ApiResponse
import com.example.plantgurucompose.api.ApiService
import com.example.plantgurucompose.api.LoginRequest
import com.example.plantgurucompose.models.*


class UserRepository(private val apiService: ApiService) {
    suspend fun signUp(user: User): UserResponse {
        return try {
            val response = apiService.signUp(user)
            Log.d("UserRepository", "Sign up response: $response")
            response
        } catch (e: Exception) {
            Log.e("UserRepository", "Sign up error: ${e.message}")
            UserResponse("Error: ${e.message}", null, null)
        }
    }

    suspend fun login(email: String, password: String): UserResponse {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            Log.d("UserRepository", "Login response: $response")
            response
        } catch (e: Exception) {
            Log.e("UserRepository", "Login error: ${e.message}")
            UserResponse("Error: ${e.message}", null, null)
        }
    }
}

class PlantRepository(private val apiService: ApiService) {
    suspend fun uploadPlant(plant: PlantResponse): ApiResponse {
        return try {
            val response = apiService.plantUpload(plant)
            Log.d("PlantRepository", "Plant upload response: $response")
            response
        } catch (e: Exception) {
            Log.e("PlantRepository", "Plant upload error: ${e.message}")
            ApiResponse(false, "Error: ${e.message}")
        }
    }

    suspend fun getPlants(userId: Int): List<PlantResponse> {
        return try {
            val response = apiService.plantRead(userId)
            Log.d("PlantRepository", "Get plants response: $response")
            response
        } catch (e: Exception) {
            Log.e("PlantRepository", "Get plants error: ${e.message}")
            emptyList()
        }
    }
}

class SensorDataRepository(private val apiService: ApiService) {
    suspend fun getSensorDataSeries(plantId: Int, timeStamp1: String, timeStamp2: String): List<SensorData> {
        return try {
            val response = apiService.sensorReadSeries(plantId, timeStamp1, timeStamp2)
            Log.d("SensorDataRepository", "Get sensor data series response: $response")
            response
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Get sensor data series error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getLastNSensorReadings(plantId: Int, n: Int): List<SensorData> {
        return try {
            val response = apiService.getLastNSensorReadings(plantId, n)
            Log.d("SensorDataRepository", "Get last N sensor readings response: $response")
            response.result
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Get last N sensor readings error: ${e.message}")
            emptyList()
        }
    }
}

class WateringEventRepository(private val apiService: ApiService) {
    suspend fun getWateringEvents(plantId: Int): List<WateringEvent> {
        return try {
            val response = apiService.wateringRead(plantId)
            Log.d("WateringEventRepository", "Get watering events response: $response")
            response
        } catch (e: Exception) {
            Log.e("WateringEventRepository", "Get watering events error: ${e.message}")
            emptyList()
        }
    }
}
