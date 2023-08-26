package com.bluetech.vidown.core.workers

import android.content.Context
import android.content.Intent
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.bluecoder.ffmpegandroidkotlin.FFmpegWrapper
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.utils.Constants
import com.bluetech.vidown.utils.formatSizeToReadableFormat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
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

    private var contentLength : Long = 0
    private var downloadedSize : Long = 0

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                downloadMedia()
                Result.success()
            } catch (ex : CancellationException){
                cleanUp()
                Result.failure()
            }catch (ex: Exception) {
                println("$TAG: Exception : \n ${ex.message}")
                cleanUp()
                Result.failure()
            }
        }
    }

    private suspend fun downloadMedia(){
        val mediaEntity = createMediaEntityInstance(params)

        nameForFile = generateFileName()
        mediaEntity.name = nameForFile

        val videoDownloadAsync = CoroutineScope(Dispatchers.IO).async {
            downloadFile(mediaEntity.downloadSource,nameForFile)
        }

        val audioDownloadAsync = CoroutineScope(Dispatchers.IO).async {
            checkIfVideoHasSeparateAudioAndDownloadIt(params)
        }

        val audioThumbnailDownloadAsync = CoroutineScope(Dispatchers.IO).async {
            checkIfAudioHasThumbnailAndDownloadIt(mediaEntity)
        }

        val audioThumbnailFileName = audioThumbnailDownloadAsync.await()
        videoDownloadAsync.await()
        val audioFileName = audioDownloadAsync.await()

        audioThumbnailFileName?.let {
            mediaEntity.thumbnail = it
        }

        audioFileName?.let {
            val videoFileName = nameForFile
            nameForFile = "${generateFileName()}.mp4"
            mediaEntity.name = nameForFile

            muxAudioAndVideo(videoFileName,audioFileName,nameForFile)
            clearOutputFiles(videoFileName)
            clearOutputFiles(audioFileName)
        }

        withContext(Dispatchers.Main){
            getDurationOfMediaIfPossible(mediaEntity)
        }

        addMediaToDB(mediaEntity)
    }

    private fun clearOutputFiles(fileName : String){
        val file = File(context.filesDir,fileName)
        if(file.exists())
            file.delete()
    }

    private suspend fun muxAudioAndVideo(videoFileName : String,audioFileName : String,outputFileName : String){
        val video = File(context.filesDir,videoFileName)
        val audio = File(context.filesDir,audioFileName)
        val output = File(context.filesDir,outputFileName)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            FFmpegKit
                .execute("-i ${video.path} -i ${audio.path} -c:v copy -c:a copy -strict normal -map 0:v:0 -map 1:a:0 -shortest ${output.path}")

        }else{
            FFmpegWrapper(context).mux(video.path, audio.path, output.path)
                .onCompletion {
                    it?.printStackTrace()
                }.catch {
                    it.printStackTrace()
                }.collect{
                    println("$TAG muxing output $it")
                }
        }
    }

    private suspend fun checkIfVideoHasSeparateAudioAndDownloadIt(params: WorkerParameters): String? {
        val audioUrl = params.inputData.getString(PARAMS_MEDIA_AUDIO_URL) ?: return null

        val audioFileName = generateFileName()
        downloadFile(audioUrl,audioFileName)

        return audioFileName
    }

    private suspend fun checkIfAudioHasThumbnailAndDownloadIt(mediaEntity: MediaEntity): String?{
        val thumbnailUrl = mediaEntity.thumbnail ?: return null

        val thumbnailFileName = "${nameForFile}_thumbnail"
        downloadFile(thumbnailUrl,thumbnailFileName)

        return thumbnailFileName
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

    private suspend fun downloadFile(fileUrl : String,fileName : String) {
        val newFile = File(context.filesDir, fileName)

        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", USER_AGENT)

        val temp = connection.getHeaderField("Content-Length").toLong()
        contentLength += temp

        connection.requestMethod = "GET"

        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024 * 1024 * 5)
        var length = inputStream.read(buffer)

        inputStream.use {
            val outputStream = FileOutputStream(newFile)

            do {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
                downloadedSize += length

                updateProgress()
            } while (length > 0 && !isStopped)
        }
    }

    private fun generateFileName(): String {
        val timeInMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } else {
            System.currentTimeMillis().toString()
        }

        return "${Constants.FILE_PREFIX_NAME}${timeInMillis}"
    }

    private suspend fun updateProgress() {
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

        const val USER_AGENT = "Mozilla/5.0 (BB10; Touch) AppleWebKit/537.1+ (KHTML, like Gecko) Version/10.0.0.1337 Mobile Safari/537.1+"
    }
}