package com.example.plantgurucompose.screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.BleScanListener
import com.example.plantgurucompose.constants.AppConstants
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MyViewModel : ViewModel() {
    val deviceConnectionEvent = MutableLiveData<DeviceConnectionEvent>()
    val connectionState = MutableLiveData<Boolean>()
    val isProvisioning = MutableLiveData<Boolean>()

    init {
        EventBus.getDefault().register(this)
        isProvisioning.value = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent) {
        deviceConnectionEvent.value = event
        when (event.eventType) {
            ESPConstants.EVENT_DEVICE_CONNECTED -> {
                connectionState.value = true
                isProvisioning.value = false
            }
            ESPConstants.EVENT_DEVICE_CONNECTION_FAILED -> {
                connectionState.value = false
                isProvisioning.value = false
            }
        }
        Log.d("BLEProvision", "Received event: ${event.eventType}")
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BLEProvisionLandingScreen(
    navController: NavController,
    viewModel: MyViewModel = viewModel()
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val provisionManager = remember { ESPProvisionManager.getInstance(context) }
    val isScanning = remember { mutableStateOf(false) }
    val deviceConnectionEvent by viewModel.deviceConnectionEvent.observeAsState()
    val connectionState by viewModel.connectionState.observeAsState()
    val isProvisioning by viewModel.isProvisioning.observeAsState(false)
    val deviceList = remember { mutableStateListOf<BluetoothDevice>() }
    val bluetoothDevices = remember { mutableStateMapOf<BluetoothDevice, String>() }
    val coroutineScope = rememberCoroutineScope()

    val bleScanListener = remember {
        object : BleScanListener {
            override fun scanStartFailed() {
                Log.e("BLEProvision", "Scan start failed")
            }

            override fun onPeripheralFound(device: BluetoothDevice, scanResult: ScanResult) {
                Log.d("BLEProvision", "Peripheral found: ${device.name}, ${device.address}")

                val serviceUuid = scanResult.scanRecord?.serviceUuids?.firstOrNull()?.toString() ?: ""
                if (!bluetoothDevices.containsKey(device)) {
                    bluetoothDevices[device] = serviceUuid
                    deviceList.add(device)
                    Log.d("BLEProvision", "Device added: ${device.name}, ${device.address}")
                }
            }

            override fun scanCompleted() {
                isScanning.value = false
                Log.d("BLEProvision", "Scan completed")
            }

            override fun onFailure(e: Exception) {
                Log.e("BLEProvision", "Scan failed with exception: ${e.message}")
            }
        }
    }

    fun startScan() {
        if (!hasBluetoothPermissions(context, bluetoothManager) || isScanning.value) {
            Log.w("BLEProvision", "Cannot start scan: permissions missing or already scanning")
            return
        }

        bluetoothDevices.clear()
        isScanning.value = true
        deviceList.clear()
        Log.d("BLEProvision", "Starting scan")
        provisionManager.searchBleEspDevices(AppConstants.DEVICE_PREFIX, bleScanListener)
    }

    fun stopScan() {
        isScanning.value = false
        provisionManager.stopBleScan()
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()
        viewModel.isProvisioning.value = true
        val uuid = bluetoothDevices[device]
        provisionManager.espDevice.connectBLEDevice(device, uuid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "BLE Provision Landing",
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = {
                startScan()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isScanning.value && !isProvisioning
        ) {
            Text(text = if (isScanning.value) "Scanning..." else "Scan Again")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isScanning.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        LazyColumn {
            items(deviceList) { device ->
                DeviceItem(
                    device = device,
                    onClick = {
                        connectToDevice(device)
                    },
                    enabled = !isProvisioning
                )
            }
        }
    }

    when (deviceConnectionEvent?.eventType) {
        ESPConstants.EVENT_DEVICE_CONNECTED -> {
            navController.navigate("wifi_scan")
        }
        ESPConstants.EVENT_DEVICE_CONNECTION_FAILED -> {
            connectionState?.let {
                if (!it) {
                    Toast.makeText(context, "Connection Failed", Toast.LENGTH_LONG).show()
                }
            }
        }
        ESPConstants.EVENT_DEVICE_DISCONNECTED -> {
            Text(text = "Disconnected")
        }
        else -> {
            deviceConnectionEvent?.eventType?.let {
                Text(text = it.toString())
            }
        }
    }

    HandleBluetoothPermissions(
        bluetoothManager = bluetoothManager,
        onPermissionDenied = { Log.w("BLEProvision", "Bluetooth permissions denied") }
    )

    LaunchedEffect(Unit) {
        startScan()
    }
}

@Composable
fun DeviceItem(device: BluetoothDevice, onClick: () -> Unit, enabled: Boolean) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick, enabled = enabled),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Device Name: ${device.name ?: "Unknown"}",
                color = contentColor
            )
            Text(
                text = "Device Address: ${device.address}",
                color = contentColor
            )
        }
    }
}


@Composable
fun HandleBluetoothPermissions(
    bluetoothManager: BluetoothManager,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Log.d("BLEProvision", "All Bluetooth permissions granted")
        } else {
            Log.w("BLEProvision", "Bluetooth permissions denied")
            onPermissionDenied()
        }
    }

    DisposableEffect(Unit) {
        if (!hasBluetoothPermissions(context, bluetoothManager)) {
            requestBluetoothPermissions(permissionLauncher)
        }
        onDispose { }
    }
}

fun hasBluetoothPermissions(context: Context, bluetoothManager: BluetoothManager): Boolean {
    val bleAdapter = bluetoothManager.adapter
    val hasPermissions = bleAdapter != null && bleAdapter.isEnabled
            && ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    Log.d("BLEProvision", "Has Bluetooth permissions: $hasPermissions")
    return hasPermissions
}

fun requestBluetoothPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    Log.d("BLEProvision", "Launching Bluetooth permissions request")
    permissionLauncher.launch(
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
}
