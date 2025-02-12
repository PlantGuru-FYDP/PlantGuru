package com.jhamburg.plantgurucompose.screens.provisioning

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantCategory
import com.jhamburg.plantgurucompose.models.PlantSubType
import com.jhamburg.plantgurucompose.utils.ImageStorageManager
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlantScreen(navController: NavController, userId: Int) {
    var plantName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Plant type selection states
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<PlantCategory?>(null) }
    var showSubTypeDropdown by remember { mutableStateOf(false) }
    var selectedSubType by remember { mutableStateOf<PlantSubType?>(null) }

    val plantViewModel: PlantViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File.createTempFile("temp_image", ".jpg", context.cacheDir)
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUri = it }
    }

    if (showImagePicker) {
        AlertDialog(
            onDismissRequest = { showImagePicker = false },
            title = { Text("Choose Image Source") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImagePicker = false
                            cameraLauncher.launch(tempImageUri)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo")
                    }
                    TextButton(
                        onClick = {
                            showImagePicker = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImagePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Plant") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { showImagePicker = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Selected plant image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Tap to select plant image")
                }
            }

            OutlinedTextField(
                value = plantName,
                onValueChange = { plantName = it },
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plant Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    PlantCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.toString()) },
                            onClick = {
                                selectedCategory = category
                                selectedSubType = null
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            selectedCategory?.let { category ->
                ExposedDropdownMenuBox(
                    expanded = showSubTypeDropdown,
                    onExpandedChange = { showSubTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedSubType?.toString() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sub Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubTypeDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showSubTypeDropdown,
                        onDismissRequest = { showSubTypeDropdown = false }
                    ) {
                        PlantSubType.getSubTypesForCategory(category).forEach { subType ->
                            DropdownMenuItem(
                                text = { Text(subType.toString()) },
                                onClick = {
                                    selectedSubType = subType
                                    showSubTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (plantName.isBlank()) {
                        errorMessage = "Plant name is required"
                        return@Button
                    }
                    scope.launch {
                        try {
                            isLoading = true
                            val imageManager = ImageStorageManager(context)
                            var tempUri: Uri? = null
                            val tempImageUri = imageUri?.let { uri ->
                                imageManager.saveImage(uri, -1).also { tempUri = it }
                            }
                            val (plantId, provisionToken) = plantViewModel.createPlant(
                                userId = userId,
                                plantName = plantName.trim(),
                                lastWatered = LocalDateTime.now(),
                                nextWateringTime = LocalDateTime.now().plusDays(7),
                                additionalDetails = PlantAdditionalDetails(
                                    scientificName = "",
                                    plantType = selectedSubType?.toString() ?: selectedCategory?.toString() ?: "",
                                    createdOn = System.currentTimeMillis(),
                                    imageUri = null // Don't set the URI yet
                                )
                            )

                            // Now that we have the plantId, save the image with the correct ID
                            val finalImageUri = tempUri?.let { uri ->
                                imageManager.saveImage(uri, plantId)
                            }
                            
                            // Clean up the temporary image
                            imageManager.deleteImage(-1)

                            plantViewModel.setNeedsRefresh(true)
                            if (provisionToken.isNotBlank()) {
                                navController.navigate("ble_provision_landing/$plantId/$provisionToken")
                            } else {
                                navController.navigate("plantDetail/$plantId") {
                                    popUpTo("createPlant/{userId}") { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("CreatePlantScreen", "Error creating plant", e)
                            errorMessage = "Failed to create plant: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && plantName.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Plant")
                }
            }
        }
    }
}