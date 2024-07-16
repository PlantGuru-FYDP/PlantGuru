package com.example.plantgurucompose.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plantgurucompose.models.*
import com.example.plantgurucompose.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class UserViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> get() = _user

    private val _isLogged = MutableLiveData<Boolean>(false)
    val isLogged: LiveData<Boolean> get() = _isLogged

    private val _loginState = MutableLiveData<LoginState>(LoginState.Initial)
    val loginState: LiveData<LoginState> get() = _loginState

    private val _message = MutableLiveData<String>("")
    val message: LiveData<String> get() = _message

    fun signUp(user: User) {
        viewModelScope.launch {
            Log.d("UserViewModel", "Attempting to sign up user: $user")
            val response = userRepository.signUp(user)
            if (response.userId != null) {
                _user.value = user.copy(userId = response.userId)
                _isLogged.value = true
                Log.d("UserViewModel", "Sign up successful: $response")
            } else {
                _message.value = response.message
                Log.e("UserViewModel", "Sign up failed: ${response.message}")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            Log.d("UserViewModel", "Attempting to log in with email: $email")
            try {
                val response = userRepository.login(email, password)
                if (response.userId != null) {
                    val user = User(response.userId, "", email, password, "", "")
                    _user.value = user
                    _isLogged.value = true
                    _loginState.value = LoginState.Success(user)
                    Log.d("UserViewModel", "Login successful: $response")
                } else {
                    _loginState.value = LoginState.Error(response.message)
                    Log.e("UserViewModel", "Login failed: ${response.message}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
                Log.e("UserViewModel", "Login error: ${e.message}")
            }
        }
    }
}

@HiltViewModel
class PlantViewModel @Inject constructor(private val plantRepository: PlantRepository) : ViewModel() {

    private val _plants = MutableStateFlow<List<PlantResponse>>(emptyList())
    val plants: StateFlow<List<PlantResponse>> get() = _plants

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun getPlants(userId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val plants = plantRepository.getPlants(userId)
                _plants.value = plants
                _loading.value = false
                Log.d("PlantViewModel", "Plants loaded: $plants")
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message
                Log.e("PlantViewModel", "Error loading plants: ${e.message}")
            }
        }
    }
}

@HiltViewModel
class SensorDataViewModel @Inject constructor(private val sensorDataRepository: SensorDataRepository) : ViewModel() {

    private val _sensorData = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorData: StateFlow<List<SensorData>> get() = _sensorData

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun getLastNSensorReadings(plantId: Int, n: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val data = sensorDataRepository.getLastNSensorReadings(plantId, n)
                _sensorData.value = data
                _loading.value = false
                Log.d("SensorDataViewModel", "Sensor data loaded: $data")
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message
                Log.e("SensorDataViewModel", "Error loading sensor data: ${e.message}")
            }
        }
    }
}

@HiltViewModel
class WateringEventViewModel @Inject constructor(private val wateringEventRepository: WateringEventRepository) : ViewModel() {

    private val _wateringEvents = MutableStateFlow<List<WateringEvent>>(emptyList())
    val wateringEvents: StateFlow<List<WateringEvent>> get() = _wateringEvents

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> get() = _error

    fun getWateringEvents(plantId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val events = wateringEventRepository.getWateringEvents(plantId)
                _wateringEvents.value = events
                _loading.value = false
                Log.d("WateringEventViewModel", "Watering events loaded: $events")
            } catch (e: Exception) {
                _loading.value = false
                _error.value = e.message
                Log.e("WateringEventViewModel", "Error loading watering events: ${e.message}")
            }
        }
    }
}
