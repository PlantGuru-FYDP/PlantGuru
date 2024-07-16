package com.example.plantgurucompose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.plantgurucompose.models.User
import com.example.plantgurucompose.viewmodels.UserViewModel

@Composable
fun SignUpScreen(navController: NavController) {
    val userViewModel: UserViewModel = hiltViewModel()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val message by userViewModel.message.observeAsState("")

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = phoneNumber, onValueChange = { phoneNumber = it }, label = { Text("Phone Number") })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            userViewModel.signUp(User(0, name, email, password, address, phoneNumber))
        }) {
            Text("Sign Up")
        }
        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        if (userViewModel.isLogged.value == true) {
            userViewModel.user.value?.let { user ->
                navController.navigate("plantList/${user.userId}")
            }
        }
    }
}
