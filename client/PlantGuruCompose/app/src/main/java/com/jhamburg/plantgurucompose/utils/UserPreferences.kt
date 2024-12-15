package com.jhamburg.plantgurucompose.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jhamburg.plantgurucompose.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: User, token: String) {
        prefs.edit()
            .putString("user", gson.toJson(user))
            .putString("token", token)
            .apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString("user", null)
        return if (userJson != null) gson.fromJson(userJson, User::class.java) else null
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun clearUser() {
        prefs.edit().clear().apply()
    }
} 