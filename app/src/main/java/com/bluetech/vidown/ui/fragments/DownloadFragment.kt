package com.bluetech.vidown.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.ui.activities.MainActivity
import com.bluetech.vidown.ui.recyclerviews.DownloadsAdapter
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bluetech.vidown.utils.snackBar
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadFragment : Fragment() {

    private lateinit var viewModel : DownloadViewModel
    private lateinit var adapter : DownloadsAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_download, container, false)

        viewModel = ViewModelProvider(requireActivity())[DownloadViewModel::class.java]



        recyclerView = view.findViewById(R.id.download_recycler_view)
        adapter = DownloadsAdapter(emptyList()){mediaEntity ->
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
        observeDownloads(view)


        return view
    }

    private fun observeDownloads(view : View){
        lifecycleScope.launch(Dispatchers.Main){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.downloadMedia.collect{results->
                    val progress = view.findViewById<CircularProgressIndicator>(R.id.download_progress)
                    progress.visibility = View.GONE
                    results.onFailure {
                        view.snackBar("An Error occurred, can not load downloads files")
                    }
                    results.onSuccess {
                        adapter.downloadsList = it
                        adapter.notifyDataSetChanged()
                        if(it.isEmpty()){
                            val emptyText = view.findViewById<TextView>(R.id.download_empty_text)
                            emptyText.visibility = View.VISIBLE
                        }else{
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}