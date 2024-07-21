package com.example.plantgurucompose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.plantgurucompose.models.PlantResponse
import com.example.plantgurucompose.viewmodels.PlantViewModel

@Composable
fun PlantListScreen(navController: NavController, userId: Int) {
    val plantViewModel: PlantViewModel = hiltViewModel()
    val plants by plantViewModel.plants.collectAsState()
    val loading by plantViewModel.loading.collectAsState()
    val error by plantViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        plantViewModel.getPlants(userId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            error != null -> {
                Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                Button(onClick = { plantViewModel.getPlants(userId) }) {
                    Text("Retry")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(plants.size) { index ->
                        val plant = plants[index]
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                navController.navigate("plantDetail/${plant.plantId}")
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(plant.plantName, style = MaterialTheme.typography.titleLarge)
                                Text("Age: ${plant.age}")
                            }
                        }
                    }
                }
            }
        }
    }
}
