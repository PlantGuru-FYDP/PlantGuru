package com.example.plantgurucompose.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.example.plantgurucompose.R
import com.example.plantgurucompose.constants.AppConstants
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.WiFiAccessPoint
import com.espressif.provisioning.listeners.WiFiScanListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WiFiScanViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WiFiScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WiFiScanViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WiFiScanViewModel(context: Context) : ViewModel() {
    val wifiAPList = mutableStateListOf<WiFiAccessPoint>()
    val isScanning = mutableStateOf(false)
    val deviceConnectionEvent = mutableStateOf<DeviceConnectionEvent?>(null)
    val provisionManager = ESPProvisionManager.getInstance(context)

    init {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent) {
        deviceConnectionEvent.value = event
        Log.d("WiFiScan", "Received event: ${event.eventType}")
    }

    fun startWifiScan(context: Context) {
        wifiAPList.clear()
        isScanning.value = true
        provisionManager.espDevice.scanNetworks(object : WiFiScanListener {
            override fun onWifiListReceived(wifiList: ArrayList<WiFiAccessPoint>) {
                wifiAPList.addAll(wifiList)
                completeWifiList(context)
            }

            override fun onWiFiScanFailed(e: Exception) {
                Log.e("WiFiScan", "WiFi scan failed", e)
                isScanning.value = false
                Toast.makeText(context, "Failed to get Wi-Fi scan list", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun completeWifiList(context: Context) {
        val joinOtherNetwork = WiFiAccessPoint().apply {
            wifiName = context.getString(R.string.join_other_network)
        }
        wifiAPList.add(joinOtherNetwork)
        isScanning.value = false
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiScanScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: WiFiScanViewModel = viewModel(factory = WiFiScanViewModelFactory(context))
    val isScanning by viewModel.isScanning
    val wifiAPList = viewModel.wifiAPList
    var showDialog by remember { mutableStateOf(false) }
    var selectedNetwork by remember { mutableStateOf<WiFiAccessPoint?>(null) }
    var networkName by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startWifiScan(context)
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Error") },
            text = { Text("Device disconnected") },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate("connection_home")
                }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("Wi-Fi Scan") },
            actions = {
                if (!isScanning) {
                    IconButton(onClick = { viewModel.startWifiScan(context) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        )

        if (isScanning) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn {
                items(wifiAPList) { wifiAp ->
                    WiFiItem(wifiAp = wifiAp, onClick = {
                        selectedNetwork = wifiAp
                        networkName = TextFieldValue(if (wifiAp.wifiName == "Join Other Network") "Data City 263 - 2.4GHz" else wifiAp.wifiName)
                        password = TextFieldValue("ECE358PROBLEM")
                        showDialog = true
                    })
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
        NetworkDialog(
            ssid = selectedNetwork!!.wifiName,
            networkName = networkName,
            password = password,
            onNetworkNameChange = { networkName = it },
            onPasswordChange = { password = it },
            onDismiss = { showDialog = false },
            onProvision = { ssid, pwd ->
                showDialog = false
                navController.navigate("provision")
            }
        )
    }
}

@Composable
fun WiFiItem(wifiAp: WiFiAccessPoint, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "SSID: ${wifiAp.wifiName}")
        }
    }
}

@Composable
fun NetworkDialog(
    ssid: String,
    networkName: TextFieldValue,
    password: TextFieldValue,
    onNetworkNameChange: (TextFieldValue) -> Unit,
    onPasswordChange: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onProvision: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ssid == "Join Other Network") "Join Other Network" else ssid) },
        text = {
            Column {
                if (ssid == "Join Other Network") {
                    OutlinedTextField(
                        value = networkName,
                        onValueChange = onNetworkNameChange,
                        label = { Text("SSID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (ssid == "Join Other Network" && TextUtils.isEmpty(networkName.text)) {
                    // Handle empty SSID error
                } else if (TextUtils.isEmpty(password.text) && ssid != ESPConstants.WIFI_OPEN.toString()) {
                    // Handle empty password error
                } else {
                    onProvision(networkName.text, password.text)
                }
            }) {
                Text("Provision")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


/*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.plantgurucompose.R
import com.example.plantgurucompose.constants.AppConstants
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.WiFiAccessPoint
import com.espressif.provisioning.listeners.WiFiScanListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class WiFiScanActivity : ComponentActivity() {

    private val provisionManager by lazy { ESPProvisionManager.getInstance(applicationContext) }
    private val wifiAPList = mutableStateListOf<WiFiAccessPoint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WiFiScanScreen(
                wifiAPList = wifiAPList,
                onRefresh = { startWifiScan() },
                onItemClick = { ssid, security -> handleNetworkSelection(ssid, security) }
            )
        }

        EventBus.getDefault().register(this)
        startWifiScan()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        provisionManager.getEspDevice().disconnectDevice()
        super.onBackPressed()
    }

    @Composable
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent) {
        when (event.eventType) {
            ESPConstants.EVENT_DEVICE_DISCONNECTED -> showAlertForDeviceDisconnected()
        }
    }

    private fun startWifiScan() {
        wifiAPList.clear()

        provisionManager.getEspDevice().scanNetworks(object : WiFiScanListener {
            override fun onWifiListReceived(wifiList: ArrayList<WiFiAccessPoint>) {
                wifiAPList.addAll(wifiList)
                wifiAPList.add(WiFiAccessPoint().apply { wifiName = getString(R.string.join_other_network) })
            }

            override fun onWiFiScanFailed(e: Exception) {
                Log.e(TAG, "onWiFiScanFailed", e)
                Toast.makeText(this@WiFiScanActivity, "Failed to get Wi-Fi scan list", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleNetworkSelection(ssid: String, security: Int) {
        // Handle network selection and provisioning
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun showAlertForDeviceDisconnected() {
        AlertDialog().Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(R.string.dialog_msg_ble_device_disconnection)
            .setPositiveButton(R.string.btn_ok) { dialog, _ -> dialog.dismiss(); finish() }
            .show()
    }

    companion object {
        private const val TAG = "WiFiScanActivity"
    }

}
/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiScanScreen(
    wifiAPList: List<WiFiAccessPoint>,
    onRefresh: () -> Unit,
    onItemClick: (ssid: String, security: Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_activity_wifi_scan_list)) }
            )
        },
        content = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onRefresh) {
                        Text(text = stringResource(R.string.refresh))
                    }
                }
                LazyColumn {
                    items(wifiAPList) { wifi ->
                        WiFiItem(wifi, onItemClick)
                    }
                }
            }
        }
    )
}

@Composable
fun WiFiItem(wifi: WiFiAccessPoint, onItemClick: (ssid: String, security: Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick(wifi.wifiName, wifi.security) }
    ) {
        Image(
            painter = rememberImagePainter(data = R.drawable.ic_wifi),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = wifi.wifiName)
    }
}*/