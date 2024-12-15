package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.jhamburg.plantgurucompose.R

@Composable
fun PlantOptionsMenu(
    onEditClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    showSettings: Boolean = false,
    onDirectSensorClick: () -> Unit = {},
    showDirectSensor: Boolean = false,
    isConnected: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_more_vert_24),
                contentDescription = "More options"
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit Plant Details") },
                onClick = {
                    showMenu = false
                    onEditClick()
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, "Edit Plant Details")
                }
            )

            if (showSettings) {
                DropdownMenuItem(
                    text = { Text("Settings") },
                    onClick = {
                        showMenu = false
                        onSettingsClick()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            "Settings"
                        )
                    }
                )
            }

            if (showDirectSensor) {
                DropdownMenuItem(
                    text = { Text(if (isConnected) "Direct Sensor Reading" else "Connect Sensor") },
                    onClick = {
                        showMenu = false
                        onDirectSensorClick()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(
                                id = if (isConnected)
                                    R.drawable.ic_bluetooth
                                else
                                    R.drawable.baseline_sensors_24
                            ),
                            contentDescription = if (isConnected)
                                "Direct Sensor Reading"
                            else
                                "Connect Sensor"
                        )
                    }
                )
            }
        }
    }
} 