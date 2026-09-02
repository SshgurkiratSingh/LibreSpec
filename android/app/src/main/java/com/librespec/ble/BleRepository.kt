package com.librespec.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import com.librespec.proto.BiochemSchema.SpectralTelemetry
import com.librespec.proto.BiochemSchema.DeviceIntent

@SuppressLint("MissingPermission")
class BleRepository(private val context: Context) {

    private val TAG = "BleRepository"

    // 128-bit UUIDs matching the ESP32 firmware
    private val SERVICE_UUID = UUID.fromString("000000FF-0000-1000-8000-00805F9B34FB") // 0x00FF base
    private val RX_CHAR_UUID = UUID.fromString("0000FF01-0000-1000-8000-00805F9B34FB")
    private val TX_CHAR_UUID = UUID.fromString("0000FF02-0000-1000-8000-00805F9B34FB")
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var bluetoothGatt: BluetoothGatt? = null

    // MutableStateFlow for the UI thread to collect
    private val _telemetryFlow = MutableStateFlow<SpectralTelemetry?>(null)
    val telemetryFlow: StateFlow<SpectralTelemetry?> = _telemetryFlow.asStateFlow()

    private val _isAcquiring = MutableStateFlow(false)
    val isAcquiring: StateFlow<Boolean> = _isAcquiring.asStateFlow()

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server. Requesting MTU 517.")
                bluetoothGatt = gatt
                // 5. Connection Phase: requestMtu(517)
                gatt.requestMtu(517)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.")
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "MTU changed to $mtu. Discovering services.")
                // 5. Upon onMtuChanged() success, discoverServices() is called.
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered. Subscribing to TX characteristic.")
                subscribeToTelemetry(gatt)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            handleCharacteristicChanged(characteristic, characteristic.value)
        }

        // Android 13+ (API 33+) compatible callback
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleCharacteristicChanged(characteristic, value)
        }

        private fun handleCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray?) {
            if (characteristic.uuid == TX_CHAR_UUID && value != null) {
                try {
                    val telemetry = SpectralTelemetry.parseFrom(value)
                    _telemetryFlow.value = telemetry
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse telemetry protobuf", e)
                }
            }
        }
    }

    fun connect(device: BluetoothDevice) {
        device.connectGatt(context, false, gattCallback)
    }

    private fun subscribeToTelemetry(gatt: BluetoothGatt) {
        val service = gatt.getService(SERVICE_UUID)
        val txChar = service?.getCharacteristic(TX_CHAR_UUID)
        
        if (txChar != null) {
            // Enable notifications locally
            gatt.setCharacteristicNotification(txChar, true)
            
            // 5. Subscription & Intent: Write 0x01 to CCCD
            val cccd = txChar.getDescriptor(CCCD_UUID)
            if (cccd != null) {
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(cccd)
            }
        }
    }

    fun sendStartAcquisitionIntent() {
        sendDeviceIntent("START_ACQUISITION")
        _isAcquiring.value = true
    }

    fun sendStopAcquisitionIntent() {
        sendDeviceIntent("STOP_ACQUISITION")
        _isAcquiring.value = false
    }

    private fun sendDeviceIntent(operation: String) {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(SERVICE_UUID)
        val rxChar = service?.getCharacteristic(RX_CHAR_UUID)

        if (rxChar != null) {
            val intent = DeviceIntent.newBuilder()
                .setRequestId(System.currentTimeMillis().toInt())
                .setTargetOperation(operation)
                .build()
            
            rxChar.value = intent.toByteArray()
            rxChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.writeCharacteristic(rxChar)
            Log.i(TAG, "Sent $operation intent.")
        }
    }
}
