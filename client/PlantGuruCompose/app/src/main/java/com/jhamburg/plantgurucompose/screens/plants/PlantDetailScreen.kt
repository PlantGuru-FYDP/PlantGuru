package com.jhamburg.plantgurucompose.screens.plants

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.components.DateTimePickerDialog
import com.jhamburg.plantgurucompose.components.PlantCareTab
import com.jhamburg.plantgurucompose.components.PlantHistoryTab
import com.jhamburg.plantgurucompose.components.PlantOptionsMenu
import com.jhamburg.plantgurucompose.components.PlantOverviewTab
import com.jhamburg.plantgurucompose.components.PlantProjectionsTab
import com.jhamburg.plantgurucompose.components.PlantSensorsTab
import com.jhamburg.plantgurucompose.models.SensorType
import com.jhamburg.plantgurucompose.models.TimeRange
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import com.jhamburg.plantgurucompose.viewmodels.ProjectionsViewModel
import com.jhamburg.plantgurucompose.viewmodels.SensorDataViewModel
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel
import com.jhamburg.plantgurucompose.viewmodels.WateringEventViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

enum class SensorStatus {
    NEVER_CONNECTED,    // No provisioning record
    PROVISIONING,       // In provisioning process
    DISCONNECTED,       // Provisioned but no recent data
    CONNECTED,          // Provisioned and recent data
    FAILED              // Provisioning failed
}

@Composable
private fun UnconnectedPlantScreen(
    plantName: String,
    onConnectClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.baseline_sensors_24),
            contentDescription = "Sensor Icon",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = plantName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Connect a sensor to start monitoring your plant",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Button(
                onClick = onConnectClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Connect Sensor")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    var selectedTimeRange by remember { mutableStateOf<TimeRange>(TimeRange.WEEK_1) }
    var selectedSensorType by remember { mutableStateOf(SensorType.SOIL_MOISTURE) }
    var customStartDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var customEndDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var showGranularityDropdown by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val plantViewModel: PlantViewModel = hiltViewModel()
    val sensorDataViewModel: SensorDataViewModel = hiltViewModel()
    val wateringEventViewModel: WateringEventViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val projectionsViewModel: ProjectionsViewModel = hiltViewModel()

    val sensorData by sensorDataViewModel.sensorData.collectAsState()
    val currentSensorData by sensorDataViewModel.currentSensorData.collectAsState()
    val sensorLoading by sensorDataViewModel.loading.collectAsState()
    val sensorError by sensorDataViewModel.error.collectAsState()

    val wateringEvents by wateringEventViewModel.wateringEvents.collectAsState()
    val wateringLoading by wateringEventViewModel.loading.collectAsState()
    val wateringError by wateringEventViewModel.error.collectAsState()

    val plants by plantViewModel.plants.collectAsState()
    val plantLoading by plantViewModel.loading.collectAsState()
    val plantError by plantViewModel.error.collectAsState()
    val prediction by plantViewModel.prediction.collectAsState()
    val plantDetails by plantViewModel.plantDetails.collectAsState()
    val user by userViewModel.user.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Sensors", "Projections")

    val currentPlant = plants.firstOrNull { it.plantId == plantId }

    val sensorStatus = remember(currentPlant?.lastSensorReading, currentPlant?.provisioningStatus) {
        when {
            currentPlant?.lastSensorReading == null && currentPlant?.provisioningStatus == "BACKEND_VERIFIED" ->
                SensorStatus.PROVISIONING

            currentPlant?.lastSensorReading == null ->
                SensorStatus.NEVER_CONNECTED

            currentPlant.provisioningStatus in listOf(
                "PENDING",
                "DEVICE_CONNECTED",
                "WIFI_SETUP"
            ) ->
                SensorStatus.PROVISIONING

            currentPlant.provisioningStatus == "BACKEND_VERIFIED" &&
                    !isOlderThanOneDay(currentPlant.lastSensorReading) ->
                SensorStatus.CONNECTED

            else -> SensorStatus.DISCONNECTED
        }
    }

    val isConnected = sensorStatus == SensorStatus.CONNECTED

    fun refreshData() {
        isRefreshing = true
        sensorDataViewModel.getLastNSensorReadings(plantId, 1)
        wateringEventViewModel.getWateringEvents(plantId)
        plantViewModel.predictNextWatering(plantId)
        plantViewModel.getPlants(12)
        user?.let { plantViewModel.getPlants(it.userId) }
        isRefreshing = false
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    var previousLifecycleState by remember { mutableStateOf<Lifecycle.State?>(null) }
    var isFirstResume by remember { mutableStateOf(true) }

    LaunchedEffect(selectedTimeRange, customStartDate, customEndDate) {
        val endTime = when (selectedTimeRange) {
            TimeRange.CUSTOM -> customEndDate ?: LocalDateTime.now(ZoneId.of("UTC"))
            else -> LocalDateTime.now(ZoneId.of("UTC"))
        }

        val startTime = when (selectedTimeRange) {
            TimeRange.CUSTOM -> customStartDate
                ?: endTime.minusHours(TimeRange.WEEK_1.hours.toLong())

            else -> endTime.minusHours(selectedTimeRange.hours.toLong())
        }

        sensorDataViewModel.getTimeSeriesData(
            plantId = plantId,
            startTime = startTime.toString() + "Z",
            endTime = endTime.toString() + "Z",
            granularity = selectedTimeRange.defaultGranularity
        )
    }

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            if (isFirstResume) {
                isFirstResume = false
            } else if (previousLifecycleState == Lifecycle.State.STARTED) {
                user?.let {
                    plantViewModel.getPlants(it.userId, forceRefresh = true)
                }
            }
        }
        previousLifecycleState = lifecycleState
    }

    LaunchedEffect(Unit) {
        sensorDataViewModel.getLastNSensorReadings(plantId, 1)

        // Get data for mini graphs (last 12 hours)
        val endTime = LocalDateTime.now(ZoneId.of("UTC"))
        val startTime = endTime.minusHours(12)

        sensorDataViewModel.getTimeSeriesData(
            plantId = plantId,
            startTime = startTime.toString() + "Z",
            endTime = endTime.toString() + "Z",
            granularity = 60
        )

        wateringEventViewModel.getWateringEvents(plantId)
        plantViewModel.predictNextWatering(plantId)
        plantViewModel.getPlants(12)
        user?.let {
            plantViewModel.getPlants(it.userId)
        }
        wateringEventViewModel.getLastWateringEvent(plantId)
    }

    var isNavigatingBack by remember { mutableStateOf(false) }

    BackHandler {
        plantViewModel.setNeedsRefresh(true)
        navController.navigateUp()
    }

    LaunchedEffect(Unit) {
        val callback = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route != "plantDetail/{plantId}") {
                plantViewModel.setNeedsRefresh(true)
            }
        }
        navController.addOnDestinationChangedListener(callback)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = plants.firstOrNull { it.plantId == plantId }?.plantName
                                ?: "Plant Details",
                            style = MaterialTheme.typography.titleLarge
                        )
                        plantDetails[plantId]?.let { details ->
                            val category = details.getCategory()
                            val subType = details.getSubType()
                            if (category != null && subType != null) {
                                Text(
                                    text = "$category - $subType",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        plantViewModel.setNeedsRefresh(true)
                        navController.navigateUp() 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {

                    IconButton(onClick = { navController.navigate("plantSettings/$plantId") }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }

                    IconButton(
                        onClick = {
                            sensorDataViewModel.getLastNSensorReadings(plantId, 1)
                            wateringEventViewModel.getWateringEvents(plantId)
                            plantViewModel.predictNextWatering(plantId)
                            plantViewModel.getPlants(12)
                            user?.let { plantViewModel.getPlants(it.userId) }

                            navController.navigate("plantDetail/$plantId") {
                                popUpTo("plantDetail/$plantId") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_refresh_24),
                            contentDescription = "Refresh"
                        )
                    }

                    // Three dot menu with Edit and Direct Sensor/Connect options
                    PlantOptionsMenu(
                        onEditClick = { navController.navigate("editPlant/$plantId") },
                        onDirectSensorClick = {
                            if (isConnected) {
                                navController.navigate("directSensor/$plantId")
                            } else {
                                navController.navigate("ble_provision_landing/$plantId/null")
                            }
                        },
                        showDirectSensor = true,
                        isConnected = isConnected,
                        showSettings = false
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = when (index) {
                                    0 -> painterResource(R.drawable.baseline_home_24)
                                    1 -> painterResource(R.drawable.baseline_sensors_24)
                                    2 -> painterResource(R.drawable.baseline_query_stats_24)
                                    else -> painterResource(R.drawable.baseline_home_24)
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (sensorStatus == SensorStatus.NEVER_CONNECTED) {
            UnconnectedPlantScreen(
                plantName = currentPlant?.plantName?.replaceFirstChar { it.uppercase() }
                    ?: "Your Plant",
                onConnectClick = {
                    navController.navigate("ble_provision_landing/$plantId/null")
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }

                        slideIntoContainer(
                            towards = direction,
                            animationSpec = tween(300)
                        ) togetherWith slideOutOfContainer(
                            towards = direction,
                            animationSpec = tween(300)
                        )
                    },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> PlantOverviewTab(
                            plant = plants.firstOrNull { it.plantId == plantId },
                            details = plantDetails[plantId],
                            currentSensorData = currentSensorData,
                            prediction = prediction,
                            sensorData = sensorData,
                            sensorDataViewModel = sensorDataViewModel,
                            lastWateringEvent = wateringEventViewModel.lastWateringEvent.collectAsState().value
                        )

                        1 -> PlantSensorsTab(
                            sensorData = sensorData,
                            selectedTimeRange = selectedTimeRange,
                            selectedSensorType = selectedSensorType,
                            onTimeRangeSelected = {
                                selectedTimeRange = it
                            },
                            onSensorTypeSelected = { selectedSensorType = it },
                            sensorDataViewModel = sensorDataViewModel,
                        )

                        2 -> PlantProjectionsTab(
                            plantId = plantId,
                            selectedTimeRange = selectedTimeRange,
                            selectedSensorType = selectedSensorType,
                            onTimeRangeSelected = { newRange ->
                                selectedTimeRange = newRange
                            },
                            onSensorTypeSelected = { selectedSensorType = it },
                            projectionsViewModel = projectionsViewModel
                        )
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        DateTimePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateTimeSelected = { dateTime ->
                customStartDate = dateTime
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        DateTimePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateTimeSelected = { dateTime ->
                customEndDate = dateTime
                showEndDatePicker = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant") },
            text = { Text("Are you sure you want to delete this plant? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val userId = user?.userId
                                plantViewModel.deletePlant(plantId)
                                if (userId != null) {
                                    navController.navigate("plantList") {
                                        popUpTo("plantList") { inclusive = true }
                                    }
                                } else {
                                    Log.e("PlantDetailScreen", "User ID is null")
                                    navController.popBackStack()
                                }
                            } catch (e: Exception) {
                                Log.e("PlantDetailScreen", "Error deleting plant", e)
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
