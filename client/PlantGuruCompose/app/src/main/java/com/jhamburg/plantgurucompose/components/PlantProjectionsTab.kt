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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.jhamburg.plantgurucompose.viewmodels.ProjectionsViewModel

private val sensorTabs = listOf(
    SensorType.SOIL_MOISTURE,
    SensorType.SOIL_TEMP,
    SensorType.LIGHT,
    SensorType.HUMIDITY
)

@Composable
fun PlantProjectionsTab(
    plantId: Int,
    selectedTimeRange: TimeRange,
    selectedSensorType: SensorType,
    onTimeRangeSelected: (TimeRange) -> Unit,
    onSensorTypeSelected: (SensorType) -> Unit,
    projectionsViewModel: ProjectionsViewModel
) {
    val state by projectionsViewModel.state.collectAsState()
    var selectedSensorTab by remember { mutableIntStateOf(sensorTabs.indexOf(selectedSensorType)) }

    LaunchedEffect(selectedTimeRange, selectedSensorType) {
        projectionsViewModel.getProjections(
            plantId = plantId,
            sensorType = selectedSensorType.apiName,
            numPoints = selectedTimeRange.numPoints,
            granularityMinutes = selectedTimeRange.defaultGranularity
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedSensorTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            sensorTabs.forEachIndexed { index, type ->
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
                                    SensorType.LIGHT -> R.drawable.baseline_sunny_24
                                    SensorType.HUMIDITY -> R.drawable.baseline_water_24
                                    else -> R.drawable.baseline_device_thermostat_24
                                }
                            ),
                            contentDescription = type.label
                        )
                    },
                    text = { Text(type.label) }
                )
            }
        }

        when (state.loadingState) {
            LoadingState.Loading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is LoadingState.Error -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            LoadingState.Success -> {
                if (state.historicalData.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        val combinedData = buildList {
                            addAll(state.historicalData)
                            state.projections?.projections?.forEach { proj ->
                                add(
                                    SensorData(
                                        plantId = plantId,
                                        sensorId = -1,
                                        timeStamp = proj.timestamp,
                                        extTemp = if (selectedSensorType == SensorType.EXTERNAL_TEMP)
                                            proj.value.toFloat() else 0f,
                                        light = if (selectedSensorType == SensorType.LIGHT)
                                            proj.value.toFloat() else 0f,
                                        humidity = if (selectedSensorType == SensorType.HUMIDITY)
                                            proj.value.toFloat() else 0f,
                                        soilTemp = if (selectedSensorType == SensorType.SOIL_TEMP)
                                            proj.value.toFloat() else 0f,
                                        soilMoisture1 = if (selectedSensorType == SensorType.SOIL_MOISTURE)
                                            proj.value.toFloat() else 0f,
                                        soilMoisture2 = 0f,
                                        confidence = proj.confidence
                                    )
                                )
                            }
                        }.sortedBy { it.timeStamp }

                        SensorDataGraph(
                            sensorData = combinedData,
                            wateringEvents = emptyList(),
                            timeRange = selectedTimeRange,
                            sensorType = selectedSensorType,
                            showWateringEvents = false
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data available")
                    }
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
                    "Projection Range",
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