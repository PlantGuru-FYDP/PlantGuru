package com.jhamburg.plantgurucompose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.bluetooth.BluetoothDevice

class BLEViewModel : ViewModel() {
    private val _deviceList = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val deviceList: StateFlow<List<BluetoothDevice>> = _deviceList

    fun updateDeviceList(devices: List<BluetoothDevice>) {
        viewModelScope.launch {
            _deviceList.value = devices
        }
    }

}
