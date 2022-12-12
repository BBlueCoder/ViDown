package com.bluetech.vidown.core.pojoclasses

data class DownloadMediaProgress(
    val fileSize : Long?,
    val downloadedSize : Long,
    var progress : Int?
)
