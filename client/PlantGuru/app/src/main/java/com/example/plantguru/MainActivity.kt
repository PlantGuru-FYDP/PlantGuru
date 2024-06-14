package com.example.plantguru

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
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.espressif.provisioning.ESPProvisionManager
import com.example.plantguru.activities.EspMainActivity
import com.example.plantguru.BuildConfig
import android.provider.Settings
import android.os.Build
import com.espressif.provisioning.ESPConstants
import com.example.plantguru.activities.BLEProvisionLanding
import com.example.plantguru.constants.AppConstants
import com.example.plantguru.models.Plant

//import com.example.plantguru.activities.PlantDetailActivity
import com.example.plantguru.utils.SharedPreferencesHelper

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothGatt: BluetoothGatt? = null

    private lateinit var plants: List<Plant>
    private lateinit var plantsAdapter: ArrayAdapter<String>

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var provisionManager: ESPProvisionManager
    private lateinit var sharedPreferences: SharedPreferences

    private val REQUEST_LOCATION = 1
    private val REQUEST_ENABLE_BT = 2

    private val detectedDevices = mutableListOf<String>()

    companion object {
        const val TAG = "MainActivity"
        const val DEVICE_NAME = "guru32"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //SharedPreferencesHelper.clearAllPlants(this)
        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        val plantsList: ListView = findViewById(R.id.plantsList)
        val connectButton: Button = findViewById(R.id.connectButton)
        val scanButton: Button = findViewById(R.id.scanButton)
        val provisionButton: Button = findViewById(R.id.provisionButton)

        plants = SharedPreferencesHelper.getPlants(this).toMutableList()
        Log.d(TAG, "num of plants: "+ plants.size.toString())
        plantsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        plantsList.adapter = plantsAdapter
        updatePlantList()

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

        scanButton.setOnClickListener {
            if (checkPermissions()) {
                startScanning()
            }
        }

        connectButton.setOnClickListener {
            //val intent = Intent(this, ConnectActivity::class.java)
            //startActivity(intent)
            SharedPreferencesHelper.clearAllPlants(this)
        }


        provisionButton.setOnClickListener{
            if (!isLocationEnabled()) {
                askForLocation()
                return@setOnClickListener
            }
            provisionManager.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE, ESPConstants.SecurityType.SECURITY_0)
            val intent = Intent(this@MainActivity, BLEProvisionLanding::class.java)
            intent.putExtra(AppConstants.KEY_SECURITY_TYPE, AppConstants.SEC_TYPE_1);
            startActivity(intent)
        }

        if (checkPermissions()) {
            initializeBluetooth()
        }
    }

    private fun askForLocation() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setMessage(R.string.dialog_msg_gps)

        // Set up the buttons
        builder.setPositiveButton(R.string.btn_ok) { dialog, which ->
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_LOCATION)
        }

        builder.setNegativeButton(R.string.btn_cancel) { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun isLocationEnabled(): Boolean {
        val lm = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            false
        }

        val networkEnabled = try {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            false
        }

        Log.d(TAG, "GPS Enabled : $gpsEnabled , Network Enabled : $networkEnabled")

        return gpsEnabled || networkEnabled
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

    override fun onResume() {
        super.onResume()
        plants = SharedPreferencesHelper.getPlants(this).toMutableList()
        updatePlantList()
    }

    private fun initializeBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            updateBluetoothState("Bluetooth Enabled")
        }
    }

    private fun startScanning() {
        updateBluetoothState("Scanning for devices...")
        val scanFilter = ScanFilter.Builder()
            .setDeviceName(DEVICE_NAME)
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothAdapter.bluetoothLeScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result?.device?.let {
                updateBluetoothState("Device found: ${it.name ?: it.address}")
                detectedDevices.add(it.address)
                updatePlantList()
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach {
                detectedDevices.add(it.device.address)
                updatePlantList()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            updateBluetoothState("Scan failed with error: $errorCode")
            Log.e(TAG, "Scan failed with error: $errorCode")
        }
    }

    private fun updatePlantList() {
        val plantNamesWithStatus = plants.map {
            val status = if (detectedDevices.contains(it.deviceUUID)) "Detected" else "Not Detected"
            "${it.deviceUUID} - $status"
        }
        Log.d(TAG, plantNamesWithStatus.toString())

        plantsAdapter.clear()
        plantsAdapter.addAll(plantNamesWithStatus)
        plantsAdapter.notifyDataSetChanged()
        Log.d(TAG, "updated plant list")
    }

    private fun updateBluetoothState(state: String) {
        runOnUiThread {
            Toast.makeText(this, state, Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, state)
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}

