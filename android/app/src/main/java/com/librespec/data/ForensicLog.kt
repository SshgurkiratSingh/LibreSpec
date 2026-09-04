package com.librespec.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forensic_logs")
data class ForensicLog(
    @PrimaryKey val testId: String,
    val timestamp: Long,
    val predictedClass: String,
    val confidenceScore: Float,
    val baselineVector: String, // Stored as comma-separated
    val plateauVector: String, // Stored as comma-separated
    val appGeneratedHash: String,
    val latitude: Double,
    val longitude: Double,
    val syncStatus: Int, // 0 for pending, 1 for synced
    val apiResponse: String? = null // Response status/body from AWS API Gateway
)
