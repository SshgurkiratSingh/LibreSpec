package com.librespec.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.librespec.db.AppDatabase
import com.librespec.math.MathEngine
import com.librespec.proto.BiochemSchema.SpectralTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

data class MatchResult(
    val materialName: String,
    val confidenceScore: Float,
    val phase: String
)

class MatcherService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private lateinit var database: AppDatabase
    
    // The rolling window of 50 samples (5 seconds @ 10Hz)
    private val rollingWindow = mutableListOf<List<Float>>()

    companion object {
        private val _matchResultFlow = MutableStateFlow<MatchResult?>(null)
        val matchResultFlow: StateFlow<MatchResult?> = _matchResultFlow.asStateFlow()

        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "MatcherServiceChannel"
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LibreSpec Matching Engine")
            .setContentText("Actively matching kinetics in background...")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    // Called whenever a new 10Hz Protobuf frame arrives from BleRepository
    fun ingestTelemetry(telemetry: SpectralTelemetry) {
        serviceScope.launch {
            val rawChannels = telemetry.spectralChannelsList.map { it.toFloat() }
            
            // 1. DSP Filtering
            val smoothed = MathEngine.smoothSignal(rawChannels)
            
            rollingWindow.add(smoothed)
            if (rollingWindow.size > 50) {
                rollingWindow.removeAt(0)
            }
            
            if (rollingWindow.size == 50) {
                runMatchingPipeline(telemetry.ambientTemperatureC)
            }
        }
    }

    private suspend fun runMatchingPipeline(currentTempC: Float) {
        val materials = database.materialDao().getAllMaterials()
        var bestMatch: String = "Unknown"
        var minDistance = Float.MAX_VALUE
        
        for (material in materials) {
            val runs = database.materialDao().getKineticRunsForMaterial(material.id)
            for (run in runs) {
                // Parse JSON array string to List<List<Float>>
                val refCurve = parseJsonCurve(run.kineticCurveData)
                
                // 2. Arrhenius Temperature Compensation
                val compensatedCurve = MathEngine.compensateTemperature(
                    refCurve, 
                    run.recordedTemperatureC, 
                    currentTempC
                )
                
                // 3. DDTW Calculation
                val distance = MathEngine.calculateDDTWDistance(rollingWindow, compensatedCurve)
                if (distance < minDistance) {
                    minDistance = distance
                    bestMatch = material.name
                }
            }
        }
        
        // Convert distance to a rough confidence score (0-100%)
        val confidence = maxOf(0f, 100f - minDistance)
        
        _matchResultFlow.value = MatchResult(
            materialName = bestMatch,
            confidenceScore = confidence,
            phase = if (confidence > 95f) "Saturation" else "Activation"
        )
        
        // 4. Closed-Loop Feedback
        if (confidence > 95f) {
            Log.i("MatcherService", "High confidence match found! Triggering ESP32 feedback.")
            // Trigger feedback via BleRepository (to be injected/singleton)
        }
    }

    private fun parseJsonCurve(jsonString: String): List<List<Float>> {
        val list = mutableListOf<List<Float>>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val frameArray = jsonArray.getJSONArray(i)
                val frame = mutableListOf<Float>()
                for (j in 0 until frameArray.length()) {
                    frame.add(frameArray.getDouble(j).toFloat())
                }
                list.add(frame)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Matching Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
