package com.bluetech.vidown.core.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.utils.Constants.DOWNLOAD_FILE_PROGRESS_ACTION
import com.bluetech.vidown.utils.Constants.DOWNLOAD_SERVICE_ACTION
import com.bluetech.vidown.utils.Constants.FILE_PREFIX_NAME
import com.bluetech.vidown.utils.Constants.NOTIFICATION_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class DownloadFileService : Service() {

    inner class DownloadFileServiceBinder : Binder(){
        val service : DownloadFileService
        get()=this@DownloadFileService
    }

    private val binder : IBinder = DownloadFileServiceBinder()

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    @Inject
    lateinit var mediaDao : MediaDao

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fileUrl = intent?.getStringExtra("fileUrl")
        val fileType = intent?.getStringExtra("fileType")
        val mediaTitle = intent?.getStringExtra("mediaTitle") ?: ""
        val fileAudioThumbnail = intent?.getStringExtra("thumbnail")
        val source = intent?.getStringExtra("source")

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

        val mediaEntity = MediaEntity(0,"",mediaType!!,mediaTitle,fileAudioThumbnail,null,0,source!!,fileUrl!!,true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(111,createNotification().build(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }else{
            startForeground(111,createNotification().build())
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                downloadFile(mediaEntity)
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
        mediaEntity.contentLength = contentLength
        println("------------------------------- content length = $contentLength")
        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)
        var downloadedSize : Long = 0

        println("Download Service : downloading...")
        while(length > 0 ){
            fileOutputStream.write(buffer,0,length)
            length = inputStream.read(buffer)
            downloadedSize += length
            mediaEntity.downloadedLength = downloadedSize
            try {
                val progress = (downloadedSize * 100f/contentLength).roundToInt()
                updateProgress(progress,contentLength,downloadedSize)
            }catch (ex : Exception){
                println("------------------- progress exp : ${ex.printStackTrace()}")
            }

        }
        println("Download Service : finished downloading...")
        var thumbnail :String? = null
        if(mediaEntity.thumbnail != null)
            thumbnail = saveThumbnail(mediaEntity)

        fileOutputStream.close()
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
        while(length > 0 ){
            outputStream.write(buffer,0,length)
            length = inputStream.read(buffer)
        }
        println("Download Service : finished downloading thumbnail ...")
        outputStream.close()
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
        println("--------------------------------cancel download")
    }
}