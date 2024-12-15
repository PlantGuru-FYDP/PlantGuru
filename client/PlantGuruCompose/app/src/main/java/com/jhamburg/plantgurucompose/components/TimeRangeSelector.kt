package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jhamburg.plantgurucompose.models.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    onCustomClicked: () -> Unit = {},
    customGranularity: Int? = null,
    onGranularitySelected: (Int) -> Unit = {},
    showGranularityDropdown: Boolean = false,
    onGranularityDropdownChange: (Boolean) -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = showDropdown,
            onExpandedChange = { showDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedRange.label,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("Time Range") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            ExposedDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }
            ) {
                TimeRange.entries.forEach { range ->
                    DropdownMenuItem(
                        text = { Text(range.label, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            if (range == TimeRange.CUSTOM) {
                                onCustomClicked()
                            } else {
                                onRangeSelected(range)
                            }
                            showDropdown = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        if (selectedRange == TimeRange.CUSTOM && customGranularity != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = showGranularityDropdown,
                onExpandedChange = onGranularityDropdownChange
            ) {
                OutlinedTextField(
                    value = when (customGranularity) {
                        5 -> "5 Minutes"
                        15 -> "15 Minutes"
                        30 -> "30 Minutes"
                        60 -> "1 Hour"
                        360 -> "6 Hours"
                        720 -> "12 Hours"
                        1440 -> "1 Day"
                        else -> "$customGranularity Minutes"
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showGranularityDropdown) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    label = { Text("Granularity") }
                )

                ExposedDropdownMenu(
                    expanded = showGranularityDropdown,
                    onDismissRequest = { onGranularityDropdownChange(false) }
                ) {
                    listOf(5, 15, 30, 60, 360, 720, 1440).forEach { minutes ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (minutes) {
                                        5 -> "5 Minutes"
                                        15 -> "15 Minutes"
                                        30 -> "30 Minutes"
                                        60 -> "1 Hour"
                                        360 -> "6 Hours"
                                        720 -> "12 Hours"
                                        1440 -> "1 Day"
                                        else -> "$minutes Minutes"
                                    }
                                )
                            },
                            onClick = {
                                onGranularitySelected(minutes)
                                onGranularityDropdownChange(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeRangeSelectorPreview() {
    MaterialTheme {
        TimeRangeSelector(
            selectedRange = TimeRange.WEEK_1,
            onRangeSelected = {},
            onCustomClicked = {},
            customGranularity = 360,
            onGranularitySelected = {},
            showGranularityDropdown = false,
            onGranularityDropdownChange = {}
        )
    }
}