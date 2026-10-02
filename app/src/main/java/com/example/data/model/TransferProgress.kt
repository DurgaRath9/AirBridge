package com.example.data.model

enum class TransferDirection {
    BACKUP_TO_LAPTOP,
    RESTORE_FROM_LAPTOP
}

data class TransferProgress(
    val isTransferring: Boolean = false,
    val isPaused: Boolean = false,
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val currentFileBytesTransferred: Long = 0L,
    val currentFileTotalBytes: Long = 0L,
    val totalBytesTransferred: Long = 0L,
    val totalBytesToTransfer: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val direction: TransferDirection = TransferDirection.BACKUP_TO_LAPTOP,
    val completedFiles: List<String> = emptyList(),
    val failedFiles: List<Pair<String, String>> = emptyList(), // Pair(fileName, reason)
    val lastVerifiedSha256: String = ""
) {
    val overallProgressPercent: Float
        get() = if (totalBytesToTransfer > 0) {
            (totalBytesTransferred.toFloat() / totalBytesToTransfer).coerceIn(0f, 1f)
        } else 0f

    val formattedSpeed: String
        get() = formatBytesSpeed(speedBytesPerSec)

    val formattedEta: String
        get() = if (etaSeconds <= 0) "--" else {
            val mins = etaSeconds / 60
            val secs = etaSeconds % 60
            if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }

    companion object {
        fun formatBytesSpeed(bytesPerSec: Long): String {
            return when {
                bytesPerSec >= 1024 * 1024 * 1024 -> String.format("%.2f GB/s", bytesPerSec.toDouble() / (1024 * 1024 * 1024))
                bytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", bytesPerSec.toDouble() / (1024 * 1024))
                bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec.toDouble() / 1024)
                else -> "$bytesPerSec B/s"
            }
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", bytes.toDouble() / (1024 * 1024 * 1024))
                bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
                bytes >= 1024 -> String.format("%.1f KB", bytes.toDouble() / 1024)
                else -> "$bytes B"
            }
        }
    }
}
