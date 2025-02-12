package com.jhamburg.plantgurucompose.viewmodels

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantResponse
import com.jhamburg.plantgurucompose.models.Prediction
import com.jhamburg.plantgurucompose.repository.PlantRepository
import com.jhamburg.plantgurucompose.repository.PredictionRepository
import com.jhamburg.plantgurucompose.utils.ImageStorageManager
import com.jhamburg.plantgurucompose.utils.LocalStorageHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class PlantViewModel @Inject constructor(
    private val repository: PlantRepository,
    private val predictionRepository: PredictionRepository,
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager
) : ViewModel() {

    private val staticPlantDetails = mapOf(
        1 to PlantAdditionalDetails(
            scientificName = "Spathiphyllum",
            plantType = "Peace Lily",
            createdOn = System.currentTimeMillis()
        ),
        2 to PlantAdditionalDetails(
            scientificName = "Ficus lyrata",
            plantType = "Fiddle Leaf Fig",
            createdOn = System.currentTimeMillis()
        )
    )

    private val localStorageHelper = LocalStorageHelper(context)
    private val imageStorageManager = ImageStorageManager(context)

    private val _plants = MutableStateFlow<List<PlantResponse>>(emptyList())
    val plants: StateFlow<List<PlantResponse>> = _plants

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _plantDetails = MutableStateFlow<Map<Int, PlantAdditionalDetails?>>(emptyMap())
    val plantDetails: StateFlow<Map<Int, PlantAdditionalDetails?>> = _plantDetails

    private val _prediction = MutableStateFlow<Prediction?>(null)
    val prediction: StateFlow<Prediction?> = _prediction

    private val _deletingPlants = MutableStateFlow<Set<Int>>(emptySet())
    val deletingPlants: StateFlow<Set<Int>> = _deletingPlants

    private val _userId = MutableStateFlow(authManager.getCurrentUserId())
    val userId: StateFlow<Int> = _userId

    private var initialLoadStarted = false

    private val updateMutex = Mutex()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _needsRefresh = MutableStateFlow(false)
    val needsRefresh: StateFlow<Boolean> = _needsRefresh

    fun isLoggedIn(): Boolean = authManager.isLoggedIn()

    fun getPlants(userId: Int, predict: Boolean = false, forceRefresh: Boolean = false) {
        Log.d("PlantViewModel", """
            getPlants called:
            - userId: $userId
            - forceRefresh: $forceRefresh 
            - predict: $predict
            - current plants: ${_plants.value.map { it.plantName }}
            - initialLoadStarted: $initialLoadStarted
            - loading: ${_loading.value}
            - isUpdating: ${_isUpdating.value}
        """.trimIndent())
        
        if (_loading.value) {
            Log.d("PlantViewModel", "Skipping getPlants call - already loading")
            return
        }

        viewModelScope.launch {
            try {
                _loading.value = true
                
                val currentPlants = _plants.value
                val currentDetails = _plantDetails.value
                
                val plants = repository.getPlants(userId)
                Log.d("PlantViewModel", "Received ${plants.size} plants from repository")
                
                if (plants.isNotEmpty()) {
                    _plants.value = plants
                    
                    val details = plants.associate { plant ->
                        val currentImageUri = imageStorageManager.getImageUri(plant.plantId)?.toString()
                        val localDetails = localStorageHelper.getPlantDetails(plant.plantId)?.let { details ->
                            details.copy(imageUri = currentImageUri)
                        }
                        val staticDetails = staticPlantDetails[plant.plantId]
                        plant.plantId to (localDetails ?: staticDetails)
                    }
                    
                    _plantDetails.value = details
                    
                    if (predict) {
                        plants.forEach { plant ->
                            predictNextWatering(plant.plantId)
                        }
                    }
                } else if (!forceRefresh) {
                    _plants.value = currentPlants
                    _plantDetails.value = currentDetails
                }
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Error loading plants", e)
                _error.value = e.message
            } finally {
                _loading.value = false
                if (!forceRefresh) {
                    initialLoadStarted = true
                }
            }
        }
    }

    private suspend fun refreshPlants(userId: Int, predict: Boolean = false, forceRefresh: Boolean = false) {
        try {
            val plants = repository.getPlants(userId)
            
            if (forceRefresh) {
                Log.d("PlantViewModel", "Force refresh: Clearing existing data")
                _plantDetails.value = emptyMap()
                initialLoadStarted = false
            }

            _plants.value = plants
            
            val details = plants.associate { plant ->
                // First get the image URI
                val currentImageUri = imageStorageManager.getImageUri(plant.plantId)?.also { uri ->
                    Log.d("PlantViewModel", "Retrieved URI for plant ${plant.plantId}: $uri")
                }?.toString()

                // Get existing details
                val existingDetails = localStorageHelper.getPlantDetails(plant.plantId)
                Log.d("PlantViewModel", "Existing details for plant ${plant.plantId}: $existingDetails")

                // Create or update details
                val updatedDetails = when {
                    // If we have an image but no details, create new details
                    currentImageUri != null && existingDetails == null -> {
                        Log.d("PlantViewModel", "Creating new details for plant ${plant.plantId} with URI: $currentImageUri")
                        PlantAdditionalDetails(
                            scientificName = "",
                            plantType = "",
                            createdOn = System.currentTimeMillis(),
                            description = "",
                            careInstructions = "",
                            imageUri = currentImageUri
                        ).also { newDetails ->
                            Log.d("PlantViewModel", "Created new details for plant ${plant.plantId}: $newDetails")
                            try {
                                localStorageHelper.savePlantDetails(plant.plantId, newDetails)
                                Log.d("PlantViewModel", "Successfully saved new details for plant ${plant.plantId}")
                                // Verify save
                                val verifiedDetails = localStorageHelper.getPlantDetails(plant.plantId)
                                Log.d("PlantViewModel", "Verified details after save for plant ${plant.plantId}: $verifiedDetails")
                            } catch (e: Exception) {
                                Log.e("PlantViewModel", "Error saving details for plant ${plant.plantId}", e)
                            }
                        }
                    }
                    // If we have existing details, update them with current image URI
                    existingDetails != null -> {
                        Log.d("PlantViewModel", "Updating existing details for plant ${plant.plantId}")
                        existingDetails.copy(imageUri = currentImageUri ?: existingDetails.imageUri).also { updatedDetails ->
                            try {
                                localStorageHelper.savePlantDetails(plant.plantId, updatedDetails)
                                Log.d("PlantViewModel", "Successfully updated details for plant ${plant.plantId}")
                            } catch (e: Exception) {
                                Log.e("PlantViewModel", "Error updating details for plant ${plant.plantId}", e)
                            }
                        }
                    }
                    // No image and no details
                    else -> {
                        Log.d("PlantViewModel", "No image or details for plant ${plant.plantId}")
                        null
                    }
                }

                plant.plantId to updatedDetails.also { details ->
                    Log.d("PlantViewModel", "Final details for plant ${plant.plantId}: $details")
                }
            }

            _plantDetails.value = details

            // Verify the update for debugging
            details.forEach { (plantId, plantDetails) ->
                Log.d("PlantViewModel", "Verifying final state - Plant $plantId: $plantDetails")
                if (plantDetails?.imageUri != null) {
                    val verifiedDetails = localStorageHelper.getPlantDetails(plantId)
                    Log.d("PlantViewModel", "Verified saved details from storage for plant $plantId: $verifiedDetails")
                }
            }

            if (predict) {
                plants.forEach { plant ->
                    predictNextWatering(plant.plantId)
                }
            }
        } catch (e: Exception) {
            Log.e("PlantViewModel", "Error refreshing plants", e)
            throw e
        }
    }

    fun predictNextWatering(plantId: Int) {
        viewModelScope.launch {
            try {
                _prediction.value = predictionRepository.predictNextWatering(plantId)
                Log.d("PlantViewModel", "Prediction loaded: ${_prediction.value}")
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Error loading prediction: ${e.message}")
            }
        }
    }

    suspend fun createPlant(
        userId: Int,
        plantName: String,
        lastWatered: LocalDateTime?,
        nextWateringTime: LocalDateTime?,
        additionalDetails: PlantAdditionalDetails? = null
    ): Pair<Int, String> {
        try {
            val response = repository.createPlant(
                userId = userId,
                plantName = plantName,
                age = 0,
                lastWatered = lastWatered ?: LocalDateTime.now(),
                nextWateringTime = nextWateringTime ?: LocalDateTime.now().plusDays(7),
                additionalDetails = additionalDetails
            )
            val plantId = response.plant_id ?: throw Exception("Plant creation failed: No plant ID returned")
            val provisionToken = response.provision_token
            return Pair(plantId, provisionToken)
        } catch (e: Exception) {
            Log.e("PlantViewModel", "Error creating plant: ${e.message}")
            throw e
        }
    }

    suspend fun savePlantAdditionalDetails(
        plantId: Int, 
        details: PlantAdditionalDetails,
        skipRefresh: Boolean = false
    ) {
        try {
            Log.d("PlantViewModel", "Starting to save details for plant $plantId")
            Log.d("PlantViewModel", "Original details: $details")

            // 1. Save image first and get its URI
            val finalImageUri = if (details.imageUri != null) {
                Log.d("PlantViewModel", "Saving new image: ${details.imageUri}")
                imageStorageManager.saveImage(Uri.parse(details.imageUri), plantId)?.also { savedUri ->
                    Log.d("PlantViewModel", "Image saved with URI: $savedUri")
                }?.toString()
            } else {
                // Keep existing image URI if no new image
                plantDetails.value[plantId]?.imageUri.also { uri ->
                    Log.d("PlantViewModel", "Keeping existing image URI: $uri")
                }
            }

            // 2. Create and save final details
            val finalDetails = PlantAdditionalDetails(
                scientificName = details.scientificName,
                plantType = details.plantType,
                createdOn = details.createdOn ?: System.currentTimeMillis(),
                description = details.description,
                careInstructions = details.careInstructions,
                imageUri = finalImageUri
            ).also { newDetails ->
                Log.d("PlantViewModel", "Created final details: $newDetails")
            }

            // 3. Save details immediately
            localStorageHelper.savePlantDetails(plantId, finalDetails)
            Log.d("PlantViewModel", "Saved details to storage")

            // 4. Update in-memory state
            _plantDetails.value = _plantDetails.value.toMutableMap().apply {
                put(plantId, finalDetails)
            }
            Log.d("PlantViewModel", "Updated in-memory state")

            // 5. Verify the update
            val savedDetails = localStorageHelper.getPlantDetails(plantId)
            Log.d("PlantViewModel", "Verified saved details: $savedDetails")

            if (!skipRefresh) {
                val userId = _userId.value
                if (userId != null) {
                    Log.d("PlantViewModel", "Triggering full refresh for user $userId")
                    getPlants(userId, forceRefresh = true)
                }
            }
        } catch (e: Exception) {
            Log.e("PlantViewModel", "Error in savePlantAdditionalDetails", e)
            throw e
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    suspend fun deletePlant(plantId: Int) {
        try {
            _deletingPlants.value = _deletingPlants.value + plantId
            repository.deletePlant(plantId)
            _plants.value = _plants.value.filter { it.plantId != plantId }
            _plantDetails.value = _plantDetails.value.toMutableMap().apply {
                remove(plantId)
            }
        } catch (e: Exception) {
            Log.e("PlantViewModel", "Error deleting plant: ${e.message}")
            throw e
        } finally {
            _deletingPlants.value = _deletingPlants.value - plantId
        }
    }

    fun getPlantDetails(plantId: Int) {
        viewModelScope.launch {
            try {
                Log.d("PlantViewModel", "Getting details for plantId: $plantId")
                
                // First check for existing details
                var details = localStorageHelper.getPlantDetails(plantId)
                Log.d("PlantViewModel", "Retrieved details from storage: $details")

                // If no details exist, check for image
                if (details == null) {
                    val imageUri = imageStorageManager.getImageUri(plantId)?.toString()
                    Log.d("PlantViewModel", "No details found, checking for image. Found URI: $imageUri")

                    if (imageUri != null) {
                        // Create new details with the image URI
                        details = PlantAdditionalDetails(
                            scientificName = "",
                            plantType = "",
                            createdOn = System.currentTimeMillis(),
                            description = "",
                            careInstructions = "",
                            imageUri = imageUri
                        ).also { newDetails ->
                            Log.d("PlantViewModel", "Created new details with image: $newDetails")
                            try {
                                localStorageHelper.savePlantDetails(plantId, newDetails)
                                Log.d("PlantViewModel", "Saved new details to storage")
                                
                                // Verify save
                                val verifiedDetails = localStorageHelper.getPlantDetails(plantId)
                                Log.d("PlantViewModel", "Verified saved details: $verifiedDetails")
                            } catch (e: Exception) {
                                Log.e("PlantViewModel", "Error saving new details", e)
                            }
                        }
                    } else {
                        // No image and no details, try static details
                        details = staticPlantDetails[plantId]
                        Log.d("PlantViewModel", "No image found, using static details: $details")
                    }
                }

                Log.d("PlantViewModel", "Final details to be set: $details")
                Log.d("PlantViewModel", "Image URI from details: ${details?.imageUri}")

                _plantDetails.value = _plantDetails.value.toMutableMap().apply {
                    put(plantId, details)
                }
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Error loading plant details", e)
            }
        }
    }

    suspend fun updatePlant(
        plantId: Int,
        plantName: String? = null,
        age: Int? = null,
        lastWatered: LocalDateTime? = null,
        nextWateringTime: LocalDateTime? = null,
        predict: Boolean = false
    ) {
        updateMutex.withLock {
            try {
                _isUpdating.value = true
                
                Log.d("PlantViewModel", """
                    Starting plant update:
                    - plantId: $plantId
                    - newName: $plantName
                    - currentPlants: ${_plants.value.map { it.plantName }}
                """.trimIndent())

                repository.updatePlant(
                    plantId = plantId,
                    plantName = plantName,
                    age = age,
                    lastWatered = lastWatered,
                    nextWateringTime = nextWateringTime
                )

                val currentPlants = _plants.value
                
                _userId.value?.let { userId ->
                    val plants = repository.getPlants(userId)
                    if (plants.isNotEmpty()) {
                        _plants.value = plants
                        val details = plants.associate { plant ->
                            val currentImageUri = imageStorageManager.getImageUri(plant.plantId)?.toString()
                            val localDetails = localStorageHelper.getPlantDetails(plant.plantId)?.let { details ->
                                details.copy(imageUri = currentImageUri)
                            }
                            val staticDetails = staticPlantDetails[plant.plantId]
                            plant.plantId to (localDetails ?: staticDetails)
                        }
                        _plantDetails.value = details
                    } else {
                        _plants.value = currentPlants
                    }
                }
            } catch (e: Exception) {
                Log.e("PlantViewModel", "Error updating plant: ${e.message}")
                throw e
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearPlants() {
        _plants.value = emptyList()
        _plantDetails.value = emptyMap()
    }

    fun setNeedsRefresh(value: Boolean) {
        _needsRefresh.value = value
    }

}