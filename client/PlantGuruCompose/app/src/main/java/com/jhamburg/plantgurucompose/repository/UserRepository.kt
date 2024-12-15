package com.jhamburg.plantgurucompose.repository

import android.util.Log
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.api.LoginRequest
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.User
import com.jhamburg.plantgurucompose.models.UserResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val authManager: AuthManager
) {
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

    suspend fun updateUser(user: User): UserResponse {
        return try {
            val token = authManager.getAuthToken()
            if (token == null) {
                throw Exception("Not authenticated")
            }
            apiService.updateUser(user, "Bearer $token")
        } catch (e: Exception) {
            UserResponse("Error: ${e.message}", null, null)
        }
    }
} 