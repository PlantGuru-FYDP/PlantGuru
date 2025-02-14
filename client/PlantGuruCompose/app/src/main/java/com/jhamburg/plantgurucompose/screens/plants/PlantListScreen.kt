package com.jhamburg.plantgurucompose.screens.plants

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.components.PlantOptionsMenu
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantResponse
import com.jhamburg.plantgurucompose.ui.theme.PlantGuruComposeTheme
import com.jhamburg.plantgurucompose.utils.DateTimeUtil
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun isOlderThanOneDay(timestamp: String): Boolean {
    return try {
        val lastReading = when {
            timestamp.contains(".") -> {
                LocalDateTime.parse(
                    timestamp,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                )
            }

            else -> {
                LocalDateTime.parse(
                    timestamp,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                )
            }
        }

        val oneDayAgo = LocalDateTime.now(ZoneOffset.UTC).minusDays(1)
        lastReading.isBefore(oneDayAgo)
    } catch (e: Exception) {
        Log.e("PlantListScreen", "Error parsing timestamp: $timestamp", e)
        true
    }
}

@Composable
private fun formatLastReading(timestamp: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    val utcDateTime = LocalDateTime.parse(timestamp, formatter)
    val utcZoned = utcDateTime.atZone(ZoneOffset.UTC)
    val deviceZone = ZoneId.systemDefault()

    val localZoned = utcZoned.withZoneSameInstant(deviceZone)
    val nowLocal = ZonedDateTime.now(deviceZone)

    val context = LocalContext.current
    val result = when {
        localZoned.toLocalDate() == nowLocal.toLocalDate() ->
            "Today ${DateTimeUtil.formatForDisplay(context, localZoned.toLocalDateTime())}"

        localZoned.toLocalDate() == nowLocal.minusDays(1).toLocalDate() ->
            "Yesterday ${DateTimeUtil.formatForDisplay(context, localZoned.toLocalDateTime())}"

        else -> DateTimeUtil.formatForDisplay(context, localZoned.toLocalDateTime())
    }
    Log.d("PlantListScreen", "Formatted result: $result")
    return result

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantListScreen(
    navController: NavController,
    plantViewModel: PlantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userId by plantViewModel.userId.collectAsState()
    val plants by plantViewModel.plants.collectAsState()
    val loading by plantViewModel.loading.collectAsState()
    val isUpdating by plantViewModel.isUpdating.collectAsState()
    val needsRefresh by plantViewModel.needsRefresh.collectAsState()

    var isRefreshingList by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    var previousLifecycleState by remember { mutableStateOf<Lifecycle.State?>(null) }
    var isFirstResume by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Log.d("PlantListScreen", "Initial screen entry")
        if (!plantViewModel.isLoggedIn()) {
            Log.d("PlantListScreen", "User not logged in, navigating to login")
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
            return@LaunchedEffect
        }

        userId?.let { uid ->
            isRefreshingList = true
            try {
                Log.d("PlantListScreen", "Initial load for user $uid")
                plantViewModel.getPlants(uid, forceRefresh = true)
            } catch (e: Exception) {
                Log.e("PlantListScreen", "Error refreshing plants", e)
            } finally {
                isRefreshingList = false
            }
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        Log.d("PlantListScreen", "Navigation triggered")
        userId?.let { uid ->
            isRefreshingList = true
            try {
                Log.d("PlantListScreen", "Navigation refresh for user $uid")
                plantViewModel.getPlants(uid, forceRefresh = true)
            } catch (e: Exception) {
                Log.e("PlantListScreen", "Error refreshing plants", e)
            } finally {
                isRefreshingList = false
            }
        }
    }

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            if (isFirstResume) {
                isFirstResume = false
            } else if (previousLifecycleState == Lifecycle.State.STARTED) {
                Log.d("PlantListScreen", "Screen resumed, refreshing data")
                userId?.let { uid ->
                    plantViewModel.getPlants(uid, forceRefresh = true)
                }
            }
        }
        previousLifecycleState = lifecycleState
    }

    LaunchedEffect(needsRefresh) {
        Log.d("PlantListScreen", "needsRefresh changed to: $needsRefresh")
        userId?.let { uid ->
            plantViewModel.getPlants(uid, forceRefresh = true)
        }
        plantViewModel.setNeedsRefresh(false)
    }

    var isRefreshing by remember { mutableStateOf(false) }
    fun refreshData() {
        Log.d("PlantListScreen", "Refresh triggered")
        isRefreshing = true
        plantViewModel.getPlants(userId, forceRefresh = true)
        isRefreshing = false
    }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    var isNavigating by remember { mutableStateOf(false) }
    val cardClickHandler = { plantId: Int ->
        Log.d("PlantListScreen", "Plant card clicked: $plantId")
        isNavigating = true
        navController.navigate("plantDetail/$plantId")
    }

    val plantDetails by plantViewModel.plantDetails.collectAsState()
    val error by plantViewModel.error.collectAsState()

    LaunchedEffect(plants, loading, error) {
        Log.d(
            "PlantListScreen", """
            State Update:
            Plants size: ${plants.size}
            Loading: $loading
            Error: $error
        """.trimIndent()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Plants") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshData() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_refresh_24),
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (plants.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    navController.navigate("createPlant/$userId")
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Plant")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isRefreshingList || (loading && !isUpdating) -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null && !isNavigating -> {
                    Log.d("PlantListScreen", "Showing error state: $error")
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { refreshData() }) {
                            Text("Retry")
                        }
                    }
                }

                plants.isEmpty() && !loading && !isNavigating -> {
                    Log.d("PlantListScreen", "Showing empty plant screen")
                    EmptyPlantScreen(navController, userId)
                }

                else -> {
                    Log.d("PlantListScreen", "Showing plant list with ${plants.size} plants")
                    PlantList(
                        plants,
                        loading,
                        isUpdating,
                        plantDetails,
                        navController,
                        userId,
                        onPlantClick = cardClickHandler
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to exit?") },
            confirmButton = {
                TextButton(onClick = {
                    (context as? Activity)?.finish()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PlantList(
    plants: List<PlantResponse>,
    loading: Boolean,
    isUpdating: Boolean,
    plantDetails: Map<Int, PlantAdditionalDetails?>,
    navController: NavController,
    userId: Int,
    onPlantClick: (Int) -> Unit
) {
    when {
        loading && !isUpdating -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        plants.isEmpty() && !isUpdating -> {
            EmptyPlantScreen(navController, userId)
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                state = rememberLazyListState()
            ) {
                items(plants.size) { index ->
                    val plant = plants[index]
                    val details = plantDetails[plant.plantId]
                    PlantCard(
                        plant = plant,
                        details = details,
                        navController = navController,
                        userId = userId,
                        onClick = { onPlantClick(plant.plantId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantCard(
    plant: PlantResponse,
    details: PlantAdditionalDetails?,
    navController: NavController,
    plantViewModel: PlantViewModel? = hiltViewModel(),
    userId: Int? = null,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val deletingPlants by plantViewModel?.deletingPlants?.collectAsState()
        ?: remember { mutableStateOf(emptySet()) }
    val isDeleting = deletingPlants.contains(plant.plantId)

    val sensorStatus = remember(plant.lastSensorReading, plant.provisioningStatus) {
        when {
            // maybe change to different name, for when waiting for data but successfulyl provisoned
            plant.lastSensorReading == null && plant.provisioningStatus == "BACKEND_VERIFIED" -> SensorStatus.PROVISIONING

            plant.lastSensorReading == null -> SensorStatus.NEVER_CONNECTED

            // Show as provisioning if in progress
            plant.provisioningStatus in listOf("PENDING", "DEVICE_CONNECTED", "WIFI_SETUP") ->
                SensorStatus.PROVISIONING

            // If completed and has recent data, show as connected
            plant.provisioningStatus == "BACKEND_VERIFIED" && !isOlderThanOneDay(plant.lastSensorReading) -> SensorStatus.CONNECTED

            // All other cases (failed, completed but old data, etc) show as disconnected
            else -> SensorStatus.DISCONNECTED
        }
    }

    val cardClickHandler = {
        Log.d("PlantListScreen", "Plant card clicked: ${plant.plantId}")
        navController.navigate("plantDetail/${plant.plantId}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when {
                    plant.plantId == 2 -> 200.dp
                    sensorStatus == SensorStatus.NEVER_CONNECTED -> 170.dp
                    else -> 145.dp
                }
            )
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                    ) {
                        AsyncImage(
                            model = details?.imageUri.also { uri ->
                                Log.d("PlantListScreen", "Loading image for plant ${plant.plantId}, URI: $uri")
                            } ?: R.drawable.default_plant,
                            contentDescription = "Plant image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.default_plant),
                            fallback = painterResource(id = R.drawable.default_plant)
                        )
                    }

                    Column {
                        Text(
                            text = plant.plantName.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        details?.scientificName?.let { scientificName ->
                            Text(
                                text = scientificName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        details?.plantType?.takeIf { it.isNotEmpty() }?.let { plantType ->
                            Text(
                                text = plantType,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    PlantOptionsMenu(
                        onEditClick = {
                            navController.navigate("editPlant/${plant.plantId}")
                        },
                        onSettingsClick = {
                            navController.navigate("plantSettings/${plant.plantId}")
                        },
                        showSettings = true,
                        onDirectSensorClick = {
                            if (sensorStatus == SensorStatus.CONNECTED) {
                                navController.navigate("directSensor/${plant.plantId}")
                            } else {
                                navController.navigate("ble_provision_landing/${plant.plantId}/null")
                            }
                        },
                        showDirectSensor = true,
                        isConnected = sensorStatus == SensorStatus.CONNECTED
                    )
                }
            }

            if (plant.plantId != 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (sensorStatus) {
                        SensorStatus.PROVISIONING -> {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_settings_24),
                                contentDescription = "Provisioning",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Device Setup in Progress",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        SensorStatus.FAILED -> {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_error_24),
                                contentDescription = "Failed",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Device Setup Failed",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        SensorStatus.DISCONNECTED -> {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_cloud_off_24),
                                contentDescription = "Disconnected",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Disconnected",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        SensorStatus.CONNECTED -> {
                            val isHealthy = plant.soilMoisture1?.let { it > 30 } ?: false
                            Icon(
                                painter = painterResource(
                                    id = if (isHealthy) R.drawable.baseline_check_circle_24
                                    else R.drawable.baseline_warning_24
                                ),
                                contentDescription = "Plant Status",
                                tint = if (isHealthy) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = if (isHealthy) "Healthy" else "Needs Watering",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isHealthy) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }

                        SensorStatus.NEVER_CONNECTED -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        ),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.baseline_sensors_24),
                                            contentDescription = "No Sensor",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "No sensor connected",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    plant.lastSensorReading?.let { lastReading ->
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Last reading: ${formatLastReading(lastReading)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sensorStatus == SensorStatus.DISCONNECTED)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val isHealthy = plant.soilMoisture1?.let { it > 30 } ?: false
                    Icon(
                        painter = painterResource(
                            id = if (isHealthy) R.drawable.baseline_check_circle_24
                            else R.drawable.baseline_warning_24
                        ),
                        contentDescription = "Plant Status",
                        tint = if (isHealthy) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isHealthy) "Healthy" else "Needs Watering",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isHealthy) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )


                    plant.lastSensorReading?.let { lastReading ->
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Last reading: Feb 14, 17:20",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (sensorStatus != SensorStatus.NEVER_CONNECTED && sensorStatus != SensorStatus.DISCONNECTED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_water_24),
                                contentDescription = "Soil Moisture",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = plant.soilMoisture1?.let { "%.1f%%".format(it) } ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_device_thermostat_24),
                                contentDescription = "Soil Temperature",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = plant.soilTemp?.let { "%.1f°C".format(it) } ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_sunny_24),
                                contentDescription = "Light",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = plant.light?.let { "%.1f%%".format(it) } ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            if (plant.plantId == 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_water_24),
                                contentDescription = "Soil Moisture",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "79%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_device_thermostat_24),
                                contentDescription = "Soil Temperature",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = plant.soilTemp?.let { "%.1f°C".format(it) } ?: "N/A",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_sunny_24),
                                contentDescription = "Light",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "80%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant") },
            text = { Text("Are you sure you want to delete this plant? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (plantViewModel != null && userId != null) {
                            scope.launch {
                                try {
                                    plantViewModel.deletePlant(plant.plantId)
                                    plantViewModel.getPlants(userId)
                                    showDeleteDialog = false
                                } catch (e: Exception) {
                                    Log.e("PlantListScreen", "Error deleting plant: ${e.message}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlantCardPreview() {
    val samplePlant = PlantResponse(
        plantId = 1,
        userId = 1,
        plantName = "Monstera Deliciosa",
        age = 365,
        lastWatered = "2024-03-20T11:55:00Z",
        nextWateringTime = "2024-03-23T11:55:00Z",
        extTemp = 23.5f,
        light = 75.0f,
        humidity = 65.0f,
        soilTemp = 21.2f,
        soilMoisture1 = 45.5f,
        soilMoisture2 = 44.8f,
        lastSensorReading = "2024-03-20T11:55:00Z",
        deviceId = "id",
        provisioningStatus = "COMPLETED"
    )

    val sampleDetails = PlantAdditionalDetails(
        scientificName = "Monstera deliciosa",
        plantType = "Tropical",
        createdOn = System.currentTimeMillis(),
        description = "A beautiful tropical plant with distinctive leaves",
        careInstructions = "Water weekly, provide indirect light",
        imageUri = null
    )

    PlantGuruComposeTheme {
        PlantCard(
            plant = samplePlant,
            details = sampleDetails,
            navController = rememberNavController(),
            plantViewModel = null,
            userId = 1
        ) { }
    }
}

@Preview(showBackground = true, name = "Connected Sensor")
@Composable
private fun PlantCardPreviewConnected() {
    val samplePlant = PlantResponse(
        plantId = 1,
        userId = 1,
        plantName = "Monstera Deliciosa",
        age = 365,
        lastWatered = "2024-03-20T11:55:00Z",
        nextWateringTime = "2024-03-23T11:55:00Z",
        extTemp = 23.5f,
        light = 75.0f,
        humidity = 65.0f,
        soilTemp = 21.2f,
        soilMoisture1 = 45.5f,
        soilMoisture2 = 44.8f,
        lastSensorReading = LocalDateTime.now().toString(),
        deviceId = "id",
        provisioningStatus = "COMPLETED"
    )

    val sampleDetails = PlantAdditionalDetails(
        scientificName = "Monstera deliciosa",
        plantType = "Tropical",
        createdOn = System.currentTimeMillis(),
        description = "A beautiful tropical plant with distinctive leaves",
        careInstructions = "Water weekly, provide indirect light",
        imageUri = null
    )

    PlantGuruComposeTheme {
        PlantCard(
            plant = samplePlant,
            details = sampleDetails,
            navController = rememberNavController(),
            plantViewModel = null,
            userId = 1
        ) { }
    }
}

@Preview(showBackground = true, name = "Never Connected")
@Composable
private fun PlantCardPreviewNeverConnected() {
    val samplePlant = PlantResponse(
        plantId = 1,
        userId = 1,
        plantName = "Monstera Deliciosa",
        age = 365,
        lastWatered = "2024-03-20T11:55:00Z",
        nextWateringTime = "2024-03-23T11:55:00Z",
        extTemp = null,
        light = null,
        humidity = null,
        soilTemp = null,
        soilMoisture1 = null,
        soilMoisture2 = null,
        lastSensorReading = null,
        deviceId = "id",
        provisioningStatus = "COMPLETED"
    )

    val sampleDetails = PlantAdditionalDetails(
        scientificName = "Monstera deliciosa",
        plantType = "Tropical",
        createdOn = System.currentTimeMillis(),
        description = "A beautiful tropical plant with distinctive leaves",
        careInstructions = "Water weekly, provide indirect light",
        imageUri = null
    )

    PlantGuruComposeTheme {
        PlantCard(
            plant = samplePlant,
            details = sampleDetails,
            navController = rememberNavController(),
            plantViewModel = null,
            userId = 1
        ) { }
    }
}