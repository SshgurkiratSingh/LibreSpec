package com.librespec.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MaterialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKineticRun(run: KineticRunEntity)

    @Query("SELECT * FROM materials")
    suspend fun getAllMaterials(): List<MaterialEntity>

    @Query("SELECT * FROM kinetic_runs WHERE materialId = :materialId")
    suspend fun getKineticRunsForMaterial(materialId: Int): List<KineticRunEntity>
}
