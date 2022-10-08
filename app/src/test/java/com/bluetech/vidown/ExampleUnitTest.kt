package com.bluetech.vidown

import com.dabluecoder.youdownloaderlib.OnVideoInfoListener
import com.dabluecoder.youdownloaderlib.YouClient
import com.dabluecoder.youdownloaderlib.pojoclasses.VideoResponse
import org.jsoup.Connection
import org.jsoup.Jsoup
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

//        val client = YouClient()
//        client.videoUrl = "https://www.youtube.com/watch?v=VDvr08sCPOc"
//        client.getVideoInfo(object : OnVideoInfoListener {
//            override fun onError(message: String) {
//                println("Error : $message")
//            }
//
//            override fun onSuccess(videoInfo: VideoResponse) {
//                videoInfo.streamingData.mixedFormats?.forEach{
//                    println("Youtube down : ${it.qualityLabel}")
//                    println("Youtube down : ${it.url}")
//                    println("__________________________________________________________________________________________________")
//                }
//            }
//
//        })

        val url = "https://ssstwitter.com/nocontextfooty/status/1576255692738293761"

        val splitUrl = url.split("/")

        val sssUrl = "https://ssstwitter.com/${splitUrl[3]}/${splitUrl[4]}/${splitUrl[5]}"

        println("url : $sssUrl")

        val doc = Jsoup.connect(sssUrl).post()

        val resultClass = doc.getElementsByClass("result_overlay")

        println("result Class : $resultClass")

        val title = resultClass[0].getElementsByTag("p")

        println(title[0].html())

        assertEquals(4, 2 + 2)
    }
}