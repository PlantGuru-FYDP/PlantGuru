package com.jhamburg.plantgurucompose.api

import com.jhamburg.plantgurucompose.models.CareScheduleResponse
import com.jhamburg.plantgurucompose.models.HealthDiagnosticsResponse
import com.jhamburg.plantgurucompose.models.PlantCreateRequest
import com.jhamburg.plantgurucompose.models.PlantCreateResponse
import com.jhamburg.plantgurucompose.models.PlantNotificationSettings
import com.jhamburg.plantgurucompose.models.PlantRecommendationsResponse
import com.jhamburg.plantgurucompose.models.PlantResponse
import com.jhamburg.plantgurucompose.models.Prediction
import com.jhamburg.plantgurucompose.models.ProjectionsResponse
import com.jhamburg.plantgurucompose.models.ProvisioningStatus
import com.jhamburg.plantgurucompose.models.ProvisioningStatusResponse
import com.jhamburg.plantgurucompose.models.ProvisioningStatusUpdate
import com.jhamburg.plantgurucompose.models.ProvisioningTokenResponse
import com.jhamburg.plantgurucompose.models.ProvisioningVerifyRequest
import com.jhamburg.plantgurucompose.models.RegisterDeviceRequest
import com.jhamburg.plantgurucompose.models.SensorAnalysisResponse
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorDataResponse
import com.jhamburg.plantgurucompose.models.SensorHealthResponse
import com.jhamburg.plantgurucompose.models.SensorStatsResponse
import com.jhamburg.plantgurucompose.models.SensorTrendlineResponse
import com.jhamburg.plantgurucompose.models.TimeSeriesResponse
import com.jhamburg.plantgurucompose.models.User
import com.jhamburg.plantgurucompose.models.UserNotificationSettings
import com.jhamburg.plantgurucompose.models.UserResponse
import com.jhamburg.plantgurucompose.models.WateringEvent
import com.jhamburg.plantgurucompose.models.WateringEventResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/signup")
    suspend fun signUp(@Body user: User): UserResponse

    @POST("api/login")
    suspend fun login(@Body loginRequest: LoginRequest): UserResponse

    @POST("api/plantUpload")
    suspend fun plantUpload(@Body plant: PlantCreateRequest): PlantCreateResponse

    @GET("api/plantRead")
    suspend fun plantRead(
        @Query("user_id") userId: Int,
        @Query("include_sensors") includeSensors: Boolean = true
    ): List<PlantResponse>

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

    @GET("api/model")
    suspend fun predictNextWatering(@Query("plant_id") plantId: Int): Prediction

    @PUT("api/updateUser")
    suspend fun updateUser(
        @Body user: User,
        @Header("Authorization") authorization: String
    ): UserResponse

    @GET("api/timeSeriesData")
    suspend fun getTimeSeriesData(
        @Query("plant_id") plantId: Int,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String,
        @Query("granularity") granularity: Int? = 0,
        @Query("sensor_types") sensorTypes: String? = null
    ): TimeSeriesResponse

    /*
        @GET("api/analysis")
        suspend fun getSensorAnalysis(
            @Query("plant_id") plantId: Int,
            @Query("start_time") startTime: String,
            @Query("end_time") endTime: String,
            @Query("metrics") metrics: String? = null
        ): SensorAnalysisResponse
    */
    @POST("api/plantUpload")
    suspend fun createPlant(@Body request: PlantCreateRequest): PlantCreateResponse

    @DELETE("api/deletePlant")
    suspend fun deletePlant(@Query("plant_id") plantId: Int)

    @GET("api/lastWateringEvent")
    suspend fun getLastWateringEvent(@Query("plant_id") plantId: Int): WateringEvent?

    @GET("api/wateringReadSeries")
    suspend fun getWateringEventSeries(
        @Query("plant_id") plantId: Int,
        @Query("time_stamp1") startTime: String,
        @Query("time_stamp2") endTime: String
    ): WateringEventResponse

    @POST("api/register-device")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest,
        @Header("Authorization") authorization: String
    ): ApiResponse

    @GET("api/sensorAnalysis")
    suspend fun getSensorAnalysis(
        @Query("plant_id") plantId: Int,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String,
        @Query("metrics") metrics: String?
    ): SensorAnalysisResponse

    @GET("api/sensorStats")
    suspend fun getSensorStats(
        @Query("plant_id") plantId: Int,
        @Query("sensor_type") sensorType: String,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String,
        @Query("remove_outliers") removeOutliers: Boolean?,
        @Query("smooth_data") smoothData: Boolean?
    ): SensorStatsResponse

    @GET("api/sensorTrendline")
    suspend fun getSensorTrendline(
        @Query("plant_id") plantId: Int,
        @Query("sensor_type") sensorType: String,
        @Query("start_time") startTime: String,
        @Query("end_time") endTime: String
    ): SensorTrendlineResponse

    @GET("api/sensorHealth")
    suspend fun getSensorHealth(
        @Query("plant_id") plantId: Int,
        @Query("sensor_type") sensorType: String
    ): SensorHealthResponse

    @GET("api/plantRecommendations")
    suspend fun getPlantRecommendations(
        @Query("plant_id") plantId: Int
    ): PlantRecommendationsResponse

    @GET("api/healthDiagnostics")
    suspend fun getHealthDiagnostics(
        @Query("plant_id") plantId: Int
    ): HealthDiagnosticsResponse

    @GET("api/careSchedule")
    suspend fun getCareSchedule(
        @Query("plant_id") plantId: Int
    ): CareScheduleResponse

    @GET("api/projections")
    suspend fun getProjections(
        @Query("plant_id") plantId: Int,
        @Query("sensor_type") sensorType: String,
        @Query("num_points") numPoints: Int,
        @Query("granularity") granularity: Int
    ): ProjectionsResponse

    @GET("api/user-notifications-settings")
    suspend fun getUserNotificationSettings(
        @Header("Authorization") authorization: String
    ): UserNotificationSettings

    @PUT("api/user-notifications-settings")
    suspend fun updateUserNotificationSettings(
        @Body settings: UserNotificationSettings,
        @Header("Authorization") authorization: String
    ): ApiResponse

    @GET("api/plant-notifications-settings/{plantId}")
    suspend fun getPlantNotificationSettings(
        @Path("plantId") plantId: Int,
        @Header("Authorization") authorization: String
    ): PlantNotificationSettings

    @PUT("api/plant-notifications-settings/{plantId}")
    suspend fun updatePlantNotificationSettings(
        @Path("plantId") plantId: Int,
        @Body settings: PlantNotificationSettings,
        @Header("Authorization") authorization: String
    ): ApiResponse

    @POST("api/provision/status")
    suspend fun updateProvisioningStatus(
        @Body request: ProvisioningStatusUpdate
    ): ProvisioningStatusResponse

    @GET("api/provision/{provisionToken}")
    suspend fun getProvisioningStatus(
        @Path("provisionToken") provisionToken: String
    ): ProvisioningStatus

    @POST("api/provision/verify")
    suspend fun verifyDeviceConnection(
        @Body request: ProvisioningVerifyRequest
    ): ProvisioningStatusResponse

    @GET("api/provision/token/{plantId}")
    suspend fun getProvisioningToken(
        @Path("plantId") plantId: Int,
        @Header("Authorization") authorization: String
    ): ProvisioningTokenResponse

    @PUT("api/updatePlant")
    suspend fun updatePlant(
        @Body request: UpdatePlantRequest
    ): ApiResponse
}

data class LoginRequest(val email: String, val password: String)

data class ApiResponse(val message: String)

data class UpdatePlantNameRequest(
    val plant_id: Int,
    val plant_name: String
)

data class UpdatePlantRequest(
    val plant_id: Int,
    val plant_name: String? = null,
    val age: Int? = null,
    val last_watered: String? = null,
    val next_watering_time: String? = null
)

