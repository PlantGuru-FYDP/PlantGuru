package com.example.plantguru.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import java.util.UUID
import com.example.plantgurucompose.constants.AppConstants

class BluetoothService(val context: Context, private val deviceAddress: String) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Handle disconnection
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Handle services discovery
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Handle characteristic read
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            // Handle characteristic change
        }
    }

    fun connect() {
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
    }

    fun readCharacteristic(uuid: UUID) {
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(UUID.fromString(AppConstants.SERVICE_UUID))
            val characteristic = service.getCharacteristic(uuid)
            gatt.readCharacteristic(characteristic)
        }
    }

    fun writeCharacteristic(uuid: UUID, value: ByteArray) {
        bluetoothGatt?.let { gatt ->
            val service = gatt.getService(UUID.fromString(AppConstants.SERVICE_UUID))
            val characteristic = service.getCharacteristic(uuid)
            characteristic.value = value
            gatt.writeCharacteristic(characteristic)
        }
    }

    fun close() {
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
