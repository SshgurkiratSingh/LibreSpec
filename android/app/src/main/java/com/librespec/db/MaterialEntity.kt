package com.librespec.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val description: String,
    // Comma-separated list or JSON array of 14 floats representing the baseline static spectral signature
    val baseSignatureData: String 
)
