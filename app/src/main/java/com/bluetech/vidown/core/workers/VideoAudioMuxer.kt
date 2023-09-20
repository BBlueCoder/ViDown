package com.bluetech.vidown.core.workers

import android.content.Context
import android.os.Build
import com.arthenica.ffmpegkit.FFmpegKit
import com.bluecoder.ffmpegandroidkotlin.FFmpegWrapper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import java.io.File

class VideoAudioMuxer(
    private val context : Context,
    private val videoFileName: String,
    private val audioFileName: String,
    private val outputFileName: String
) {
    suspend operator fun invoke(){
        val video = File(context.filesDir, videoFileName)
        val audio = File(context.filesDir, audioFileName)
        val output = File(context.filesDir, outputFileName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FFmpegKit
                .execute("-i ${video.path} -i ${audio.path} -c:v copy -c:a copy -strict normal -map 0:v:0 -map 1:a:0 -shortest ${output.path}")

        } else {
            FFmpegWrapper(context).mux(video.path, audio.path, output.path)
                .onCompletion {
                    it?.printStackTrace()
                }.catch {
                    it.printStackTrace()
                }.collect {
                }
        }
    }
}