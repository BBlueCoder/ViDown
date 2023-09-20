package com.bluetech.vidown.core.db.entities

data class DownloadData(
    val downloadUrl: String,
    val downloadStatus : DownloadStatus,
    val sizeInBytes : Long,
    val downloadSizeInBytes : Long,
)
