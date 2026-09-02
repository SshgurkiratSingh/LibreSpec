package com.librespec

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.librespec.ble.BleRepository
import com.librespec.ui.GraphScreen

class MainActivity : ComponentActivity() {

    private lateinit var bleRepository: BleRepository

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GraphScreen(bleRepository = bleRepository)
                }
            }
        }

        checkPermissionsAndConnect()
    }

    private fun checkPermissionsAndConnect() {
        val requiredPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
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

    private val scanCallback = object : android.bluetooth.le.ScanCallback() {
        @android.annotation.SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            val device = result.device
            // We use a ScanFilter for the Service UUID, so any device found here is the right one.
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothManager.adapter?.bluetoothLeScanner?.stopScan(this)
            if (isScanning) {
                isScanning = false
                android.util.Log.i("MainActivity", "Found device via Service UUID, connecting to ${device.address}...")
                bleRepository.connect(device)
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun scanAndConnect() {
        if (isScanning) return
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        val bleScanner = bluetoothAdapter?.bluetoothLeScanner

        if (bleScanner != null) {
            isScanning = true
            android.util.Log.i("MainActivity", "Starting BLE Scan with Service UUID filter...")
            
            val filter = android.bluetooth.le.ScanFilter.Builder()
                .setServiceUuid(android.os.ParcelUuid(java.util.UUID.fromString("000000FF-0000-1000-8000-00805F9B34FB")))
                .build()
                
            val settings = android.bluetooth.le.ScanSettings.Builder()
                .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
                
            bleScanner.startScan(listOf(filter), settings, scanCallback)
        }
    }
}
