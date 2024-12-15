package com.jhamburg.plantgurucompose.screens.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.components.SaveSettingsButton
import com.jhamburg.plantgurucompose.components.SaveStatusDialog
import com.jhamburg.plantgurucompose.components.SectionHeader
import com.jhamburg.plantgurucompose.components.SettingsSwitch
import com.jhamburg.plantgurucompose.models.UserNotificationSettings
import com.jhamburg.plantgurucompose.utils.PreferenceManager
import com.jhamburg.plantgurucompose.viewmodels.NotificationSettingsViewModel
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    userViewModel: UserViewModel = hiltViewModel(),
    notificationSettingsViewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var use24HourFormat by remember {
        mutableStateOf(PreferenceManager.is24HourFormat(context))
    }

    val userSettings by notificationSettingsViewModel.userSettings.collectAsState()
    val loading by notificationSettingsViewModel.loading.collectAsState()
    val error by notificationSettingsViewModel.error.collectAsState()
    val saveStatus by notificationSettingsViewModel.saveStatus.collectAsState()
    val navigationEvent by userViewModel.navigationEvent.collectAsState(initial = null)

    var hasChanges by remember { mutableStateOf(false) }
    var currentSettings by remember { mutableStateOf<UserNotificationSettings?>(null) }

    var showExitConfirmation by remember { mutableStateOf(false) }

    BackHandler(enabled = hasChanges) {
        showExitConfirmation = true
    }

    LaunchedEffect(Unit) {
        notificationSettingsViewModel.loadUserSettings()
    }

    LaunchedEffect(userSettings) {
        userSettings?.let {
            currentSettings = it
        }
    }

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { destination ->
            navController.navigate(destination) {
                popUpTo(0) { inclusive = true }
            }
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
                title = { Text("Settings") },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp)
            ) {
                item {
                    SectionHeader("Display Settings")
                }

                item {
                    SettingsSwitch(
                        title = "24-hour format",
                        subtitle = "Use 24-hour time",
                        checked = use24HourFormat,
                        onCheckedChange = { enabled ->
                            use24HourFormat = enabled
                            PreferenceManager.set24HourFormat(context, enabled)
                        }
                    )
                }

                item {
                    SectionHeader("Notifications")
                }

                userSettings?.let { settings ->
                    item {
                        SettingsSwitch(
                            title = "Email Alerts",
                            subtitle = "Receive email alerts",
                            checked = currentSettings?.emailNotifications
                                ?: settings.emailNotifications,
                            onCheckedChange = { enabled ->
                                currentSettings =
                                    currentSettings?.copy(emailNotifications = enabled)
                                        ?: settings.copy(emailNotifications = enabled)
                                hasChanges = true
                            }
                        )
                    }

                    item {
                        SettingsSwitch(
                            title = "Email Digests",
                            subtitle = "Receive email summaries of plant health",
                            checked = currentSettings?.emailDigests ?: settings.emailDigests,
                            onCheckedChange = { enabled ->
                                currentSettings = currentSettings?.copy(emailDigests = enabled)
                                    ?: settings.copy(emailDigests = enabled)
                                hasChanges = true
                            }
                        )
                    }

                    if ((currentSettings?.emailNotifications ?: settings.emailNotifications) ||
                        (currentSettings?.emailDigests ?: settings.emailDigests)
                    ) {
                        item {
                            OutlinedTextField(
                                value = currentSettings?.email ?: settings.email ?: "",
                                onValueChange = { email ->
                                    currentSettings = currentSettings?.copy(email = email)
                                        ?: settings.copy(email = email)
                                    hasChanges = true
                                },
                                label = { Text("Email") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    if (currentSettings?.emailDigests ?: settings.emailDigests) {
                        item {
                            DigestFrequencyDropdown(
                                selected = currentSettings?.digestsFrequency
                                    ?: settings.digestsFrequency,
                                onSelected = { frequency ->
                                    currentSettings =
                                        currentSettings?.copy(digestsFrequency = frequency)
                                            ?: settings.copy(digestsFrequency = frequency)
                                    hasChanges = true
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { userViewModel.logout() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Logout")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SaveSettingsButton(
                        hasChanges = hasChanges,
                        onSave = {
                            currentSettings?.let {
                                notificationSettingsViewModel.updateUserSettings(it)
                            }
                        },
                        loading = loading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
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
fun DigestFrequencyDropdown(
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("DAILY", "WEEKLY")

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Digest Frequency",
            style = MaterialTheme.typography.bodyLarge,
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
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
} 