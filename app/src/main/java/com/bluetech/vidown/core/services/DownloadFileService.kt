package com.bluetech.vidown.core.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bluecoder.ffmpegandroidkotlin.FFmpegWrapper
import com.bluecoder.ffmpegandroidkotlin.ffmpegwrapper.FFmpeg
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.utils.Constants.DOWNLOAD_FILE_PROGRESS_ACTION
import com.bluetech.vidown.utils.Constants.DOWNLOAD_SERVICE_ACTION
import com.bluetech.vidown.utils.Constants.FILE_PREFIX_NAME
import com.bluetech.vidown.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.bluetech.vidown.utils.Constants.USER_AGENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class DownloadFileService : Service() {

    inner class DownloadFileServiceBinder : Binder(){
        val service : DownloadFileService
        get()=this@DownloadFileService
    }

    private val binder : IBinder = DownloadFileServiceBinder()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    @Inject
    lateinit var mediaDao : MediaDao

    private var isServiceStopped = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fileUrl = intent?.getStringExtra("fileUrl")
        val fileType = intent?.getStringExtra("fileType")
        val mediaTitle = intent?.getStringExtra("mediaTitle") ?: ""
        val fileAudioThumbnail = intent?.getStringExtra("thumbnail")
        val source = intent?.getStringExtra("source")
        val audioUrl = intent?.getStringExtra("audioUrl")



        val action = intent?.action
        action?.let {
            fileUrl
        }

        val mediaType = when(fileType){
            "image"->MediaType.Image
            "video"->MediaType.Video
            "audio"->MediaType.Audio
            else -> null
        }

        val mediaEntity = MediaEntity(
            0,
            "",
            mediaType!!,
            mediaTitle,
            fileAudioThumbnail,
            source!!,
            fileUrl!!,
            0
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(111,createNotification().build(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }else{
            startForeground(111,createNotification().build())
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("------------------------------- audio Url : $audioUrl")
                println("------------------------------- video Url : $fileUrl")
                if(audioUrl != null){
                    println("------------------------ download youtube video and mux audio")
                    downloadYoutubeVideoWithSound(mediaEntity,fileUrl,audioUrl)
                }else{
                    downloadFile(mediaEntity)
                }
            }catch (ex : Exception){
                println("DownloadServiceException : ${ex.printStackTrace()}")
                val notificationManager = NotificationManagerCompat.from(this@DownloadFileService)
                val notification = NotificationCompat.Builder(this@DownloadFileService, NOTIFICATION_CHANNEL_ID)
                    .setContentText("An error occurred when saving videos")
                    .setTicker("Error Saving videos!")
                    .setSmallIcon(R.drawable.ic_downloand_notification)

                notificationManager.notify(111, notification.build())

                Intent(DOWNLOAD_SERVICE_ACTION).also {
                    it.putExtra("result","fail")
                    LocalBroadcastManager.getInstance(this@DownloadFileService).sendBroadcast(it)
                }

                stopService()
            }
        }


        return START_STICKY
    }

    private fun setUpDownload(){

    }

    private suspend fun downloadYoutubeVideoWithSound(mediaEntity: MediaEntity,videoSource : String,audioSource : String) {

        var totalDownloadedSize: Long = 0
        var totalContentLength: Long = 0
        val buffer = ByteArray(1024 * 5)

        val timeInMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } else {
            System.currentTimeMillis().toString()
        }

        val videoFileName = "${timeInMillis}_temp_video.mp4"
        val audioFileName = "${timeInMillis}_temp_audio.m4a"

        val videoMediaEntity = MediaEntity(55,videoFileName,mediaEntity.mediaType,mediaEntity.title,mediaEntity.thumbnail,mediaEntity.source,mediaEntity.downloadSource,mediaEntity.duration)

        val audioMediaEntity = MediaEntity(65,audioFileName,MediaType.Audio,mediaEntity.title,mediaEntity.thumbnail,mediaEntity.source,mediaEntity.downloadSource,mediaEntity.duration)


        val videoDownloadJob = scope.launch {
            val outputStream = openFileOutput(videoFileName, MODE_PRIVATE)

            val videoUrl = URL(videoSource)
            val videoConnection = videoUrl.openConnection() as HttpURLConnection
            videoConnection.setRequestProperty("User-Agent", USER_AGENT)

            videoConnection.getHeaderField("Content-Length")?.also {
                totalContentLength += it.toLong()
            }
            videoConnection.requestMethod = "GET"

            videoConnection.connect()

            val videoInputStream = videoConnection.inputStream
            var videoLength = videoInputStream.read(buffer)

            while (videoLength > 0) {
                outputStream.write(buffer, 0, videoLength)
                videoLength = videoInputStream.read(buffer)
                totalDownloadedSize += videoLength

                try {
                    val progress = (totalDownloadedSize * 100f / totalContentLength).roundToInt()
                    updateProgress(progress, totalContentLength, totalDownloadedSize)
                } catch (ex: Exception) {
                    println("------------------- progress exp : ${ex.printStackTrace()}")
                }
            }

            outputStream.close()
            videoConnection.disconnect()

        }

        val audioDownloadJob = scope.launch {
            val outputStream = openFileOutput(audioFileName, MODE_PRIVATE)

            val audioUrl = URL(audioSource)

            val audioConnection = audioUrl.openConnection() as HttpURLConnection
            audioConnection.setRequestProperty("User-Agent", USER_AGENT)

            audioConnection.getHeaderField("Content-Length")?.also {
                totalContentLength += it.toLong()
            }
            audioConnection.requestMethod = "GET"

            videoDownloadJob.join()

            audioConnection.connect()

            val audioInputStream = audioConnection.inputStream

            var audioLength = audioInputStream.read(buffer)

            while (audioLength > 0) {
                outputStream.write(buffer, 0, audioLength)
                audioLength = audioInputStream.read(buffer)
                totalDownloadedSize += audioLength

                try {
                    val progress = (totalDownloadedSize * 100f / totalContentLength).roundToInt()
                    updateProgress(progress, totalContentLength, totalDownloadedSize)
                } catch (ex: Exception) {
                    println("------------------- progress exp : ${ex.printStackTrace()}")
                }
            }

            outputStream.close()
            audioConnection.disconnect()
        }

        audioDownloadJob.join()


        val videoFile = File(filesDir, videoFileName)
        val audioFile = File(filesDir, audioFileName)

        val savedFileName = "$FILE_PREFIX_NAME${timeInMillis}.mp4"
        mediaEntity.name = savedFileName

        val outputFile = File(filesDir, savedFileName)

        FFmpegWrapper(this).mux(videoFile.path,audioFile.path,outputFile.path)
            .onCompletion {
                if (it == null) {
                    //Get duration of media if it is video or audio and check if media is working fine
                    if (mediaEntity.mediaType == MediaType.Audio || mediaEntity.mediaType == MediaType.Video) {
                        val file = File(filesDir, mediaEntity.name)
                        try {
                            MediaPlayer.create(this@DownloadFileService, Uri.fromFile(file))
                                .also { mp ->
                                    mediaEntity.duration = mp.duration.toLong()
                                    videoMediaEntity.duration = mediaEntity.duration
                                    audioMediaEntity.duration = mediaEntity.duration
                                    mp.reset()
                                    mp.release()
                                }.setOnErrorListener { _, _, _ ->
                                mediaEntity.isMediaCorrupted = true
                                true
                            }
                        } catch (ex: Exception) {
                            mediaEntity.isMediaCorrupted = true
                        }
                    }

                    mediaDao.addMedia(videoMediaEntity)
                    mediaDao.addMedia(audioMediaEntity)
                    saveFileToDB(mediaEntity)
                } else {
                    outputFile.delete()
                }
                videoFile.delete()
                audioFile.delete()

            }.catch {
                it.printStackTrace()
            }.collect {
                println("-------------------------------------- ffmpeg $it")
            }

    }

    private fun createNotification() = run {
        NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ).setContentTitle("Downloading...")
            .setSmallIcon(R.drawable.ic_downloand_notification)
            .setContentText("saving...")
            .setTicker("Download started")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setProgress(100,0,true)
    }

    private fun downloadFile(mediaEntity: MediaEntity){

        val timeInMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } else {
            System.currentTimeMillis().toString()
        }

        println("Download Service : Start downloading")
        val savedFileName = "$FILE_PREFIX_NAME${timeInMillis}"
        mediaEntity.name = savedFileName
        val fileOutputStream = openFileOutput(savedFileName, MODE_PRIVATE)


        val url = URL(mediaEntity.downloadSource)
        val connection = url.openConnection() as HttpURLConnection
//        val contentLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            connection.contentLengthLong
//        } else {
//            connection.contentLength
//        }
        val contentLength = connection.getHeaderField("Content-Length").toLong()

        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)
        var downloadedSize : Long = 0

        println("Download Service : downloading...")
        while(length > 0 && !isServiceStopped){
            fileOutputStream.write(buffer,0,length)
            length = inputStream.read(buffer)
            downloadedSize += length

            try {
                val progress = (downloadedSize * 100f/contentLength).roundToInt()
                updateProgress(progress,contentLength,downloadedSize)
            }catch (ex : Exception){
                println("------------------- progress exp : ${ex.printStackTrace()}")
            }

        }
        println("Download Service : finished downloading...")

        if(mediaEntity.thumbnail != null)
            mediaEntity.thumbnail = saveThumbnail(mediaEntity)

        fileOutputStream.close()

        if(isServiceStopped){
            val file = File(filesDir,savedFileName)
            if(file.exists())
                file.delete()
            return
        }

        //Get duration of media if it is video or audio and check if media is working fine
        if(mediaEntity.mediaType == MediaType.Audio || mediaEntity.mediaType == MediaType.Video){
            val file = File(filesDir,mediaEntity.name)
            try{
                MediaPlayer.create(this, Uri.fromFile(file)).also { mp ->
                    mediaEntity.duration = mp.duration.toLong()
                    mp.reset()
                    mp.release()
                }.setOnErrorListener { _, _, _ ->
                    println("---------------- create media player error")
                    mediaEntity.isMediaCorrupted = true
                    true
                }
            }catch (ex : Exception){
                println("---------------- create media player exception")
                mediaEntity.isMediaCorrupted = true
            }
        }

        saveFileToDB(mediaEntity)

    }

    private fun saveThumbnail(mediaEntity: MediaEntity) : String {
        val fileName = "${mediaEntity.name}_thumbnail"
        val outputStream = openFileOutput(fileName, MODE_PRIVATE)

        val url = URL(mediaEntity.thumbnail)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)

        println("Download Service : downloading thumbnail...")
        while(length > 0 && !isServiceStopped){
            outputStream.write(buffer,0,length)
            length = inputStream.read(buffer)
        }
        println("Download Service : finished downloading thumbnail ...")
        outputStream.close()
        if(isServiceStopped){
            val file = File(filesDir,fileName)
            if(file.exists())
                file.delete()
        }
        return fileName
    }

    private fun saveFileToDB(mediaEntity: MediaEntity){

        mediaDao.addMedia(mediaEntity)

        val notificationManager = NotificationManagerCompat.from(this)
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_downloand_notification)
            .setContentText("Videos Have been saved successfully!")
            .setTicker("Videos saved!")
            .setOngoing(false)
            .setProgress(0, 0, false)

        notificationManager.notify(111, notification.build())
        println("Download Service : download finished")

        Intent(DOWNLOAD_SERVICE_ACTION).also {
            it.putExtra("result","success")
            LocalBroadcastManager.getInstance(this).sendBroadcast(it)
        }

        stopService()

    }

    private fun updateProgress(progress: Int, contentLength: Long, downloadedSize: Long){
        Intent(DOWNLOAD_FILE_PROGRESS_ACTION).also {
            it.putExtra("progress",progress)
            it.putExtra("fileSizeInByte",contentLength)
            it.putExtra("downloadSizeInByte",downloadedSize)
            LocalBroadcastManager.getInstance(this).sendBroadcast(it)
        }
    }

    private fun stopService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }else{
            stopForeground(false)
        }
        stopSelf()
    }

    fun cancelDownload(){
        println("-------------------------------Service stopped")
        isServiceStopped = true
        Intent(DOWNLOAD_SERVICE_ACTION).also {
            it.putExtra("result","canceled")
            LocalBroadcastManager.getInstance(this).sendBroadcast(it)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }else{
            stopForeground(true)
        }
        stopSelf()
    }
}