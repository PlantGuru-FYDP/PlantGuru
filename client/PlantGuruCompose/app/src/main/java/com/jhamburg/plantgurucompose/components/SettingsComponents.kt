package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    divider: Boolean = true,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        if (divider) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun SensorSettingsSection(
    title: String,
    subtitle: String,
    enabled: Boolean,
    minValue: Float?,
    maxValue: Float?,
    unit: String,
    onUpdate: (Boolean, Float?, Float?) -> Unit
) {
    var minText by remember(minValue) { mutableStateOf(minValue?.toString() ?: "") }
    var maxText by remember(maxValue) { mutableStateOf(maxValue?.toString() ?: "") }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        SettingsSwitch(
            title = "$title Notifications",
            subtitle = "Alerts for out-of-range values",
            checked = enabled,
            onCheckedChange = { newEnabled ->
                onUpdate(newEnabled, minValue, maxValue)
            },
            divider = false
        )

        if (enabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = minText,
                    onValueChange = {
                        minText = it
                        onUpdate(enabled, it.toFloatOrNull(), maxValue)
                    },
                    label = { Text("Min") },
                    suffix = { Text(unit) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = maxText,
                    onValueChange = {
                        maxText = it
                        onUpdate(enabled, minValue, it.toFloatOrNull())
                    },
                    label = { Text("Max") },
                    suffix = { Text(unit) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
} 