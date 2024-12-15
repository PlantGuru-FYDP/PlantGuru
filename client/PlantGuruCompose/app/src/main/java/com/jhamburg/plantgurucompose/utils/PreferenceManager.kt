package com.jhamburg.plantgurucompose.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager

object PreferenceManager {
    private const val PREF_NAME = "plant_guru_preferences"
    private const val KEY_24_HOUR_FORMAT = "24_hour_format"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun set24HourFormat(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_24_HOUR_FORMAT, enabled).apply()
    }

    fun is24HourFormat(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_24_HOUR_FORMAT, true)
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        if (!enabled) {
            WorkManager.getInstance(context).cancelAllWork()
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
} 