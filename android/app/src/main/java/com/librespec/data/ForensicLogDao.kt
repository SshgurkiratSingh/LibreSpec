package com.librespec.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ForensicLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ForensicLog)

    @Query("SELECT * FROM forensic_logs WHERE syncStatus = 0")
    suspend fun getPendingLogs(): List<ForensicLog>

    @Query("SELECT * FROM forensic_logs ORDER BY timestamp DESC")
    fun getAllLogs(): kotlinx.coroutines.flow.Flow<List<ForensicLog>>

    @Update
    suspend fun update(log: ForensicLog)
}
