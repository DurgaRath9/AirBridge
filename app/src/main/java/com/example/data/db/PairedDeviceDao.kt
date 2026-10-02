package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PairedDeviceDao {
    @Query("SELECT * FROM paired_devices ORDER BY lastActiveTimestamp DESC")
    fun getAllDevices(): Flow<List<PairedDeviceEntity>>

    @Query("SELECT * FROM paired_devices WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getDeviceById(deviceId: String): PairedDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(device: PairedDeviceEntity)

    @Query("UPDATE paired_devices SET lastActiveTimestamp = :timestamp WHERE deviceId = :deviceId")
    suspend fun updateLastActive(deviceId: String, timestamp: Long)

    @Query("UPDATE paired_devices SET isTrusted = :isTrusted WHERE deviceId = :deviceId")
    suspend fun setDeviceTrust(deviceId: String, isTrusted: Boolean)

    @Query("DELETE FROM paired_devices WHERE deviceId = :deviceId")
    suspend fun deleteDevice(deviceId: String)

    @Query("DELETE FROM paired_devices")
    suspend fun clearAll()
}
