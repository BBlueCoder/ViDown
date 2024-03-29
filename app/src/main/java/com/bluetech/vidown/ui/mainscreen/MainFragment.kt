package com.bluetech.vidown.ui.mainscreen

import android.graphics.Paint
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.data.db.dao.MediaDao
import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import com.bluetech.vidown.utils.hideKeyboard
import com.bluetech.vidown.utils.snackBar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var circularProgress: CircularProgressIndicator
    private lateinit var lookUpBtn: Button
    private lateinit var showAvailableFormatsBtn: Button

    private lateinit var recentDownloadsRecyclerView: RecyclerView
    private lateinit var recentDownloadsAdapter: HorizontalRecyclerViewAdapter

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoritesAdapter: HorizontalRecyclerViewAdapter

    private lateinit var recentTextLayout: LinearLayout
    private lateinit var favoritesLayout: LinearLayout

    private lateinit var resultCard: ResultCardView

    private lateinit var rootView: View

    @Inject
    lateinit var mediaDao: MediaDao

    private var showSearchBtn = true

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

        rootView = view

        view.apply {
            circularProgress = findViewById(R.id.main_progress)

            recentTextLayout = findViewById(R.id.recent_layout)
            recentDownloadsRecyclerView = findViewById(R.id.recent_recycler_view)

            favoritesLayout = findViewById(R.id.favorite_layout)
            favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)

            resultCard = findViewById(R.id.card_result)

            lookUpBtn = findViewById(R.id.btn_look_up)

            showAvailableFormatsBtn = findViewById(R.id.card_result_available_formats)

        }

        recentDownloadsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recentDownloadsAdapter = HorizontalRecyclerViewAdapter(
            emptyList(),
            requireContext()
        ) {
            val action = MainFragmentDirections.displayMediaAction(it)
            Navigation.findNavController(requireActivity(), R.id.nav_host).navigate(action)
        }
        recentDownloadsRecyclerView.adapter = recentDownloadsAdapter

        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        favoritesAdapter = HorizontalRecyclerViewAdapter(
            emptyList(),
            requireContext()
        ) {
            val action = MainFragmentDirections.displayMediaAction(it)
            Navigation.findNavController(requireActivity(), R.id.nav_host).navigate(action)
        }

        favoritesRecyclerView.adapter = favoritesAdapter

        observeMediaLink()
        observeSearchResults()
        observeLastDownloads()
        observeLastFavorites()

        showAvailableFormatsBtn.paintFlags =
            showAvailableFormatsBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        lookUpBtn.setOnClickListener {
            val link = view.findViewById<EditText>(R.id.link_text)
            link.clearFocus()
            val url = link.text.toString()

            requireActivity().hideKeyboard(lookUpBtn)

            searchButtonAction(url)
        }

        showAvailableFormatsBtn.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.nav_host)
                .navigate(R.id.show_available_formats)
        }
    }

    private fun isUrlValid(url: String): Boolean {
        return url.matches(Regex("(https)?(:\\/\\/)?(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.([a-zA-Z0-9()]{1,6})(?=)\\/.+"))
    }

    private fun searchForMedia(url: String) {
        hideItemCard()

        lookUpBtn.visibility = View.INVISIBLE
        circularProgress.visibility = View.VISIBLE

        lifecycleScope.launch {
            viewModel.searchForResult(url)
        }
    }

    private fun searchButtonAction(url: String) {

        if (url.isEmpty())
            return

        if (!isUrlValid(url)) {
            rootView.snackBar(resources.getString(R.string.input_is_not_an_url))
            return
        }

        showSearchBtn = false

        searchForMedia(url)
    }

    private fun observeMediaLink() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mediaLink.collect {
                    if (it == "")
                        return@collect

                    val link = rootView.findViewById<TextInputEditText>(R.id.link_text)
                    link.setText(it)
                    searchButtonAction(link.text.toString())
                }
            }
        }
    }

    private fun observeSearchResults() {
        lifecycleScope.launch(Dispatchers.IO) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lookUpResults.collect { result ->
                    withContext(Dispatchers.Main) {
                        result.onFailure { exp ->
                            showAndHideLookUpButtonAndProgress()
                            exp.printStackTrace()
                            rootView.snackBar(
                                exp.message!!
                            )
                        }
                        result.onSuccess {
                            if (it.isEmpty())
                                return@onSuccess
                            val itemInfo = it.filterIsInstance<ResultItem.ItemInfo>().first()

                            showItemInfoCard(itemInfo)
                            showAvailableFormats()
                        }
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

    private fun showAvailableFormats() {
        if (!showAvailableFormatsBtn.isVisible) {

            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
            showAvailableFormatsBtn.visibility = View.VISIBLE
            showAvailableFormatsBtn.startAnimation(anim)

            showAndHideLookUpButtonAndProgress()

            return
        }
    }

    private fun showAndHideLookUpButtonAndProgress() {
        circularProgress.visibility = View.GONE
        lookUpBtn.visibility = View.VISIBLE
    }

    private fun observeLastDownloads() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentDownloads.collect { result ->
                    recentDownloadsRecyclerView.isVisible = result.isNotEmpty()
                    recentTextLayout.isVisible = result.isNotEmpty()

                    recentDownloadsAdapter.recentDownloadList = result
                    recentDownloadsAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun observeLastFavorites() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lastFavorites.collect { result ->
                    favoritesLayout.isVisible = result.isNotEmpty()
                    favoritesRecyclerView.isVisible = result.isNotEmpty()

                    favoritesAdapter.recentDownloadList = result
                    favoritesAdapter.notifyItemChanged(0, result.size)
                }
            }
        }
    }

    private fun hideItemCard() {
        if (resultCard.visibility == View.GONE || resultCard.visibility == View.INVISIBLE)
            return

        val hideAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up_with_fade)
        hideAnimation.setAnimationListener(object : AnimationListener {
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