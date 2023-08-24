package com.bluetech.vidown.core.workers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.utils.Constants
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

@HiltWorker
class DownloadFileWorker @AssistedInject constructor(
    private val mediaDao: MediaDao,
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "DownloadFileWorker"

    private var nameForFile = ""


    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                println("$TAG: download started")
                val mediaEntity = createMediaEntityInstance(params)

                downloadFile(mediaEntity)
                println("$TAG: download end")
                Result.success()
            } catch (ex : CancellationException){
                cleanUp()
                Result.failure()
            }catch (ex: Exception) {
                println("$TAG: Exception : \n ${ex.printStackTrace()}")
                Result.failure()
            }
        }
    }

    private suspend fun createMediaEntityInstance(params: WorkerParameters): MediaEntity {
        val fileUrl = params.inputData.getString(PARAMS_MEDIA_URL)
        val fileType = params.inputData.getString(PARAMS_MEDIA_TYPE)
        val mediaTitle = params.inputData.getString(PARAMS_MEDIA_TITLE) ?: ""
        val fileAudioThumbnail = params.inputData.getString(PARAMS_MEDIA_AUDIO_THUMBNAIL)
        val source = params.inputData.getString(PARAMS_MEDIA_SOURCE)
        val thumbnail = params.inputData.getString(PARAMS_MEDIA_THUMBNAIL)?: ""

        updateDownloadMediaInfo(thumbnail,mediaTitle)

        val mediaType = when (fileType) {
            "image" -> MediaType.Image
            "video" -> MediaType.Video
            "audio" -> MediaType.Audio
            else -> null
        }

        return MediaEntity(
            0,
            "",
            mediaType!!,
            mediaTitle,
            fileAudioThumbnail,
            source!!,
            fileUrl!!,
            0
        )
    }

    private suspend fun updateDownloadMediaInfo(thumbnail : String,title : String){
        setProgress(
            workDataOf(
                PARAMS_MEDIA_TITLE to title,
                PARAMS_MEDIA_THUMBNAIL to thumbnail
            )
        )
    }

    private suspend fun downloadFile(mediaEntity: MediaEntity) {
        nameForFile = generateFileName()
        mediaEntity.name = nameForFile

        val newFile = File(context.filesDir, nameForFile)

        val url = URL(mediaEntity.downloadSource)
        val connection = url.openConnection() as HttpURLConnection

        val contentLength = connection.getHeaderField("Content-Length").toLong()

        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024 * 5)
        var length = inputStream.read(buffer)
        var downloadedSize: Long = 0

        inputStream.use {
            val outputStream = FileOutputStream(newFile)

            do {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
                downloadedSize += length

                updateProgress(downloadedSize, contentLength)
            } while (length > 0 && !isStopped)
        }


        withContext(Dispatchers.Main){
            getDurationOfMediaIfPossible(mediaEntity)
        }

        addMediaToDB(mediaEntity)
    }

    private fun generateFileName(): String {
        val timeInMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } else {
            System.currentTimeMillis().toString()
        }

        return "${Constants.FILE_PREFIX_NAME}${timeInMillis}"
    }

    private suspend fun updateProgress(downloadedSize: Long, contentLength: Long) {
        try {
            val progress = (downloadedSize * 100f / contentLength).roundToInt()

            setProgress(
                workDataOf(
                    KEY_DOWNLOAD_PROGRESS to progress,
                    KEY_FILE_SIZE_IN_BYTES to contentLength,
                    KEY_DOWNLOADED_SIZE_IN_BYTES to downloadedSize
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun getDurationOfMediaIfPossible(mediaEntity: MediaEntity) {
        if (mediaEntity.mediaType == MediaType.Audio || mediaEntity.mediaType == MediaType.Video) {
            val file = File(context.filesDir, nameForFile)

            try {
                suspendCoroutine<Long> { coroutine ->
                    ExoPlayer.Builder(context).build().also {
                        it.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                        it.prepare()
                        it.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_READY) {
                                    it.removeListener(this)
                                    it.release()
                                    mediaEntity.duration = it.duration
                                    coroutine.resume(it.duration)
                                }
                            }
                        })
                    }
                }
            } catch (e: Exception) {
                println("$TAG: duration exception ${e.message}")
                e.printStackTrace()
            }

        }
    }

    private fun addMediaToDB(mediaEntity: MediaEntity) {
        mediaDao.addMedia(mediaEntity)
    }

    private fun cleanUp(){
        val file = File(context.filesDir,nameForFile)
        if(file.exists())
            try {
                file.delete()
            }catch(e : Exception){
                e.printStackTrace()
            }
    }

    companion object {
        const val KEY_DOWNLOAD_PROGRESS = "DOWNLOAD_PROGRESS"
        const val KEY_FILE_SIZE_IN_BYTES = "FILE_SIZE_IN_BYTES"
        const val KEY_DOWNLOADED_SIZE_IN_BYTES = "DOWNLOADED_SIZE"

        const val PARAMS_MEDIA_URL = "MEDIA_URL"
        const val PARAMS_MEDIA_TYPE = "MEDIA_TYPE"
        const val PARAMS_MEDIA_TITLE = "MEDIA_TITLE"
        const val PARAMS_MEDIA_THUMBNAIL = "MEDIA_THUMBNAIL"
        const val PARAMS_MEDIA_AUDIO_THUMBNAIL = "AUDIO_THUMBNAIL"
        const val PARAMS_MEDIA_SOURCE = "MEDIA_SOURCE"
        const val PARAMS_MEDIA_AUDIO_URL = "MEDIA_AUDIO_URL"
    }
}