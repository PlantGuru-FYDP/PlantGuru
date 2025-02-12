package com.jhamburg.plantgurucompose.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import java.io.File
import java.io.FileOutputStream

class LocalStorageHelper(private val context: Context) {
    private val gson = Gson()
    private val plantDetailsDir = File(context.filesDir, "plant_details")
    private val plantImagesDir = File(context.filesDir, "plant_images")

    init {
        plantDetailsDir.mkdirs()
        plantImagesDir.mkdirs()
    }

    fun savePlantDetails(plantId: Int, details: PlantAdditionalDetails) {
        try {
            Log.d("LocalStorageHelper", "Saving details for plant $plantId: $details")
            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            val jsonString = gson.toJson(details)
            detailsFile.writeText(jsonString)
            Log.d("LocalStorageHelper", "Saved JSON details to: ${detailsFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("LocalStorageHelper", "Error saving plant details", e)
            throw e
        }
    }

    fun getPlantDetails(plantId: Int): PlantAdditionalDetails? {
        return try {
            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            Log.d("LocalStorageHelper", "Looking for details at: ${detailsFile.absolutePath}")
            if (detailsFile.exists()) {
                val jsonString = detailsFile.readText()
                Log.d("LocalStorageHelper", "Found JSON details: $jsonString")
                gson.fromJson(jsonString, PlantAdditionalDetails::class.java)
            } else {
                Log.d("LocalStorageHelper", "No details file found for plant $plantId")
                null
            }
        } catch (e: Exception) {
            Log.e("LocalStorageHelper", "Error reading plant details", e)
            null
        }
    }
} 