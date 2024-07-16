package com.example.plantgurucompose.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.plantgurucompose.models.SensorData
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color
import android.view.ViewGroup
import androidx.compose.runtime.livedata.observeAsState
import com.example.plantgurucompose.viewmodels.PlantViewModel
import com.example.plantgurucompose.viewmodels.SensorDataViewModel
import com.example.plantgurucompose.viewmodels.WateringEventViewModel
import com.example.plantgurucompose.viewmodels.UserViewModel

@Composable
fun PlantDetailScreen(navController: NavController, plantId: Int) {
    val plantViewModel: PlantViewModel = hiltViewModel()
    val sensorDataViewModel: SensorDataViewModel = hiltViewModel()
    val wateringEventViewModel: WateringEventViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()

    val sensorData by sensorDataViewModel.sensorData.collectAsState()
    val sensorLoading by sensorDataViewModel.loading.collectAsState()
    val sensorError by sensorDataViewModel.error.collectAsState()

    val wateringEvents by wateringEventViewModel.wateringEvents.collectAsState()
    val wateringLoading by wateringEventViewModel.loading.collectAsState()
    val wateringError by wateringEventViewModel.error.collectAsState()

    val plants by plantViewModel.plants.collectAsState()
    val plantLoading by plantViewModel.loading.collectAsState()
    val plantError by plantViewModel.error.collectAsState()

    val user by userViewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        sensorDataViewModel.getLastNSensorReadings(plantId, 10)
        wateringEventViewModel.getWateringEvents(plantId)
        user?.let { plantViewModel.getPlants(it.userId) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (sensorLoading || wateringLoading || plantLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            plantError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            plants.firstOrNull { it.plantId == plantId }?.let { plant ->
                Text("Plant Information", style = MaterialTheme.typography.titleLarge)
                Text("Name: ${plant.plantName}")
                Text("Age: ${plant.age}")
                Text("Last Watered: ${plant.lastWatered}")
                Text("Next Watering Time: ${plant.nextWateringTime}")
            }
            sensorError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            sensorData.firstOrNull()?.let { sensor ->
                Text("Latest Sensor Data", style = MaterialTheme.typography.titleLarge)
                Text("External Temp: ${sensor.extTemp}")
                Text("Humidity: ${sensor.humidity}")
                Text("Light: ${sensor.light}")
                Text("Soil Temp: ${sensor.soilTemp}")
                Text("Soil Moisture 1: ${sensor.soilMoisture1}")
                Text("Soil Moisture 2: ${sensor.soilMoisture2}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            wateringError?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            wateringEvents.firstOrNull()?.let { wateringEvent ->
                Text("Latest Watering Event", style = MaterialTheme.typography.titleLarge)
                Text("Duration: ${wateringEvent.wateringDuration}")
                Text("Peak Temp: ${wateringEvent.peakTemp}")
                Text("Peak Moisture: ${wateringEvent.peakMoisture}")
                Text("Avg Temp: ${wateringEvent.avgTemp}")
                Text("Avg Moisture: ${wateringEvent.avgMoisture}")
                Text("Volume: ${wateringEvent.volume}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (sensorData.isNotEmpty()) {
                Text("Sensor Data Graph", style = MaterialTheme.typography.titleLarge)
                SensorDataGraph(sensorData)
            }
        }
    }
}

@Composable
fun SensorDataGraph(sensorData: List<SensorData>) {
    val context = LocalContext.current

    AndroidView(factory = { context ->
        LineChart(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            axisRight.isEnabled = false

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = IndexAxisValueFormatter(sensorData.map { it.timeStamp })

            data = LineData().apply {
                val extTempEntries = sensorData.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.extTemp)
                }
                val humidityEntries = sensorData.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.humidity)
                }
                // Add more entries for other sensor data here

                addDataSet(LineDataSet(extTempEntries, "External Temp").apply {
                    color = Color.RED
                    valueTextColor = Color.BLACK
                })
                addDataSet(LineDataSet(humidityEntries, "Humidity").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                })
                // Add more data sets for other sensor data here
            }
        }
    })
}
