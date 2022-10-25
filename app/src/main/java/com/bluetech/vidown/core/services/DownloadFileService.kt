package com.bluetech.vidown.core.services

import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.AppLocalDB
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
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

@AndroidEntryPoint
class DownloadFileService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @Inject
    lateinit var mediaDao : MediaDao

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fileUrl = intent?.getStringExtra("fileUrl")
        val fileType = intent?.getStringExtra("fileType")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(111,createNotification().build(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }else{
            startForeground(111,createNotification().build())
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                downloadFile(fileUrl!!,fileType!!)
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

    private fun downloadFile(fileUrl : String,fileMimeType : String){

        val timeInMillis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        } else {
            System.currentTimeMillis().toString()
        }

        println("Download Service : Start downloading")
        val savedFileName = "$FILE_PREFIX_NAME${timeInMillis}"
        val fileOutputStream = openFileOutput(savedFileName, MODE_PRIVATE)


        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)

        println("Download Service : downloading...")
        while(length > 0 ){
            fileOutputStream.write(buffer,0,length)
            length = inputStream.read(buffer)
        }
        println("Download Service : finished downloading...")

        fileOutputStream.close()
        saveFileToDB(savedFileName,fileMimeType)

    }

    private fun saveFileToDB(fileName: String, fileMimeType: String){

        val mediaType = when(fileMimeType){
            "image"->MediaType.Image
            "video"->MediaType.Video
            "audio"->MediaType.Audio
            else -> null
        }

        val mediaEntity = MediaEntity(0,fileName,mediaType!!)

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

    private fun stopService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }else{
            stopForeground(false)
        }
        stopSelf()
    }
}