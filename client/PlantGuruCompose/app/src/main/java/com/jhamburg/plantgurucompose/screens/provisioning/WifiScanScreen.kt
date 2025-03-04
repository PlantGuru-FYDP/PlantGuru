package com.jhamburg.plantgurucompose.screens.provisioning

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.R
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.WiFiAccessPoint
import com.espressif.provisioning.listeners.WiFiScanListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.net.URLEncoder

class WiFiScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WiFiScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WiFiScanViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WiFiScanViewModel(private val appContext: Context) : ViewModel() {
    val wifiAPList = mutableStateListOf<WiFiAccessPoint>()
    val isScanning = mutableStateOf(true)
    val deviceConnectionEvent = mutableStateOf<DeviceConnectionEvent?>(null)
    val provisionManager = ESPProvisionManager.getInstance(appContext)
    private var isDeviceConnected = false

    init {
        EventBus.getDefault().register(this)
        Log.d("WiFiScan", "ViewModel initialized, waiting for device connection event")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent) {
        deviceConnectionEvent.value = event
        Log.d("WiFiScan", "Received event: ${event.eventType}")
        
        when (event.eventType) {
            ESPConstants.EVENT_DEVICE_CONNECTED -> {
                isDeviceConnected = true
                Log.d("WiFiScan", "Device connected, starting WiFi scan")
                startWifiScan()
            }
            ESPConstants.EVENT_DEVICE_DISCONNECTED -> {
                isDeviceConnected = false
                Log.d("WiFiScan", "Device disconnected, clearing WiFi list")
                wifiAPList.clear()
                isScanning.value = false
            }
        }
    }

    fun startWifiScan() {
        if (!isDeviceConnected) {
            Log.d("WiFiScan", "Cannot start WiFi scan - device not connected")
            isScanning.value = false
            return
        }

        wifiAPList.clear()
        isScanning.value = true
        Log.d("WiFiScan", "Starting WiFi scan...")
        
        try {
            provisionManager.espDevice?.let { device ->
                device.scanNetworks(object : WiFiScanListener {
                    override fun onWifiListReceived(wifiList: ArrayList<WiFiAccessPoint>) {
                        Log.d("WiFiScan", "WiFi scan successful, found ${wifiList.size} networks")
                        wifiAPList.addAll(wifiList)
                        completeWifiList()
                    }

                    override fun onWiFiScanFailed(e: Exception) {
                        Log.e("WiFiScan", "WiFi scan failed", e)
                        isScanning.value = false
                        Toast.makeText(appContext, "Failed to get Wi-Fi scan list: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } ?: run {
                Log.e("WiFiScan", "ESP device is null")
                isScanning.value = false
                Toast.makeText(appContext, "Device not initialized", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("WiFiScan", "Error starting WiFi scan", e)
            isScanning.value = false
            Toast.makeText(appContext, "Error starting WiFi scan: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun completeWifiList() {
        // Add the "Join Other Network" option at the end
        val joinOtherNetwork = WiFiAccessPoint().apply {
            wifiName = ""
        }
        wifiAPList.add(joinOtherNetwork)
        isScanning.value = false
    }

    private fun createMockNetwork(ssid: String, rssi: Int): WiFiAccessPoint {
        return WiFiAccessPoint().apply {
            wifiName = ssid
            security = 3  // WPA/WPA2
            password = "b"
        }
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
    }

    fun setEnterpriseCredentials(identity: String, username: String, password: String) {
        Log.d("WifiScanScreen", "Setting enterprise credentials - Identity: $identity, Username: $username")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiScanScreen(
    navController: NavController,
    plantId: Int,
    provisionToken: String
) {
    val context = LocalContext.current
    val viewModel: WiFiScanViewModel = viewModel(factory = WiFiScanViewModelFactory(context))
    val provisionViewModel: ProvisionViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProvisionViewModel(context, "", "", plantId, provisionToken) as T
            }
        }
    )
    val isScanning by viewModel.isScanning
    val wifiAPList = viewModel.wifiAPList
    var showDialog by remember { mutableStateOf(false) }
    var selectedNetwork by remember { mutableStateOf<WiFiAccessPoint?>(null) }
    var networkName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }

    BackHandler {
        navController.navigate("plantList") {
            popUpTo("wifi_scan/$plantId/$provisionToken") { inclusive = true }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Error") },
            text = { Text("Device disconnected") },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Networks") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.provisionManager.espDevice?.let { device ->
                            try {
                                device.disconnectDevice()
                            } catch (e: Exception) {
                                Log.e("WiFiScan", "Error disconnecting device", e)
                            }
                        }
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!viewModel.isScanning.value) {
                        IconButton(onClick = { viewModel.startWifiScan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(wifiAPList) { wifiAp ->
                        WiFiItem(wifiAp = wifiAp, onClick = {
                            selectedNetwork = wifiAp
                            networkName = if (wifiAp.wifiName == "") {
                                ""
                            } else {
                                wifiAp.wifiName
                            }
                            password = ""
                            showDialog = true
                        })
                    }
                }
            }
        }
    }

    when (viewModel.deviceConnectionEvent.value?.eventType) {
        ESPConstants.EVENT_DEVICE_DISCONNECTED -> {
            showErrorDialog = true
            viewModel.deviceConnectionEvent.value = null
        }
    }

    if (showDialog && selectedNetwork != null) {
        WifiCredentialsDialog(
            networkName = selectedNetwork!!.wifiName,
            onDismiss = { showDialog = false },
            onProvision = { ssid, networkPassword, isEnterprise, identity, userPassword ->
                showDialog = false
                try {
                    val encodedSsid = URLEncoder.encode(ssid, "UTF-8")
                    val encodedNetworkPassword = networkPassword?.let { 
                        URLEncoder.encode(it, "UTF-8") 
                    } ?: "null"
                    
                    val route = if (isEnterprise) {
                        val encodedIdentity = URLEncoder.encode(identity ?: "", "UTF-8")
                        val encodedUserPassword = URLEncoder.encode(userPassword ?: "", "UTF-8")
                        "provision/$plantId/$encodedSsid/$encodedNetworkPassword/$provisionToken/true/$encodedIdentity/$encodedUserPassword"
                    } else {
                        "provision/$plantId/$encodedSsid/$encodedNetworkPassword/$provisionToken/false/null/null"
                    }
                    
                    Log.d("Navigation", "Navigating to: $route")
                    navController.navigate(route)
                } catch (e: Exception) {
                    Log.e("Navigation", "Navigation failed", e)
                    Toast.makeText(context, "Navigation failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            viewModel = provisionViewModel
        )
    }
}

@Composable
fun WiFiItem(wifiAp: WiFiAccessPoint, onClick: () -> Unit) {
    val isJoinOtherNetwork = wifiAp.wifiName.isBlank() || wifiAp.wifiName.isEmpty()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_wifi
                ),
                contentDescription = "WiFi Network",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (isJoinOtherNetwork) {
                    "Join Other Network"
                } else {
                    wifiAp.wifiName
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun WifiCredentialsDialog(
    networkName: String,
    onDismiss: () -> Unit,
    onProvision: (
        ssid: String, 
        networkPassword: String?,
        isEnterprise: Boolean,
        identity: String?,
        userPassword: String?
    ) -> Unit,
    viewModel: ProvisionViewModel
) {
    var ssid by remember { mutableStateOf(networkName) }
    var networkPassword by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter WiFi Details") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Network Name (SSID)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = networkPassword,
                    onValueChange = { networkPassword = it },
                    label = { Text("Network Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onProvision(ssid, networkPassword, false, null, null)
                }
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}