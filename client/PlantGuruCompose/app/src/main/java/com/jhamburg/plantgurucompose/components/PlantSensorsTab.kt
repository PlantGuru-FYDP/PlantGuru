package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.LoadingState
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorType
import com.jhamburg.plantgurucompose.models.TimeRange
import com.jhamburg.plantgurucompose.viewmodels.SensorDataViewModel
import java.time.LocalDateTime
import java.time.ZoneId


@Composable
fun PlantSensorsTab(
    sensorData: List<SensorData>,
    selectedTimeRange: TimeRange,
    selectedSensorType: SensorType,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onSensorTypeSelected: (SensorType) -> Unit,
    sensorDataViewModel: SensorDataViewModel
) {
    val state by sensorDataViewModel.state.collectAsState()
    var selectedSensorTab by remember { mutableIntStateOf(0) }
    var showWateringEvents by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val endTime = LocalDateTime.now(ZoneId.of("UTC"))
        val startTime = endTime.minusHours(TimeRange.WEEK_1.hours.toLong())

        sensorDataViewModel.getTimeSeriesData(
            plantId = sensorData.firstOrNull()?.plantId ?: return@LaunchedEffect,
            startTime = startTime.toString() + "Z",
            endTime = endTime.toString() + "Z",
            granularity = TimeRange.WEEK_1.defaultGranularity
        )
    }

    LaunchedEffect(selectedTimeRange) {
        if (selectedTimeRange != TimeRange.WEEK_1) {
            val endTime = LocalDateTime.now(ZoneId.of("UTC"))
            val startTime = endTime.minusHours(selectedTimeRange.hours.toLong())

            sensorDataViewModel.getTimeSeriesData(
                plantId = sensorData.firstOrNull()?.plantId ?: return@LaunchedEffect,
                startTime = startTime.toString() + "Z",
                endTime = endTime.toString() + "Z",
                granularity = selectedTimeRange.defaultGranularity
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSensorTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                SensorType.SOIL_MOISTURE,
                SensorType.SOIL_TEMP,
                SensorType.EXTERNAL_TEMP,
                SensorType.LIGHT,
                SensorType.HUMIDITY
            ).forEachIndexed { index, type ->
                Tab(
                    selected = selectedSensorTab == index,
                    onClick = {
                        selectedSensorTab = index
                        onSensorTypeSelected(type)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                when (type) {
                                    SensorType.SOIL_MOISTURE -> R.drawable.baseline_water_24
                                    SensorType.SOIL_TEMP -> R.drawable.baseline_device_thermostat_24
                                    SensorType.EXTERNAL_TEMP -> R.drawable.baseline_air_24
                                    SensorType.LIGHT -> R.drawable.baseline_sunny_24
                                    SensorType.HUMIDITY -> R.drawable.baseline_water_24
                                }
                            ),
                            contentDescription = type.label
                        )
                    },
                    text = { Text(type.label) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (state.loadingState) {
                LoadingState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is LoadingState.Error -> {
                    Text(
                        text = state.error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                LoadingState.Success -> {
                    if (state.sensorData.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            SensorDataGraph(
                                sensorData = state.sensorData,
                                wateringEvents = state.wateringEvents,
                                timeRange = selectedTimeRange,
                                sensorType = selectedSensorType,
                                showWateringEvents = showWateringEvents
                            )
                        }
                    } else {
                        Text(
                            text = "No sensor data available",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        if (listOf(SensorType.SOIL_MOISTURE, SensorType.HUMIDITY).contains(selectedSensorType)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_water_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Watering Events",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = showWateringEvents,
                        onCheckedChange = { showWateringEvents = it }
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Time Range",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeRange.values().forEach { timeRange ->
                        FilterChip(
                            selected = timeRange == selectedTimeRange,
                            onClick = { onTimeRangeSelected(timeRange) },
                            label = { Text(timeRange.label) }
                        )
                    }
                }
            }
        }
    }
}
