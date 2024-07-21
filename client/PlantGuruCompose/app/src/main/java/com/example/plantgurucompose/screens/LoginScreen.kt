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
import com.example.plantgurucompose.viewmodels.UserViewModel
import com.example.plantgurucompose.viewmodels.LoginState

@Composable
fun LoginScreen(navController: NavController) {
    val userViewModel: UserViewModel = hiltViewModel()
    var email by remember { mutableStateOf("placeholder@email.com") }
    var password by remember { mutableStateOf("password") }
    val loginState by userViewModel.loginState.observeAsState(LoginState.Initial)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        when (loginState) {
            is LoginState.Loading -> {
                CircularProgressIndicator()
            }
            is LoginState.Error -> {
                Text((loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    userViewModel.login(email, password)
                }) {
                    Text("Retry")
                }
            }
            else -> {
                Button(onClick = {
                    userViewModel.login(email, password)
                }) {
                    Text("Login")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (loginState is LoginState.Success) {
            val user = (loginState as LoginState.Success).user
            LaunchedEffect(user) {
                navController.navigate("plantList/${user.userId}")
            }
        }
    }
}
