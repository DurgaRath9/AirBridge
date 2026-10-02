package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paired_devices")
data class PairedDeviceEntity(
    @PrimaryKey
    val deviceId: String,
    val deviceName: String,
    val ipAddress: String,
    val pairedTimestamp: Long,
    val lastActiveTimestamp: Long,
    val isTrusted: Boolean = true,
    val authPin: String = "",
    val sharedSecretKey: String = ""
)
