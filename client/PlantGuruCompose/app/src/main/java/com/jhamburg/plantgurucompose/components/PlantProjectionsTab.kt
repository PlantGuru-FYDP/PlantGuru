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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.LoadingState
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorType
import com.jhamburg.plantgurucompose.models.TimeRange
import com.jhamburg.plantgurucompose.viewmodels.ProjectionsViewModel
import com.jhamburg.plantgurucompose.viewmodels.WateringEventViewModel
import java.time.LocalDateTime

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
    var showWateringEvents by remember { mutableStateOf(true) }
    val wateringEventViewModel: WateringEventViewModel = viewModel()
    val wateringEvents by wateringEventViewModel.wateringEvents.collectAsState()

    LaunchedEffect(selectedTimeRange) {
        projectionsViewModel.getProjections(
            plantId = plantId,
            sensorType = SensorType.SOIL_MOISTURE.apiName,
            numPoints = selectedTimeRange.numPoints,
            granularityMinutes = selectedTimeRange.defaultGranularity
        )
        
        // Fetch watering events for the selected time range
        val endTime = LocalDateTime.now()
        val startTime = endTime.minusHours(selectedTimeRange.hours.toLong())
        wateringEventViewModel.getWateringEvents(
            plantId = plantId
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Soil Moisture",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "Next predicted watering: Feb 19, 2024",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            val combinedData = buildList {
                                addAll(state.historicalData)
                                state.projections?.projections?.forEach { proj ->
                                    add(
                                        SensorData(
                                            plantId = plantId,
                                            sensorId = -1,
                                            timeStamp = proj.timestamp,
                                            extTemp = 0f,
                                            light = 0f,
                                            humidity = 0f,
                                            soilTemp = 0f,
                                            soilMoisture1 = proj.value.toFloat(),
                                            soilMoisture2 = 0f,
                                            confidence = proj.confidence
                                        )
                                    )
                                }
                            }.sortedBy { it.timeStamp }

                            SensorDataGraph(
                                sensorData = combinedData,
                                wateringEvents = wateringEvents,
                                timeRange = selectedTimeRange,
                                sensorType = SensorType.SOIL_MOISTURE,
                                showWateringEvents = showWateringEvents
                            )
                        }
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