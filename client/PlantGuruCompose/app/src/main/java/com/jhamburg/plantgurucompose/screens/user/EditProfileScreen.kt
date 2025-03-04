package com.jhamburg.plantgurucompose.screens.user

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.components.SaveSettingsButton
import com.jhamburg.plantgurucompose.components.SaveStatusDialog
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val message by viewModel.message.collectAsState()
    val loading by viewModel.loading.collectAsState(initial = false)

    // Load user data when screen opens
    LaunchedEffect(Unit) {
        Log.d("EditProfileScreen", "Screen opened - Loading user data")
        viewModel.loadUserData()
    }

    // Store initial values from user
    val initialUser = remember(user) { 
        Log.d("EditProfileScreen", "Remembering initial user - User: ${user?.name}")
        user 
    }
    
    var name by remember(initialUser) { 
        Log.d("EditProfileScreen", "Setting initial name value: ${initialUser?.name}")
        mutableStateOf(initialUser?.name ?: "") 
    }
    var email by remember(initialUser) { 
        Log.d("EditProfileScreen", "Setting initial email value: ${initialUser?.email}")
        mutableStateOf(initialUser?.email ?: "") 
    }
    var address by remember(initialUser) { 
        Log.d("EditProfileScreen", "Setting initial address value: ${initialUser?.address}")
        mutableStateOf(initialUser?.address ?: "") 
    }
    var phoneNumber by remember(initialUser) { 
        Log.d("EditProfileScreen", "Setting initial phone value: ${initialUser?.phoneNumber}")
        mutableStateOf(initialUser?.phoneNumber ?: "") 
    }
    
    // Calculate if there are actual changes by comparing with initial values
    val hasChanges = remember(name, email, address, phoneNumber, initialUser) {
        val changes = initialUser != null && (
            name != initialUser.name ||
            email != initialUser.email ||
            address != (initialUser.address ?: "") ||
            phoneNumber != (initialUser.phoneNumber ?: "")
        )
        Log.d("EditProfileScreen", "Checking for changes - Has changes: $changes")
        changes
    }

    BackHandler {
        navController.navigateUp()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                SaveSettingsButton(
                    hasChanges = hasChanges,
                    onSave = {
                        initialUser?.let { currentUser ->
                            viewModel.updateProfile(
                                currentUser.copy(
                                    name = name,
                                    email = email,
                                    address = address,
                                    phoneNumber = phoneNumber
                                )
                            )
                        }
                    },
                    loading = loading,
                    modifier = Modifier.fillMaxWidth()
                )

                message?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
} 