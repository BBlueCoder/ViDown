package com.bluetech.vidown.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.ui.recyclerviews.ResultsAdapter
import com.bluetech.vidown.ui.vm.MainViewModel
import com.bluetech.vidown.utils.snackBar
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: MainViewModel

    private lateinit var adapter: ResultsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_media_sheet,container,false)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        recyclerView = view.findViewById(R.id.media_recycler_view)
        adapter = ResultsAdapter(mutableListOf())


        setupRecyclerView()
        observeSearchResults(view)

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
                    result.onFailure { exp ->
                        view.snackBar(
                            exp.message!!
                        )
                    }
                    result.onSuccess {
                        if(it.isEmpty())
                            return@onSuccess
                        val itemInfo = it.filterIsInstance<ResultItem.ItemInfo>().first()
                        val mediaThumbnail = view.findViewById<ImageView>(R.id.media_thumbnail)
                        val mediaTitle = view.findViewById<TextView>(R.id.media_title)
                        mediaTitle.text = itemInfo.title
                        Glide
                            .with(requireContext())
                            .load(itemInfo.thumbnail)
                            .into(mediaThumbnail)
                        adapter.submitList(it.filter {  item -> item !is ResultItem.ItemInfo })
                    }
                }
            }
        }

    }

}