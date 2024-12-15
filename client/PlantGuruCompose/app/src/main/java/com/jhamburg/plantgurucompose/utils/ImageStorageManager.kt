package com.jhamburg.plantgurucompose.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageStorageManager(private val context: Context) {
    private val imagesDir = context.filesDir.resolve("images").apply { mkdirs() }

    fun saveImage(sourceUri: Uri, plantId: Int): Uri? {
        val timestamp = System.currentTimeMillis()
        val destinationFile = File(imagesDir, "plant_${plantId}_${timestamp}.jpg")

        return try {
            Log.d(
                "ImageStorageManager",
                "Saving image from $sourceUri to ${destinationFile.absolutePath}"
            )

            deleteImage(plantId)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (!destinationFile.exists() || destinationFile.length() == 0L) {
                Log.e("ImageStorageManager", "Failed to save image - file not created or empty")
                return null
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                destinationFile
            ).also { uri ->
                Log.d("ImageStorageManager", "Generated URI: $uri")
            }
        } catch (e: IOException) {
            Log.e("ImageStorageManager", "Failed to save image", e)
            null
        }
    }

    fun getImageUri(plantId: Int): Uri? {
        val imageFiles = imagesDir.listFiles { file ->
            file.name.startsWith("plant_${plantId}_") && file.name.endsWith(".jpg")
        }?.sortedByDescending { it.name }

        return imageFiles?.firstOrNull()?.let { imageFile ->
            try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                ).also { uri ->
                    Log.d("ImageStorageManager", "Retrieved URI for plant $plantId: $uri")
                }
            } catch (e: IllegalArgumentException) {
                Log.e("ImageStorageManager", "Failed to generate URI for file", e)
                null
            }
        }.also { uri ->
            if (uri == null) {
                Log.d("ImageStorageManager", "No image file exists for plant $plantId")
            }
        }
    }

    fun deleteImage(plantId: Int) {
        imagesDir.listFiles { file ->
            file.name.startsWith("plant_${plantId}_") && file.name.endsWith(".jpg")
        }?.forEach { file ->
            if (file.exists()) {
                file.delete()
                Log.d("ImageStorageManager", "Deleted image: ${file.name}")
            }
        }
    }
} 