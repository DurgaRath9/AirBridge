package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_records")
data class BackupRecordEntity(
    @PrimaryKey
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val lastModified: Long,
    val sha256: String,
    val syncedAt: Long,
    val targetDevice: String = "Laptop",
    val status: String = "SYNCED"
)
