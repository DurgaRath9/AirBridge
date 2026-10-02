package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupRecordDao {
    @Query("SELECT * FROM backup_records ORDER BY syncedAt DESC")
    fun getAllRecords(): Flow<List<BackupRecordEntity>>

    @Query("SELECT * FROM backup_records WHERE filePath = :filePath LIMIT 1")
    suspend fun getRecordByPath(filePath: String): BackupRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: BackupRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BackupRecordEntity>)

    @Query("DELETE FROM backup_records WHERE filePath = :filePath")
    suspend fun deleteByPath(filePath: String)

    @Query("DELETE FROM backup_records")
    suspend fun clearAll()
}
