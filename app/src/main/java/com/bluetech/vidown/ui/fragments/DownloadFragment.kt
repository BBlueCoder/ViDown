package com.bluetech.vidown.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.ui.recyclerviews.DownloadsAdapter
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadFragment : Fragment() {

    private lateinit var viewModel : DownloadViewModel
    private lateinit var adapter : DownloadsAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var emptyText : TextView
    private lateinit var recyclerViewFooterProgress : CircularProgressIndicator
    private lateinit var recyclerViewHeaderProgress : CircularProgressIndicator

    private lateinit var downloadProgress : LinearProgressIndicator
    private lateinit var downloadTextProgress : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_download, container, false)

        viewModel = ViewModelProvider(requireActivity())[DownloadViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            recyclerView = findViewById(R.id.download_recycler_view)
            emptyText = findViewById(R.id.download_empty_text)
            recyclerViewFooterProgress = findViewById(R.id.download_progress_footer)
            recyclerViewHeaderProgress = findViewById(R.id.download_progress_header)
            downloadProgress = findViewById(R.id.download_media_progress)
            downloadTextProgress = findViewById(R.id.download_media_progress_text)
        }

        adapter = DownloadsAdapter(requireContext()){mediaEntity ->
            when(mediaEntity.mediaType){
                MediaType.Video -> {
                    val action = DownloadFragmentDirections.displayMediaAction(mediaEntity)
                    Navigation.findNavController(requireActivity() ,R.id.nav_host).navigate(action)
                }
                MediaType.Image -> {

                }
                MediaType.Audio -> {

                }
            }
        }
        recyclerView.adapter = adapter
        observeDownloads()

        adapter.addLoadStateListener {  loadState ->
            adapterLoadStateListening(loadState)
        }

        observeItemInfo(view)
        observeDownloadProgress()

    }

    private fun adapterLoadStateListening(loadState: CombinedLoadStates) {

        recyclerViewFooterProgress.isVisible = loadState.source.append is LoadState.Loading
        recyclerViewHeaderProgress.isVisible = loadState.source.prepend is LoadState.Loading

        recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading

        if(loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount<1){
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        }

        if(loadState.source.refresh is LoadState.Error){
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            emptyText.text = "An error occurred while loading media"
        }
    }

    private fun observeDownloads(){
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.downloadsMedia.collectLatest{pagingData->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeItemInfo(view : View){
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.downloadItemInfo.collectLatest { resultItemInfo ->
                    val card = view.findViewById<MaterialCardView>(R.id.download_progress_card)
                    if(resultItemInfo == null){
                        card.visibility = View.GONE
                    }else{
                        val thumbnail = view.findViewById<ImageView>(R.id.download_media_thumbnail)
                        val title = view.findViewById<TextView>(R.id.download_media_title)
                        title.text = resultItemInfo.title
                        Glide.with(requireContext())
                            .load(resultItemInfo.thumbnail)
                            .into(thumbnail)
                        card.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun observeDownloadProgress(){
        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.downloadProgress.collectLatest { progress->
                    downloadProgress.progress = progress
                    downloadTextProgress.text = "$progress%"
                }
            }
        }
    }
}