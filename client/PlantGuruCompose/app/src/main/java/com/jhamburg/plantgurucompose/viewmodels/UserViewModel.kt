package com.jhamburg.plantgurucompose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jhamburg.plantgurucompose.auth.AuthManager
import com.jhamburg.plantgurucompose.models.User
import com.jhamburg.plantgurucompose.notifications.FCMTokenManager
import com.jhamburg.plantgurucompose.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authManager: AuthManager,
    private val fcmTokenManager: FCMTokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        authManager.getCurrentUser()?.let { user ->
            _loginState.value = LoginState.Success(user)
            _user.value = user
        }
    }

    fun signUp(user: User) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = userRepository.signUp(user)
                if (response.userId != null && response.token != null) {
                    val newUser = user.copy(userId = response.userId)
                    authManager.saveAuthToken(response.token)
                    authManager.saveUser(newUser)
                    fcmTokenManager.getToken()?.let { fcmToken ->
                        fcmTokenManager.registerTokenWithBackend(fcmToken)
                    }
                    _loginState.value = LoginState.Success(newUser)
                    _user.value = newUser
                    _message.value = "Sign up successful"
                } else {
                    _loginState.value = LoginState.Error(response.message)
                    _message.value = response.message
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
                _message.value = e.message
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = userRepository.login(email, password)
                if (response.userId != null && response.token != null) {
                    val user = User(
                        userId = response.userId,
                        name = "",
                        email = email,
                        password = "",
                        address = null,
                        phoneNumber = null
                    )
                    handleLoginSuccess(user, response.token)
                } else {
                    _loginState.value = LoginState.Error(response.message)
                    _message.value = response.message
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
                _message.value = e.message
            }
        }
    }

    private fun handleLoginSuccess(user: User, token: String) {
        viewModelScope.launch {
            authManager.saveAuthToken(token)
            authManager.saveUser(user)
            fcmTokenManager.getToken()?.let { fcmToken ->
                fcmTokenManager.registerTokenWithBackend(fcmToken)
            }
            _loginState.value = LoginState.Success(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            fcmTokenManager.deleteToken()
            authManager.clearAuth()
            _loginState.value = LoginState.Initial
            _user.value = null
            _message.value = null
            _navigationEvent.emit("welcome")
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            try {
                val response = userRepository.updateUser(user)
                if (response.userId != null) {
                    authManager.saveUser(user)
                    _loginState.value = LoginState.Success(user)
                    _user.value = user
                    _message.value = "Profile updated successfully"
                } else {
                    _message.value = response.message
                }
            } catch (e: Exception) {
                _message.value = e.message
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}