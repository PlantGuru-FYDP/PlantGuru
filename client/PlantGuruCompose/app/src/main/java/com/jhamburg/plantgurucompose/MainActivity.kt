package com.jhamburg.plantgurucompose

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.espressif.provisioning.ESPProvisionManager
import com.jhamburg.plantgurucompose.constants.AppConstants
import com.jhamburg.plantgurucompose.screens.provisioning.BLEProvisionLandingScreen
import com.jhamburg.plantgurucompose.screens.provisioning.ConnectionHomeScreen
import com.jhamburg.plantgurucompose.screens.provisioning.ProvisionScreen
import com.jhamburg.plantgurucompose.screens.provisioning.WiFiScanScreen
import com.jhamburg.plantgurucompose.screens.user.LoginScreen
import com.jhamburg.plantgurucompose.screens.plants.PlantDetailScreen
import com.jhamburg.plantgurucompose.screens.plants.PlantListScreen
import com.jhamburg.plantgurucompose.screens.user.SignUpScreen
import com.jhamburg.plantgurucompose.screens.user.WelcomeScreen
import com.jhamburg.plantgurucompose.screens.user.EditProfileScreen
import com.jhamburg.plantgurucompose.screens.plants.EditPlantScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.jhamburg.plantgurucompose.screens.provisioning.CreatePlantScreen
import com.jhamburg.plantgurucompose.ui.theme.PlantGuruComposeTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.jhamburg.plantgurucompose.config.BuildConfig
import com.jhamburg.plantgurucompose.viewmodels.UserViewModel
import com.jhamburg.plantgurucompose.screens.plants.DirectSensorScreen
import com.jhamburg.plantgurucompose.screens.user.SettingsScreen
import android.os.Build
import androidx.lifecycle.lifecycleScope
import com.jhamburg.plantgurucompose.notifications.FCMTokenManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.collectAsState
import androidx.navigation.navArgument
import com.jhamburg.plantgurucompose.screens.plants.PlantSettingsScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var provisionManager: ESPProvisionManager
    private lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var fcmTokenManager: FCMTokenManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getAndSendToken()
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlantGuruComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val userViewModel: UserViewModel = hiltViewModel()
                    val savedUser by userViewModel.user.collectAsState()

                    LaunchedEffect(Unit) {
                        if (!BuildConfig.DEBUG && savedUser != null) {
                            navController.navigate("plantList") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    }

                    // Handle notification navigation
                    LaunchedEffect(Unit) {
                        intent.getStringExtra("navigation")?.let { route ->
                            navController.navigate(route)
                        }
                    }

                    NavHost(navController = navController, startDestination = "welcome") {
                        composable("welcome") {
                            WelcomeScreen(navController)
                        }
                        composable("connection_home") {
                            ConnectionHomeScreen(
                                navController = navController,
                                bluetoothAdapter = bluetoothAdapter,
                                provisionManager = provisionManager,
                                sharedPreferences = sharedPreferences,
                                enableBluetoothLauncher = enableBluetoothLauncher,
                                requestPermissionsLauncher = requestPermissionsLauncher
                            )
                        }
                        composable(
                            "ble_provision_landing/{plantId}/{provisionToken}",
                            arguments = listOf(
                                navArgument("plantId") { type = NavType.IntType },
                                navArgument("provisionToken") { 
                                    type = NavType.StringType 
                                    nullable = true
                                }
                            )
                        ) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
                            val provisionToken = backStackEntry.arguments?.getString("provisionToken") ?: ""
                            BLEProvisionLandingScreen(
                                navController = navController,
                                plantId = plantId,
                                provisionToken = provisionToken
                            )
                        }
                        composable("wifi_scan/{plantId}/{provisionToken}", 
                                    arguments = listOf(
                                        navArgument("plantId") { type = NavType.IntType },
                                        navArgument("provisionToken") { 
                                            type = NavType.StringType
                                            nullable = true 
                                        }
                                    )) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
                            val provisionToken = backStackEntry.arguments?.getString("provisionToken")
                            WiFiScanScreen(
                                navController = navController,
                                plantId = plantId,
                                provisionToken = provisionToken ?: ""
                            )
                        }
                        composable(
                            route = "provision/{plantId}/{ssid}/{networkPassword}/{provisionToken}/{isEnterprise}/{identity}/{userPassword}",
                            arguments = listOf(
                                navArgument("plantId") { type = NavType.IntType },
                                navArgument("ssid") { type = NavType.StringType },
                                navArgument("networkPassword") { 
                                    type = NavType.StringType
                                    nullable = true 
                                },
                                navArgument("provisionToken") { type = NavType.StringType },
                                navArgument("isEnterprise") { type = NavType.BoolType },
                                navArgument("identity") { 
                                    type = NavType.StringType
                                    nullable = true 
                                },
                                navArgument("userPassword") { 
                                    type = NavType.StringType
                                    nullable = true 
                                }
                            )
                        ) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
                            val ssid = java.net.URLDecoder.decode(
                                backStackEntry.arguments?.getString("ssid") ?: "", 
                                "UTF-8"
                            )
                            val networkPassword = java.net.URLDecoder.decode(
                                backStackEntry.arguments?.getString("networkPassword") ?: "",
                                "UTF-8"
                            )
                            val provisionToken = backStackEntry.arguments?.getString("provisionToken") ?: ""
                            val isEnterprise = backStackEntry.arguments?.getBoolean("isEnterprise") ?: false
                            val identity = backStackEntry.arguments?.getString("identity")?.let {
                                java.net.URLDecoder.decode(it, "UTF-8")
                            }
                            val userPassword = backStackEntry.arguments?.getString("userPassword")?.let {
                                java.net.URLDecoder.decode(it, "UTF-8")
                            }
                            
                            ProvisionScreen(
                                navController = navController,
                                ssid = ssid,
                                passphrase = networkPassword,
                                plantId = plantId,
                                provisionToken = provisionToken,
                                isEnterprise = isEnterprise,
                                identity = identity,
                                enterprisePassword = userPassword
                            )
                        }
                        composable("login") {
                            LoginScreen(navController)
                        }
                        composable("signup") {
                            SignUpScreen(navController)
                        }
                        composable("plantList") { 
                            PlantListScreen(navController = navController)
                        }
                        composable("plantDetail/{plantId}", arguments = listOf(navArgument("plantId") { type = NavType.IntType })) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getInt("plantId")
                            plantId?.let { PlantDetailScreen(navController, it) }
                        }
                        composable("editProfile/{userId}", 
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId")
                            userId?.let { EditProfileScreen(navController) }
                        }
                        composable(
                            "createPlant/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            CreatePlantScreen(navController = navController, userId = userId)
                        }
                        composable("editPlant/{plantId}") { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull() ?: return@composable
                            EditPlantScreen(navController = navController, plantId = plantId)
                        }
                        composable(
                            "directSensor/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getInt("plantId") ?: return@composable
                            DirectSensorScreen(
                                navController = navController,
                                plantId = plantId,
                                bluetoothAdapter = bluetoothAdapter,
                                requestPermissionsLauncher = requestPermissionsLauncher
                            )
                        }
                        composable("settings") {
                            SettingsScreen(navController)
                        }
                        composable(
                            "plantSettings/{plantId}",
                            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getInt("plantId") ?: return@composable
                            PlantSettingsScreen(
                                navController = navController,
                                plantId = plantId
                            )
                        }
                    }
                }
            }
        }

        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        sharedPreferences = getSharedPreferences(AppConstants.ESP_PREFERENCES, Context.MODE_PRIVATE)
        provisionManager = ESPProvisionManager.getInstance(applicationContext)

        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                updateBluetoothState("Bluetooth Enabled")
            } else {
                updateBluetoothState("Bluetooth not enabled")
            }
        }

        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                initializeBluetooth()
            } else {
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            getAndSendToken()
        }
    }

    private fun updateBluetoothState(state: String) {
        runOnUiThread {
            Toast.makeText(this, state, Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, state)
    }

    private fun initializeBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            updateBluetoothState("Bluetooth Enabled")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    private fun getAndSendToken() {
        lifecycleScope.launch {
            fcmTokenManager.getToken()?.let { token ->
                val success = fcmTokenManager.registerTokenWithBackend(token)
                if (success) {
                    println("FCM Token registered successfully: $token")
                } else {
                    println("Failed to register FCM token")
                }
            }
        }
    }
}
