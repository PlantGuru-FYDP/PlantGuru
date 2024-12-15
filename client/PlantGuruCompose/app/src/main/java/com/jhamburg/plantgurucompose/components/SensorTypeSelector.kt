package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.jhamburg.plantgurucompose.models.SensorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorTypeSelector(
    selectedType: SensorType,
    onTypeSelected: (SensorType) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = showDropdown,
        onExpandedChange = { showDropdown = it }
    ) {
        OutlinedTextField(
            value = selectedType.label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            label = { Text("Sensor Type") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        ExposedDropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            SensorType.values().forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onTypeSelected(type)
                        showDropdown = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SensorTypeSelectorPreview() {
    MaterialTheme {
        SensorTypeSelector(
            selectedType = SensorType.SOIL_MOISTURE,
            onTypeSelected = {}
        )
    }
}