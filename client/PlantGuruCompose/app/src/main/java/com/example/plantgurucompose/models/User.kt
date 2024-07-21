package com.example.plantgurucompose.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val email: String,
    val password: String,
    val address: String?,
    @SerializedName("phone_number") val phoneNumber: String?
)

data class UserResponse(
    val message: String,
    @SerializedName("user_id") val userId: Int?,
    val token: String?
)