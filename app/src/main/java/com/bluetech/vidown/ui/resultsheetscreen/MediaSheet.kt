package com.bluetech.vidown.ui.resultsheetscreen

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.data.db.entities.MediaType
import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import com.bluetech.vidown.ui.downloadscreen.DownloadViewModel
import com.bluetech.vidown.ui.mainscreen.MainViewModel
import com.bluetech.vidown.utils.Constants.YOUTUBE
import com.bluetech.vidown.utils.snackBar
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var downloadViewModel: DownloadViewModel

    private lateinit var adapter: ResultsAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private var itemInfo: ResultItem.ItemInfo? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_media_sheet, container, false)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        downloadViewModel = ViewModelProvider(requireActivity())[DownloadViewModel::class.java]



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from((view.parent as View))
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val mediaSheetLayout = view.findViewById<RelativeLayout>(R.id.media_sheet_layout)
        mediaSheetLayout.minimumHeight = Resources.getSystem().displayMetrics.heightPixels - 180

        recyclerView = view.findViewById(R.id.media_recycler_view)
        adapter = ResultsAdapter(mutableListOf()) { itemData ->
            if (itemData.url != "")
                sendRequestToDownloadWorker(itemData)
        }

        setupRecyclerView()
        observeSearchResults(view)

    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun observeSearchResults(view: View) {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lookUpResults.collect { result ->
                    result.onFailure { exp ->
                        view.snackBar(
                            exp.message!!
                        )
                    }
                    result.onSuccess {
                        if (it.isEmpty())
                            return@onSuccess
                        itemInfo = it.filterIsInstance<ResultItem.ItemInfo>().first()
                        val mediaThumbnail = view.findViewById<ImageView>(R.id.media_thumbnail)
                        val mediaTitle = view.findViewById<TextView>(R.id.media_title)
                        mediaTitle.text = itemInfo!!.title

                        Glide
                            .with(requireContext())
                            .load(itemInfo!!.thumbnail)
                            .into(mediaThumbnail)
                        adapter.submitList(it.filter { item -> item !is ResultItem.ItemInfo })
                    }
                }
            }
        }

    }

    private fun sendRequestToDownloadWorker(itemData: ResultItem.ItemData) {
        Toast.makeText(requireContext(), "Downloading started...", Toast.LENGTH_SHORT).show()

        viewModel.addNewDownload(itemData,itemInfo!!,getAudioForYoutubeVideo(itemData))

        viewModel.startDownloadWorker()

        dismiss()
    }

    private fun getAudioForYoutubeVideo(itemData: ResultItem.ItemData): String? {
        if (itemInfo!!.platform == YOUTUBE && itemData.hasAudio == false) {
            adapter.getListData().filterIsInstance<ResultItem.ItemData>()
                .filter { item -> item.format == MediaType.Audio }.forEach { item ->
                return item.url
            }
        }
        return null
    }

}