package com.jhamburg.plantgurucompose.screens.provisioning

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.jhamburg.plantgurucompose.models.Plant
import com.jhamburg.plantgurucompose.utils.SharedPreferencesHelper
import com.jhamburg.plantgurucompose.utils.updateBluetoothState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionHomeScreen(
    navController: NavController,
    bluetoothAdapter: BluetoothAdapter,
    provisionManager: ESPProvisionManager,
    sharedPreferences: SharedPreferences,
    enableBluetoothLauncher: ActivityResultLauncher<Intent>,
    requestPermissionsLauncher: ActivityResultLauncher<Array<String>>,
) {
    val context = LocalContext.current
    var plants by remember { mutableStateOf(SharedPreferencesHelper.getPlants(context)) }
    val detectedDevices = remember { mutableStateListOf<String>() }
    var isScanning by remember { mutableStateOf(false) }

    val TAG = "ConnectionHomeScreen"
    val DEVICE_NAME = "GURU_123"

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            if (checkPermissions(context, requestPermissionsLauncher)) {
                isScanning = true
                startScanning(
                    bluetoothAdapter,
                    detectedDevices,
                    plants,
                    context,
                    TAG,
                    DEVICE_NAME
                ) {
                    isScanning = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plant Guru") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Button(
                    onClick = {
                        if (!bluetoothAdapter.isEnabled) {
                            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        } else if (checkPermissions(context, requestPermissionsLauncher)) {
                            isScanning = true
                            startScanning(
                                bluetoothAdapter,
                                detectedDevices,
                                plants,
                                context,
                                TAG,
                                DEVICE_NAME
                            ) {
                                isScanning = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(60.dp),
                    enabled = !isScanning
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isScanning) "Scanning..." else "Scan")
                    }
                }

                Text(
                    text = "My Plants",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(200.dp)
                ) {
                    items(plants.size) { index ->
                        Text(
                            text = "${plants[index].deviceUUID} - ${
                                if (detectedDevices.contains(
                                        plants[index].deviceUUID
                                    )
                                ) "Detected" else "Not Detected"
                            }"
                        )
                    }
                }

                Button(
                    onClick = {
                        SharedPreferencesHelper.clearAllPlants(context)
                        plants = SharedPreferencesHelper.getPlants(context)
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Connect")
                }

                Button(
                    onClick = {
                        if (!isLocationEnabled(context)) {
                            askForLocation(context)
                        } else {
                            provisionManager.createESPDevice(
                                ESPConstants.TransportType.TRANSPORT_BLE,
                                ESPConstants.SecurityType.SECURITY_0
                            )
                            navController.navigate("ble_provision_landing")
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Provision New Device")
                }
            }
        }
    )
}

fun checkPermissions(
    context: Context,
    requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
): Boolean {
    val basePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 (S) and above
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        // Android 11 and below
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    // Location permission is required for BLE scanning on all Android versions
    val permissions = basePermissions + Manifest.permission.ACCESS_FINE_LOCATION

    val permissionsToRequest = permissions.filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

    return if (permissionsToRequest.isNotEmpty()) {
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        false
    } else {
        true
    }
}

fun startScanning(
    bluetoothAdapter: BluetoothAdapter,
    detectedDevices: MutableList<String>,
    plants: List<Plant>,
    context: Context,
    TAG: String,
    DEVICE_NAME: String,
    onScanComplete: () -> Unit
) {
    if (!checkRequiredPermissions(context)) {
        Log.e(TAG, "Required permissions not granted")
        Toast.makeText(context, "Required permissions not granted", Toast.LENGTH_SHORT).show()
        onScanComplete()
        return
    }

    try {
        updateBluetoothState(context, "Scanning for devices...")
        val scanFilter = ScanFilter.Builder().setDeviceName(DEVICE_NAME).build()
        val scanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (bluetoothLeScanner == null) {
            Toast.makeText(
                context,
                "Bluetooth LE is not available on this device",
                Toast.LENGTH_SHORT
            ).show()
            onScanComplete()
            return
        }

        bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                try {
                    super.onScanResult(callbackType, result)
                    result?.device?.let {
                        val deviceName = try {
                            it.name ?: it.address
                        } catch (e: SecurityException) {
                            "Unknown Device"
                        }
                        updateBluetoothState(context, "Device found: $deviceName")
                        detectedDevices.add(it.address)
                        plants.forEach { plant ->
                            if (plant.deviceUUID == it.address) {
                                //plant.isDetected = true
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception during scan result: ${e.message}")
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                try {
                    super.onBatchScanResults(results)
                    results?.forEach {
                        detectedDevices.add(it.device.address)
                        plants.forEach { plant ->
                            if (plant.deviceUUID == it.device.address) {
                                //plant.isDetected = true
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception during batch scan: ${e.message}")
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                updateBluetoothState(context, "Scan failed with error: $errorCode")
                Log.e(TAG, "Scan failed with error: $errorCode")
                onScanComplete()
            }
        })
    } catch (e: SecurityException) {
        Log.e(TAG, "Security exception during scan start: ${e.message}")
        Toast.makeText(context, "Permission denied for Bluetooth scanning", Toast.LENGTH_SHORT)
            .show()
        onScanComplete()
    }
}

private fun checkRequiredPermissions(context: Context): Boolean {
    val basePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Android 12 (S) and above
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        // Android 11 and below
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    // Location permission is required for BLE scanning on all Android versions
    val permissions = basePermissions + Manifest.permission.ACCESS_FINE_LOCATION

    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

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