package com.bluetech.vidown.data.db.entities

data class DownloadData(
    val downloadUrl: String,
    val downloadStatus : DownloadStatus,
    val sizeInBytes : Long,
    val downloadSizeInBytes : Long,
)
