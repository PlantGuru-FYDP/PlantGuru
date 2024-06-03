package com.example.plantguru

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private lateinit var sensorDataTextView: TextView
    private lateinit var bluetoothStateTextView: TextView
    private lateinit var connectButton: Button

    private lateinit var temp1TextView: TextView
    private lateinit var temp2TextView: TextView
    private lateinit var lightTextView: TextView
    private lateinit var soilMoisture1TextView: TextView
    private lateinit var soilMoisture2TextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var wifiStatusTextView: TextView

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    companion object {
        const val TAG = "MainActivity"
        const val SERVICE_UUID = "19b10000-e8f2-537e-4f6c-d104768a1214"
        const val SENSOR_CHARACTERISTIC_UUID = "19b10001-e8f2-537e-4f6c-d104768a1214"
        const val DEVICE_NAME = "PlantGuru 2"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothStateTextView = findViewById(R.id.bluetoothStateTextView)
        connectButton = findViewById(R.id.connectButton)

        temp1TextView = findViewById(R.id.temp1TextView)
        temp2TextView = findViewById(R.id.temp2TextView)
        lightTextView = findViewById(R.id.lightTextView)
        soilMoisture1TextView = findViewById(R.id.soilMoisture1TextView)
        soilMoisture2TextView = findViewById(R.id.soilMoisture2TextView)
        humidityTextView = findViewById(R.id.humidityTextView)
        wifiStatusTextView = findViewById(R.id.wifiStatusTextView)

        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                updateBluetoothState("Bluetooth Enabled")
            } else {
                updateBluetoothState("Bluetooth not enabled")
            }
        }

        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                initBluetooth()
            } else {
                Toast.makeText(this, "Permissions required to use Bluetooth", Toast.LENGTH_LONG).show()
            }
        }

        connectButton.setOnClickListener {
            if (checkPermissions()) {
                startScanning()
            }
        }

        if (checkPermissions()) {
            initBluetooth()
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionsNeeded = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsToRequest = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            false
        } else {
            true
        }
    }

    private fun initBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            updateBluetoothState("Bluetooth Enabled")
        }
    }

    private fun startScanning() {
        updateBluetoothState("Scanning for devices...")
        val scanFilter = ScanFilter.Builder()
            .setDeviceName(DEVICE_NAME)
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let {
                bluetoothAdapter.bluetoothLeScanner.stopScan(this)
                updateBluetoothState("Device found: ${it.name}")
                connectToDevice(it)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach {
                Log.d(TAG, "Batch scan result: ${it.device.name} - ${it.device.address}")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            updateBluetoothState("Scan failed with error: $errorCode")
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        updateBluetoothState("Connecting to device...")
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                updateBluetoothState("Connected to device, discovering services...")
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                updateBluetoothState("Disconnected from device")
                Log.d(TAG, "Disconnected from device")
            } else {
                Log.e(TAG, "Connection state change error: status=$status newState=$newState")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service: BluetoothGattService? = gatt.getService(java.util.UUID.fromString(SERVICE_UUID))
                val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(java.util.UUID.fromString(SENSOR_CHARACTERISTIC_UUID))

                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                        updateBluetoothState("Service discovered, notifications enabled")
                    } else {
                        updateBluetoothState("Descriptor not found")
                        Log.e(TAG, "Descriptor not found")
                    }
                } else {
                    updateBluetoothState("Service or characteristic not found")
                    Log.e(TAG, "Service or characteristic not found")
                }
                gatt.requestMtu(517)
            } else {
                updateBluetoothState("Service discovery failed with status: $status")
                Log.e(TAG, "Service discovery failed with status: $status")
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            if (characteristic.uuid.toString() == SENSOR_CHARACTERISTIC_UUID) {
                val sensorData = value.toString(Charsets.UTF_8)
                val sensorValues = sensorData.split(",")
                runOnUiThread {
                    temp1TextView.text = sensorValues[0]
                    temp2TextView.text = sensorValues[1]
                    lightTextView.text = sensorValues[2]
                    soilMoisture1TextView.text = sensorValues[3]
                    soilMoisture2TextView.text = sensorValues[4]
                    humidityTextView.text = sensorValues[5]
                    wifiStatusTextView.text = sensorValues[6]
                }
                Log.d(TAG, "Sensor data received: $sensorData")
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                val value = characteristic.value
                val sensorData = value.toString(Charsets.UTF_8)
                runOnUiThread {
                    sensorDataTextView.text = sensorData
                }
                Log.d(TAG, "Sensor data received: $sensorData")
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.d(TAG, "Characteristic written: ${characteristic.uuid}, status: $status")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Descriptor written: ${descriptor.uuid}")
            } else {
                Log.e(TAG, "Descriptor write failed with status: $status")
            }
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Log.d(TAG, "Descriptor read: ${descriptor.uuid}, status: $status")
        }
    }

    private fun updateBluetoothState(state: String) {
        runOnUiThread {
            bluetoothStateTextView.text = state
        }
        Log.d(TAG, state)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}