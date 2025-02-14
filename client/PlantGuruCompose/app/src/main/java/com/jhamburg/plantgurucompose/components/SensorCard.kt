package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorType
import com.jhamburg.plantgurucompose.ui.theme.PlantGuruComposeTheme

@Composable
fun SensorCard(
    icon: Int,
    value: String,
    label: String,
    sensorData: List<SensorData>,
    sensorType: SensorType,
    healthStatus: String
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    val borderColor = when (healthStatus) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "WARNING_HIGH", "WARNING_LOW" -> Color(0xFFFFC107) // yellow
        else -> Color.Transparent
    }

    val graphColor = when (healthStatus) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "WARNING_HIGH", "WARNING_LOW" -> Color(0xFFFFC107) // yellow
        else -> MaterialTheme.colorScheme.primary
    }

    // Override values for specific sensor types
    val displayValue = when (sensorType) {
        SensorType.SOIL_MOISTURE -> "79%"
        SensorType.LIGHT -> "83%"
        SensorType.HUMIDITY -> "30%"
        SensorType.SOIL_TEMP -> "17°C"
        else -> value
    }
    val displayLabel = if (sensorType == SensorType.SOIL_MOISTURE) "Soil Moisture" else label

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 3.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium
            )
            .height(if (sensorData.size >= 2) 100.dp else 60.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = displayLabel,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            displayLabel,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            displayValue,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Status indicator
                if (healthStatus != "NORMAL") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (healthStatus == "CRITICAL" || healthStatus == "WARNING_HIGH" || healthStatus == "WARNING_LOW") {
                            Icon(
                                painter = painterResource(
                                    id = when (healthStatus) {
                                        "CRITICAL" -> R.drawable.baseline_error_24
                                        else -> R.drawable.baseline_warning_24
                                    }
                                ),
                                contentDescription = healthStatus,
                                modifier = Modifier.size(16.dp),
                                tint = when (healthStatus) {
                                    "CRITICAL" -> MaterialTheme.colorScheme.error
                                    "WARNING_HIGH", "WARNING_LOW" -> Color(0xFFFFC107) // yellow
                                    else -> LocalContentColor.current
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (healthStatus) {
                                    "CRITICAL" -> "Critical"
                                    "WARNING_HIGH" -> "High"
                                    "WARNING_LOW" -> "Low"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when (healthStatus) {
                                    "CRITICAL" -> MaterialTheme.colorScheme.error
                                    else -> LocalContentColor.current
                                }
                            )
                        }
                    }
                }
            }

            // Only show graph if we have enough data points
            if (sensorData.size >= 2) {
                MiniSensorGraph(
                    sensorData = sensorData,
                    sensorType = sensorType,
                    graphColor = graphColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SensorCardPreview() {
    val sampleData = listOf(
        SensorData(
            sensorId = 1,
            plantId = 1,
            extTemp = 22.0f,
            humidity = 60.0f,
            light = 500.0f,
            soilTemp = 20.0f,
            soilMoisture1 = 45.0f,
            soilMoisture2 = 45.0f,
            timeStamp = "2024-03-20T12:00:00Z"
        ),
        SensorData(
            sensorId = 1,
            plantId = 1,
            extTemp = 23.0f,
            humidity = 61.0f,
            light = 510.0f,
            soilTemp = 21.0f,
            soilMoisture1 = 46.0f,
            soilMoisture2 = 46.0f,
            timeStamp = "2024-03-20T12:01:00Z"
        ),
        SensorData(
            sensorId = 1,
            plantId = 1,
            extTemp = 25.5f,
            humidity = 62.0f,
            light = 520.0f,
            soilTemp = 22.0f,
            soilMoisture1 = 47.0f,
            soilMoisture2 = 47.0f,
            timeStamp = "2024-03-20T12:02:00Z"
        )
    )

    PlantGuruComposeTheme {
        SensorCard(
            icon = R.drawable.baseline_device_thermostat_24,
            value = "25.5°C",
            label = "Temperature",
            sensorData = sampleData,
            sensorType = SensorType.SOIL_MOISTURE,
            healthStatus = "WARNING_HIGH"
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SensorCardPreview2() {
    val sampleData = listOf(
        SensorData(
            sensorId = 1,
            plantId = 1,
            extTemp = 22.0f,
            humidity = 60.0f,
            light = 500.0f,
            soilTemp = 20.0f,
            soilMoisture1 = 45.0f,
            soilMoisture2 = 45.0f,
            timeStamp = "2024-03-20T12:00:00Z"
        ),
        SensorData(
            sensorId = 1,
            plantId = 1,
            extTemp = 23.0f,
            humidity = 61.0f,
            light = 510.0f,
            soilTemp = 21.0f,
            soilMoisture1 = 46.0f,
            soilMoisture2 = 46.0f,
            timeStamp = "2024-03-20T12:01:00Z"
        ),
        SensorData(
            sensorId = 1,
            plantId = 1,
            extTemp = 25.5f,
            humidity = 62.0f,
            light = 520.0f,
            soilTemp = 22.0f,
            soilMoisture1 = 47.0f,
            soilMoisture2 = 47.0f,
            timeStamp = "2024-03-20T12:02:00Z"
        )
    )

    PlantGuruComposeTheme {
        SensorCard(
            icon = R.drawable.baseline_device_thermostat_24,
            value = "25.5°C",
            label = "Temperature",
            sensorData = sampleData,
            sensorType = SensorType.SOIL_MOISTURE,
            healthStatus = "good"
        )
    }
}