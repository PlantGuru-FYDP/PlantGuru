package com.jhamburg.plantgurucompose.screens.user

import android.annotation.SuppressLint
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.models.User
import com.jhamburg.plantgurucompose.viewmodels.LoginState
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel

@SuppressLint("RestrictedApi")
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }
    var phoneNumber by remember { mutableStateOf(TextFieldValue("")) }
    var passwordVisible by remember { mutableStateOf(false) }

    val userViewModel: UserViewModel = hiltViewModel()
    val loginState by userViewModel.loginState.collectAsState()
    val isLoading = loginState is LoginState.Loading

    val focusManager = LocalFocusManager.current

    val isNameValid = remember(name.text) {
        derivedStateOf { name.text.length >= 2 }
    }

    val isEmailValid = remember(email.text) {
        derivedStateOf {
            Patterns.EMAIL_ADDRESS.matcher(email.text).matches()
        }
    }

    val isPasswordValid = remember(password.text) {
        derivedStateOf {
            password.text.length >= 6 &&
                    password.text.any { it.isLetter() } &&
                    password.text.any { it.isDigit() }
        }
    }

    val isPhoneValid = remember(phoneNumber.text) {
        derivedStateOf {
            phoneNumber.text.isEmpty() || phoneNumber.text.matches(Regex("^[0-9]{10}$"))
        }
    }

    val canSubmit = remember(name.text, email.text, password.text, phoneNumber.text) {
        derivedStateOf {
            isNameValid.value && isEmailValid.value &&
                    isPasswordValid.value && isPhoneValid.value && !isLoading
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                val user = (loginState as LoginState.Success).user
                navController.navigate("plantList") {
                    popUpTo(0) { inclusive = true }
                }
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            enabled = !isLoading,
            label = { Text("Name") },
            supportingText = {
                if (!isNameValid.value && name.text.isNotEmpty()) {
                    Text(
                        "Name must be at least 2 characters",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            isError = !isNameValid.value && name.text.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            enabled = !isLoading,
            label = { Text("Email") },
            supportingText = {
                if (!isEmailValid.value && email.text.isNotEmpty()) {
                    Text(
                        "Please enter a valid email address",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            isError = !isEmailValid.value && email.text.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            enabled = !isLoading,
            label = { Text("Password") },
            supportingText = {
                if (!isPasswordValid.value && password.text.isNotEmpty()) {
                    Text(
                        "Password must be at least 6 characters with letters and numbers",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ),
                        contentDescription = if (passwordVisible)
                            "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            isError = !isPasswordValid.value && password.text.isNotEmpty()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            enabled = !isLoading,
            label = { Text("Address") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            enabled = !isLoading,
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            isError = phoneNumber.text.isNotEmpty() && !phoneNumber.text.matches(Regex("^[0-9]{10}$"))
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = loginState is LoginState.Error,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (loginState is LoginState.Error) {
                Text(
                    (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                userViewModel.signUp(
                    User(
                        userId = 0,
                        name = name.text,
                        email = email.text,
                        password = password.text,
                        address = address.text.takeIf { it.isNotEmpty() },
                        phoneNumber = phoneNumber.text.takeIf { it.isNotEmpty() }
                    )
                )
            },
            enabled = canSubmit.value,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }
    }
}
