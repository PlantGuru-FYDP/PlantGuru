package com.jhamburg.plantgurucompose.api

import com.google.gson.GsonBuilder
import com.jhamburg.plantgurucompose.config.ServerConfig
import com.jhamburg.plantgurucompose.utils.BooleanSerializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.java, BooleanSerializer())
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ServerConfig.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
