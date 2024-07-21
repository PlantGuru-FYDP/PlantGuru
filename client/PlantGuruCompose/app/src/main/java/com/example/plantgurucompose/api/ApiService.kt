package com.example.plantgurucompose.api

import com.example.plantgurucompose.models.*
import retrofit2.http.*

interface ApiService {

    @POST("api/signup")
    suspend fun signUp(@Body user: User): UserResponse

    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): UserResponse

    @POST("api/plantUpload")
    suspend fun plantUpload(@Body plant: PlantResponse): ApiResponse

    @GET("api/plantRead")
    suspend fun plantRead(@Query("user_id") userId: Int): List<PlantResponse>

    @GET("api/sensorReadSeries")
    suspend fun sensorReadSeries(
        @Query("plant_id") plantId: Int,
        @Query("time_stamp1") timeStamp1: String,
        @Query("time_stamp2") timeStamp2: String
    ): List<SensorData>

    @GET("api/lastNSensorReadings")
    suspend fun getLastNSensorReadings(
        @Query("plant_id") plantId: Int,
        @Query("n") n: Int
    ): SensorDataResponse

    @GET("api/wateringRead")
    suspend fun wateringRead(@Query("plant_id") plantId: Int): List<WateringEvent>
}

data class LoginRequest(val email: String, val password: String)

data class ApiResponse(val success: Boolean, val message: String)
