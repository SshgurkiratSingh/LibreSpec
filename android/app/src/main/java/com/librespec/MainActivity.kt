package com.librespec

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.librespec.ble.BleRepository
import com.librespec.ui.GraphScreen
import com.librespec.ui.HistoryScreen
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var bleRepository: BleRepository
    private val handler = Handler(Looper.getMainLooper())

    // Handle Bluetooth permissions request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            scanAndConnect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        bleRepository = BleRepository(this)

        setContent {
            MaterialTheme {
                var currentRoute by remember { mutableStateOf("graph") }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute == "graph",
                                onClick = { currentRoute = "graph" },
                                icon = { Icon(Icons.Default.Info, contentDescription = "Graph") },
                                label = { Text("Graph") }
                            )
                            NavigationBarItem(
                                selected = currentRoute == "history",
                                onClick = { currentRoute = "history" },
                                icon = { Icon(Icons.Default.List, contentDescription = "History") },
                                label = { Text("History") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (currentRoute == "graph") {
                            GraphScreen(bleRepository = bleRepository)
                        } else {
                            HistoryScreen()
                        }
                    }
                }
            }
        }

        checkPermissionsAndConnect()
    }

    private fun checkPermissionsAndConnect() {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            scanAndConnect()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private var isScanning = false

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord
            val serviceUuids = scanRecord?.serviceUuids
            val deviceName = device.name ?: scanRecord?.deviceName ?: ""

            Log.d("MainActivity", "Scanned BLE device: ${device.address} ($deviceName)")

            val targetUuid = ParcelUuid(UUID.fromString("000000FF-0000-1000-8000-00805F9B34FB"))
            val isTargetDevice = serviceUuids?.contains(targetUuid) == true ||
                    deviceName.contains("LibreSpec", ignoreCase = true) ||
                    deviceName.contains("AS7343", ignoreCase = true) ||
                    deviceName.contains("ESP32", ignoreCase = true)

            if (isTargetDevice && isScanning) {
                stopBleScan()
                Log.i("MainActivity", "Found target BLE device (${device.address}), connecting...")
                bleRepository.connect(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("MainActivity", "BLE Scan failed with error code: $errorCode")
            isScanning = false
            bleRepository.startSimulatedTelemetryIfDisconnected()
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        if (!isScanning) return
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
        handler.removeCallbacksAndMessages(null)
    }

    @SuppressLint("MissingPermission")
    private fun scanAndConnect() {
        if (isScanning) return
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        val bleScanner = bluetoothAdapter?.bluetoothLeScanner

        if (bluetoothAdapter?.isEnabled == true && bleScanner != null) {
            isScanning = true
            Log.i("MainActivity", "Starting broad BLE Scan...")
            
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
                
            bleScanner.startScan(null, settings, scanCallback)

            // Stop scan after 5 seconds and start fallback continuous telemetry if no device found
            handler.postDelayed({
                if (isScanning) {
                    stopBleScan()
                    Log.i("MainActivity", "Scan window finished. Initializing 10Hz telemetry stream.")
                    bleRepository.startSimulatedTelemetryIfDisconnected()
                }
            }, 5000)
        } else {
            Log.w("MainActivity", "Bluetooth disabled or scanner unavailable. Starting telemetry stream.")
            bleRepository.startSimulatedTelemetryIfDisconnected()
        }
    }
}
