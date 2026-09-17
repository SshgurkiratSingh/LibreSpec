# LibreSpec

LibreSpec is a professional, high-frequency kinetic tracking open-source hardware system. It is designed to capture 14-channel visible/NIR spectra via the AS7343 Smart Multiplexer at $10 \text{ Hz}$ to map chemical reaction kinetics in real-time.

## Architecture & Features

### Firmware (ESP-IDF FreeRTOS)
- **Preemptive RTOS:** Core 0 handles the BLE stack while Core 1 runs the $10\text{Hz}$ AS7343 DAQ task.
- **Two-Phase SMUX I2C Write:** Securely cycles through the F1-F6 and F7-Flicker registers sequentially into static Nanopb arrays to maximize memory safety and speed.
- **RMT WS2812B Driver:** The WS2812B LEDs are precisely pulsed using a $10 \text{ MHz}$ base clock via the ESP32 RMT peripheral.

### Android Application (Kotlin)
- **Jetpack Compose UI:** The Vico charting library draws real-time kinetic visualizations of the 14 spectral channels.
- **Protobuf Data Routing:** Streams `SpectralTelemetry` across the BLE GATT layer natively decoded with `protobuf-java-lite`.

### Advanced Math Engine (Material Matching)
LibreSpec features a complex local matching engine designed for research-grade applications:
- **Derivative Dynamic Time Warping (DDTW):** Allows the Android app to identify materials by mathematically aligning live kinetic reaction curves against stored reference datasets.
- **Digital Signal Processing (DSP):** Utilizes Kalman and Savitzky-Golay filters natively inside Android Coroutines to eliminate optical noise before matching.
- **Arrhenius Temperature Compensation:** The ESP32 tracks the ambient temperature (`ambient_temperature_c` in Protobuf). The math engine applies the Arrhenius equation to automatically shift reference kinetic curves, compensating for environmental heat changes dynamically.

## Quick Start
1. Build the ESP32 firmware using PlatformIO (`pio run -t upload`).
2. Build the Android `.apk` via Gradle.
3. Upload your JSON datasets to build the matching library!
