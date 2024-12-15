package com.jhamburg.plantgurucompose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jhamburg.plantgurucompose.components.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhamburg.plantgurucompose.models.SensorData
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.TimeRange
import com.jhamburg.plantgurucompose.models.WateringEvent
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import com.jhamburg.plantgurucompose.models.SensorType
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.livedata.observeAsState
import com.jhamburg.plantgurucompose.models.Granularity
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import com.jhamburg.plantgurucompose.viewmodels.SensorDataViewModel
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel
import com.jhamburg.plantgurucompose.viewmodels.WateringEventViewModel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import com.jhamburg.plantgurucompose.utils.DateTimeUtil


@Composable
fun PlantHistoryTab(
    plantId: Int,
    wateringEventViewModel: WateringEventViewModel = hiltViewModel()
) {
    val wateringEvents by wateringEventViewModel.wateringEvents.collectAsState()
    val loading by wateringEventViewModel.loading.collectAsState()
    val error by wateringEventViewModel.error.collectAsState()

    LaunchedEffect(plantId) {
        wateringEventViewModel.getWateringEvents(plantId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Watering History",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            wateringEvents.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No watering history available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(wateringEvents.size) { index ->
                        val event = wateringEvents[index]
                        WateringEventCard(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun WateringEventCard(event: WateringEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeUtil.formatForDisplay(LocalContext.current, event.timeStamp),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${event.volume}L",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WateringDetailItem(
                        label = "Duration",
                        value = "${event.wateringDuration}s"
                    )
                    WateringDetailItem(
                        label = "Peak Temperature",
                        value = "${event.peakTemp}°C"
                    )
                    WateringDetailItem(
                        label = "Peak Moisture",
                        value = "${event.peakMoisture}%"
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WateringDetailItem(
                        label = "Average Temperature",
                        value = "${event.avgTemp}°C"
                    )
                    WateringDetailItem(
                        label = "Average Moisture",
                        value = "${event.avgMoisture}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun WateringDetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
