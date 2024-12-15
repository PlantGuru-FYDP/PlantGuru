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
            Log.d("LocalStorageHelper", "Saving details for plant $plantId")
            Log.d("LocalStorageHelper", "Details dir exists: ${plantDetailsDir.exists()}")
            Log.d("LocalStorageHelper", "Images dir exists: ${plantImagesDir.exists()}")

            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            val jsonString = gson.toJson(details)
            detailsFile.writeText(jsonString)
            Log.d("LocalStorageHelper", "Saved JSON details to: ${detailsFile.absolutePath}")

            details.imageUri?.let { uri ->
                Log.d("LocalStorageHelper", "Processing image URI: $uri")
                val imageFile = File(plantImagesDir, "plant_${plantId}_image.jpg")
                Log.d("LocalStorageHelper", "Target image file: ${imageFile.absolutePath}")

                context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("LocalStorageHelper", "Successfully saved image")
            }
        } catch (e: Exception) {
            Log.e("LocalStorageHelper", "Error saving plant details: ${e.message}")
            Log.e("LocalStorageHelper", "Stack trace:", e)
            throw e
        }
    }

    fun getPlantDetails(plantId: Int): PlantAdditionalDetails? {
        return try {
            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            if (detailsFile.exists()) {
                val jsonString = detailsFile.readText()
                gson.fromJson(jsonString, PlantAdditionalDetails::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocalStorageHelper", "Error reading plant details: ${e.message}")
            null
        }
    }
} 