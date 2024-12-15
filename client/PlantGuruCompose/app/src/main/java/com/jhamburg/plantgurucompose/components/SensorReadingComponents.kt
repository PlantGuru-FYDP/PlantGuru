package com.jhamburg.plantgurucompose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.SensorData
import com.jhamburg.plantgurucompose.models.SensorType
import com.jhamburg.plantgurucompose.ui.theme.PlantGuruComposeTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SensorReadingCardWithGraph(
    @DrawableRes icon: Int,
    value: String,
    label: String,
    sensorData: List<SensorData>,
    sensorType: SensorType
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SensorReadingChip(icon, value, label)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)

            ) {
                MiniSensorGraph(
                    sensorData = sensorData,
                    sensorType = sensorType
                )
            }
        }
    }
}

@Composable
fun SensorReadingChip(
    @DrawableRes icon: Int,
    value: String,
    label: String
) {

    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SensorReadingCardWithGraphPreview() {
    val sampleData = (0 until 12).map { hour ->
        val baseTemp = 20.0 + (hour % 24) * 0.5
        SensorData(
            plantId = 1,
            extTemp = baseTemp.toFloat(),
            light = 500f,
            humidity = 60f,
            soilTemp = (baseTemp - 2).toFloat(),
            soilMoisture1 = 70f,
            soilMoisture2 = 68f,
            timeStamp = LocalDateTime.now()
                .minusHours(12 - hour.toLong())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            sensorId = hour + 1
        )
    }

    PlantGuruComposeTheme {
        SensorReadingCardWithGraph(
            icon = R.drawable.baseline_water_24,
            value = "70%",
            label = "Soil Moisture",
            sensorData = sampleData,
            sensorType = SensorType.SOIL_MOISTURE
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SensorReadingChipPreview() {
    PlantGuruComposeTheme {
        SensorReadingChip(
            icon = R.drawable.baseline_device_thermostat_24,
            value = "22°C",
            label = "Temperature"
        )
    }
} 