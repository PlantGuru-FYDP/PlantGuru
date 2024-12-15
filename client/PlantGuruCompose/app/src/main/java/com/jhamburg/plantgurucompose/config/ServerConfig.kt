package com.jhamburg.plantgurucompose.config

import com.jhamburg.plantgurucompose.BuildConfig

object ServerConfig {
    private const val PRODUCTION_URL = "http://52.14.140.110:3000/"
    private const val DEVELOPMENT_URL = "http://192.168.2.225:3000/"

    fun getBaseUrl(): String {
        val url = if (BuildConfig.DEBUG) {
            DEVELOPMENT_URL
        } else {
            PRODUCTION_URL
        }
        println("Using URL: $url, BuildConfig.DEBUG = ${BuildConfig.DEBUG}")
        return url
    }
} 