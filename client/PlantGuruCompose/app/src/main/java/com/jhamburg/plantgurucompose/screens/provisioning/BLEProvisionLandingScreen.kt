package com.jhamburg.plantgurucompose.screens.provisioning

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.BleScanListener
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.constants.AppConstants
import com.jhamburg.plantgurucompose.viewmodels.ProvisioningViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BLEProvisionLandingScreen(
    navController: NavController,
    plantId: Int,
    provisionToken: String?
) {
    val context = LocalContext.current
    val provisioningViewModel: ProvisioningViewModel = hiltViewModel()
    val provisionTokenState by provisioningViewModel.provisionToken.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Device") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@IconButton
                        }
                        ESPProvisionManager.getInstance(context).stopBleScan()
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LaunchedEffect(plantId) {
                if (provisionToken.isNullOrBlank()) {
                    isLoading = true
                    Log.d("BLEProvision", "Requesting new provision token for plant $plantId")
                    try {
                        provisioningViewModel.getProvisioningToken(plantId)
                    } catch (e: Exception) {
                        Log.e("BLEProvision", "Error getting provision token", e)
                        errorMessage = when (e) {
                            is java.net.SocketTimeoutException -> "Connection to server timed out. Please check your internet connection and try again."
                            is java.net.UnknownHostException -> "Could not reach server. Please check your internet connection and try again."
                            else -> "Failed to get provision token: ${e.message}"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    } finally {
                        isLoading = false
                    }
                } else {
                    Log.d("BLEProvision", "Using provided token: $provisionToken")
                    provisioningViewModel.setProvisionToken(provisionToken)
                }
            }
            LaunchedEffect(provisionTokenState) {
                Log.d("BLEProvision", "Token state updated: $provisionTokenState")
            }
            val effectiveToken = provisionTokenState
            Log.d("BLEProvision", "Effective token: $effectiveToken")

            if (isLoading || effectiveToken.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (errorMessage != null) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Go Back")
                            }
                        } else {
                            CircularProgressIndicator()
                            Text("Getting device connection token...")
                        }
                    }
                }
            }

            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val provisionManager = remember { ESPProvisionManager.getInstance(context) }
            var isScanning by remember { mutableStateOf(false) }
            val deviceList = remember { mutableStateListOf<BluetoothDevice>() }
            val bluetoothDevices = remember { mutableStateMapOf<BluetoothDevice, String>() }
            val scope = rememberCoroutineScope()


            fun handleConnectDevice(
                device: BluetoothDevice,
                provisionManager: ESPProvisionManager,
                context: Context,
                provisionToken: String?
            ) {
                isConnecting = true

                if (provisionToken.isNullOrBlank()) {
                    Log.e("BLEProvision", "Provision token is null or blank")
                    Toast.makeText(
                        context,
                        "Provision token is missing. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                    isConnecting = false
                    return
                }

                if (!checkRequiredPermissions(context)) {
                    Log.e("BLEProvision", "Missing required permissions for connection")
                    isConnecting = false
                    return
                }

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    provisionManager.stopBleScan()

                    try {
                        provisionManager.createESPDevice(
                            ESPConstants.TransportType.TRANSPORT_BLE,
                            ESPConstants.SecurityType.SECURITY_0
                        ).apply {
                            proofOfPossession = "abcd1234"
                            primaryServiceUuid = AppConstants.SERVICE_UUID
                        }

                        var retryCount = 0
                        var connected = false
                        while (!connected && retryCount < 3) {
                            try {
                                Thread.sleep(1000)
                                provisionManager.espDevice.connectBLEDevice(
                                    device,
                                    AppConstants.SERVICE_UUID
                                )
                                connected = true
                                Log.d("BLEProvision", "Successfully connected to device")
                              
                                navController.navigate("wifi_scan/$plantId/$provisionToken") {
                                    popUpTo("ble_provision_landing/$plantId") { inclusive = true }
                                }
                                isConnecting = false

                            } catch (e: Exception) {
                                Log.e(
                                    "BLEProvision",
                                    "Connection attempt ${retryCount + 1} failed: ${e.message}"
                                )
                                retryCount++
                                if (retryCount >= 3) {
                                    isConnecting = false
                                    Toast.makeText(
                                        context,
                                        "Failed to connect after 3 attempts",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BLEProvision", "Failed to create/connect ESP device: ${e.message}")
                        isConnecting = false
                        Toast.makeText(
                            context,
                            "Connection failed: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    isConnecting = false
                    Toast.makeText(
                        context,
                        "Bluetooth connect permission required",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val bleScanListener = remember {
                object : BleScanListener {
                    override fun scanStartFailed() {
                        isScanning = false
                        Log.e("BLEProvision", "Scan start failed")
                    }

                    override fun onPeripheralFound(
                        device: BluetoothDevice,
                        scanResult: ScanResult
                    ) {
                        if (!deviceList.contains(device)) {
                            deviceList.add(device)
                            bluetoothDevices[device] = scanResult.device.address
                        }
                    }

                    override fun scanCompleted() {
                        isScanning = false
                    }

                    override fun onFailure(e: Exception) {
                        isScanning = false
                        Log.e("BLEProvision", "Scan failed with exception: ${e.message}")
                    }
                }
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                if (permissions.values.all { it }) {
                    handleStartScan(
                        context,
                        provisionManager,
                        deviceList,
                        bluetoothDevices,
                        bleScanListener
                    ) { newIsScanning ->
                        isScanning = newIsScanning
                    }
                } else {
                    Toast.makeText(context, "Permissions required for scanning", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val bluetoothEnableLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    if (checkRequiredPermissions(context)) {
                        handleStartScan(
                            context,
                            provisionManager,
                            deviceList,
                            bluetoothDevices,
                            bleScanListener
                        ) { newIsScanning ->
                            isScanning = newIsScanning
                        }
                    } else {
                        checkAndRequestPermissions(context, permissionLauncher)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                SetupInstructionsCard()

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!bluetoothManager.adapter.isEnabled) {
                            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        } else if (checkRequiredPermissions(context)) {
                            handleStartScan(
                                context,
                                provisionManager,
                                deviceList,
                                bluetoothDevices,
                                bleScanListener
                            ) { newIsScanning ->
                                isScanning = newIsScanning
                            }
                        } else {
                            checkAndRequestPermissions(context, permissionLauncher)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isScanning && !isConnecting
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
                        Text(
                            when {
                                isScanning -> "Scanning..."
                                else -> "Scan for Devices"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (deviceList.isEmpty() && !isScanning) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_bluetooth),
                                contentDescription = "No devices found",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No devices found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Make sure your device is in pairing mode and try scanning again",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn {
                            items(deviceList) { device ->
                                DeviceItem(
                                    device = device,
                                    onClick = {
                                        if (!isConnecting) {
                                            handleConnectDevice(
                                                device,
                                                provisionManager,
                                                context,
                                                effectiveToken
                                            )
                                        }
                                    },
                                    enabled = !isScanning && !isConnecting
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleStartScan(
    context: Context,
    provisionManager: ESPProvisionManager,
    deviceList: MutableList<BluetoothDevice>,
    bluetoothDevices: MutableMap<BluetoothDevice, String>,
    bleScanListener: BleScanListener,
    updateScanning: (Boolean) -> Unit
) {
    if (!checkRequiredPermissions(context)) {
        Toast.makeText(context, "Permission denied for Bluetooth scanning", Toast.LENGTH_SHORT)
            .show()
        updateScanning(false)
        return
    }

    deviceList.clear()
    bluetoothDevices.clear()
    updateScanning(true)

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
        PackageManager.PERMISSION_GRANTED
    ) {
        provisionManager.searchBleEspDevices(AppConstants.DEVICE_PREFIX, bleScanListener)
    } else {
        updateScanning(false)
        Toast.makeText(context, "Bluetooth scan permission required", Toast.LENGTH_SHORT).show()
    }
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

private fun checkAndRequestPermissions(
    context: Context,
    permissionLauncher: ActivityResultLauncher<Array<String>>
) {
    val permissionsToRequest = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    ).filter {
        ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
    }

    if (permissionsToRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}

@Composable
private fun DeviceItem(device: BluetoothDevice, onClick: () -> Unit, enabled: Boolean) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(enabled = enabled, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bluetooth),
                    contentDescription = "Bluetooth Device",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                } else {
                    Text(
                        text = "Permission Required",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (enabled) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Connect",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SetupInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Before scanning:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SetupInstruction(
                number = "1",
                text = "Ensure your Plant Guru device is plugged in and in range"
            )

            SetupInstruction(
                number = "2",
                text = "Hold both button for 5 seconds"
            )

            SetupInstruction(
                number = "3",
                text = "Ensure the LED is flashing green"
            )
        }
    }
}

@Composable
private fun SetupInstruction(
    number: String,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

