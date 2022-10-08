package com.bluetech.vidown.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.ui.ResultsAdapter
import com.bluetech.vidown.customviews.ResultCardView
import com.bluetech.vidown.pojoclasses.ResultItem
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val link = view.findViewById<TextInputEditText>(R.id.link_text)
        val lookUpBtn = view.findViewById<Button>(R.id.btn_look_up)

        val resultCard = view.findViewById<ResultCardView>(R.id.card_result)

        val recyclerView = view.findViewById<RecyclerView>(R.id.result_list)

        link.setText("https://www.youtube.com/watch?v=Q3IkQxzpFfw")
        val adapter = ResultsAdapter(mutableListOf())
        val gridLayoutManager = GridLayoutManager(requireContext(),2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)){
                    R.layout.result_category_title -> 1
                    R.layout.result_list_item -> 1
                    else -> 1
                }
            }

        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lookUpBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO){
//                val client = YouClient()
//                client.videoUrl = link.text.toString()
//
//                val vidResp = client.getVideoInfo()
//
                val resultList = mutableListOf<ResultItem>()
//
//                vidResp.streamingData.adaptiveFormats!!.map {
//                    resultList.add(ResultItem("test","mp4",it.url!!))
//                }
//


                withContext(Dispatchers.Main){

//                    resultCard.setThumbnail(vidResp.videoDetails.thumbnail.thumbnails.first().url)
//                    resultCard.setTitle(vidResp.videoDetails.title)
                    resultCard.visibility = View.VISIBLE
                    val anim = AnimationUtils.loadAnimation(requireContext(),R.anim.slide_down_with_fade)
                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(p0: Animation?) {

                        }

                        override fun onAnimationEnd(p0: Animation?) {

                        }

                        override fun onAnimationRepeat(p0: Animation?) {

                        }

                    })
//                    resultCard.startAnimation(anim)
//
//                    resultList.add(ResultItem.CategoryTitle("Video"))
//                    resultList.add(ResultItem.ItemData(1,"test","mp4","url"))
//                    resultList.add(ResultItem.ItemData(2,"test 2","mp4","url"))
//                    resultList.add(ResultItem.CategoryTitle("Audio"))
//                    resultList.add(ResultItem.ItemData(3,"test 3","audio","url"))
//                    resultList.add(ResultItem.ItemData(4,"test 4","audio","url"))
//
//                    recyclerView.adapter = adapter
//                    adapter.submitList(resultList)

                }


            }

        }

        return view
    }

    companion object {
    }
}