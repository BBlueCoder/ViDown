package com.bluetech.vidown.core.repos

import android.content.Context
import com.bluetech.vidown.core.db.dao.DownloadHistoryDao
import com.bluetech.vidown.core.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.core.db.dao.MediaDao
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.db.entities.MediaEntity
import com.bluetech.vidown.core.db.entities.MediaThumbnail
import com.bluetech.vidown.core.db.entities.MediaWithThumbnail
import com.bluetech.vidown.core.pojoclasses.DownloadItemPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import java.io.File
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DBRepo @Inject constructor(
    private var mediaDao: MediaDao,
    private var downloadHistoryDao: DownloadHistoryDao
) {

    fun addMedia(mediaEntity: MediaEntity): Long {
        return mediaDao.addMedia(mediaEntity)
    }

    fun addThumbnail(mediaThumbnail: MediaThumbnail) {
        mediaDao.addThumbnail(mediaThumbnail)
    }

    fun getRecentRecords() = mediaDao.getLastSevenRecords()

    fun getMedia(
        limit: Int,
        offset: Int,
        orderByNewest: Boolean,
        onlyFavorites: Boolean
    ): List<MediaWithThumbnail> {
        if (!orderByNewest && onlyFavorites)
            return mediaDao.getOnlyFavoritesByOld(limit, offset)
        if (orderByNewest && onlyFavorites)
            return mediaDao.getOnlyFavorites(limit, offset)
        if (!orderByNewest)
            return mediaDao.getAllMediaByOld(limit, offset)
        return mediaDao.getAllMedia(limit, offset)
    }

    fun getMediaStream() = mediaDao.getAllMediaStream()

    fun getLastFavorites() = mediaDao.getLastFavorites()

    fun updateMediaFavorite(id: Long, favorite: Boolean) {
        mediaDao.updateMediaFavorite(id, favorite)
    }

    fun removeMedia(mediaEntity: MediaEntity, context: Context) {
        mediaDao.deleteMedia(mediaEntity)
        val file = File(context.filesDir, mediaEntity.savedName)
        file.delete()
    }

    fun removeMedia(media: List<MediaEntity>, context: Context) {

        mediaDao.deleteMedias(media)

        media.forEach {
            val file = File(context.filesDir, it.savedName)
            file.delete()
        }
    }

    fun renameMedia(id: Long, title: String) {
        mediaDao.updateMediaTitle(title, id)
    }


    fun addDownloadHistoryItem(downloadHistoryEntity: DownloadHistoryEntity): Long {
        return downloadHistoryDao.addDownloadHistoryItem(downloadHistoryEntity)
    }

    fun addDownloadExtras(downloadHistoryItemExtras: DownloadHistoryItemExtras) {
        downloadHistoryDao.addDownloadExtras(downloadHistoryItemExtras)
    }

    fun getAllDownloadHistory() = downloadHistoryDao.getAllDownloadHistory()

    fun getPendingDownloads(): List<DownloadHistoryWithExtras> {
        return downloadHistoryDao.getPendingDownloads()
    }

    fun getDownloadInProgressStream() = downloadHistoryDao.getDownloadInProgressStream()

    fun getDownloadHistory(id: Long) = downloadHistoryDao.getDownloadHistoryEntity(id)

    fun isThereAnyUncompletedDownloads(): Boolean {
        return downloadHistoryDao.getCountOfUncompletedDownloads() != 0
    }

    fun deleteDownloadHistoryItem(downloadHistoryEntity: DownloadHistoryEntity) =
        downloadHistoryDao.deleteDownloadHistoryItem(downloadHistoryEntity.uid)

    fun deleteDownloadExtra(downloadHistoryItemExtras: DownloadHistoryItemExtras) {
        downloadHistoryDao.deleteDownloadExtras(downloadHistoryItemExtras)
    }

    fun updateDownloadHistoryItem(downloadHistoryEntity: DownloadHistoryEntity) =
        downloadHistoryDao.updateDownloadHistoryItem(downloadHistoryEntity)

}