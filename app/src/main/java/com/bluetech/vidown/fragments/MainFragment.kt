package com.bluetech.vidown.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bluetech.vidown.R
import com.dabluecoder.youdownloaderlib.OnVideoInfoListener
import com.dabluecoder.youdownloaderlib.YouClient
import com.dabluecoder.youdownloaderlib.pojoclasses.VideoResponse

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
            val client = YouClient()
            client.videoUrl = "https://www.youtube.com/watch?v=VDvr08sCPOc"
            client.getVideoInfo(object : OnVideoInfoListener{
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
        }
    }

    companion object {
    }
}