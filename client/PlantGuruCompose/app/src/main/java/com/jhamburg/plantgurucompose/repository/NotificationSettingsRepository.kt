package com.jhamburg.plantgurucompose.repository

import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.PlantNotificationSettings
import com.jhamburg.plantgurucompose.models.UserNotificationSettings
import javax.inject.Inject

class NotificationSettingsRepository @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
    private fun getAuthHeader(): String {
        return "Bearer ${authManager.getAuthToken()}"
    }

    suspend fun getUserSettings(): UserNotificationSettings {
        return apiService.getUserNotificationSettings(getAuthHeader())
    }

    suspend fun updateUserSettings(settings: UserNotificationSettings) {
        apiService.updateUserNotificationSettings(settings, getAuthHeader())
    }

    suspend fun getPlantSettings(plantId: Int): PlantNotificationSettings {
        return apiService.getPlantNotificationSettings(plantId, getAuthHeader())
    }

    suspend fun updatePlantSettings(plantId: Int, settings: PlantNotificationSettings) {
        apiService.updatePlantNotificationSettings(plantId, settings, getAuthHeader())
    }
} 