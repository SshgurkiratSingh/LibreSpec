#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Adafruit_NeoPixel.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#include "pb_encode.h"
#include "pb_decode.h"
#include "biochem_schema.pb.h"

// 1. Hardware Abstraction & Peripheral Allocation
#define I2C_MASTER_SCL_IO           22
#define I2C_MASTER_SDA_IO           21
#define I2C_MASTER_FREQ_HZ          400000 // Fast Mode

#define RMT_TX_GPIO                 13
#define LED_STRIP_NUM_PIXELS        10

#define GPIO_TUBE_INSERTION         4
#define GPIO_CONTROL_BUTTON         5

// I2C Addresses
#define AS7343_ADDR                 0x59
#define SSD1306_ADDR                0x3C
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

// Global RTOS Primitives
SemaphoreHandle_t i2c_mutex = NULL;
TaskHandle_t daq_task_handle = NULL;
EventGroupHandle_t ble_event_group;
#define ACQUISITION_START_BIT       (1 << 0)
#define ACQUISITION_STOP_BIT        (1 << 1)

// BLE Definitions
#define SERVICE_UUID           "000000ff-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID_RX "0000ff01-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID_TX "0000ff02-0000-1000-8000-00805f9b34fb"

BLEServer* pServer = NULL;
BLECharacteristic* pTxCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

Adafruit_NeoPixel strip(LED_STRIP_NUM_PIXELS, RMT_TX_GPIO, NEO_GRB + NEO_KHZ800);

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    }

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();

      if (value.length() > 0) {
        DeviceIntent intent = DeviceIntent_init_zero;
        pb_istream_t stream = pb_istream_from_buffer((const pb_byte_t*)value.data(), value.length());
        
        if (pb_decode(&stream, DeviceIntent_fields, &intent)) {
            if (strcmp(intent.target_operation, "START_ACQUISITION") == 0) {
                xEventGroupSetBits(ble_event_group, ACQUISITION_START_BIT);
            } else if (strcmp(intent.target_operation, "STOP_ACQUISITION") == 0) {
                xEventGroupSetBits(ble_event_group, ACQUISITION_STOP_BIT);
            }
        }
      }
    }
};

void IRAM_ATTR gpio_isr_handler() {
    BaseType_t xHigherPriorityTaskWoken = pdFALSE;
    if (daq_task_handle != NULL) {
        vTaskNotifyGiveFromISR(daq_task_handle, &xHigherPriorityTaskWoken);
        portYIELD_FROM_ISR(xHigherPriorityTaskWoken);
    }
}

void as7343_daq_task(void *pvParameters) {
    uint32_t seq_num = 0;
    bool is_streaming = false;
    
    while (1) {
        // Check for BLE intent
        EventBits_t bits = xEventGroupWaitBits(ble_event_group, ACQUISITION_START_BIT | ACQUISITION_STOP_BIT, pdTRUE, pdFALSE, 0);
        if (bits & ACQUISITION_START_BIT) {
            is_streaming = true;
            Serial.println("BLE Intent: Starting DAQ stream.");
        }
        if (bits & ACQUISITION_STOP_BIT) {
            is_streaming = false;
            Serial.println("BLE Intent: Stopping DAQ stream.");
        }
        
        // Check for physical Push Button
        if (ulTaskNotifyTake(pdTRUE, 0) > 0) {
            is_streaming = !is_streaming; // Toggle on Button press
            Serial.println(is_streaming ? "Button Pressed: Starting DAQ stream." : "Button Pressed: Stopping DAQ stream.");
        }

        // We always acquire data for the OLED and Serial, but only stream over BLE if requested.


        if (xSemaphoreTake(i2c_mutex, portMAX_DELAY) == pdTRUE) {
            // I2C operations to read AS7343
            uint8_t smux_phase1[] = {0xAF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07}; // Dummy Phase 1 Command
            Wire.beginTransmission(AS7343_ADDR);
            Wire.write(smux_phase1, sizeof(smux_phase1));
            Wire.endTransmission();
            
            // Read ADCs
            Wire.beginTransmission(AS7343_ADDR);
            Wire.write(0x95); // Base ADC register
            Wire.endTransmission();
            Wire.requestFrom(AS7343_ADDR, 12);
            uint8_t adc_data_phase1[12];
            for(int i=0; i<12 && Wire.available(); i++) {
                adc_data_phase1[i] = Wire.read();
            }
            
            // Write SMUX Phase 2
            uint8_t smux_phase2[] = {0xAF, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F}; // Dummy Phase 2 Command
            Wire.beginTransmission(AS7343_ADDR);
            Wire.write(smux_phase2, sizeof(smux_phase2));
            Wire.endTransmission();
            
            // Read ADCs Phase 2
            Wire.beginTransmission(AS7343_ADDR);
            Wire.write(0x95);
            Wire.endTransmission();
            Wire.requestFrom(AS7343_ADDR, 12);
            uint8_t adc_data_phase2[12];
            for(int i=0; i<12 && Wire.available(); i++) {
                adc_data_phase2[i] = Wire.read();
            }
            xSemaphoreGive(i2c_mutex);
            
            SpectralTelemetry telemetry = SpectralTelemetry_init_zero;
            telemetry.timestamp_ms = millis();
            telemetry.sequence_number = seq_num++;
            telemetry.spectral_channels_count = 14;
            
            for(int i=0; i<6; i++) {
                telemetry.spectral_channels[i] = (adc_data_phase1[i*2+1] << 8) | adc_data_phase1[i*2];
                telemetry.spectral_channels[i+6] = (adc_data_phase2[i*2+1] << 8) | adc_data_phase2[i*2];
            }
            telemetry.spectral_channels[12] = 0;
            telemetry.spectral_channels[13] = 0;
            
            if (is_streaming) {
                uint8_t buffer[64];
                pb_ostream_t stream = pb_ostream_from_buffer(buffer, sizeof(buffer));
                
                if (pb_encode(&stream, SpectralTelemetry_fields, &telemetry)) {
                    if (deviceConnected && pTxCharacteristic) {
                        pTxCharacteristic->setValue((uint8_t*)buffer, stream.bytes_written);
                        pTxCharacteristic->notify();
                    }
                }
            }
            
            // Print to Serial Monitor
            Serial.printf("Sensor Readings | CH0: %d | CH1: %d | CH2: %d | CH3: %d | CH4: %d | CH5: %d\n", 
                telemetry.spectral_channels[0], telemetry.spectral_channels[1], telemetry.spectral_channels[2], 
                telemetry.spectral_channels[3], telemetry.spectral_channels[4], telemetry.spectral_channels[5]);
                
            // Display on OLED
            display.clearDisplay();
            display.setTextSize(1);
            display.setTextColor(SSD1306_WHITE);
            display.setCursor(0, 0);
            display.println("LibreSpec DAQ (14CH)");
            display.printf("0:%-4d 1:%-4d 2:%-4d\n", telemetry.spectral_channels[0], telemetry.spectral_channels[1], telemetry.spectral_channels[2]);
            display.printf("3:%-4d 4:%-4d 5:%-4d\n", telemetry.spectral_channels[3], telemetry.spectral_channels[4], telemetry.spectral_channels[5]);
            display.printf("6:%-4d 7:%-4d 8:%-4d\n", telemetry.spectral_channels[6], telemetry.spectral_channels[7], telemetry.spectral_channels[8]);
            display.printf("9:%-4d A:%-4d B:%-4d\n", telemetry.spectral_channels[9], telemetry.spectral_channels[10], telemetry.spectral_channels[11]);
            display.printf("C:%-4d D:%-4d\n", telemetry.spectral_channels[12], telemetry.spectral_channels[13]);
            display.display();

        }
        
        vTaskDelay(pdMS_TO_TICKS(100)); // 10Hz
    }
}

void ui_indicator_task(void *pvParameters) {
    uint8_t brightness = 0;
    int8_t step = 5;
    
    while (1) {
        brightness += step;
        if (brightness == 0 || brightness >= 255) {
            step = -step;
        }
        
        for (int i = 0; i < LED_STRIP_NUM_PIXELS; i++) {
            if(deviceConnected) {
                strip.setPixelColor(i, strip.Color(0, brightness, 0)); // Green when connected
            } else {
                strip.setPixelColor(i, strip.Color(0, 0, brightness)); // Blue when standby
            }
        }
        strip.show();
        vTaskDelay(pdMS_TO_TICKS(50));
    }
}

void setup() {
    Serial.begin(115200);
    
    i2c_mutex = xSemaphoreCreateMutex();
    ble_event_group = xEventGroupCreate();
    
    Wire.begin(I2C_MASTER_SDA_IO, I2C_MASTER_SCL_IO, I2C_MASTER_FREQ_HZ);
    
    // Power on AS7343
    Wire.beginTransmission(AS7343_ADDR);
    Wire.write(0x80); // ENABLE register
    Wire.write(0x01); // Power ON (PON)
    Wire.endTransmission();
    delay(10);
    Wire.beginTransmission(AS7343_ADDR);
    Wire.write(0x80);
    Wire.write(0x03); // SPEN (Spectral Enable) + PON
    Wire.endTransmission();
    delay(10);
    
    // Turn on the built-in LED (Maximum Brightness)
    Wire.beginTransmission(AS7343_ADDR);
    Wire.write(0xCD); // AS7343 LED register
    Wire.write(0xFF); // 0x80 (LED_ACT) | 0x7F (Max Current)
    Wire.endTransmission();
    delay(10);
    
    if(!display.begin(SSD1306_SWITCHCAPVCC, SSD1306_ADDR)) {
        Serial.println("SSD1306 allocation failed");
    }
    display.clearDisplay();
    display.display();
    
    strip.begin();
    strip.show();
    
    pinMode(GPIO_TUBE_INSERTION, INPUT_PULLUP);
    attachInterrupt(digitalPinToInterrupt(GPIO_TUBE_INSERTION), gpio_isr_handler, FALLING);
    
    // Setup BLE
    BLEDevice::init("LibreSpec_Sensor");
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());
    
    BLEService *pService = pServer->createService(SERVICE_UUID);
    
    pTxCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
    pTxCharacteristic->addDescriptor(new BLE2902());
    
    BLECharacteristic *pRxCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID_RX,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
    pRxCharacteristic->setCallbacks(new MyCallbacks());
    
    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);  
    pAdvertising->setMinPreferred(0x12);
    BLEDevice::startAdvertising();
    Serial.println("BLE Ready.");
    
    xTaskCreatePinnedToCore(as7343_daq_task, "DAQ_Task", 4096, NULL, 4, &daq_task_handle, 1);
    xTaskCreatePinnedToCore(ui_indicator_task, "UI_Task", 2048, NULL, 2, NULL, 1);
}

void loop() {
    if (!deviceConnected && oldDeviceConnected) {
        delay(500); // give the bluetooth stack the chance to get things ready
        pServer->startAdvertising(); // restart advertising
        Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
    }
    if (deviceConnected && !oldDeviceConnected) {
        oldDeviceConnected = deviceConnected;
    }
    vTaskDelay(pdMS_TO_TICKS(1000));
}
