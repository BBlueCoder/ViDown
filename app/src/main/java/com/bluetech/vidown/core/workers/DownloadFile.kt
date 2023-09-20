package com.bluetech.vidown.core.workers

import com.bluetech.vidown.core.db.entities.DownloadData
import com.bluetech.vidown.core.db.entities.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadFile(
    private val rootPath : String,
    private val fileUrl : String,
    private val fileName : String,
    private val savedDownloadedSize : Long) {

    operator fun invoke() = flow {
        val newFile = File(rootPath, fileName)

        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", DownloadFileWorker.USER_AGENT)

        var contentLength : Long = 0
        try{
            contentLength = connection.getHeaderField("Content-Length").toLong()
        }catch (ex : Exception){
            println("get content-Length exp for url $fileUrl")
        }

        connection.requestMethod = "GET"

        connection.connect()

        val inputStream = connection.inputStream

        val buffer = ByteArray(1024 * 1024 * 5)
        inputStream.skip(savedDownloadedSize)
        var downloadedSize = savedDownloadedSize
        var length = 0
        length = inputStream.read(buffer,0,length)

        inputStream.use {
            val outputStream = FileOutputStream(newFile,true)

            do {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
                downloadedSize += length

                emit(updateProgress(fileUrl, contentLength, downloadedSize))
            } while (length > 0)
        }
    }
        .flowOn(Dispatchers.IO)

    private fun updateProgress(
        downloadUrl: String,
        contentLength: Long,
        downloadedSize: Long
    ): DownloadData {
        return DownloadData(downloadUrl, DownloadStatus.INPROGRESS, contentLength, downloadedSize)
    }
}