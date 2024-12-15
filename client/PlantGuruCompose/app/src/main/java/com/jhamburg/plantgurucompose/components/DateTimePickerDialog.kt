package com.jhamburg.plantgurucompose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    initialDateTime: LocalDateTime? = null
) {
    var selectedDate by remember {
        mutableStateOf<Long?>(
            initialDateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
    }
    var showTimePicker by remember { mutableStateOf(false) }

    if (!showTimePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showTimePicker = true
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        val initialTime = initialDateTime?.toLocalTime() ?: LocalTime.now()
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                selectedDate?.let { date ->
                    val dateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(date),
                        ZoneId.systemDefault()
                    ).withHour(hour).withMinute(minute)
                    onDateTimeSelected(dateTime)
                }
            },

            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(state.hour, state.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = state)
        }
    )
} 