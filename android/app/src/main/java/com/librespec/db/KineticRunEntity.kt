package com.librespec.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "kinetic_runs",
    foreignKeys = [
        ForeignKey(
            entity = MaterialEntity::class,
            parentColumns = ["id"],
            childColumns = ["materialId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class KineticRunEntity(
    @PrimaryKey(autoGenerate = true) val runId: Int = 0,
    val materialId: Int,
    val recordedTemperatureC: Float,
    // JSON string representing a List of Lists (time series of 14-channel data)
    val kineticCurveData: String 
)
