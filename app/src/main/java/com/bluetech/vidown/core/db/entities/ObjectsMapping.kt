package com.bluetech.vidown.core.db.entities

fun DownloadHistoryItemExtras.mapToNewDownloadWithNewDownloadData(newDownloadData: DownloadData) : DownloadHistoryItemExtras{
    return DownloadHistoryItemExtras(uid,mainItemId,savedName,mediaType,newDownloadData)
}

fun DownloadHistoryWithExtras.mapToNewDownloadItemWithNewDownloadData(newDownloadData: DownloadData): DownloadHistoryEntity {
    return DownloadHistoryEntity(downloadHistoryEntity.uid,downloadHistoryEntity.type,downloadHistoryEntity.title
    ,downloadHistoryEntity.savedName,downloadHistoryEntity.originalUrl,downloadHistoryEntity.quality,newDownloadData,downloadHistoryEntity.date)
}

fun DownloadHistoryEntity.mapToMediaEntity(newName : String?,duration : Long = 0): MediaEntity {
    return MediaEntity(0,newName?: savedName,type,title,duration)
}

fun DownloadHistoryItemExtras.mapToMediaThumbnail(mediaId : Long,blurredSavedName : String?): MediaThumbnail {
    return MediaThumbnail(0,mediaId,savedName,blurredSavedName)
}

fun DownloadHistoryWithExtras.updateDownloadStatus(status: DownloadStatus): DownloadHistoryEntity {
    val downloadData = DownloadData(
        downloadHistoryEntity.downloadData.downloadUrl,
        status,
        downloadHistoryEntity.downloadData.sizeInBytes,
        downloadHistoryEntity.downloadData.downloadSizeInBytes)
    return mapToNewDownloadItemWithNewDownloadData(downloadData)
}

fun DownloadHistoryItemExtras.updateDownloadStatus(status: DownloadStatus): DownloadHistoryItemExtras {
    val downloadData = DownloadData(
        downloadData.downloadUrl,
        status,
        downloadData.sizeInBytes,
        downloadData.downloadSizeInBytes)
    return mapToNewDownloadWithNewDownloadData(downloadData)
}