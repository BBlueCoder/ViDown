package com.bluetech.vidown.data.repos.pojoclasses

data class DownloadMediaProgress(
    val fileSize : Long?,
    val downloadedSize : Long,
    var progress : Int?
)
