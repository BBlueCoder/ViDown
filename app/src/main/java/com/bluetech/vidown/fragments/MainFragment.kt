package com.bluetech.vidown.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.ResultsAdapter
import com.bluetech.vidown.pojoclasses.ResultItem
import com.bluetech.vidown.services.DownloadFileService
import com.bumptech.glide.Glide
import com.dabluecoder.youdownloaderlib.OnVideoInfoListener
import com.dabluecoder.youdownloaderlib.YouClient
import com.dabluecoder.youdownloaderlib.pojoclasses.VideoResponse
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URI

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val link = view.findViewById<TextInputEditText>(R.id.link_text)
        val lookUpBtn = view.findViewById<Button>(R.id.btn_look_up)

        val recyclerView = view.findViewById<RecyclerView>(R.id.result_list)

        val cardTitle = view.findViewById<TextView>(R.id.card_title)
        val cardThumbnail = view.findViewById<ImageView>(R.id.card_thumbnail)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lookUpBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
                val client = YouClient()
                client.videoUrl = "https://www.youtube.com/watch?v=Q3IkQxzpFfw"

                val vidResp = client.getVideoInfo()

                val resultList = mutableListOf<ResultItem>()

                vidResp.streamingData.adaptiveFormats!!.map {
                    resultList.add(ResultItem("test",it.mimeType,it.url!!))
                }

                val adapter = ResultsAdapter(resultList)

                withContext(Dispatchers.Main){
                    cardTitle.text = vidResp.videoDetails.title



                    recyclerView.adapter = adapter

                }


            }

        }

        return view
    }

    companion object {
    }
}