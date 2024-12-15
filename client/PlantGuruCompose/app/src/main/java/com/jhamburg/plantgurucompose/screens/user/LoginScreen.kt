package com.jhamburg.plantgurucompose.screens.user

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel
import com.jhamburg.plantgurucompose.viewmodels.LoginState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import android.util.Patterns
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@SuppressLint("RestrictedApi")
@Composable
fun LoginScreen(navController: NavController) {
    val userViewModel: UserViewModel = hiltViewModel()
    var email by remember { mutableStateOf("placeholder@email.com") }
    var password by remember { mutableStateOf("password") }
    val loginState by userViewModel.loginState.collectAsState()
    val isLoading = loginState is LoginState.Loading
    
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isLoading) {
        if (isLoading) {
            navController.enableOnBackPressed(false)
        } else {
            navController.enableOnBackPressed(true)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            enabled = !isLoading,
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            isError = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            enabled = !isLoading,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        userViewModel.login(email, password)
                    }
                }
            ),
            singleLine = true,
            isError = password.isNotEmpty() && password.length < 6
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        when (loginState) {
            is LoginState.Loading -> {
                CircularProgressIndicator()
            }
            is LoginState.Error -> {
                Text((loginState as LoginState.Error).message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { userViewModel.login(email, password) },
                    enabled = !isLoading
                ) {
                    Text("Retry")
                }
            }
            else -> {
                Button(
                    onClick = { userViewModel.login(email, password) },
                    enabled = !isLoading
                ) {
                    Text("Login")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (loginState is LoginState.Success) {
            val user = (loginState as LoginState.Success).user
            LaunchedEffect(user) {
                navController.navigate("plantList") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}
