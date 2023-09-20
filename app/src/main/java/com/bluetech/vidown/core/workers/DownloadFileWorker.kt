package com.bluetech.vidown.core.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.arthenica.ffmpegkit.FFmpegKit
import com.bluecoder.ffmpegandroidkotlin.FFmpegWrapper
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.dao.DownloadHistoryDao
import com.bluetech.vidown.core.db.dao.MediaDao
import com.bluetech.vidown.core.db.entities.DownloadData
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.db.entities.DownloadStatus
import com.bluetech.vidown.core.db.entities.MediaEntity
import com.bluetech.vidown.core.db.entities.mapToMediaEntity
import com.bluetech.vidown.core.db.entities.mapToMediaThumbnail
import com.bluetech.vidown.core.db.entities.mapToNewDownloadItemWithNewDownloadData
import com.bluetech.vidown.core.db.entities.mapToNewDownloadWithNewDownloadData
import com.bluetech.vidown.core.db.entities.updateDownloadStatus
import com.bluetech.vidown.core.domain.GetPendingDownloadsAsQueueUseCase
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.utils.Constants
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

@HiltWorker
class DownloadFileWorker @AssistedInject constructor(
    private val dbRepo: DBRepo,
    private val downloadHistoryDao: DownloadHistoryDao,
    private val getPendingDownloadsAsQueueUseCase: GetPendingDownloadsAsQueueUseCase,
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "DownloadFileWorker"

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            var downloadsQueue = getPendingDownloadsAsQueueUseCase()
            while (downloadsQueue.isNotEmpty()) {
                val download = downloadsQueue.poll()
                try {
                    updateDownloadStatusOFDownloadItem(download!!,DownloadStatus.INPROGRESS)
                    downloadMedia(download)
                    updateDownloadStatusOFDownloadItem(download, DownloadStatus.COMPLETED)

                } catch (ex: CancellationException) {
                    cleanUp(download!!)
                } catch (ex: Exception) {
                    updateDownloadStatusOFDownloadItem(download!!, DownloadStatus.FAILED)
                    println("$TAG: Exception : \n ${ex.message}")
                    ex.printStackTrace()
                    cleanUp(download)
                }finally {
                    downloadsQueue = getPendingDownloadsAsQueueUseCase()
                }
            }
            Result.success()
        }
    }

    private suspend fun downloadMedia(currentDownloadItem: DownloadHistoryWithExtras) {

        var blurredThumbnailSavedName: String? = null

        val mainDownloadAsync = CoroutineScope(Dispatchers.IO).async {
            DownloadFile(
                context.filesDir.path,
                currentDownloadItem.downloadHistoryEntity.downloadData.downloadUrl,
                currentDownloadItem.downloadHistoryEntity.savedName,
                currentDownloadItem.downloadHistoryEntity.downloadData.downloadSizeInBytes
            )()
                .collect {
                    if(isDownloadCancelled(currentDownloadItem.downloadHistoryEntity.uid))
                        throw CancellationException()

                    val downloadHistoryItem =
                        currentDownloadItem.mapToNewDownloadItemWithNewDownloadData(it)
                    downloadHistoryDao.updateDownloadHistoryItem(downloadHistoryItem)

                }
        }

        var audioFileName: String? = null
        val separatedAudioDownloadAsync = CoroutineScope(Dispatchers.IO).async {
            if (currentDownloadItem.downloadHistoryEntity.type == MediaType.Video) {
                val isVideoHasASeparatedAudio = checkIfVideoHasASeparatedAudio(currentDownloadItem)
                if (isVideoHasASeparatedAudio) {
                    downloadItemExtra(getSeparatedAudioExtra(currentDownloadItem)!!)
                    audioFileName = getSeparatedAudioExtra(currentDownloadItem)!!.savedName
                }
            }
        }

        val thumbnailDownloadAsync = CoroutineScope(Dispatchers.IO).async {
            if(currentDownloadItem.downloadHistoryEntity.type == MediaType.Image)
                return@async

            downloadItemExtra(getThumbnailExtra(currentDownloadItem)!!)
            if (currentDownloadItem.downloadHistoryEntity.type == MediaType.Audio)
                blurredThumbnailSavedName =
                    BlurImage(context, getThumbnailExtra(currentDownloadItem)!!.savedName)()

        }

        thumbnailDownloadAsync.await()
        mainDownloadAsync.await()
        separatedAudioDownloadAsync.await()

        var mixedVideoAndAudioSavedName: String? = null

        audioFileName?.let {
            mixedVideoAndAudioSavedName = "${Constants.generateFileName()}.mp4"

            VideoAudioMuxer(
                context,
                currentDownloadItem.downloadHistoryEntity.savedName,
                it,
                mixedVideoAndAudioSavedName!!
            )()
            clearOutputFiles(currentDownloadItem.downloadHistoryEntity.savedName)
            clearOutputFiles(it)
            dbRepo.deleteDownloadExtra(getSeparatedAudioExtra(currentDownloadItem)!!)
        }

        val duration = CoroutineScope(Dispatchers.Main).async {
            if(currentDownloadItem.downloadHistoryEntity.type == MediaType.Image)
                return@async 0

            GetDurationOfMedia(context, mixedVideoAndAudioSavedName?: currentDownloadItem.downloadHistoryEntity.savedName)()
        }.await()

        val mediaEntity = currentDownloadItem.downloadHistoryEntity.mapToMediaEntity(
            mixedVideoAndAudioSavedName,
            duration
        )
        val mediaId = addMediaToDB(mediaEntity)
        val mediaThumbnail = getThumbnailExtra(currentDownloadItem)!!.mapToMediaThumbnail(
            mediaId,
            blurredThumbnailSavedName
        )
        dbRepo.addThumbnail(mediaThumbnail)
    }

    private fun isDownloadCancelled(id : Long) : Boolean {
        val downloadItem = dbRepo.getDownloadHistory(id)
        return downloadItem.downloadData.downloadStatus == DownloadStatus.CANCELLED
    }

    private fun updateDownloadStatusOFDownloadItem(
        currentDownloadItem: DownloadHistoryWithExtras,
        status: DownloadStatus
    ) {
        val downloadHistoryItem = currentDownloadItem.updateDownloadStatus(status)
        downloadHistoryDao.updateDownloadHistoryItem(downloadHistoryItem)
    }

    private fun updateDownloadStatusOFDownloadItemExtra(
        downloadExtra: DownloadHistoryItemExtras,
        status: DownloadStatus
    ){
        val downloadItemExtra = downloadExtra.updateDownloadStatus(status)
        downloadHistoryDao.updateDownloadItemExtra(downloadItemExtra)
    }

    private fun checkIfVideoHasASeparatedAudio(currentDownloadItem: DownloadHistoryWithExtras): Boolean {
        return currentDownloadItem.downloadHistoryItemExtras.find {
            it.mediaType == MediaType.Audio
        } != null
    }

    private fun clearOutputFiles(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists())
            file.delete()
    }

    private fun getSeparatedAudioExtra(currentDownloadItem: DownloadHistoryWithExtras) =
        currentDownloadItem.downloadHistoryItemExtras.find { it.mediaType == MediaType.Audio }

    private fun getThumbnailExtra(currentDownloadItem: DownloadHistoryWithExtras) =
        currentDownloadItem.downloadHistoryItemExtras.find { it.mediaType == MediaType.Image }

    private suspend fun downloadItemExtra(downloadExtra: DownloadHistoryItemExtras) {

        if(downloadExtra.downloadData.downloadStatus == DownloadStatus.COMPLETED)
            return

        DownloadFile(
            context.filesDir.path,
            downloadExtra.downloadData.downloadUrl,
            downloadExtra.savedName,
            downloadExtra.downloadData.downloadSizeInBytes
        )()
            .onCompletion {
                if(it == null){
                    updateDownloadStatusOFDownloadItemExtra(downloadExtra,DownloadStatus.COMPLETED)
                }else{
                    updateDownloadStatusOFDownloadItemExtra(downloadExtra,DownloadStatus.FAILED)
                }
            }
            .collect {
                val newDownloadExtra = downloadExtra.mapToNewDownloadWithNewDownloadData(it)
                downloadHistoryDao.updateDownloadItemExtra(newDownloadExtra)
            }
    }

    private fun addMediaToDB(mediaEntity: MediaEntity): Long {
        return dbRepo.addMedia(mediaEntity)
    }

    private fun cleanUp(downloadItem: DownloadHistoryWithExtras) {
        deleteFile(downloadItem.downloadHistoryEntity.savedName)
        downloadItem.downloadHistoryItemExtras.forEach {
            deleteFile(it.savedName)
        }

    }

    private fun deleteFile(fileName: String) {
        val file = File(context.filesDir, fileName)
        if (file.exists())
            try {
                file.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    companion object {
        const val KEY_DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS"
        const val KEY_FILE_SIZE_IN_BYTES = "FILE_SIZE_IN_BYTES"
        const val KEY_DOWNLOADED_SIZE_IN_BYTES = "DOWNLOADED_SIZE"

        const val USER_AGENT =
            "Mozilla/5.0 (BB10; Touch) AppleWebKit/537.1+ (KHTML, like Gecko) Version/10.0.0.1337 Mobile Safari/537.1+"
    }
}