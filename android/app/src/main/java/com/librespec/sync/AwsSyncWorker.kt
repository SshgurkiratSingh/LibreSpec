package com.librespec.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.librespec.data.AppDatabase
import com.librespec.util.CryptoUtils
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AwsSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val TAG = "AwsSyncWorker"

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.forensicLogDao()
        
        val pendingLogs = dao.getPendingLogs()
        if (pendingLogs.isEmpty()) {
            return Result.success()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://szcon5jvb0.execute-api.us-east-1.amazonaws.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val apiService = retrofit.create(AwsApiService::class.java)

        var allSynced = true

        for (log in pendingLogs) {
            val baselineVector = log.baselineVector.split(",").mapNotNull { it.trim().toIntOrNull() }
            val plateauVector = log.plateauVector.split(",").mapNotNull { it.trim().toIntOrNull() }

            val baselineHmac = CryptoUtils.calculateHmac(baselineVector, log.timestamp)
            val plateauHmac = CryptoUtils.calculateHmac(plateauVector, log.timestamp)

            val payload = SyncPayload(
                testId = log.testId,
                timestamp = log.timestamp,
                predictedClass = log.predictedClass,
                confidenceScore = log.confidenceScore,
                baselineVector = baselineVector,
                plateauVector = plateauVector,
                baselineHmac = baselineHmac,
                plateauHmac = plateauHmac,
                latitude = log.latitude,
                longitude = log.longitude
            )

            try {
                val response = apiService.syncLog(payload)
                val responseBodyStr = if (response.isSuccessful || response.code() == 201) {
                    val statusText = response.body()?.status ?: "Forensic log securely ingested."
                    "HTTP ${response.code()}: $statusText"
                } else {
                    val errText = response.errorBody()?.string() ?: response.message()
                    "HTTP ${response.code()}: $errText"
                }

                if (response.isSuccessful || response.code() == 201) {
                    val updatedLog = log.copy(syncStatus = 1, apiResponse = responseBodyStr)
                    dao.update(updatedLog)
                    Log.i(TAG, "Successfully synced log: ${log.testId}, response: $responseBodyStr")
                } else {
                    val updatedLog = log.copy(syncStatus = 0, apiResponse = responseBodyStr)
                    dao.update(updatedLog)
                    Log.e(TAG, "Failed to sync log: ${log.testId}, response: $responseBodyStr")
                    allSynced = false
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage ?: e.message}"
                val updatedLog = log.copy(syncStatus = 0, apiResponse = errorMsg)
                dao.update(updatedLog)
                Log.e(TAG, "Error syncing log: ${log.testId}", e)
                allSynced = false
            }
        }

        return if (allSynced) Result.success() else Result.retry()
    }
}
