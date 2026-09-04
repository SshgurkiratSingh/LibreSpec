package com.librespec.ble;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"J\u0010\u0010#\u001a\u00020 2\u0006\u0010$\u001a\u00020\u000bH\u0002J\u0006\u0010%\u001a\u00020 J\u0006\u0010&\u001a\u00020 J\u0010\u0010'\u001a\u00020 2\u0006\u0010(\u001a\u00020\u0016H\u0002R\u0016\u0010\u0005\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\b\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\t\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n \u0007*\u0004\u0018\u00010\u00060\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0012\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001bR\u0016\u0010\u001c\u001a\n\u0012\u0004\u0012\u00020\u0014\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001b\u00a8\u0006)"}, d2 = {"Lcom/librespec/ble/BleRepository;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "CCCD_UUID", "Ljava/util/UUID;", "kotlin.jvm.PlatformType", "RX_CHAR_UUID", "SERVICE_UUID", "TAG", "", "TX_CHAR_UUID", "_isAcquiring", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_telemetryFlow", "Lcom/librespec/proto/BiochemSchema$SpectralTelemetry;", "baselineVector", "", "", "bluetoothGatt", "Landroid/bluetooth/BluetoothGatt;", "gattCallback", "Landroid/bluetooth/BluetoothGattCallback;", "isAcquiring", "Lkotlinx/coroutines/flow/StateFlow;", "()Lkotlinx/coroutines/flow/StateFlow;", "plateauVector", "telemetryFlow", "getTelemetryFlow", "connect", "", "device", "Landroid/bluetooth/BluetoothDevice;", "sendDeviceIntent", "operation", "sendStartAcquisitionIntent", "sendStopAcquisitionIntent", "subscribeToTelemetry", "gatt", "app_debug"})
@android.annotation.SuppressLint(value = {"MissingPermission"})
public final class BleRepository {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String TAG = "BleRepository";
    private final java.util.UUID SERVICE_UUID = null;
    private final java.util.UUID RX_CHAR_UUID = null;
    private final java.util.UUID TX_CHAR_UUID = null;
    private final java.util.UUID CCCD_UUID = null;
    @org.jetbrains.annotations.Nullable()
    private android.bluetooth.BluetoothGatt bluetoothGatt;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.librespec.proto.BiochemSchema.SpectralTelemetry> _telemetryFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.librespec.proto.BiochemSchema.SpectralTelemetry> telemetryFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isAcquiring = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isAcquiring = null;
    @org.jetbrains.annotations.NotNull()
    private final android.bluetooth.BluetoothGattCallback gattCallback = null;
    @org.jetbrains.annotations.Nullable()
    private java.util.List<java.lang.Integer> baselineVector;
    @org.jetbrains.annotations.Nullable()
    private java.util.List<java.lang.Integer> plateauVector;
    
    public BleRepository(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.librespec.proto.BiochemSchema.SpectralTelemetry> getTelemetryFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isAcquiring() {
        return null;
    }
    
    public final void connect(@org.jetbrains.annotations.NotNull()
    android.bluetooth.BluetoothDevice device) {
    }
    
    private final void subscribeToTelemetry(android.bluetooth.BluetoothGatt gatt) {
    }
    
    private final void sendDeviceIntent(java.lang.String operation) {
    }
    
    public final void sendStartAcquisitionIntent() {
    }
    
    public final void sendStopAcquisitionIntent() {
    }
}