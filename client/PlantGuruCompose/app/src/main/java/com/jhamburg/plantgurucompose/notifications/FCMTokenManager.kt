package com.jhamburg.plantgurucompose.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.RegisterDeviceRequest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManager @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
    suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }

    suspend fun registerTokenWithBackend(fcmToken: String): Boolean {
        return try {
            val token = authManager.getAuthToken()
            if (token == null) {
                Log.e(TAG, "No auth token available")
                return false
            }

            val response = apiService.registerDevice(
                RegisterDeviceRequest(fcmToken),
                "Bearer $token"
            )
            true
        } catch (e: Exception) {
            if (e is retrofit2.HttpException && e.code() == 500 &&
                e.message?.contains("Duplicate entry", ignoreCase = true) == true
            ) {
                Log.i(TAG, "Token already registered")
                return true
            }
            Log.e(TAG, "Error registering FCM token with backend", e)
            false
        }
    }

    suspend fun deleteToken() {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
        } catch (e: Exception) {
            println("Error deleting FCM token: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "FCMTokenManager"
    }
} 