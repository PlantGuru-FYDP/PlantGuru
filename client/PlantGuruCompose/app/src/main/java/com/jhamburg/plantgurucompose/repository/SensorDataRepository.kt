package com.jhamburg.plantgurucompose.repository

import android.util.Log
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.models.SensorAnalysis
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorStatsResult
import com.jhamburg.plantgurucompose.models.SensorTrendlineResult
import com.jhamburg.plantgurucompose.models.TimeSeriesData

class SensorDataRepository(private val apiService: ApiService) {
    suspend fun getSensorDataSeries(
        plantId: Int,
        timeStamp1: String,
        timeStamp2: String
    ): List<SensorData> {
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

    suspend fun getTimeSeriesData(
        plantId: Int,
        startTime: String,
        endTime: String,
        granularity: Int? = 0,
        sensorTypes: String? = null
    ): List<TimeSeriesData> {
        return try {
            Log.d(
                "SensorDataRepository",
                "Get time series data request: plantId=$plantId, startTime=$startTime, endTime=$endTime, granularity=$granularity, sensorTypes=$sensorTypes"
            )
            val response =
                apiService.getTimeSeriesData(plantId, startTime, endTime, granularity, sensorTypes)
            Log.d("SensorDataRepository", "Get time series data response: $response")
            response.result
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Get time series data error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getSensorAnalysis(
        plantId: Int,
        startTime: String,
        endTime: String,
        metrics: String? = null
    ): SensorAnalysis? {
        return try {
            val response = apiService.getSensorAnalysis(plantId, startTime, endTime, metrics)
            Log.d("SensorDataRepository", "Get sensor analysis response: $response")
            response.result
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Get sensor analysis error: ${e.message}")
            null
        }
    }

    suspend fun getSensorStats(
        plantId: Int,
        sensorType: String,
        startTime: String,
        endTime: String,
        removeOutliers: Boolean? = null,
        smoothData: Boolean? = null
    ): SensorStatsResult? {
        return try {
            val response = apiService.getSensorStats(
                plantId,
                sensorType,
                startTime,
                endTime,
                removeOutliers,
                smoothData
            )
            Log.d("SensorDataRepository", "Get sensor stats response: $response")
            response.result
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Get sensor stats error: ${e.message}")
            null
        }
    }

    suspend fun getSensorTrendline(
        plantId: Int,
        sensorType: String,
        startTime: String,
        endTime: String
    ): SensorTrendlineResult? {
        return try {
            val response = apiService.getSensorTrendline(plantId, sensorType, startTime, endTime)
            Log.d("SensorDataRepository", "Get sensor trendline response: $response")
            response.result
        } catch (e: Exception) {
            Log.e("SensorDataRepository", "Get sensor trendline error: ${e.message}")
            null
        }
    }

}