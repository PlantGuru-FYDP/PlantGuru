package com.jhamburg.plantgurucompose.repository

import android.content.Context
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PlantProvisioningRepository @Inject constructor(
    private val apiService: ApiService,
    private val plantRepository: PlantRepository,
    @ApplicationContext private val context: Context,
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

    suspend fun verifyDeviceConnection(
        provisionToken: String,
        deviceId: String
    ): ProvisioningStatusResponse {
        return apiService.verifyDeviceConnection(
            ProvisioningVerifyRequest(
                provision_token = provisionToken,
                device_id = deviceId
            )
        )
    }

    suspend fun savePlantAdditionalDetails(plantId: Int, details: PlantAdditionalDetails) {
        plantRepository.savePlantAdditionalDetails(plantId, details)
    }
} 