package com.example.plantgurucompose

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPProvisionManager
import com.example.plantgurucompose.constants.AppConstants
import com.example.plantgurucompose.screens.BLEProvisionLandingScreen
import com.example.plantgurucompose.screens.ConnectionHomeScreen
import com.example.plantgurucompose.screens.ProvisionScreen
import com.example.plantgurucompose.utils.SharedPreferencesHelper
import com.example.plantgurucompose.screens.WiFiScanScreen
import com.example.plantgurucompose.screens.LoginScreen
import com.example.plantgurucompose.screens.PlantDetailScreen
import com.example.plantgurucompose.screens.PlantListScreen
import com.example.plantgurucompose.screens.SignUpScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var provisionManager: ESPProvisionManager
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {
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
                composable("ble_provision_landing") {
                    BLEProvisionLandingScreen(
                        navController = navController,
                    )
                }
                composable("wifi_scan") {
                    WiFiScanScreen(navController = navController)
                }
                composable("provision") {
                    ProvisionScreen(
                        navController = navController,
                        ssid = "Data City 263 - 2.4GHz",
                        passphrase = "ECE358PROBLEM"
                    )
                }
                composable("login") {
                    LoginScreen(navController)
                }
                composable("signup") {
                    SignUpScreen(navController)
                }
                composable("plantList/{userId}", arguments = listOf(navArgument("userId") { type = NavType.IntType })) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getInt("userId")
                    userId?.let { PlantListScreen(navController, it) }
                }
                composable("plantDetail/{plantId}", arguments = listOf(navArgument("plantId") { type = NavType.IntType })) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getInt("plantId")
                    plantId?.let { PlantDetailScreen(navController, it) }
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
                Toast.makeText(this, "Permissions required to use Bluetooth", Toast.LENGTH_LONG).show()
            }
        }

        if (checkPermissions()) {
            initializeBluetooth()
        }
    }

    private fun updateBluetoothState(state: String) {
        runOnUiThread {
            Toast.makeText(this, state, Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, state)
    }

    private fun checkPermissions(): Boolean {
        val permissionsNeeded = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsToRequest = permissionsNeeded.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (permissionsToRequest.isNotEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            false
        } else {
            true
        }
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
}
