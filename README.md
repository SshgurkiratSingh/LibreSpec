# LibreSpec

LibreSpec is a full-stack, open-source biochemical IoT spectrometer ecosystem. It consists of highly optimized ESP32 firmware and a modern Android companion app designed to capture, stream, and visualize real-time 14-channel spectral kinetics data using Bluetooth Low Energy (BLE).

## Key Features

* **High-Fidelity Spectral Telemetry**: Interfaces with the AMS AS7343 14-channel multi-spectral sensor to acquire rich color and spectral data.
* **Real-Time 10Hz BLE Streaming**: Utilizes a robust GATT server to broadcast continuous, low-latency telemetry to mobile clients.
* **Efficient Protobuf Serialization**: Built on Nanopb to ensure cross-platform data integrity with zero-allocation, lightweight message packing.
* **On-Device Hardware UI**: Features an SSD1306 OLED integration for instant on-device visualization of all 14 spectral channels.
* **Modern Android Dashboard**: Built with Jetpack Compose and Vico charts, featuring dynamic UI modes for live Spectrum visualization (405nm-855nm), total intensity Kinetics M(t), and real-time color prediction.
* **Dual Trigger System**: Data acquisition can be toggled via a physical push-button interrupt on the hardware or a BLE `START_ACQUISITION` intent from the app.

## Project Structure

```text
LibreSpec/
├── android/          # Android companion app (Kotlin, Jetpack Compose, BLE Client)
├── firmware/         # ESP32 IoT Firmware (C++, PlatformIO, Arduino framework, FreeRTOS)
└── proto/            # Protobuf schema defining the cross-platform telemetry contract
```

## Hardware Architecture

* **Microcontroller**: ESP32 DevKit (240MHz, FreeRTOS)
* **Spectral Sensor**: AS7343 (I2C Address `0x59`)
* **Display**: 128x64 SSD1306 OLED (I2C Address `0x3C`)
* **Indicators**: NeoPixel LED Strip (WS2812B)
* **Control**: Hardware push-button for DAQ triggering

## Building & Deployment

### 1. Firmware (ESP32)
The firmware is configured as a PlatformIO project. 
* Open the `firmware/` folder in VS Code with the PlatformIO extension.
* Ensure you have the required dependencies (`Nanopb`, `Adafruit SSD1306`, `Adafruit AS7341`, etc.) which are managed automatically via `platformio.ini`.
* Build and flash to your ESP32 target.

### 2. Android App
The mobile app is a standard Gradle-based Android project.
* Open the `android/` folder in Android Studio.
* Ensure you have the Android SDK (API 34) installed.
* Run the app on a physical Android device (BLE features do not work on the emulator). The app handles all runtime BLE scanning and location permissions automatically.

### 3. Protocol Buffers (Optional)
If you modify `biochem_schema.proto` in the `proto/` directory, you must re-run the Nanopb generator to update the `.pb.h`/`.pb.c` files in the firmware and the Java generated classes in the Android app to ensure both sides remain perfectly synced.

## Contributing
Contributions are welcome! Whether it's expanding the Android UI, adding calibration models for the spectrometer, or optimizing the RTOS task scheduling, feel free to open a pull request.
