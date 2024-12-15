package com.jhamburg.plantgurucompose.repository

import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.ProvisioningStatus
import com.jhamburg.plantgurucompose.models.ProvisioningStatusResponse
import com.jhamburg.plantgurucompose.models.ProvisioningStatusUpdate
import com.jhamburg.plantgurucompose.models.ProvisioningTokenResponse
import javax.inject.Inject

class ProvisioningRepository @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
    private fun getAuthHeader(): String {
        return "Bearer ${authManager.getAuthToken()}"
    }

    suspend fun getProvisioningToken(plantId: Int): ProvisioningTokenResponse {
        return apiService.getProvisioningToken(plantId, getAuthHeader())
    }

    suspend fun updateProvisioningStatus(
        provisionToken: String,
        deviceId: String?,
        status: String,
        plantName: String? = null,
        age: Int? = null
    ): ProvisioningStatusResponse {
        return apiService.updateProvisioningStatus(
            ProvisioningStatusUpdate(
                provision_token = provisionToken,
                device_id = deviceId,
                status = status,
                plant_name = plantName,
                age = age
            )
        )
    }

    suspend fun getProvisioningStatus(provisionToken: String): ProvisioningStatus {
        return apiService.getProvisioningStatus(provisionToken)
    }
} 