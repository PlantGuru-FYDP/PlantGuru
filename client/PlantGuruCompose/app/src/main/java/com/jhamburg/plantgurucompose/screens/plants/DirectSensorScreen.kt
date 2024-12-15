package com.jhamburg.plantgurucompose.screens.plants


import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.screens.provisioning.checkPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

private fun askForLocation(context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setCancelable(true)
    builder.setMessage("Location is disabled. Do you want to enable it?")
        .setPositiveButton("Yes") { _, _ ->
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        .setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }
    builder.show()
}

private fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(
        LocationManager.NETWORK_PROVIDER
    )
}

private fun checkRequiredPermissions(context: Context): Boolean {
    return listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    ).all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectSensorScreen(
    navController: NavController,
    plantId: Int,
    bluetoothAdapter: BluetoothAdapter,
    requestPermissionsLauncher: ActivityResultLauncher<Array<String>>,
) {
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var currentSensorData by remember { mutableStateOf<SensorData?>(null) }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    val scope = rememberCoroutineScope()

    val TAG = "DirectSensorScreen"
    val DEVICE_NAME = "GURU_$plantId"

    val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    var gatt: BluetoothGatt? by remember { mutableStateOf(null) }
    var readJob: Job? by remember { mutableStateOf(null) }

    var showLocationDialog by remember { mutableStateOf(false) }

    fun startScan() {
        if (!checkRequiredPermissions(context)) {
            Log.e(TAG, "Required permissions not granted")
            Toast.makeText(context, "Required permissions not granted", Toast.LENGTH_SHORT).show()
            connectionStatus = "Missing permissions"
            return
        }

        if (!isLocationEnabled(context)) {
            showLocationDialog = true
            return
        }

        try {
            val scanFilter = ScanFilter.Builder()
                .setDeviceName(DEVICE_NAME)
                .build()

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            bluetoothAdapter.bluetoothLeScanner?.startScan(
                listOf(scanFilter),
                scanSettings,
                object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult?) {
                        result?.device?.let { device ->
                            bluetoothAdapter.bluetoothLeScanner?.stopScan(this)
                            isScanning = false
                            connectToDevice(
                                device = device,
                                context = context,
                                scope = scope,
                                onConnectionStatusChange = { status ->
                                    connectionStatus = status
                                },
                                onSensorDataUpdate = { data ->
                                    currentSensorData = data
                                },
                                SERVICE_UUID = SERVICE_UUID,
                                CHARACTERISTIC_UUID = CHARACTERISTIC_UUID
                            )
                        }
                    }

                    override fun onScanFailed(errorCode: Int) {
                        connectionStatus = "Scan failed: $errorCode"
                        isScanning = false
                    }
                }
            )
            isScanning = true
            connectionStatus = "Scanning..."
        } catch (e: SecurityException) {
            connectionStatus = "Permission denied: ${e.message}"
            isScanning = false
        }
    }

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            if (checkPermissions(context, requestPermissionsLauncher)) {
                if (isLocationEnabled(context)) {
                    startScan()
                } else {
                    showLocationDialog = true
                }
            }
        }
    }

    fun checkRequiredPermissions(context: Context): Boolean {
        return listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        ).all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            readJob?.cancel()
            gatt?.disconnect()
            gatt?.close()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Direct Sensor Reading") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Status: $connectionStatus")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!bluetoothAdapter.isEnabled) {
                        bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } else if (checkPermissions(context, requestPermissionsLauncher)) {
                        if (isLocationEnabled(context)) {
                            if (isScanning) {
                                bluetoothAdapter.bluetoothLeScanner?.stopScan(
                                    object : ScanCallback() {}
                                )
                                isScanning = false
                                connectionStatus = "Scan stopped"
                            } else {
                                startScan()
                            }
                        } else {
                            showLocationDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isScanning) "Stop Scan" else "Start Scan")
            }

            if (gatt != null) {
                Button(
                    onClick = {
                        readJob?.cancel()
                        gatt?.disconnect()
                        gatt?.close()
                        gatt = null
                        currentSensorData = null
                        connectionStatus = "Disconnected"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Disconnect")
                }
            }

            currentSensorData?.let { sensor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("External Temperature: ${sensor.extTemp}°C")
                        Text("Humidity: ${sensor.humidity}%")
                        Text("Light: ${sensor.light}%")
                        Text("Soil Temperature: ${sensor.soilTemp}°C")
                        Text("Soil Moisture 1: ${sensor.soilMoisture1}%")
                        Text("Soil Moisture 2: ${sensor.soilMoisture2}%")
                    }
                }
            }

            if (showLocationDialog) {
                AlertDialog(
                    onDismissRequest = { showLocationDialog = false },
                    title = { Text("Location Required") },
                    text = { Text("Location services are required for Bluetooth scanning. Would you like to enable it?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLocationDialog = false
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                        ) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

private fun connectToDevice(
    device: BluetoothDevice,
    context: Context,
    scope: CoroutineScope,
    onConnectionStatusChange: (String) -> Unit,
    onSensorDataUpdate: (SensorData) -> Unit,
    SERVICE_UUID: UUID,
    CHARACTERISTIC_UUID: UUID
) {
    var gatt: BluetoothGatt? = null
    var readJob: Job? = null

    try {
        readJob?.cancel()
        gatt?.disconnect()
        gatt?.close()

        onConnectionStatusChange("Connecting...")

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        onConnectionStatusChange("Connected, discovering services...")
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            gatt.discoverServices()
                        }
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        onConnectionStatusChange("Disconnected")
                        readJob?.cancel()
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                            == PackageManager.PERMISSION_GRANTED
                        ) {
                            gatt.close()
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

                    if (characteristic != null) {
                        onConnectionStatusChange("Services discovered, starting readings")
                        readJob = scope.launch(Dispatchers.IO) {
                            while (isActive) {
                                if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    gatt.readCharacteristic(characteristic)
                                }
                                delay(1000)
                            }
                        }
                    } else {
                        onConnectionStatusChange("Required characteristic not found")
                    }
                } else {
                    onConnectionStatusChange("Service discovery failed")
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                handleCharacteristicRead(gatt, characteristic, characteristic.value, status)
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                handleCharacteristicRead(gatt, characteristic, value, status)
            }

            private fun handleCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    scope.launch(Dispatchers.Main) {
                        try {
                            val data = parseSensorData(value)
                            onSensorDataUpdate(data)
                            onConnectionStatusChange("Reading successful")
                        } catch (e: Exception) {
                            onConnectionStatusChange("Failed to parse data: ${e.message}")
                        }
                    }
                } else {
                    onConnectionStatusChange("Read failed: $status")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED
        ) {
            gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            onConnectionStatusChange("Missing BLUETOOTH_CONNECT permission")
        }

    } catch (e: SecurityException) {
        onConnectionStatusChange("Permission denied: ${e.message}")
    } catch (e: Exception) {
        onConnectionStatusChange("Connection error: ${e.message}")
    }
}

private fun parseSensorData(bytes: ByteArray): SensorData {
    val dataString = String(bytes)
    val values = dataString.split(",")

    return SensorData(
        sensorId = 0,
        plantId = 0,
        timeStamp = java.time.LocalDateTime.now().toString(),
        extTemp = values.getOrNull(0)?.toFloatOrNull() ?: 0f,
        humidity = values.getOrNull(1)?.toFloatOrNull() ?: 0f,
        light = values.getOrNull(2)?.toFloatOrNull() ?: 0f,
        soilTemp = values.getOrNull(3)?.toFloatOrNull() ?: 0f,
        soilMoisture1 = values.getOrNull(4)?.toFloatOrNull() ?: 0f,
        soilMoisture2 = values.getOrNull(5)?.toFloatOrNull() ?: 0f
    )
} 