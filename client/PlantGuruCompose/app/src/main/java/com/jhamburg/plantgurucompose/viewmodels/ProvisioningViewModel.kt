package com.jhamburg.plantgurucompose.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.repository.PlantProvisioningRepository
import com.jhamburg.plantgurucompose.repository.PlantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    private val plantProvisioningRepository: PlantProvisioningRepository,
    private val plantRepository: PlantRepository
) : ViewModel() {
    private val _provisioningState = MutableStateFlow<ProvisioningState>(ProvisioningState.Initial)
    val provisioningState: StateFlow<ProvisioningState> = _provisioningState

    private val _provisionToken = MutableStateFlow<String?>(null)
    val provisionToken: StateFlow<String?> = _provisionToken

    private var plantDetails: PlantDetails? = null
    private var pollingJob: Job? = null

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    suspend fun getProvisioningToken(plantId: Int, maxRetries: Int = 3) {
        var retryCount = 0
        var lastException: Exception? = null

        while (retryCount < maxRetries) {
            try {
                Log.d(
                    "ProvisioningViewModel",
                    "Requesting token for plant $plantId (attempt ${retryCount + 1}/$maxRetries)"
                )
                val response = plantProvisioningRepository.getProvisioningToken(plantId)
                Log.d("ProvisioningViewModel", "Got token response: ${response.provision_token}")

                if (response.provision_token.isBlank()) {
                    throw Exception("Received empty provision token")
                }

                setProvisionToken(response.provision_token)
                return
            } catch (e: Exception) {
                lastException = e
                Log.e(
                    "ProvisioningViewModel",
                    "Error getting provision token (attempt ${retryCount + 1}/$maxRetries)",
                    e
                )
                retryCount++

                if (retryCount < maxRetries) {
                    delay(2000L * retryCount)
                    continue
                }
            }
        }

        // If we get here, all retries failed
        val errorMessage = when (lastException) {
            is java.net.SocketTimeoutException -> "Connection to server timed out. Please check your internet connection."
            is java.net.UnknownHostException -> "Could not reach server. Please check your internet connection."
            else -> "Failed to get provision token: ${lastException?.message}"
        }

        _provisioningState.value = ProvisioningState.Error(errorMessage)
        throw lastException ?: Exception("Failed to get provision token after $maxRetries attempts")
    }

    fun setProvisionToken(token: String) {
        Log.d("ProvisioningViewModel", "Setting provision token: $token")
        _provisionToken.value = token
        _provisioningState.value = ProvisioningState.ReadyForDevice
    }
}

sealed class ProvisioningState {
    object Initial : ProvisioningState()
    object Starting : ProvisioningState()
    object ReadyForDevice : ProvisioningState()
    data class InProgress(val status: String, val message: String) : ProvisioningState()
    data class Warning(val message: String) : ProvisioningState()
    data class Completed(val plantId: Int) : ProvisioningState()
    data class Error(val message: String) : ProvisioningState()
}

data class PlantDetails(
    val name: String,
    val age: Int,
    val additionalDetails: PlantAdditionalDetails?
) 