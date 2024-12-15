package com.jhamburg.plantgurucompose.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.CareAction
import com.jhamburg.plantgurucompose.models.CareScheduleResponse
import com.jhamburg.plantgurucompose.models.HealthDiagnosticsResponse
import com.jhamburg.plantgurucompose.models.Issue
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantRecommendationsResponse
import com.jhamburg.plantgurucompose.models.PlantResponse
import com.jhamburg.plantgurucompose.ui.theme.LogoFont
import com.jhamburg.plantgurucompose.viewmodels.InsightsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun PlantCareTab(
    plant: PlantResponse?,
    details: PlantAdditionalDetails?,
    insightsViewModel: InsightsViewModel = hiltViewModel()
) {
    var careSchedule by remember { mutableStateOf<Result<CareScheduleResponse>?>(null) }
    var recommendations by remember { mutableStateOf<Result<PlantRecommendationsResponse>?>(null) }
    var healthDiagnostics by remember { mutableStateOf<Result<HealthDiagnosticsResponse>?>(null) }

    LaunchedEffect(plant?.plantId) {
        plant?.plantId?.let { plantId ->
            insightsViewModel.getCareSchedule(plantId)
            insightsViewModel.getPlantRecommendations(plantId)
            insightsViewModel.getHealthDiagnostics(plantId)
        }
    }

    LaunchedEffect(Unit) {
        launch {
            insightsViewModel.careSchedule.collect { careSchedule = it }
        }
        launch {
            insightsViewModel.recommendations.collect { recommendations = it }
        }
        launch {
            insightsViewModel.healthDiagnostics.collect { healthDiagnostics = it }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                healthDiagnostics?.getOrNull()?.let { health ->
                    HealthScoreCard(health)
                } ?: LoadingCard("Health Overview")
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                careSchedule?.getOrNull()?.let { schedule ->
                    CareScheduleCard(schedule)
                } ?: LoadingCard("Care Schedule")
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                recommendations?.getOrNull()?.let { response ->
                    RecommendationsCard(response.current_issues)
                } ?: LoadingCard("Care Recommendations")
            }
        }
    }
}

@Composable
private fun LoadingCard(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HealthScoreCard(health: HealthDiagnosticsResponse) {
    val isCritical = health.overall_health == "CRITICAL"
    val contentColor = if (isCritical) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Plant Health Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = if (isCritical) {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${health.health_score}",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp
                    ),
                    color = contentColor
                )
                Text(
                    health.overall_health,
                    style = MaterialTheme.typography.titleMedium,
                    color = when (health.overall_health) {
                        "EXCELLENT" -> MaterialTheme.colorScheme.primary
                        "GOOD" -> MaterialTheme.colorScheme.secondary
                        "FAIR" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorStatusMetric(
                label = "Temperature",
                value = health.latest_readings.temperature,
                unit = "°C",
                status = health.sensor_health["ext_temp"] ?: "UNKNOWN",
                icon = R.drawable.baseline_device_thermostat_24
            )
            SensorStatusMetric(
                label = "Humidity",
                value = health.latest_readings.humidity,
                unit = "%",
                status = health.sensor_health["humidity"] ?: "UNKNOWN",
                icon = R.drawable.baseline_water_24
            )
            SensorStatusMetric(
                label = "Soil Moisture",
                value = health.latest_readings.soil_moisture,
                unit = "%",
                status = health.sensor_health["soil_moisture_1"] ?: "UNKNOWN",
                icon = R.drawable.baseline_grass_24
            )
        }
    }
}

@Composable
private fun SensorStatusMetric(
    label: String,
    value: Double,
    unit: String,
    status: String,
    @DrawableRes icon: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = when (status) {
            "CRITICAL" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            "WARNING_HIGH", "WARNING_LOW" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f)
            "GOOD" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = when (status) {
                        "CRITICAL" -> MaterialTheme.colorScheme.error
                        "WARNING_HIGH", "WARNING_LOW" -> MaterialTheme.colorScheme.tertiary
                        "GOOD" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Column {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        when (status) {
                            "CRITICAL" -> "Critical - ${if (value > 0) "Too High" else "Too Low"}"
                            "WARNING_HIGH" -> "Warning - Running High"
                            "WARNING_LOW" -> "Warning - Running Low"
                            "GOOD" -> "Healthy"
                            else -> "Status Unknown"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (status) {
                            "CRITICAL" -> MaterialTheme.colorScheme.error
                            "WARNING_HIGH", "WARNING_LOW" -> MaterialTheme.colorScheme.tertiary
                            "GOOD" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Text(
                "$value$unit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CareScheduleCard(schedule: CareScheduleResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Care Schedule",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LogoFont,
                    fontWeight = FontWeight.Bold
                )
            )

            if (schedule.next_actions.isEmpty() || schedule.next_actions.all { it.due_in_hours == null }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "No scheduled actions at this time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                schedule.next_actions.forEach { action ->
                    if (action.due_in_hours != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            when (action.type) {
                                                "WATERING" -> R.drawable.baseline_water_24
                                                "MISTING" -> R.drawable.baseline_water_24
                                                else -> R.drawable.baseline_warning_24
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(action.details)
                                }
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            when {
                                                action.due_in_hours == 0 -> "Due now"
                                                action.due_in_hours < 24 -> "In ${action.due_in_hours}h"
                                                else -> "In ${action.due_in_hours / 24}d"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<Issue>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Care Recommendations",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LogoFont,
                    fontWeight = FontWeight.Bold
                )
            )

            val sortedRecs = recommendations.sortedBy {
                when (it.priority) {
                    "HIGH" -> 0
                    "MEDIUM" -> 1
                    else -> 2
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sortedRecs.forEach { rec ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = when (rec.priority) {
                            "HIGH" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                            "MEDIUM" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = when (rec.priority) {
                                "HIGH" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(
                                    when (rec.type) {
                                        "SOIL_MOISTURE_1", "SOIL_MOISTURE_2" -> R.drawable.baseline_water_24
                                        "EXT_TEMP" -> R.drawable.baseline_device_thermostat_24
                                        "LIGHT" -> R.drawable.baseline_sunny_24
                                        else -> R.drawable.baseline_warning_24
                                    }
                                ),
                                contentDescription = null,
                                tint = when (rec.priority) {
                                    "HIGH" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                            Text(
                                rec.message.ifEmpty { getDefaultMessage(rec.type) },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (rec.priority == "HIGH") {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("Urgent") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                                            alpha = 0.2f
                                        ),
                                        labelColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getDefaultMessage(type: String): String {
    return "message idk"

}

@Composable
private fun CareTimelineCard(actions: List<CareAction>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Care Timeline",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = LogoFont,
                    fontWeight = FontWeight.Bold
                )
            )

            AnimatedContent(
                targetState = actions,
                transitionSpec = {
                    slideInVertically(
                        animationSpec = tween(300)
                    ) { height -> height } togetherWith
                            slideOutVertically(
                                animationSpec = tween(300)
                            ) { height -> -height }
                }
            ) { actionsList ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actionsList.forEach { action ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            when (action.type) {
                                                "WATERING" -> R.drawable.baseline_water_24
                                                "LOCATION" -> R.drawable.baseline_place_24
                                                "LIGHT_ADJUSTMENT" -> R.drawable.baseline_sunny_24
                                                else -> R.drawable.baseline_warning_24
                                            }
                                        ),
                                        contentDescription = null,
                                        tint = when (action.priority) {
                                            "HIGH" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                                    Text(action.details)
                                }
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            when {
                                                action.due_in_hours == 0 -> "Due now"
                                                action.due_in_hours < 24 -> "In ${action.due_in_hours}h"
                                                else -> "In ${action.due_in_hours / 24}d"
                                            }
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = when {
                                            action.due_in_hours == 0 -> MaterialTheme.colorScheme.errorContainer
                                            action.due_in_hours < 24 -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectionsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Growth Projections",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProjectionMetric(
                    label = "Expected Height",
                    value = "25-30 cm",
                    timeline = "in 3 months"
                )
                ProjectionMetric(
                    label = "Next Bloom",
                    value = "2-3 flowers",
                    timeline = "in 6 weeks"
                )
            }
        }
    }
}

@Composable
private fun ProjectionMetric(
    label: String,
    value: String,
    timeline: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            timeline,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatRelativeTime(timestamp: String): String {
    val dateTime = LocalDateTime.parse(timestamp)
    val now = LocalDateTime.now()
    val hours = java.time.Duration.between(dateTime, now).toHours()

    return when {
        hours < 24 -> "$hours hours ago"
        hours < 48 -> "Yesterday"
        hours < 168 -> "${hours / 24} days ago"
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}
