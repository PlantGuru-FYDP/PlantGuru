package com.jhamburg.plantgurucompose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.models.PlantNotificationSettings
import com.jhamburg.plantgurucompose.models.UserNotificationSettings
import com.jhamburg.plantgurucompose.repository.NotificationSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val repository: NotificationSettingsRepository
) : ViewModel() {

    private val _userSettings = MutableStateFlow<UserNotificationSettings?>(null)
    val userSettings: StateFlow<UserNotificationSettings?> = _userSettings

    private val _plantSettings = MutableStateFlow<PlantNotificationSettings?>(null)
    val plantSettings: StateFlow<PlantNotificationSettings?> = _plantSettings

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveStatus = MutableStateFlow<SaveStatus?>(null)
    val saveStatus: StateFlow<SaveStatus?> = _saveStatus

    sealed class SaveStatus {
        object Success : SaveStatus()
        data class Error(val message: String) : SaveStatus()
    }

    fun loadUserSettings() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val settings = repository.getUserSettings()
                _userSettings.value = settings
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateUserSettings(settings: UserNotificationSettings) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val convertedSettings = settings.copy(
                    emailNotifications = settings.emailNotifications,
                    emailDigests = settings.emailDigests,
                    email = settings.email,
                    digestsFrequency = settings.digestsFrequency
                )
                repository.updateUserSettings(convertedSettings)
                _userSettings.value = settings
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Unknown error occurred")
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPlantSettings(plantId: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val settings = repository.getPlantSettings(plantId)
                _plantSettings.value = settings
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updatePlantSettings(plantId: Int, settings: PlantNotificationSettings) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val convertedSettings = settings.copy(
                    extTempNotifications = settings.extTempNotifications,
                    humidityNotifications = settings.humidityNotifications,
                    wateringReminderEnabled = settings.wateringReminderEnabled,
                    healthStatusNotifications = settings.healthStatusNotifications,
                    criticalAlertsOnly = settings.criticalAlertsOnly,
                    extTempMin = settings.extTempMin,
                    extTempMax = settings.extTempMax,
                    humidityMin = settings.humidityMin,
                    humidityMax = settings.humidityMax,
                    wateringReminderFrequency = settings.wateringReminderFrequency,
                    wateringReminderInterval = settings.wateringReminderInterval,
                    wateringReminderTime = settings.wateringReminderTime,
                    healthCheckFrequency = settings.healthCheckFrequency
                )
                repository.updatePlantSettings(plantId, convertedSettings)
                _plantSettings.value = settings
                _saveStatus.value = SaveStatus.Success
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Unknown error occurred")
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSaveStatus() {
        _saveStatus.value = null
    }
} 