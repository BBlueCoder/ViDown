package com.bluetech.vidown.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

        val link = view.findViewById<EditText>(R.id.edt_url)
        val lookUpBtn = view.findViewById<Button>(R.id.btn_look_up)

        lookUpBtn.setOnClickListener {
            val client = YouClient()
            client.videoUrl = link.text!!.toString()

        }

        return view
    }

    companion object {
    }
}