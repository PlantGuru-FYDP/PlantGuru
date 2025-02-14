package com.jhamburg.plantgurucompose.components


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.HealthDiagnosticsResponse
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantResponse
import com.jhamburg.plantgurucompose.models.Prediction
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorHealthResponse
import com.jhamburg.plantgurucompose.models.SensorType
import com.jhamburg.plantgurucompose.models.WateringEvent
import com.jhamburg.plantgurucompose.ui.theme.WaterBlue
import com.jhamburg.plantgurucompose.utils.DateTimeUtil
import com.jhamburg.plantgurucompose.viewmodels.InsightsViewModel
import com.jhamburg.plantgurucompose.viewmodels.SensorDataViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

enum class SensorCardState {
    LOADING,
    ERROR,
    SUCCESS,
    NOT_AVAILABLE
}

@Composable
private fun SensorCardContainer(
    currentSensorData: SensorData?,
    sensorType: String,
    sensorHealthState: Result<SensorHealthResponse>?,
    sensorData: List<SensorData>,
    icon: Int,
    label: String,
    valueFormatter: (Double) -> String
) {
    var cardState by remember { mutableStateOf(SensorCardState.LOADING) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentSensorData, sensorHealthState) {
        cardState = when {
            currentSensorData == null -> SensorCardState.LOADING
            sensorHealthState == null -> SensorCardState.LOADING
            sensorHealthState.isFailure -> {
                errorMessage = sensorHealthState.exceptionOrNull()?.message
                SensorCardState.ERROR
            }

            getSensorValue(currentSensorData, sensorType) == null -> SensorCardState.NOT_AVAILABLE
            else -> SensorCardState.SUCCESS
        }
    }

    when (cardState) {
        SensorCardState.LOADING -> LoadingSensorCard()
        SensorCardState.ERROR -> ErrorSensorCard(
            icon = icon,
            label = label,
            error = errorMessage ?: "Failed to load sensor data"
        )

        SensorCardState.NOT_AVAILABLE -> NotAvailableSensorCard(icon = icon, label = label)
        SensorCardState.SUCCESS -> {
            val sensorValue = getSensorValue(currentSensorData!!, sensorType)
            val sensorTypeEnum = getSensorTypeEnum(sensorType)
            val healthStatus = sensorHealthState?.getOrNull()?.immediate_status?.status ?: "UNKNOWN"

            SensorCard(
                icon = icon,
                value = valueFormatter(sensorValue!!.toDouble()),
                label = label,
                sensorData = sensorData,
                sensorType = sensorTypeEnum!!,
                healthStatus = healthStatus
            )
        }
    }

    Log.d(
        "SensorCardContainer",
        "Loading state for ${sensorType}: loading=${cardState == SensorCardState.LOADING}"
    )
    Log.d("SensorCardContainer", "Received sensor health for ${sensorType}: ${sensorHealthState}")
    Log.d(
        "SensorCardContainer",
        "Received time series data for ${sensorType}: ${sensorData.size} points"
    )
}

private fun getSensorValue(sensorData: SensorData, sensorType: String): Float? {
    return when (sensorType) {
        "soil_moisture_1" -> sensorData.soilMoisture1.takeIf { it != -1f }
        "soil_temp" -> sensorData.soilTemp.takeIf { it != -1f }
        "ext_temp" -> sensorData.extTemp.takeIf { it != -1f }
        "humidity" -> sensorData.humidity.takeIf { it != -1f }
        "light" -> sensorData.light.takeIf { it != -1f }
        else -> null
    }
}

private fun getSensorTypeEnum(sensorType: String): SensorType? {
    return when (sensorType) {
        "soil_moisture_1" -> SensorType.SOIL_MOISTURE
        "soil_temp" -> SensorType.SOIL_TEMP
        "ext_temp" -> SensorType.EXTERNAL_TEMP
        "humidity" -> SensorType.HUMIDITY
        "light" -> SensorType.LIGHT
        else -> null
    }
}

@Composable
private fun ErrorSensorCard(
    icon: Int,
    label: String,
    error: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NotAvailableSensorCard(
    icon: Int,
    label: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Not Available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun calculateWateringProgress(prediction: Prediction?): Float {
    if (prediction?.hoursToNextWatering == null) return 0f
    // Assume 7 days (168 hours) is the maximum watering interval
    val maxInterval = 168f
    return (maxInterval - prediction.hoursToNextWatering) / maxInterval
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantOverviewTab(
    plant: PlantResponse?,
    details: PlantAdditionalDetails?,
    currentSensorData: SensorData?,
    prediction: Prediction?,
    sensorData: List<SensorData>,
    sensorDataViewModel: SensorDataViewModel,
    lastWateringEvent: WateringEvent?,
    insightsViewModel: InsightsViewModel = hiltViewModel()
) {
    var healthDiagnosticsLoading by remember { mutableStateOf(false) }
    var healthDiagnostics by remember { mutableStateOf<Result<HealthDiagnosticsResponse>?>(null) }
    var sensorHealthStates = remember { mutableStateMapOf<String, Result<SensorHealthResponse>?>() }

    LaunchedEffect(plant?.plantId) {
        plant?.plantId?.let { plantId ->
            healthDiagnosticsLoading = true
            insightsViewModel.getHealthDiagnostics(plantId)

            listOf(
                "ext_temp",
                "soil_temp",
                "humidity",
                "soil_moisture_1",
                "light"
            ).forEach { sensorType ->
                insightsViewModel.getSensorHealth(plantId, sensorType)
            }
        }
    }

    LaunchedEffect(Unit) {
        insightsViewModel.healthDiagnostics.collect {
            healthDiagnostics = it
            healthDiagnosticsLoading = false
        }
    }
    listOf("ext_temp", "soil_temp", "humidity", "soil_moisture_1", "light").forEach { sensorType ->
        LaunchedEffect(Unit) {
            insightsViewModel.sensorHealthStates[sensorType]?.collect { result ->
                result?.let {
                    sensorHealthStates[sensorType] = it
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box {
                AsyncImage(
                    model = details?.imageUri ?: R.drawable.default_plant,
                    contentDescription = "Plant image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = plant?.plantName?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        }
                            ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    details?.scientificName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (currentSensorData == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(2) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = WaterBlue.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = WaterBlue.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_water_24),
                                contentDescription = "Watering Info",
                                tint = WaterBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    WaterBlue.copy(alpha = 0.1f),
                                    RoundedCornerShape(2.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(calculateWateringProgress(prediction))
                                    .fillMaxHeight()
                                    .background(
                                        WaterBlue,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Last Watered",
                                style = MaterialTheme.typography.labelSmall,
                                color = WaterBlue.copy(alpha = 0.7f)
                            )
                            Text(
                                text = lastWateringEvent?.let {
                                    DateTimeUtil.formatForDisplay(
                                        context = LocalContext.current,
                                        it.timeStamp
                                    )
                                } ?: "Never",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WaterBlue
                            )

                            Text(
                                text = prediction?.hoursToNextWatering?.let { hours ->
                                    when {
                                        hours < 1 -> "Water now!"
                                        hours < 24 -> "Water in ${hours.toInt()} hours"
                                        else -> "Water in ${(hours / 24).toInt()} days"
                                    }
                                } ?: "Watering schedule unknown",
                                style = MaterialTheme.typography.titleMedium,
                                color = WaterBlue,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                healthDiagnostics?.getOrNull()?.let { diagnostics ->
                    val (statusColor, statusText, statusIcon) = when (diagnostics.overall_health) {
                        "EXCELLENT" -> Triple(
                            Color(0xFF4CAF50), // Green
                            "Excellent",
                            R.drawable.baseline_check_circle_24
                        )

                        "GOOD" -> Triple(
                            Color(0xFF4CAF50), // Green
                            "Healthy",
                            R.drawable.baseline_check_circle_24
                        )

                        "FAIR" -> Triple(
                            Color(0xFFFFC107), // Yellow
                            "Fair",
                            R.drawable.baseline_warning_24
                        )

                        "POOR" -> Triple(
                            Color(0xFFF44336), // Red
                            "Poor",
                            R.drawable.baseline_warning_24
                        )

                        "CRITICAL" -> Triple(
                            Color(0xFFF44336), // Red
                            "Critical",
                            R.drawable.baseline_warning_24
                        )

                        else -> Triple(
                            MaterialTheme.colorScheme.outline,
                            "Unknown",
                            R.drawable.baseline_help_24
                        )
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(statusIcon),
                                contentDescription = "Plant Status",
                                tint = statusColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Status",
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = statusColor
                            )
                            Text(
                                text = "${diagnostics.health_score}%",
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                color = statusColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                } ?: run {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Current Readings",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (currentSensorData == null) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(5) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            LaunchedEffect(Unit) {
                val endTime = LocalDateTime.now(ZoneId.of("UTC"))
                val startTime = endTime.minusHours(12)

                sensorDataViewModel.getTimeSeriesData(
                    plantId = plant?.plantId ?: 0,
                    startTime = DateTimeUtil.formatForApi(startTime),
                    endTime = DateTimeUtil.formatForApi(endTime),
                    granularity = 60
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SensorCardContainer(
                        currentSensorData = currentSensorData,
                        sensorType = "soil_moisture_1",
                        sensorHealthState = sensorHealthStates["soil_moisture_1"],
                        sensorData = sensorData,
                        icon = R.drawable.baseline_water_24,
                        label = "Moisture",
                        valueFormatter = { "${it.toInt()}%" }
                    )
                }

                item {
                    SensorCardContainer(
                        currentSensorData = currentSensorData,
                        sensorType = "soil_temp",
                        sensorHealthState = sensorHealthStates["soil_temp"],
                        sensorData = sensorData,
                        icon = R.drawable.baseline_device_thermostat_24,
                        label = "Soil Temp",
                        valueFormatter = { "${it.toInt()}°C" }
                    )
                }

                item {
                    SensorCardContainer(
                        currentSensorData = currentSensorData,
                        sensorType = "ext_temp",
                        sensorHealthState = sensorHealthStates["ext_temp"],
                        sensorData = sensorData,
                        icon = R.drawable.baseline_air_24,
                        label = "Air Temp",
                        valueFormatter = { "${it.toInt()}°C" }
                    )
                }

                item {
                    SensorCardContainer(
                        currentSensorData = currentSensorData,
                        sensorType = "humidity",
                        sensorHealthState = sensorHealthStates["humidity"],
                        sensorData = sensorData,
                        icon = R.drawable.baseline_water_24,
                        label = "Humidity",
                        valueFormatter = { "${it.toInt()}%" }
                    )
                }

                item {
                    SensorCardContainer(
                        currentSensorData = currentSensorData,
                        sensorType = "light",
                        sensorHealthState = sensorHealthStates["light"],
                        sensorData = sensorData,
                        icon = R.drawable.baseline_sunny_24,
                        label = "Light",
                        valueFormatter = { "${it.toInt()}%" }
                    )
                }
            }
        }

        if (healthDiagnosticsLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            healthDiagnostics?.getOrNull()?.let { diagnostics ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (diagnostics.overall_health) {
                            "EXCELLENT", "GOOD" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                            "FAIR" -> Color(0xFFFFC107).copy(alpha = 0.15f)
                            else -> Color(0xFFF44336).copy(alpha = 0.15f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Plant Health",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                diagnostics.overall_health,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text(
                            "${diagnostics.health_score}%",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingSensorCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
