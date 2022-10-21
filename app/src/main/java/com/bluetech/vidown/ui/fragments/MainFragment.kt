package com.bluetech.vidown.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.ui.recyclerviews.ResultsAdapter
import com.bluetech.vidown.ui.customviews.ResultCardView
import com.bluetech.vidown.core.pojoclasses.ResultItem
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

    private lateinit var adapter: ResultsAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var circularProgress : CircularProgressIndicator
    private lateinit var lookUpBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        adapter = ResultsAdapter(mutableListOf())
        recyclerView = view.findViewById(R.id.result_list)
        circularProgress = view.findViewById(R.id.main_progress)

        setupRecyclerView()
        observeSearchResults(view)

        lookUpBtn = view.findViewById(R.id.btn_look_up)

        //link.setText("https://www.youtube.com/watch?v=Q3IkQxzpFfw")

        lookUpBtn.setOnClickListener {
            val link = view.findViewById<TextInputEditText>(R.id.link_text)
            val url = link.text.toString()

            //check if input is a valid url
            if (!url.matches(Regex("(https)?(:\\/\\/)?(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.([a-zA-Z0-9()]{1,6})(?=)\\/.+"))) {
                view.snackBar(resources.getString(R.string.input_is_not_an_url))
                return@setOnClickListener
            }
            circularProgress.visibility = View.VISIBLE
            lookUpBtn.visibility = View.GONE

            lifecycleScope.launch {
                viewModel.searchForResult(url)
            }
        }

        return view
    }

    private fun setupRecyclerView(){
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    R.layout.result_category_title -> 1
                    R.layout.result_list_item -> 1
                    else -> 1
                }
            }

        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
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
                        showItemInfoCard(view,itemInfo)
                        showResultsRecyclerView(it.filter { item -> item !is ResultItem.ItemInfo })
                    }
                }
            }
        }

    }

    private fun showItemInfoCard(view: View, itemInfo: ResultItem.ItemInfo) {
        val resultCard = view.findViewById<ResultCardView>(R.id.card_result)
        if (resultCard.visibility == View.VISIBLE)
            return

        resultCard.setThumbnail(itemInfo.thumbnail)
        resultCard.setTitle(itemInfo.title)

        resultCard.visibility = View.VISIBLE

        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
        resultCard.startAnimation(anim)

    }

    private fun showResultsRecyclerView(results : List<ResultItem>){
        if(!recyclerView.isVisible){
            adapter.submitList(results)

            val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down_with_fade)
            recyclerView.visibility = View.VISIBLE
            recyclerView.startAnimation(anim)
            return
        }

        adapter.submitList(results)
    }

}