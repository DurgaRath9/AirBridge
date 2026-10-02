package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_logs")
data class TransferLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val bytesTransferred: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val direction: String, // "BACKUP_TO_LAPTOP" or "RESTORE_FROM_LAPTOP"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "COMPLETED", "FAILED", "PAUSED", "CANCELLED"
    val sha256Checksum: String = "",
    val errorMessage: String? = null
)
