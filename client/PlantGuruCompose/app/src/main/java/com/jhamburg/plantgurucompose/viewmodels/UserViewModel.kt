package com.jhamburg.plantgurucompose.viewmodels

import android.util.Log
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

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        _loginState.value = LoginState.Initial
        val initialUser = authManager.getCurrentUser()
        Log.d("UserViewModel", "Init - Loading initial user from AuthManager: ${initialUser?.name}")
        _user.value = initialUser
    }

    fun signUp(user: User) {
        viewModelScope.launch {
            _loading.value = true
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
            } finally {
                _loading.value = false
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _loginState.value = LoginState.Loading
            try {
                val response = userRepository.login(email, password)
                if (response.userId != null && response.token != null) {
                    Log.d("UserViewModel", "Login response received - User data: ${response.user}")
                    // Create user with all data from response
                    val user = User(
                        userId = response.userId,
                        name = response.user?.name ?: "",
                        email = response.user?.email ?: email,
                        password = "",
                        address = response.user?.address,
                        phoneNumber = response.user?.phoneNumber
                    )
                    handleLoginSuccess(user, response.token)
                } else {
                    _loginState.value = LoginState.Error(response.message)
                    _message.value = response.message
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
                _message.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun handleLoginSuccess(user: User, token: String) {
        viewModelScope.launch {
            Log.d("UserViewModel", "handleLoginSuccess - Saving user: ${user.name}")
            authManager.saveAuthToken(token)
            authManager.saveUser(user)
            fcmTokenManager.getToken()?.let { fcmToken ->
                fcmTokenManager.registerTokenWithBackend(fcmToken)
            }
            _loginState.value = LoginState.Success(user)
            _user.value = user
            Log.d("UserViewModel", "Login success complete - User saved and state updated")
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
            Log.d("UserViewModel", "updateProfile started - Updating user: ${user.name}")
            _loading.value = true
            try {
                val response = userRepository.updateUser(user)
                if (response.userId != null) {
                    Log.d("UserViewModel", "Update successful - Saving updated user")
                    authManager.saveUser(user)
                    _loginState.value = LoginState.Success(user)
                    _user.value = user
                    _message.value = "Profile updated successfully"
                } else {
                    Log.d("UserViewModel", "Update failed - ${response.message}")
                    _message.value = response.message
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Update error", e)
                _message.value = e.message
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun loadUserData() {
        val currentUser = authManager.getCurrentUser()
        Log.d("UserViewModel", "loadUserData called - Current user from AuthManager: ${currentUser?.name}")
        currentUser?.let {
            Log.d("UserViewModel", "Setting user in ViewModel - Name: ${it.name}, Email: ${it.email}")
            _user.value = it
        } ?: Log.d("UserViewModel", "No user found in AuthManager")
    }
}