package com.example.plantgurucompose.screens

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.example.plantgurucompose.constants.AppConstants
import com.example.plantgurucompose.models.Plant
import com.example.plantgurucompose.utils.SharedPreferencesHelper
import com.example.plantgurucompose.utils.updateBluetoothState

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

    val TAG = "ConnectionHomeScreen"
    val DEVICE_NAME = "guru32"

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
                    onClick = { if (checkPermissions(context, requestPermissionsLauncher)) startScanning(bluetoothAdapter, detectedDevices, plants, context, TAG, DEVICE_NAME) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(60.dp)
                ) {
                    Text("Scan")
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
                        Text(text = "${plants[index].deviceUUID} - ${if (detectedDevices.contains(plants[index].deviceUUID)) "Detected" else "Not Detected"}")
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
                            provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_0)
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

private fun checkPermissions(
    context: Context,
    requestPermissionsLauncher: ActivityResultLauncher<Array<String>>
): Boolean {
    val permissionsNeeded = listOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val permissionsToRequest = permissionsNeeded.filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

    return if (permissionsToRequest.isNotEmpty()) {
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        false
    } else {
        true
    }
}

private fun startScanning(
    bluetoothAdapter: BluetoothAdapter,
    detectedDevices: MutableList<String>,
    plants: List<Plant>,
    context: Context,
    TAG: String,
    DEVICE_NAME: String
) {
    updateBluetoothState(context, "Scanning for devices...")
    val scanFilter = ScanFilter.Builder().setDeviceName(DEVICE_NAME).build()
    val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    bluetoothAdapter.bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let {
                updateBluetoothState(context, "Device found: ${it.name ?: it.address}")
                detectedDevices.add(it.address)
                plants.forEach { plant ->
                    if (plant.deviceUUID == it.address) {
                        //plant.isDetected = true
                    }
                }
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach {
                detectedDevices.add(it.device.address)
                plants.forEach { plant ->
                    if (plant.deviceUUID == it.device.address) {
                        //plant.isDetected = true
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            updateBluetoothState(context, "Scan failed with error: $errorCode")
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    })
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
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}