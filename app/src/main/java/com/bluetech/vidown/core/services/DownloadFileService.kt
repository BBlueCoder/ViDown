package com.bluetech.vidown.core.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class DownloadFileService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val fileName = intent?.getStringExtra("fileName")
        val fileUrl = intent?.getStringExtra("fileUrl")

        CoroutineScope(Dispatchers.IO).launch {
            downloadFile(fileUrl!!,fileName!!,"")
        }

        return START_STICKY
    }

    private fun downloadFile(fileUrl : String,fileName : String,fileMimeType : String){

        println("Download Service : Start downloading")
        val fileOutputStream = openFileOutput("$fileName.mp4", MODE_PRIVATE)


        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024)
        var length = inputStream.read(buffer)

        while(length > 0 ){
            println("Download Service : downloading...")
            fileOutputStream.write(buffer,0,length)
            length = inputStream.read(buffer)
        }

        fileOutputStream.close()
        println("Download Service : download finished")
    }
}