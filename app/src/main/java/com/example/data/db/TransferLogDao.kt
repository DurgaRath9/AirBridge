package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferLogDao {
    @Query("SELECT * FROM transfer_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<TransferLogEntity>>

    @Query("SELECT * FROM transfer_logs WHERE status = 'FAILED' ORDER BY timestamp DESC")
    fun getFailedLogs(): Flow<List<TransferLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TransferLogEntity): Long

    @Query("UPDATE transfer_logs SET status = :status, errorMessage = :error WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, error: String? = null)

    @Query("DELETE FROM transfer_logs")
    suspend fun clearLogs()
}
