package com.jhamburg.plantgurucompose.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
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
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUser(user: User) {
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
        if (userId == -1) return null

        return User(
            userId = userId,
            name = prefs.getString(KEY_USER_NAME, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            password = "",
            address = prefs.getString(KEY_USER_ADDRESS, null),
            phoneNumber = prefs.getString(KEY_USER_PHONE, null)
        )
    }

    fun getCurrentUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun clearAuth() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null && getCurrentUserId() != -1
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