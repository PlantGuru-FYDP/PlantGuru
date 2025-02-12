package com.jhamburg.plantgurucompose.screens.provisioning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.espressif.provisioning.listeners.ProvisionListener
import com.espressif.provisioning.listeners.ResponseListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
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
import com.jhamburg.plantgurucompose.constants.AppConstants
import org.json.JSONObject
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel
import javax.inject.Inject
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.jhamburg.plantgurucompose.R
import kotlinx.coroutines.withTimeout

class ProvisionViewModel @Inject constructor(
    context: Context, 
    ssid: String, 
    passphrase: String,
    private val plantId: Int,
    private val provisionToken: String,
) : ViewModel() {

    private val provisionManager = ESPProvisionManager.getInstance(context)
    private val _uiState = MutableStateFlow(ProvisionUiState(ssid = ssid, passphrase = passphrase))
    val uiState: StateFlow<ProvisionUiState> = _uiState

    private var isProvisioningCompleted = false

    var isEnterpriseWifi: Boolean = false
        private set
    var enterpriseIdentity: String = ""
        private set
    var enterpriseUsername: String = ""
        private set
    var enterprisePassword: String = ""
        private set

    init {
        EventBus.getDefault().register(this)
        startProvisioning()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DeviceConnectionEvent) {
        if (event.eventType == ESPConstants.EVENT_DEVICE_DISCONNECTED) {
            viewModelScope.launch {
                Log.d("ProvisionViewModel", "Device disconnected event received")
                
                if (isProvisioningCompleted) {
                    Log.d("ProvisionViewModel", "Expected disconnection after successful provisioning")
                    updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.COMPLETED)
                    updateStepState(ProvisionStep.BACKEND_VERIFIED, StepState.COMPLETED)
                    _uiState.value = _uiState.value.copy(
                        isProvisioningCompleted = true,
                        isDeviceDisconnected = false
                    )
                } else {
                    Log.e("ProvisionViewModel", "Device disconnected before provisioning completed")
                    _uiState.value = _uiState.value.copy(
                        isDeviceDisconnected = true,
                        provisioningError = "Device disconnected unexpectedly"
                    )
                }
            }
        }
    }

    override fun onCleared() {
        if (provisionManager.espDevice != null) {
            provisionManager.espDevice.disconnectDevice()
        }
        EventBus.getDefault().unregister(this)
        super.onCleared()
    }

    private fun updateStepState(step: ProvisionStep, state: StepState) {
        val steps = _uiState.value.steps.toMutableList()
        steps[step.ordinal] = state
        _uiState.value = _uiState.value.copy(steps = steps)
    }

    private fun startProvisioning() {
        viewModelScope.launch {
            try {
                sendProvisionToken()
            } catch (e: Exception) {
                Log.e("ProvisionViewModel", "Error in provisioning", e)
                _uiState.value = _uiState.value.copy(
                    provisioningError = "Failed to start provisioning: ${e.message}"
                )
            }
        }
    }

    private fun sendProvisionToken() {
        updateStepState(ProvisionStep.SEND_PROVISION_TOKEN, StepState.IN_PROGRESS)
        val tokenJson = JSONObject().apply {
            put("provision_token", provisionToken)
        }
        
        provisionManager.espDevice.sendDataToCustomEndPoint(
            AppConstants.PROVISION_TOKEN_ENDPOINT,
            tokenJson.toString().toByteArray(),
            object : ResponseListener {
                override fun onSuccess(data: ByteArray?) {
                    viewModelScope.launch {
                        updateStepState(ProvisionStep.SEND_PROVISION_TOKEN, StepState.COMPLETED)
                        
                        if (isEnterpriseWifi) {
                            sendEnterpriseConfig()
                        } else {
                            setupWiFi()
                        }
                    }
                }

                override fun onFailure(e: Exception) {
                    viewModelScope.launch {
                        Log.e("ProvisionViewModel", "Failed to send provision token", e)
                        updateStepState(ProvisionStep.SEND_PROVISION_TOKEN, StepState.FAILED)
                        _uiState.value = _uiState.value.copy(
                            provisioningError = "Failed to send provision token: ${e.message}"
                        )
                    }
                }
            }
        )
    }

    private fun sendEnterpriseConfig() {
        viewModelScope.launch {
            try {
                Log.d("ProvisionViewModel", "Starting enterprise WiFi config")
                updateStepState(ProvisionStep.SEND_ENTERPRISE_CONFIG, StepState.IN_PROGRESS)
                
                val enterpriseJson = JSONObject().apply {
                    put("ssid", _uiState.value.ssid)
                    put("isEnterprise", true)
                    put("identity", enterpriseIdentity)
                    put("username", enterpriseUsername)
                    put("password", enterprisePassword)
                }

                Log.d("ProvisionViewModel", "Sending enterprise config to endpoint: enterprise-wifi")
                Log.d("ProvisionViewModel", "Enterprise config data: $enterpriseJson")
                
                val configSent = CompletableDeferred<Boolean>()
                
                provisionManager.espDevice.sendDataToCustomEndPoint(
                    "enterprise-wifi",
                    enterpriseJson.toString().toByteArray(),
                    object : ResponseListener {
                        override fun onSuccess(data: ByteArray?) {
                            viewModelScope.launch {
                                val response = data?.let { String(it) }
                                Log.d("ProvisionViewModel", "Enterprise config response: $response")
                                updateStepState(ProvisionStep.SEND_ENTERPRISE_CONFIG, StepState.COMPLETED)
                                configSent.complete(true)
                            }
                        }

                        override fun onFailure(e: Exception) {
                            viewModelScope.launch {
                                Log.e("ProvisionViewModel", "Enterprise config failed", e)
                                Log.e("ProvisionViewModel", "Error details: ${e.message}")
                                e.printStackTrace()
                                updateStepState(ProvisionStep.SEND_ENTERPRISE_CONFIG, StepState.FAILED)
                                _uiState.value = _uiState.value.copy(
                                    provisioningError = "Failed to send enterprise config: ${e.message}"
                                )
                                configSent.complete(false)
                            }
                        }
                    }
                )

                if (configSent.await()) {
                    Log.d("ProvisionViewModel", "Enterprise config successful, continuing with WiFi setup")
                    delay(1000)
                    setupWiFi()
                } else {
                    Log.e("ProvisionViewModel", "Enterprise config failed, stopping provisioning")
                }
            } catch (e: Exception) {
                Log.e("ProvisionViewModel", "Exception in enterprise config", e)
                updateStepState(ProvisionStep.SEND_ENTERPRISE_CONFIG, StepState.FAILED)
                _uiState.value = _uiState.value.copy(
                    provisioningError = "Failed to configure enterprise WiFi: ${e.message}"
                )
            }
        }
    }

    private fun setupWiFi() {
        viewModelScope.launch {
            try {
                Log.d("ProvisionViewModel", "Starting WiFi setup")
                updateStepState(ProvisionStep.WIFI_SETUP, StepState.IN_PROGRESS)

                provisionManager.espDevice.provision(_uiState.value.ssid, _uiState.value.passphrase,
                    object : ProvisionListener {
                        override fun createSessionFailed(e: Exception?) {
                            viewModelScope.launch {
                                Log.e("ProvisionViewModel", "Failed to create session", e)
                                updateStepState(ProvisionStep.SEND_WIFI_CREDENTIALS, StepState.FAILED)
                                _uiState.value = _uiState.value.copy(
                                    provisioningError = "Failed to create session: ${e?.message}"
                                )
                            }
                        }

                        override fun wifiConfigSent() {
                            viewModelScope.launch {
                                Log.d("ProvisionViewModel", "WiFi config sent successfully")
                                updateStepState(ProvisionStep.SEND_WIFI_CREDENTIALS, StepState.COMPLETED)
                                updateStepState(ProvisionStep.APPLY_WIFI_CREDENTIALS, StepState.IN_PROGRESS)
                            }
                        }

                        override fun wifiConfigFailed(e: Exception?) {
                            viewModelScope.launch {
                                Log.e("ProvisionViewModel", "Failed to send WiFi config", e)
                                updateStepState(ProvisionStep.APPLY_WIFI_CREDENTIALS, StepState.FAILED)
                                _uiState.value = _uiState.value.copy(
                                    provisioningError = "Failed to send WiFi config: ${e?.message}"
                                )
                            }
                        }

                        override fun wifiConfigApplied() {
                            viewModelScope.launch {
                                Log.d("ProvisionViewModel", "WiFi config applied successfully")
                                updateStepState(ProvisionStep.APPLY_WIFI_CREDENTIALS, StepState.COMPLETED)
                                updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.IN_PROGRESS)
                                
                                try {
                                    val statusJson = JSONObject().apply {
                                        put("provision_token", provisionToken)
                                        put("status", "WIFI_SETUP")
                                    }
                                    
                                    provisionManager.espDevice.sendDataToCustomEndPoint(
                                        "status",
                                        statusJson.toString().toByteArray(),
                                        object : ResponseListener {
                                            override fun onSuccess(data: ByteArray?) {
                                                viewModelScope.launch {
                                                    Log.d("ProvisionViewModel", "Status update successful")
                                                    isProvisioningCompleted = true
                                                    val completedSteps = List(ProvisionStep.values().size) { StepState.COMPLETED }
                                                    _uiState.value = _uiState.value.copy(
                                                        steps = completedSteps,
                                                        isProvisioningCompleted = true,
                                                        isDeviceDisconnected = false
                                                    )
                                                }
                                            }

                                            override fun onFailure(e: Exception) {
                                                viewModelScope.launch {
                                                    Log.e("ProvisionViewModel", "Failed to update status", e)
                                                    isProvisioningCompleted = true
                                                    val completedSteps = List(ProvisionStep.values().size) { StepState.COMPLETED }
                                                    _uiState.value = _uiState.value.copy(
                                                        steps = completedSteps,
                                                        isProvisioningCompleted = true,
                                                        isDeviceDisconnected = false
                                                    )
                                                }
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    Log.e("ProvisionViewModel", "Error updating status", e)
                                    isProvisioningCompleted = true
                                    val completedSteps = List(ProvisionStep.values().size) { StepState.COMPLETED }
                                    _uiState.value = _uiState.value.copy(
                                        steps = completedSteps,
                                        isProvisioningCompleted = true,
                                        isDeviceDisconnected = false
                                    )
                                }
                            }
                        }

                        override fun wifiConfigApplyFailed(e: Exception?) {
                            viewModelScope.launch {
                                Log.e("ProvisionViewModel", "Failed to apply WiFi config", e)
                                updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.FAILED)
                                _uiState.value = _uiState.value.copy(
                                    provisioningError = "Failed to apply WiFi config: ${e?.message}"
                                )
                            }
                        }

                        override fun provisioningFailedFromDevice(failureReason: ESPConstants.ProvisionFailureReason) {
                            viewModelScope.launch {
                                val error = when (failureReason) {
                                    ESPConstants.ProvisionFailureReason.AUTH_FAILED -> "Authentication failed"
                                    ESPConstants.ProvisionFailureReason.NETWORK_NOT_FOUND -> "Network not found"
                                    else -> "Provisioning failed"
                                }
                                Log.e("ProvisionViewModel", "Provisioning failed: $error")
                                updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.FAILED)
                                _uiState.value = _uiState.value.copy(provisioningError = error)
                            }
                        }

                        override fun deviceProvisioningSuccess() {
                            viewModelScope.launch {
                                Log.d("ProvisionViewModel", "Device provisioning successful")
                                isProvisioningCompleted = true
                                val completedSteps = List(ProvisionStep.values().size) { StepState.COMPLETED }
                                _uiState.value = _uiState.value.copy(
                                    steps = completedSteps,
                                    isProvisioningCompleted = true,
                                    isDeviceDisconnected = false
                                )
                            }
                        }

                        override fun onProvisioningFailed(e: Exception?) {
                            viewModelScope.launch {
                                Log.e("ProvisionViewModel", "Provisioning failed", e)
                                updateStepState(ProvisionStep.CHECK_PROVISIONING_STATUS, StepState.FAILED)
                                _uiState.value = _uiState.value.copy(
                                    provisioningError = "Provisioning failed: ${e?.message}",
                                    isProvisioningCompleted = false
                                )
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("ProvisionViewModel", "Error in WiFi setup", e)
                updateStepState(ProvisionStep.WIFI_SETUP, StepState.FAILED)
                _uiState.value = _uiState.value.copy(
                    provisioningError = "Failed to setup WiFi: ${e.message}"
                )
            }
        }
    }

    fun handleOkButtonClick() {
        if (_uiState.value.isProvisioningCompleted) {
            _uiState.value = _uiState.value.copy(finishActivity = true)
        }
    }

    fun setEnterpriseCredentials(identity: String, username: String, password: String) {
        Log.d("ProvisionViewModel", "Setting enterprise credentials - Identity: $identity, Username: $username")
        isEnterpriseWifi = true
        enterpriseIdentity = identity
        enterpriseUsername = username
        enterprisePassword = password
    }

    fun disconnectDevice() {
        provisionManager.espDevice?.disconnectDevice()
    }

}

enum class ProvisionStep {
    SEND_ENTERPRISE_CONFIG,
    SEND_PROVISION_TOKEN,
    SEND_WIFI_CREDENTIALS,
    APPLY_WIFI_CREDENTIALS,
    CHECK_PROVISIONING_STATUS,
    BACKEND_VERIFIED,
    WIFI_SETUP
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

class ProvisionViewModelFactory(
    private val context: Context,
    private val ssid: String,
    private val passphrase: String,
    private val plantId: Int,
    private val provisionToken: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProvisionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProvisionViewModel(
                context, 
                ssid, 
                passphrase, 
                plantId, 
                provisionToken,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun ProvisionScreen(
    navController: NavController,
    ssid: String,
    passphrase: String,
    plantId: Int,
    provisionToken: String,
    isEnterprise: Boolean,
    identity: String?,
    enterprisePassword: String?,
    userViewModel: UserViewModel = hiltViewModel(),
    viewModel: ProvisionViewModel = viewModel(
        factory = ProvisionViewModelFactory(
            LocalContext.current, 
            ssid, 
            passphrase, 
            plantId,
            provisionToken,
        )
    )
) {
    LaunchedEffect(Unit) {
        Log.d("ProvisionScreen", "\n=== WiFi Configuration Details ===")
        Log.d("ProvisionScreen", "SSID: $ssid")
        Log.d("ProvisionScreen", "Is Enterprise: $isEnterprise")
        if (isEnterprise) {
            Log.d("ProvisionScreen", "Identity present: ${!identity.isNullOrEmpty()}")

            Log.d("ProvisionScreen", "Enterprise password present: ${!enterprisePassword.isNullOrEmpty()}")
        }
        Log.d("ProvisionScreen", "===================================\n")

        if (isEnterprise && !identity.isNullOrEmpty() && !enterprisePassword.isNullOrEmpty()) {
            Log.d("ProvisionScreen", "Setting enterprise credentials...")
            viewModel.setEnterpriseCredentials(identity, identity, enterprisePassword)
            Log.d("ProvisionScreen", "Enterprise credentials set successfully")
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val user by userViewModel.user.collectAsState()

    if (uiState.finishActivity) {
        LaunchedEffect(Unit) {
            user?.userId?.let { userId ->
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refresh", true)
                navController.navigate("plantList") {
                    popUpTo("plantList") {
                        inclusive = false
                    }
                }
            }
        }
    }

    if (uiState.isDeviceDisconnected && !uiState.isProvisioningCompleted) {
        AlertDialog(
            onDismissRequest = { navController.navigateUp() },
            title = { Text("Error") },
            text = { Text("Device disconnected unexpectedly") },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.disconnectDevice()
                    navController.navigateUp() 
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (uiState.provisioningError.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { 
                user?.userId?.let { userId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh", true)
                    navController.navigate("plantList") {
                        popUpTo("plantList") {
                            inclusive = false
                        }
                    }
                }
            },
            title = { Text("Provisioning Failed") },
            text = { Text(uiState.provisioningError) },
            confirmButton = {
                TextButton(onClick = {
                    user?.userId?.let { userId ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refresh", true)
                        navController.navigate("plantList") {
                            popUpTo("plantList") {
                                inclusive = false
                            }
                        }
                    }
                }) {
                    Text("Exit")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_wifi_setup),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                "Setting Up Your Device",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProvisionStepItem(
                        title = "Sending provision token",
                        state = uiState.steps[ProvisionStep.SEND_PROVISION_TOKEN.ordinal]
                    )
                    ProvisionStepItem(
                        title = "Sending WiFi credentials",
                        state = uiState.steps[ProvisionStep.SEND_WIFI_CREDENTIALS.ordinal]
                    )
                    ProvisionStepItem(
                        title = "Applying WiFi credentials",
                        state = uiState.steps[ProvisionStep.APPLY_WIFI_CREDENTIALS.ordinal]
                    )
                    ProvisionStepItem(
                        title = "Checking connection",
                        state = uiState.steps[ProvisionStep.CHECK_PROVISIONING_STATUS.ordinal]
                    )
                }
            }

            if (uiState.provisioningError.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_error_24),
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            uiState.provisioningError,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (uiState.isProvisioningCompleted) {
                Button(
                    onClick = { viewModel.handleOkButtonClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Setup Complete")
                    }
                }
            }
        }

        if (!uiState.isProvisioningCompleted && !uiState.provisioningError.isNotEmpty()) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            )
        }
    }
}

@Composable
private fun ProvisionStepItem(
    title: String,
    state: StepState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (state) {
                            StepState.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                            StepState.FAILED -> MaterialTheme.colorScheme.errorContainer
                            StepState.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                            StepState.NOT_STARTED -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    StepState.IN_PROGRESS -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    StepState.COMPLETED -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    StepState.FAILED -> Icon(
                         painter = painterResource(id = R.drawable.baseline_error_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    else -> {}
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}