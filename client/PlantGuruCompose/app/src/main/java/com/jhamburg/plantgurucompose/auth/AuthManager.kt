package com.jhamburg.plantgurucompose.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import android.util.Log
import com.jhamburg.plantgurucompose.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "auth_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        Log.d("AuthManager", "Saving new auth token")
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        val token = prefs.getString(KEY_AUTH_TOKEN, null)
        Log.d("AuthManager", "Retrieved auth token: ${if (token != null) "present" else "null"}")
        return token
    }

    fun saveUser(user: User) {
        Log.d("AuthManager", "Saving user data - ID: ${user.userId}, Name: ${user.name}")
        with(prefs.edit()) {
            putInt(KEY_USER_ID, user.userId)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ADDRESS, user.address)
            putString(KEY_USER_PHONE, user.phoneNumber)
            apply()
        }
    }

    fun getCurrentUser(): User? {
        val userId = prefs.getInt(KEY_USER_ID, -1)
        if (userId == -1) {
            Log.d("AuthManager", "No current user found")
            return null
        }

        val user = User(
            userId = userId,
            name = prefs.getString(KEY_USER_NAME, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            password = "",
            address = prefs.getString(KEY_USER_ADDRESS, null),
            phoneNumber = prefs.getString(KEY_USER_PHONE, null)
        )
        Log.d("AuthManager", "Retrieved current user - ID: ${user.userId}, Name: ${user.name}")
        return user
    }

    fun getCurrentUserId(): Int {
        val userId = prefs.getInt(KEY_USER_ID, -1)
        Log.d("AuthManager", "Retrieved current user ID: $userId")
        return userId
    }

    fun clearAuth() {
        Log.d("AuthManager", "Clearing all authentication data")
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = getAuthToken() != null && getCurrentUserId() != -1
        Log.d("AuthManager", "Checking login status: $loggedIn")
        return loggedIn
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ADDRESS = "user_address"
        private const val KEY_USER_PHONE = "user_phone"
    }
} 