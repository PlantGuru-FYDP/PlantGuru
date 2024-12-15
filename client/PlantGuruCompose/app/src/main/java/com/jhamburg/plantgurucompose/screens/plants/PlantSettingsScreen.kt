package com.jhamburg.plantgurucompose.screens.plants

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.components.SaveSettingsButton
import com.jhamburg.plantgurucompose.components.SaveStatusDialog
import com.jhamburg.plantgurucompose.components.SectionHeader
import com.jhamburg.plantgurucompose.components.SensorSettingsSection
import com.jhamburg.plantgurucompose.components.SettingsSwitch
import com.jhamburg.plantgurucompose.models.PlantNotificationSettings
import com.jhamburg.plantgurucompose.viewmodels.NotificationSettingsViewModel
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantSettingsScreen(
    navController: NavController,
    plantId: Int,
    notificationSettingsViewModel: NotificationSettingsViewModel = hiltViewModel(),
    plantViewModel: PlantViewModel = hiltViewModel()
) {
    var hasChanges by remember { mutableStateOf(false) }
    var currentSettings by remember { mutableStateOf<PlantNotificationSettings?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val plantSettings by notificationSettingsViewModel.plantSettings.collectAsState()
    val loading by notificationSettingsViewModel.loading.collectAsState()
    val error by notificationSettingsViewModel.error.collectAsState()
    val saveStatus by notificationSettingsViewModel.saveStatus.collectAsState()

    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = hasChanges) {
        showExitConfirmation = true
    }

    LaunchedEffect(plantId) {
        notificationSettingsViewModel.loadPlantSettings(plantId)
    }

    LaunchedEffect(plantSettings) {
        plantSettings?.let {
            currentSettings = it
        }
    }

    LaunchedEffect(saveStatus) {
        when (saveStatus) {
            is NotificationSettingsViewModel.SaveStatus.Success -> {
                hasChanges = false
            }

            else -> {}
        }
    }

    error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { notificationSettingsViewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { notificationSettingsViewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    SaveStatusDialog(
        saveStatus = saveStatus,
        onDismiss = { notificationSettingsViewModel.clearSaveStatus() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plant Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showExitConfirmation = true
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            plantSettings?.let { settings ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(8.dp)
                ) {
                    item {
                        SectionHeader("Sensor Notifications")
                        Text(
                            "Alerts when sensors fall outside of the desired range",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        SensorSettingsSection(
                            title = "Soil Moisture",
                            subtitle = "Notify on range breach.",
                            enabled = currentSettings?.soilMoistureNotifications
                                ?: settings.soilMoistureNotifications,
                            minValue = currentSettings?.soilMoistureMin ?: settings.soilMoistureMin,
                            maxValue = currentSettings?.soilMoistureMax ?: settings.soilMoistureMax,
                            unit = "%"
                        ) { enabled, min, max ->
                            currentSettings = currentSettings?.copy(
                                soilMoistureNotifications = enabled,
                                soilMoistureMin = min,
                                soilMoistureMax = max
                            ) ?: settings.copy(
                                soilMoistureNotifications = enabled,
                                soilMoistureMin = min,
                                soilMoistureMax = max
                            )
                            hasChanges = true
                        }
                    }

                    item {
                        SensorSettingsSection(
                            title = "Soil Temperature",
                            subtitle = "Notify on range breach.",
                            enabled = currentSettings?.soilTempNotifications
                                ?: settings.soilTempNotifications,
                            minValue = currentSettings?.soilTempMin ?: settings.soilTempMin,
                            maxValue = currentSettings?.soilTempMax ?: settings.soilTempMax,
                            unit = "°C"
                        ) { enabled, min, max ->
                            currentSettings = currentSettings?.copy(
                                soilTempNotifications = enabled,
                                soilTempMin = min,
                                soilTempMax = max
                            ) ?: settings.copy(
                                soilTempNotifications = enabled,
                                soilTempMin = min,
                                soilTempMax = max
                            )
                            hasChanges = true
                        }
                    }

                    item {
                        WateringFrequencySettings(
                            enabled = currentSettings?.wateringReminderEnabled
                                ?: settings.wateringReminderEnabled,
                            frequency = currentSettings?.wateringReminderFrequency
                                ?: settings.wateringReminderFrequency,
                            interval = currentSettings?.wateringReminderInterval
                                ?: settings.wateringReminderInterval,
                            reminderTime = currentSettings?.wateringReminderTime
                                ?: settings.wateringReminderTime
                        ) { enabled, freq, interval, time ->
                            currentSettings = currentSettings?.copy(
                                wateringReminderEnabled = enabled,
                                wateringReminderFrequency = freq,
                                wateringReminderInterval = interval,
                                wateringReminderTime = time
                            ) ?: settings.copy(
                                wateringReminderEnabled = enabled,
                                wateringReminderFrequency = freq,
                                wateringReminderInterval = interval,
                                wateringReminderTime = time
                            )
                            hasChanges = true
                        }
                    }
                    item {
                        SectionHeader("Health Monitoring")

                        SettingsSwitch(
                            title = "Health Status Notifications",
                            subtitle = "Alerts on health status",
                            checked = currentSettings?.healthStatusNotifications
                                ?: settings.healthStatusNotifications,
                            onCheckedChange = { enabled ->
                                currentSettings =
                                    currentSettings?.copy(healthStatusNotifications = enabled)
                                        ?: settings.copy(healthStatusNotifications = enabled)
                                hasChanges = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SaveSettingsButton(
                            hasChanges = hasChanges,
                            onSave = {
                                currentSettings?.let {
                                    notificationSettingsViewModel.updatePlantSettings(plantId, it)
                                }
                            },
                            loading = loading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader("Danger Zone")
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Delete Plant")
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
            text = {
                Text(
                    "Are you sure you want to delete this plant? This action cannot be undone and will remove all associated data including sensor readings and watering history."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            plantViewModel.deletePlant(plantId)
                            navController.navigate("plantList") {
                                popUpTo("plantList") { inclusive = true }
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

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to exit without saving?") },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirmation = false
                    navController.navigateUp()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WateringFrequencySettings(
    enabled: Boolean,
    frequency: String,
    interval: Int?,
    reminderTime: String?,
    onUpdate: (Boolean, String, Int?, String?) -> Unit
) {
    var isEnabled by remember(enabled) { mutableStateOf(enabled) }
    var selectedFrequency by remember(frequency) { mutableStateOf(frequency) }
    var customInterval by remember(interval) { mutableStateOf(interval?.toString() ?: "") }
    var selectedTime by remember(reminderTime) { mutableStateOf(reminderTime ?: "09:00") }
    var timePickerVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SectionHeader("Watering Reminders")

        SettingsSwitch(
            title = "Enable Watering Reminders",
            subtitle = "Receive reminders to water your plant",
            checked = isEnabled,
            onCheckedChange = { newEnabled ->
                isEnabled = newEnabled
                onUpdate(newEnabled, selectedFrequency, customInterval.toIntOrNull(), selectedTime)
            }
        )

        if (isEnabled) {
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { }
            ) {
                Column {
                    Text(
                        "Reminder Frequency",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    SegmentedButtons(
                        items = listOf("SMART", "DAILY", "WEEKLY"),
                        selectedItem = selectedFrequency,
                        onItemSelect = { newFrequency ->
                            selectedFrequency = newFrequency
                            onUpdate(
                                isEnabled,
                                newFrequency,
                                customInterval.toIntOrNull(),
                                selectedTime
                            )
                        }
                    )
                }
            }
        }
    }

    if (timePickerVisible) {
        TimePickerDialog(
            initialTime = selectedTime,
            onTimeSelected = { newTime ->
                selectedTime = newTime
                onUpdate(isEnabled, selectedFrequency, customInterval.toIntOrNull(), newTime)
                timePickerVisible = false
            },
            onDismiss = { timePickerVisible = false }
        )
    }
}

@Composable
private fun SegmentedButtons(
    items: List<String>,
    selectedItem: String,
    onItemSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (item == selectedItem)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    )
                    .clickable { onItemSelect(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item == selectedItem)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = initialTime.split(":")[0].toInt()
    val initialMinute = initialTime.split(":")[1].toInt()

    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            TimePicker(
                initialHour = initialHour,
                initialMinute = initialMinute,
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    //broken
                    val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
                    onTimeSelected(timeString)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HealthCheckFrequencyDropdown(
    selected: String,
    onFrequencySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("HOURLY", "DAILY", "WEEKLY")

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Health Check Frequency",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onFrequencySelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NumberPicker(
                value = hour,
                onValueChange = {
                    hour = it
                    onTimeSelected(it, minute)
                },
                range = 0..23,
                format = { "%02d" }
            )

            Text(":", modifier = Modifier.padding(horizontal = 8.dp))

            NumberPicker(
                value = minute,
                onValueChange = {
                    minute = it
                    onTimeSelected(hour, it)
                },
                range = 0..59,
                format = { "%02d" }
            )
        }
    }
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    format: (Int) -> String = { it.toString() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Increase")
        }

        Text(
            text = format(value),
            style = MaterialTheme.typography.headlineMedium
        )

        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Decrease")
        }
    }
}
