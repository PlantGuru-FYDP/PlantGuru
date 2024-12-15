package com.jhamburg.plantgurucompose.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.jhamburg.plantgurucompose.api.ApiService
import com.jhamburg.plantgurucompose.api.UpdatePlantRequest
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantCreateRequest
import com.jhamburg.plantgurucompose.models.PlantCreateResponse
import com.jhamburg.plantgurucompose.models.PlantResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import javax.inject.Inject

class PlantRepository @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) {
    private val plantDetailsDir = File(context.filesDir, "plant_details").apply { mkdirs() }
    private val plantImagesDir = File(context.filesDir, "plant_images").apply { mkdirs() }

    suspend fun createPlant(
        userId: Int,
        plantName: String,
        age: Int,
        lastWatered: LocalDateTime,
        nextWateringTime: LocalDateTime,
        additionalDetails: PlantAdditionalDetails? = null
    ): PlantCreateResponse {
        try {
            val request = PlantCreateRequest(
                userId = userId,
                plantName = plantName,
                age = age,
                lastWatered = lastWatered.toString(),
                nextWateringTime = nextWateringTime.toString()
            )
            val response = apiService.createPlant(request)

            response.plant_id?.let { plantId ->
                additionalDetails?.let {
                    savePlantAdditionalDetails(plantId, it)
                }
            }

            return response
        } catch (e: Exception) {
            Log.e("PlantRepository", "Plant creation error: ${e.message}")
            throw e
        }
    }

    suspend fun getPlants(userId: Int): List<PlantResponse> {
        return try {
            Log.d("PlantRepository", "Fetching plants with sensors for user: $userId")
            val response = apiService.plantRead(userId, includeSensors = true)
            Log.d("PlantRepository", "Received plants with sensors: $response")
            response.map { plant ->
                getPlantAdditionalDetails(plant.plantId)
                plant
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Get plants error: ${e.message}")
            emptyList()
        }
    }

    suspend fun savePlantAdditionalDetails(plantId: Int, details: PlantAdditionalDetails) {
        try {
            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            val jsonObject = JSONObject().apply {
                put("scientificName", details.scientificName)
                put("plantType", details.plantType)
                put("createdOn", details.createdOn)
                put("description", details.description ?: "")
                put("careInstructions", details.careInstructions ?: "")
                put("imageUri", details.imageUri ?: "")
            }
            detailsFile.writeText(jsonObject.toString())

            details.imageUri?.let { uri ->
                val imageFile = File(plantImagesDir, "plant_${plantId}_image.jpg")
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error saving plant details: ${e.message}")
            throw e
        }
    }

    suspend fun getPlantAdditionalDetails(plantId: Int): PlantAdditionalDetails? {
        return try {
            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            if (detailsFile.exists()) {
                val jsonObject = JSONObject(detailsFile.readText())
                val storedImageUri = jsonObject.getString("imageUri")

                val imageUri = if (storedImageUri.isNotEmpty()) {
                    val imageFile = File(plantImagesDir, "plant_${plantId}_image.jpg")
                    if (imageFile.exists()) {
                        try {
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                imageFile
                            ).toString().also {
                                Log.d("PlantRepository", "Generated FileProvider URI: $it")
                            }
                        } catch (e: Exception) {
                            Log.e("PlantRepository", "Error generating FileProvider URI", e)
                            null
                        }
                    } else {
                        Log.d(
                            "PlantRepository",
                            "Image file does not exist: ${imageFile.absolutePath}"
                        )
                        null
                    }
                } else null

                PlantAdditionalDetails(
                    scientificName = jsonObject.getString("scientificName"),
                    plantType = jsonObject.getString("plantType"),
                    createdOn = jsonObject.getLong("createdOn"),
                    description = jsonObject.getString("description").takeIf { it.isNotEmpty() },
                    careInstructions = jsonObject.getString("careInstructions")
                        .takeIf { it.isNotEmpty() },
                    imageUri = imageUri
                )
            } else null
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error getting plant details", e)
            null
        }
    }

    suspend fun deletePlant(plantId: Int) {
        try {
            apiService.deletePlant(plantId)

            // Clean up local storage
            val detailsFile = File(plantDetailsDir, "plant_${plantId}_details.json")
            val imageFile = File(plantImagesDir, "plant_${plantId}_image.jpg")

            detailsFile.delete()
            imageFile.delete()

            Log.d("PlantRepository", "Successfully deleted plant $plantId")
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error deleting plant: ${e.message}")
            throw e
        }
    }

    suspend fun updatePlant(
        plantId: Int,
        plantName: String? = null,
        age: Int? = null,
        lastWatered: LocalDateTime? = null,
        nextWateringTime: LocalDateTime? = null
    ) {
        try {
            apiService.updatePlant(
                UpdatePlantRequest(
                    plant_id = plantId,
                    plant_name = plantName,
                    age = age,
                    last_watered = lastWatered?.toString(),
                    next_watering_time = nextWateringTime?.toString()
                )
            )
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error updating plant: ${e.message}")
            throw e
        }
    }
} 