package com.bluetech.vidown.ui.fragments

import android.content.Intent
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.ui.recyclerviews.ResultsAdapter
import com.bluetech.vidown.ui.customviews.ResultCardView
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.core.services.DownloadFileService
import com.bluetech.vidown.ui.recyclerviews.RecentDownloadAdapter
import com.bluetech.vidown.ui.vm.MainViewModel
import com.bluetech.vidown.utils.snackBar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var circularProgress : CircularProgressIndicator
    private lateinit var lookUpBtn : Button
    private lateinit var showAvailableFormatsBtn : Button

    private lateinit var recentDownloadsRecyclerView : RecyclerView
    private lateinit var recentDownloadsAdapter : RecentDownloadAdapter

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: RecentDownloadAdapter

    private lateinit var recentTextLayout : LinearLayout
    private lateinit var favoritesLayout : LinearLayout

    private lateinit var resultCard : ResultCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            circularProgress = findViewById(R.id.main_progress)

            recentTextLayout = findViewById(R.id.recent_layout)
            recentDownloadsRecyclerView = findViewById(R.id.recent_recycler_view)

            favoritesLayout = findViewById(R.id.favorites_layout)
            favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)

            resultCard = findViewById(R.id.card_result)

            lookUpBtn = findViewById(R.id.btn_look_up)

            showAvailableFormatsBtn = findViewById(R.id.card_result_available_formats)

        }

        recentDownloadsRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        recentDownloadsAdapter = RecentDownloadAdapter(
            emptyList(),
            requireContext()
        )
        recentDownloadsRecyclerView.adapter = recentDownloadsAdapter

        favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        favoritesAdapter = RecentDownloadAdapter(
            emptyList(),
            requireContext()
        )
        favoritesRecyclerView.adapter = favoritesAdapter

        observeSearchResults(view)
        observeLastDownloads()
        observeLastFavorites()

        showAvailableFormatsBtn.paintFlags = showAvailableFormatsBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        lookUpBtn.setOnClickListener {
            hideItemCard()

            val link = view.findViewById<TextInputEditText>(R.id.link_text)
            link.clearFocus()
            val url = link.text.toString()
            //link.setText("https://www.youtube.com/shorts/lZEDLCk6drY")

            //check if input is a valid url
            if (!url.matches(Regex("(https)?(:\\/\\/)?(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.([a-zA-Z0-9()]{1,6})(?=)\\/.+"))) {
                view.snackBar(resources.getString(R.string.input_is_not_an_url))
                return@setOnClickListener
            }
            circularProgress.visibility = View.VISIBLE
            lookUpBtn.visibility = View.INVISIBLE

            lifecycleScope.launch {
                viewModel.searchForResult(url)
            }
        }

        showAvailableFormatsBtn.setOnClickListener {
            Navigation.findNavController(requireActivity() ,R.id.nav_host).navigate(R.id.show_available_formats)
        }
    }

    private fun observeSearchResults(view: View) {
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lookUpResults.collect { result ->
                    circularProgress.visibility= View.GONE
                    lookUpBtn.visibility = View.VISIBLE
                    result.onFailure { exp ->
                        view.snackBar(
                            exp.message!!
                        )
                    }
                    result.onSuccess {
                        if(it.isEmpty())
                            return@onSuccess
                        val itemInfo = it.filterIsInstance<ResultItem.ItemInfo>().first()
                        showItemInfoCard(itemInfo)
                        showAvailableFormats()
                    }
                }
            }
        }

    }

    private fun showItemInfoCard(itemInfo: ResultItem.ItemInfo) {
        if (resultCard.visibility == View.VISIBLE)
            return

        resultCard.setThumbnail(itemInfo.thumbnail)
        resultCard.setTitle(itemInfo.title)

        resultCard.visibility = View.VISIBLE

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
        resultCard.startAnimation(anim)

    }

    private fun showAvailableFormats(){
        if(!showAvailableFormatsBtn.isVisible){

            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
            showAvailableFormatsBtn.visibility = View.VISIBLE
            showAvailableFormatsBtn.startAnimation(anim)
            return
        }
    }

    private fun observeLastDownloads(){
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.recentDownloads.collect{result ->
                    result.onSuccess {

                        recentDownloadsRecyclerView.isVisible = it.isNotEmpty()
                        recentTextLayout.isVisible= it.isNotEmpty()

                        recentDownloadsAdapter.recentDownloadList = it
                        recentDownloadsAdapter.notifyItemRangeChanged(0,it.size)
                    }
                    result.onFailure {
                        recentTextLayout.visibility = View.INVISIBLE
                        recentTextLayout.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun observeLastFavorites(){
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.lastFavorites.collect{ result ->
                    result.onSuccess {
                        favoritesLayout.isVisible = it.isNotEmpty()
                        favoritesRecyclerView.isVisible = it.isNotEmpty()

                        favoritesAdapter.recentDownloadList =it
                        favoritesAdapter.notifyItemChanged(0,it.size)
                    }
                    result.onFailure {
                        favoritesLayout.visibility = View.INVISIBLE
                        favoritesRecyclerView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun hideItemCard(){
        if(resultCard.visibility == View.GONE || resultCard.visibility == View.INVISIBLE)
            return

        val hideAnimation = AnimationUtils.loadAnimation(requireContext(),R.anim.slide_up_with_fade)
        hideAnimation.setAnimationListener(object : AnimationListener{
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                showAvailableFormatsBtn.visibility = View.GONE
                resultCard.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })
        resultCard.startAnimation(hideAnimation)
        showAvailableFormatsBtn.startAnimation(hideAnimation)
    }

}