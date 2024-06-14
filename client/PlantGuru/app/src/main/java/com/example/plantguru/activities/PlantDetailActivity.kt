// PlantDetailScreen.kt
package com.example.plantguru.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.plantguru.data.PlantRepository
import com.example.plantguru.models.Plant
import com.example.plantguru.utils.BluetoothService
import com.example.plantguru.viewmodels.PlantViewModel
import com.example.plantguru.viewmodels.PlantViewModelFactory
import java.util.UUID
/*
class PlantDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = PlantRepository()
        val viewModel = ViewModelProvider(this, PlantViewModelFactory(repository))
            .get(PlantViewModel::class.java)

        //val plant = Plant("Plant Name", "UUID")
        //val deviceAddress = "bruh address"
        //val bluetoothService = BluetoothService(this, deviceAddress)
        //val plantViewModelFactory = PlantViewModelFactory(bluetoothService)

        setContent {
            PlantDetailScreen(viewModel)
        }
    }
}

@Composable
fun PlantDetailScreen(plantViewModel: PlantViewModel) {
    //val sensorData by plantViewModel.sensorData.observeAsState()
    //val settings by plantViewModel.settings.observeAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "ferny", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Display the sensor data in a chart
        //sensorData?.let {
        //    SensorDataChart(sensorData = it)
        //}
    }
}

@Composable
fun SensorDataChart(sensorData: Int) {
    // Implement chart to display sensor data
    // This could use a library like MPAndroidChart or Compose Canvas
}

@Composable
fun SettingsList(settings: Map<String, String>, onSettingChange: (UUID, String) -> Unit) {
    Column {
        settings.forEach { (uuid, value) ->
            SettingItem(uuid = UUID.fromString(uuid), value = value, onSettingChange = onSettingChange)
        }
    }
}

@Composable
fun SettingItem(uuid: UUID, value: String, onSettingChange: (UUID, String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = uuid.toString(), modifier = Modifier.weight(1f))
        TextField(
            value = value,
            onValueChange = { newValue -> onSettingChange(uuid, newValue) },
            modifier = Modifier.weight(2f)
        )
    }
}*/