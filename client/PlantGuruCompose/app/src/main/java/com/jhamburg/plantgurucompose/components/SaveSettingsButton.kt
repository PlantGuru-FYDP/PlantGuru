package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.viewmodels.NotificationSettingsViewModel


@Composable
fun SaveSettingsButton(
    hasChanges: Boolean,
    onSave: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onSave,
            enabled = hasChanges && !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Changes")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveStatusDialog(
    saveStatus: NotificationSettingsViewModel.SaveStatus?,
    onDismiss: () -> Unit
) {
    saveStatus?.let { status ->
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    painter = painterResource(
                        id = when (status) {
                            is NotificationSettingsViewModel.SaveStatus.Success -> R.drawable.baseline_check_circle_24
                            is NotificationSettingsViewModel.SaveStatus.Error -> R.drawable.baseline_error_24
                        }
                    ),
                    contentDescription = null,
                    tint = when (status) {
                        is NotificationSettingsViewModel.SaveStatus.Success -> MaterialTheme.colorScheme.primary
                        is NotificationSettingsViewModel.SaveStatus.Error -> MaterialTheme.colorScheme.error
                    }
                )
            },
            title = {
                Text(
                    when (status) {
                        is NotificationSettingsViewModel.SaveStatus.Success -> "Settings Saved"
                        is NotificationSettingsViewModel.SaveStatus.Error -> "Error Saving Settings"
                    }
                )
            },
            text = {
                when (status) {
                    is NotificationSettingsViewModel.SaveStatus.Success ->
                        Text("Your settings have been successfully updated.")

                    is NotificationSettingsViewModel.SaveStatus.Error ->
                        Text(status.message)
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
} 