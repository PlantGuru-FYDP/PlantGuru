package com.example.plantgurucompose.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.ProvisionListener
import com.espressif.provisioning.listeners.ResponseListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantgurucompose.constants.AppConstants


class ProvisionViewModel(context: Context, ssid: String, passphrase: String) : ViewModel() {

    private val provisionManager = ESPProvisionManager.getInstance(context)
    private val _uiState = MutableStateFlow(ProvisionUiState(ssid = ssid, passphrase = passphrase))
    val uiState: StateFlow<ProvisionUiState> = _uiState

    init {
        EventBus.getDefault().register(this)
        startProvisioning()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent) {
        if (event.eventType == ESPConstants.EVENT_DEVICE_DISCONNECTED) {
            if (!_uiState.value.isProvisioningCompleted) {
                _uiState.value = _uiState.value.copy(isDeviceDisconnected = true)
            }
        }
    }

    override fun onCleared() {
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }

    private fun updateStepState(step: ProvisionStep, state: StepState) {
        val steps = _uiState.value.steps.toMutableList()
        steps[step.ordinal] = state
        _uiState.value = _uiState.value.copy(steps = steps)
    }

    private fun startProvisioning() {
        updateStepState(ProvisionStep.SEND_WIFI_CREDENTIALS, StepState.IN_PROGRESS)
        provisionManager.espDevice.provision(_uiState.value.ssid, _uiState.value.passphrase, object : ProvisionListener {
            override fun createSessionFailed(e: Exception?) {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.SEND_WIFI_CREDENTIALS, StepState.FAILED)
                    _uiState.value = _uiState.value.copy(provisioningError = "Session creation failed")
                }
            }

            override fun wifiConfigSent() {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.SEND_WIFI_CREDENTIALS, StepState.COMPLETED)
                    updateStepState(ProvisionStep.APPLY_WIFI_CREDENTIALS, StepState.IN_PROGRESS)
                }
            }

            override fun wifiConfigFailed(e: Exception?) {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.APPLY_WIFI_CREDENTIALS, StepState.FAILED)
                    _uiState.value = _uiState.value.copy(provisioningError = "WiFi config failed")
                }
            }

            override fun wifiConfigApplied() {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.APPLY_WIFI_CREDENTIALS, StepState.COMPLETED)
                    updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.IN_PROGRESS)
                }
            }

            override fun wifiConfigApplyFailed(e: Exception?) {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.FAILED)
                    _uiState.value = _uiState.value.copy(provisioningError = "WiFi config apply failed")
                }
            }

            override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason) {
                viewModelScope.launch {
                    val error = when (failureReason) {
                        ESPConstants.ProvisionFailureReason.AUTH_FAILED -> "Authentication failed"
                        ESPConstants.ProvisionFailureReason.NETWORK_NOT_FOUND -> "Network not found"
                        else -> "Provisioning failed"
                    }
                    updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.FAILED)
                    _uiState.value = _uiState.value.copy(provisioningError = error)
                }
            }

            override fun deviceProvisioningSuccess() {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.COMPLETED)
                    updateStepState(ProvisionStep.SEND_USER_TOKEN, StepState.IN_PROGRESS)
                    sendDataToCustomEndPoint()
                }
            }

            override fun onProvisioningFailed(e: Exception?) {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.FAILED)
                    _uiState.value = _uiState.value.copy(provisioningError = "Provisioning failed")
                }
            }
        })
    }

    private fun sendDataToCustomEndPoint() {
        val userToken = "android"
        provisionManager.espDevice.sendDataToCustomEndPoint(AppConstants.USER_TOKEN_ENDPOINT, userToken.toByteArray(), object : ResponseListener {
            override fun onSuccess(returnData: ByteArray?) {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.SEND_USER_TOKEN, StepState.COMPLETED)
                    _uiState.value = _uiState.value.copy(isProvisioningCompleted = true)
                }
            }

            override fun onFailure(e: Exception?) {
                viewModelScope.launch {
                    updateStepState(ProvisionStep.SEND_USER_TOKEN, StepState.FAILED)
                    _uiState.value = _uiState.value.copy(provisioningError = "Failed at sending user token")
                }
            }
        })
    }

    fun handleOkButtonClick() {
        //provisionManager.espDevice.disconnectDevice()
        _uiState.value = _uiState.value.copy(finishActivity = true)
    }
}

enum class ProvisionStep {
    SEND_WIFI_CREDENTIALS,
    APPLY_WIFI_CREDENTIALS,
    CHECK_PROVISIONING_STATUS,
    SEND_USER_TOKEN
}

enum class StepState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class ProvisionUiState(
    val ssid: String = "",
    val passphrase: String = "",
    val isProvisioningCompleted: Boolean = false,
    val steps: List<StepState> = List(ProvisionStep.values().size) { StepState.NOT_STARTED },
    val provisioningError: String = "",
    val isDeviceDisconnected: Boolean = false,
    val finishActivity: Boolean = false
)

class ProvisionViewModelFactory(private val context: Context, private val ssid: String, private val passphrase: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProvisionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProvisionViewModel(context, ssid, passphrase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun ProvisionScreen(
    navController: NavController,
    ssid: String,
    passphrase: String,
    viewModel: ProvisionViewModel = viewModel(factory = ProvisionViewModelFactory(LocalContext.current, ssid, passphrase))
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.finishActivity) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }

    if (uiState.isDeviceDisconnected) {
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
        Text("Provisioning", style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp))

        ProvisionStep("Step 1: Sending WiFi credentials", uiState.steps[ProvisionStep.SEND_WIFI_CREDENTIALS.ordinal])
        ProvisionStep("Step 2: Applying WiFi credentials", uiState.steps[ProvisionStep.APPLY_WIFI_CREDENTIALS.ordinal])
        ProvisionStep("Step 3: Checking provisioning status", uiState.steps[ProvisionStep.CHECK_PROVISIONING_STATUS.ordinal])
        ProvisionStep("Step 4: Sending user token", uiState.steps[ProvisionStep.SEND_USER_TOKEN.ordinal])

        if (uiState.provisioningError.isNotEmpty()) {
            Text(uiState.provisioningError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.handleOkButtonClick()
                navController.navigate("connection_home")
                      },
            enabled = uiState.isProvisioningCompleted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("OK")
        }
    }
}

@Composable
fun ProvisionStep(title: String, state: StepState) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        when (state) {
            StepState.IN_PROGRESS -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
            StepState.COMPLETED -> Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Green, modifier = Modifier.size(24.dp))
            StepState.FAILED -> Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
            else -> Spacer(modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title)
    }
}

/*
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.ProvisionListener
import com.espressif.provisioning.listeners.ResponseListener
import com.example.plantgurucompose.R
import com.example.plantgurucompose.constants.AppConstants
import com.example.plantgurucompose.models.Plant
import com.example.plantgurucompose.utils.SharedPreferencesHelper
import kotlinx.coroutines.launch

class ProvisionActivity : ComponentActivity() {

    private lateinit var provisionManager: ESPProvisionManager
    private lateinit var device: BluetoothDevice
    private var isProvisioningCompleted by mutableStateOf(false)
    private var ssidValue by mutableStateOf("")
    private var passphraseValue by mutableStateOf("")
    private var userToken by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize variables from intent
        ssidValue = intent.getStringExtra(AppConstants.KEY_WIFI_SSID) ?: ""
        passphraseValue = intent.getStringExtra(AppConstants.KEY_WIFI_PASSWORD) ?: ""
        userToken = "android" // Retrieve the user token from the intent

        provisionManager = ESPProvisionManager.getInstance(applicationContext)

        setContent {
            ProvisionScreen(
                ssidValue = ssidValue,
                passphraseValue = passphraseValue,
                userToken = userToken,
                onProvisioningCompleted = { isCompleted ->
                    isProvisioningCompleted = isCompleted
                },
                provisionManager = provisionManager
            )
        }

        Log.d(TAG, "Selected AP - $ssidValue")
    }

    override fun onBackPressed() {
        provisionManager.getEspDevice().disconnectDevice()
        super.onBackPressed()
    }

    companion object {
        private const val TAG = "ProvisionActivity"
    }
}

@Composable
fun ProvisionScreen(
    ssidValue: String,
    passphraseValue: String,
    userToken: String,
    onProvisioningCompleted: (Boolean) -> Unit,
    provisionManager: ESPProvisionManager
) {
    var isProvisioningInProgress by remember { mutableStateOf(true) }
    var provisioningStep by remember { mutableStateOf(0) }
    var provisioningError by remember { mutableStateOf("") }
    var provisioningSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        sendDataToCustomEndPoint(
            provisionManager,
            userToken,
            onProvisioningStep = { step -> provisioningStep = step },
            onProvisioningError = { error -> provisioningError = error },
            onProvisioningCompleted = { success ->
                onProvisioningCompleted(success)
                provisioningSuccess = success
                isProvisioningInProgress = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Provisioning Device",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isProvisioningInProgress) {
            CircularProgressIndicator()
        } else {
            if (provisioningSuccess) {
                Text(
                    text = "Provisioning Completed Successfully",
                    color = Color.Green
                )
                Button(onClick = { /* Handle OK action */ }) {
                    Text(text = "OK")
                }
            } else {
                Text(
                    text = "Provisioning Failed: $provisioningError",
                    color = Color.Red
                )
                Button(onClick = { /* Handle OK action */ }) {
                    Text(text = "Retry")
                }
            }
        }

        ProvisioningStepIndicator(provisioningStep = provisioningStep)
    }
}

@Composable
fun ProvisioningStepIndicator(provisioningStep: Int) {
    val steps = listOf("Sending Wi-Fi Credentials", "Applying Wi-Fi Credentials", "Checking Provisioning Status", "Sending User Token")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (provisioningStep > index) R.drawable.ic_checkbox_on else R.drawable.ic_checkbox_unselected),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = step)
            }
        }
    }
}

private fun sendDataToCustomEndPoint(
    provisionManager: ESPProvisionManager,
    userToken: String,
    onProvisioningStep: (Int) -> Unit,
    onProvisioningError: (String) -> Unit,
    onProvisioningCompleted: (Boolean) -> Unit
) {
    onProvisioningStep(1)
    provisionManager.getEspDevice().sendDataToCustomEndPoint(
        AppConstants.USER_TOKEN_ENDPOINT,
        userToken.toByteArray(),
        object : ResponseListener {
            override fun onSuccess(returnData: ByteArray?) {
                onProvisioningStep(2)
                doProvisioning(provisionManager, onProvisioningStep, onProvisioningError, onProvisioningCompleted)
            }

            override fun onFailure(e: Exception) {
                onProvisioningError(e.message ?: "Unknown Error")
                onProvisioningCompleted(false)
            }
        }
    )
}

private fun doProvisioning(
    provisionManager: ESPProvisionManager,
    onProvisioningStep: (Int) -> Unit,
    onProvisioningError: (String) -> Unit,
    onProvisioningCompleted: (Boolean) -> Unit
) {
    provisionManager.getEspDevice().provision("", "", object : ProvisionListener {
        override fun createSessionFailed(e: Exception?) {
            onProvisioningError("Session Creation Failed")
            onProvisioningCompleted(false)
        }

        override fun wifiConfigSent() {
            onProvisioningStep(3)
        }

        override fun wifiConfigFailed(e: Exception?) {
            onProvisioningError("Wi-Fi Config Failed")
            onProvisioningCompleted(false)
        }

        override fun wifiConfigApplied() {
            onProvisioningStep(4)
        }

        override fun wifiConfigApplyFailed(e: Exception?) {
            onProvisioningError("Wi-Fi Config Apply Failed")
            onProvisioningCompleted(false)
        }

        override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason?) {
            val error = when (failureReason) {
                ESPConstants.ProvisionFailureReason.AUTH_FAILED -> "Authentication Failed"
                ESPConstants.ProvisionFailureReason.NETWORK_NOT_FOUND -> "Network Not Found"
                ESPConstants.ProvisionFailureReason.DEVICE_DISCONNECTED,
                ESPConstants.ProvisionFailureReason.UNKNOWN -> "Provisioning Failed"
                else -> "Unknown Error"
            }
            onProvisioningError(error)
            onProvisioningCompleted(false)
        }

        override fun deviceProvisioningSuccess() {
            onProvisioningCompleted(true)
        }

        override fun onProvisioningFailed(e: Exception?) {
            onProvisioningError("Provisioning Failed")
            onProvisioningCompleted(false)
        }
    })
}
*/