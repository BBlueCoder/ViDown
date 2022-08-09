package com.bluetech.vidown

import com.dabluecoder.youdownloaderlib.OnVideoInfoListener
import com.dabluecoder.youdownloaderlib.YouClient
import com.dabluecoder.youdownloaderlib.pojoclasses.VideoResponse
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        val client = YouClient()
        client.videoUrl = "https://www.youtube.com/watch?v=VDvr08sCPOc"
        client.getVideoInfo(object : OnVideoInfoListener {
            override fun onError(message: String) {
                println("Error : $message")
            }

            override fun onSuccess(videoInfo: VideoResponse) {
                videoInfo.streamingData.mixedFormats?.forEach{
                    println("Youtube down : ${it.qualityLabel}")
                    println("Youtube down : ${it.url}")
                    println("__________________________________________________________________________________________________")
                }
            }

        })

        assertEquals(4, 2 + 2)
    }
}