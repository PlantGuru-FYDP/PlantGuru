package com.jhamburg.plantgurucompose.screens.plants

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.jhamburg.plantgurucompose.models.PlantAdditionalDetails
import com.jhamburg.plantgurucompose.models.PlantCategory
import com.jhamburg.plantgurucompose.models.PlantSubType
import com.jhamburg.plantgurucompose.viewmodels.PlantViewModel
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.io.File
import android.content.pm.PackageManager
import android.content.Intent
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Deferred


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantScreen(
    navController: NavController,
    plantId: Int
) {
    val plantViewModel: PlantViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val user by userViewModel.user.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val plants by plantViewModel.plants.collectAsState()
    val currentPlant = plants.find { it.plantId == plantId }
    val plantDetails by plantViewModel.plantDetails.collectAsState()
    val currentDetails = plantDetails[plantId]

    var plantName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<PlantCategory?>(null) }
    var selectedSubType by remember { mutableStateOf<PlantSubType?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

        var isSaving by remember { mutableStateOf(false) }
    var originalPlantName by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSubTypeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(currentPlant, currentDetails) {
        Log.d("EditPlantScreen", "Current plant: $currentPlant")
        Log.d("EditPlantScreen", "Current details: $currentDetails")
        Log.d("EditPlantScreen", "Current image URI: ${currentDetails?.imageUri}")
        
        currentPlant?.let {
            plantName = it.plantName
            originalPlantName = it.plantName
            Log.d("EditPlantScreen", "Set plant name: $plantName")
        }
        currentDetails?.let {
            selectedCategory = it.getCategory()
            selectedSubType = it.getSubType()
            Log.d("EditPlantScreen", "Category: $selectedCategory, SubType: $selectedSubType")
            
            imageUri = it.imageUri?.let { uri -> 
                Uri.parse(uri).also { parsed ->
                    Log.d("EditPlantScreen", "Parsed image URI: $parsed")
                }
            }
        }
    }

        LaunchedEffect(plantId) {
        Log.d("EditPlantScreen", "Loading data for plantId: $plantId")
        Log.d("EditPlantScreen", "Current user: $user")
        user?.let { 
            Log.d("EditPlantScreen", "Fetching plants for userId: ${it.userId}")
            plantViewModel.getPlants(it.userId)
            plantViewModel.getPlantDetails(plantId)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }

    val tempImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File.createTempFile("temp_image", ".jpg", context.cacheDir)
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
        }
    }

        var hasValidationErrors by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    fun validateInputs(): Boolean {
        return when {
            plantName.isBlank() -> {
                validationMessage = "Plant name is required"
                false
            }
            selectedCategory == null -> {
                validationMessage = "Plant type is required"
                false
            }
            selectedSubType == null -> {
                validationMessage = "Plant sub-type is required"
                false
            }
            else -> true
        }
    }

        var showPermissionDialog by remember { mutableStateOf(false) }
    
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(tempImageUri)
        } else {
            showPermissionDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Plant") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete Plant")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { showImagePicker = true }
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Plant Image",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            "Tap to add image",
                            modifier = Modifier.align(Alignment.Center)
                        )
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

                if (hasValidationErrors) {
                    Text(
                        text = validationMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (validateInputs() && !isSaving) {
                            hasValidationErrors = false
                            isSaving = true
                            Log.d("EditPlantScreen", """
                                Save initiated:
                                - Original name: $originalPlantName
                                - New name: $plantName
                                - Plant ID: $plantId
                            """.trimIndent())
                            
                            scope.launch {
                                try {
                                    val nameChanged = plantName.trim() != originalPlantName
                                    
                                    if (nameChanged) {
                                        Log.d("EditPlantScreen", """
                                            Starting name update:
                                            - Original name: $originalPlantName
                                            - New name: $plantName
                                            - Plant ID: $plantId
                                        """.trimIndent())
                                        
                                        plantViewModel.updatePlant(
                                            plantId = plantId,
                                            plantName = plantName.trim()
                                        )
                                        
                                        Log.d("EditPlantScreen", "Name update completed")
                                    }

                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("refresh", true)
                                    navController.navigateUp()
                                } catch (e: Exception) {
                                    Log.e("EditPlantScreen", "Error saving changes: ${e.message}")
                                    hasValidationErrors = true
                                    validationMessage = "Failed to save changes: ${e.message}"
                                } finally {
                                    isSaving = false
                                }
                            }
                        } else {
                            hasValidationErrors = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving && plantName.isNotBlank() && selectedCategory != null && selectedSubType != null
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }

                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Plant") },
            text = { Text("Are you sure you want to delete this plant? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            plantViewModel.deletePlant(plantId)
                            navController.navigate("plantList") {
                                popUpTo("plantList") { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) -> {
                                    cameraLauncher.launch(tempImageUri)
                                }
                                else -> {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Take Photo")
                    }
                    TextButton(
                        onClick = {
                            showImagePicker = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
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

        if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { 
                Text("Camera permission is needed to take photos of your plants. " +
                     "Please grant permission in Settings to use this feature.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 