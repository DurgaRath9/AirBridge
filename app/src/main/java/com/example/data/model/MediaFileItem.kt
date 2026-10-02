package com.example.data.model

enum class FileCategory(val label: String) {
    ALL("All Files"),
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    DOCUMENTS("Documents"),
    AUDIO("Audio"),
    DOWNLOADS("Downloads")
}

enum class SyncStatus {
    NOT_SYNCED,
    PENDING_INCREMENTAL,
    SYNCED,
    SYNCING,
    FAILED
}

data class MediaFileItem(
    val id: String,
    val name: String,
    val path: String,
    val uriString: String,
    val size: Long,
    val mimeType: String,
    val lastModified: Long,
    val category: FileCategory,
    val sha256: String = "",
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    val isSelected: Boolean = false
)
