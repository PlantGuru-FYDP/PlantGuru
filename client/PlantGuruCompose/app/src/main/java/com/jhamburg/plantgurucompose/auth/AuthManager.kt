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
    private var currentAuthToken: String? = null
    private var currentUser: User? = null

    fun saveAuthToken(token: String) {
        Log.d("AuthManager", "Saving new auth token")
        currentAuthToken = token
    }

    fun getAuthToken(): String? {
        return currentAuthToken
    }

    fun saveUser(user: User) {
        Log.d("AuthManager", "Saving user data - ID: ${user.userId}, Name: ${user.name}")
        currentUser = user
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun getCurrentUserId(): Int {
        return currentUser?.userId ?: -1
    }

    fun clearAuth() {
        Log.d("AuthManager", "Clearing all authentication data")
        currentAuthToken = null
        currentUser = null
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = currentAuthToken != null && currentUser != null
        Log.d("AuthManager", "Checking login status: $loggedIn")
        return loggedIn
    }
} 