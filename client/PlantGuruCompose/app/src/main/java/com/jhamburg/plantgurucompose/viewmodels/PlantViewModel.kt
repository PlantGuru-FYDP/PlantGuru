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
                val currentImageUri = imageStorageManager.getImageUri(plant.plantId)?.toString()

                val localDetails =
                    localStorageHelper.getPlantDetails(plant.plantId)?.let { details ->
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
        } catch (e: Exception) {
            Log.e("PlantViewModel", "Error refreshing plants: ${e.message}")
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
        age: Int,
        lastWatered: LocalDateTime?,
        nextWateringTime: LocalDateTime?
    ): Pair<Int, String> {
        try {
            val response = repository.createPlant(
                userId = userId,
                plantName = plantName,
                age = age,
                lastWatered = lastWatered ?: LocalDateTime.now(),
                nextWateringTime = nextWateringTime ?: LocalDateTime.now().plusDays(7)
            )
            val plantId =
                response.plant_id ?: throw Exception("Plant creation failed: No plant ID returned")
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

            val finalImageUri = if (details.imageUri != null && details.imageUri != plantDetails.value[plantId]?.imageUri) {
                Log.d("PlantViewModel", "Saving new image: ${details.imageUri}")
                imageStorageManager.saveImage(Uri.parse(details.imageUri), plantId)?.also { savedUri ->
                    Log.d("PlantViewModel", "Image saved with URI: $savedUri")
                }?.toString()
            } else {
                plantDetails.value[plantId]?.imageUri
            }

            // 2. Create and save final details
            val finalDetails = details.copy(imageUri = finalImageUri)
            Log.d("PlantViewModel", "Saving final details: $finalDetails")
            localStorageHelper.savePlantDetails(plantId, finalDetails)

            // 3. Update in-memory state
            _plantDetails.value = _plantDetails.value.toMutableMap().apply {
                put(plantId, finalDetails)
            }

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
                val details = localStorageHelper.getPlantDetails(plantId)
                    ?: staticPlantDetails[plantId]
                Log.d("PlantViewModel", "Retrieved details: $details")
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

}