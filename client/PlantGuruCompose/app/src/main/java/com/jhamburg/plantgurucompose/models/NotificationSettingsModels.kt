package com.jhamburg.plantgurucompose.models

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.jhamburg.plantgurucompose.utils.BooleanSerializer

data class UserNotificationSettings(
    @SerializedName("setting_id")
    val settingId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("email_notifications")
    @JsonAdapter(BooleanSerializer::class)
    val emailNotifications: Boolean,
    @SerializedName("email_digests")
    @JsonAdapter(BooleanSerializer::class)
    val emailDigests: Boolean,
    @SerializedName("email")
    val email: String?,
    @SerializedName("digests_frequency")
    val digestsFrequency: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class PlantNotificationSettings(
    @SerializedName("ext_temp_notifications")
    val extTempNotifications: Boolean,
    @SerializedName("ext_temp_min")
    val extTempMin: Float?,
    @SerializedName("ext_temp_max")
    val extTempMax: Float?,
    @SerializedName("humidity_notifications")
    val humidityNotifications: Boolean,
    @SerializedName("humidity_min")
    val humidityMin: Float?,
    @SerializedName("humidity_max")
    val humidityMax: Float?,
    @SerializedName("light_notifications")
    val lightNotifications: Boolean,
    @SerializedName("light_min")
    val lightMin: Float?,
    @SerializedName("light_max")
    val lightMax: Float?,
    @SerializedName("soil_temp_notifications")
    val soilTempNotifications: Boolean,
    @SerializedName("soil_temp_min")
    val soilTempMin: Float?,
    @SerializedName("soil_temp_max")
    val soilTempMax: Float?,
    @SerializedName("soil_moisture_notifications")
    val soilMoistureNotifications: Boolean,
    @SerializedName("soil_moisture_min")
    val soilMoistureMin: Float?,
    @SerializedName("soil_moisture_max")
    val soilMoistureMax: Float?,
    @SerializedName("watering_reminder_enabled")
    val wateringReminderEnabled: Boolean,
    @SerializedName("watering_reminder_frequency")
    val wateringReminderFrequency: String,
    @SerializedName("watering_reminder_interval")
    val wateringReminderInterval: Int?,
    @SerializedName("watering_reminder_time")
    val wateringReminderTime: String?,
    @SerializedName("watering_event_notifications")
    val wateringEventNotifications: Boolean,
    @SerializedName("health_status_notifications")
    val healthStatusNotifications: Boolean,
    @SerializedName("health_check_frequency")
    val healthCheckFrequency: String,
    @SerializedName("critical_alerts_only")
    val criticalAlertsOnly: Boolean
)