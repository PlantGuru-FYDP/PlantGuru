package com.example.plantguru.viewmodels
/*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.plantguru.constants.AppConstants
import com.example.plantguru.data.PlantRepository
import com.example.plantguru.utils.BluetoothService
import kotlinx.coroutines.launch
import java.util.UUID

class PlantViewModel(private val repository: PlantRepository) : ViewModel() {

    private val _sensorData = MutableLiveData<Int>()
    val sensorData: LiveData<Int> = _sensorData

    private val _settings = MutableLiveData<Map<String, String>>()
    val settings: LiveData<Map<String, String>> = _settings

    /*
    init {
        bluetoothService.connect()
    }

    fun readSensorData() {
        // Read sensor data from the Bluetooth server
        bluetoothService.readCharacteristic(UUID.fromString(AppConstants.TEMPERATURE1_CHARACTERISTIC_UUID))
        // Read other characteristics similarly
    }

    fun updateSetting(uuid: UUID, value: String) {
        bluetoothService.writeCharacteristic(uuid, value.toByteArray())
    }

    override fun onCleared() {
        bluetoothService.close()
        super.onCleared()
    }*/

}
*/