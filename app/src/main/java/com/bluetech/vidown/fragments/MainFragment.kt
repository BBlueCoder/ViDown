package com.bluetech.vidown.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import com.bluetech.vidown.R
import com.bluetech.vidown.services.DownloadFileService
import com.dabluecoder.youdownloaderlib.OnVideoInfoListener
import com.dabluecoder.youdownloaderlib.YouClient
import com.dabluecoder.youdownloaderlib.pojoclasses.VideoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URI

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

//        val button = view.findViewById<Button>(R.id.btn_test)
//
//        button.setOnClickListener {
//            val client = YouClient()
//            client.videoUrl = "https://www.youtube.com/watch?v=VDvr08sCPOc"
//            client.getVideoInfo(object : OnVideoInfoListener{
//                override fun onError(message: String) {
//                    println("Error : $message")
//                }
//
//                override fun onSuccess(videoInfo: VideoResponse) {
//                    videoInfo.streamingData.mixedFormats?.forEach{
//                        println("Youtube down : ${it.qualityLabel}")
//                        println("Youtube down : ${it.url}")
//                        println("__________________________________________________________________________________________________")
//                    }
//                }
//
//            })
//        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<Button>(R.id.btn_test)

        button.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                val client = YouClient()
                client.videoUrl = "https://www.youtube.com/watch?v=VDvr08sCPOc"
                client.getVideoInfo(object : OnVideoInfoListener{
                    override fun onError(message: String) {
                        println("Error : $message")
                    }

                    override fun onSuccess(videoInfo: VideoResponse) {
                        videoInfo.streamingData.mixedFormats?.forEach{
                            println("Youtube down : ${it.mimeType}")
                            println("Youtube down : ${it.qualityLabel}")
                            println("Youtube down : ${it.url}")
                            println("__________________________________________________________________________________________________")
                            Intent(context,DownloadFileService::class.java).also {intent ->
                                intent.putExtra("fileName","video_test2")
                                intent.putExtra("fileUrl",it.url)
                                context?.startService(intent)
                            }
                        }
                    }

                })
            }
        }

        val playButton = view.findViewById<Button>(R.id.btn_play)
        playButton.setOnClickListener {
            val videoView = view.findViewById<VideoView>(R.id.video_player)

            val mediaController = MediaController(context)
            mediaController.setAnchorView(videoView)

            val files = context?.filesDir?.listFiles()

            files?.filter { it.canRead() && it.name == "video_test2.mp4" }

            videoView.setMediaController(mediaController)
            videoView.setVideoURI(Uri.parse(files?.get(0)!!.toString()))
            videoView.requestFocus()
            videoView.start()
        }
    }

    companion object {
    }
}