package com.bluetech.vidown.ui.downloadscreen

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.bluetech.vidown.R
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.db.entities.MediaWithThumbnail
import com.bluetech.vidown.core.pojoclasses.SelectItem
import com.bluetech.vidown.core.workers.DownloadFileWorker
import com.bluetech.vidown.ui.mainscreen.MainFragmentDirections
import com.bluetech.vidown.utils.calculateDownloadedSize
import com.bluetech.vidown.utils.calculateSize
import com.bluetech.vidown.utils.snackBar
import com.bluetech.vidown.utils.toggleVisibility
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@AndroidEntryPoint
class DownloadFragment : Fragment() {

    private lateinit var viewModel: DownloadViewModel
    private lateinit var adapter: DownloadsAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var emptyText: TextView
    private lateinit var recyclerViewFooterProgress: CircularProgressIndicator
    private lateinit var recyclerViewHeaderProgress: CircularProgressIndicator

    private lateinit var saveProgress: LinearProgressIndicator

    private lateinit var editActionSelect: MaterialButton
    private lateinit var editActionSort: MaterialButton
    private lateinit var editActionFavorite: MaterialButton
    private lateinit var editActionRemove: MaterialButton
    private lateinit var editActionCancel: MaterialButton

    private lateinit var showDownloadHistoryBtn: MaterialButton


    private var isSelectionEnabled = false
    private val selectedMedia = mutableListOf<SelectItem>()

    private lateinit var selectedItemsText: TextView

    private lateinit var rootView: View

    private lateinit var workManager: WorkManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_download, container, false)

        viewModel = ViewModelProvider(requireActivity())[DownloadViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view

        view.apply {
            recyclerView = findViewById(R.id.download_recycler_view)
            emptyText = findViewById(R.id.download_empty_text)
            recyclerViewFooterProgress = findViewById(R.id.download_progress_footer)
            recyclerViewHeaderProgress = findViewById(R.id.download_progress_header)

            saveProgress = findViewById(R.id.download_save_progress)
            showDownloadHistoryBtn = findViewById(R.id.download_history_btn)
        }

        showDownloadHistoryBtn.setOnClickListener {
            viewModel.updateSelectionState(false)
            Navigation.findNavController(requireActivity(), R.id.nav_host).navigate(
                MainFragmentDirections.downloadHistory()
            )
        }

        initializeEditActionsButtons()

        workManager = WorkManager.getInstance(requireContext())

        initializeAdapterForRecyclerView()
        observeDownloads()

        adapter.addLoadStateListener { loadState ->
            adapterLoadStateListening(loadState)
        }

        observeDownloadProgress()
        observeSelection()
        observeMediaStream()
    }

    private fun observeMediaStream() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allMediaStream.collect {
                    adapter.refresh()
                }
            }
        }
    }

    private fun initializeEditActionsButtons() {
        editActionSelect = rootView.findViewById(R.id.edit_btn_select)
        editActionSort = rootView.findViewById(R.id.edit_btn_sort)
        editActionFavorite = rootView.findViewById(R.id.edit_btn_favorite)
        editActionRemove = rootView.findViewById(R.id.edit_btn_remove)
        editActionCancel = rootView.findViewById(R.id.edit_btn_close)

        selectedItemsText = rootView.findViewById(R.id.selected_items_count)

        editActionSelect.setOnClickListener {
            viewModel.updateSelectionState(!isSelectionEnabled)
        }

        editActionCancel.setOnClickListener {
            viewModel.updateSelectionState(!isSelectionEnabled)
        }

        editActionRemove.setOnClickListener {
            showRemoveDialog()
        }

        editActionFavorite.setOnClickListener {
            viewModel.fetchArgs.onlyFavorites = !viewModel.fetchArgs.onlyFavorites
            if (viewModel.fetchArgs.onlyFavorites) {
                editActionFavorite.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_facorite_filled)
            } else {
                editActionFavorite.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_favorite)
            }
            adapter.refresh()
        }

        editActionSort.setOnClickListener {
            viewModel.fetchArgs.orderByNewest = !viewModel.fetchArgs.orderByNewest
            if (!viewModel.fetchArgs.orderByNewest) {
                editActionSort.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_sort_asc)
            } else {
                editActionSort.icon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_sort_desc)
            }
            adapter.refresh()
        }
    }

    private fun showRemoveDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Remove Media")
            .setMessage("Are you sure you want to remove media?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("YES") { dialog, _ ->
                dialog.dismiss()
                viewModel.removeMedia(selectedMedia, requireContext())
                viewModel.updateSelectionState(!isSelectionEnabled)
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun initializeAdapterForRecyclerView() {
        adapter = DownloadsAdapter(
            requireContext(),
            { mediaEntity, position ->
                if (isSelectionEnabled) {
                    selectMedia(mediaEntity, position)
                } else {
                    val action = MainFragmentDirections.displayMedia(mediaEntity)
                    Navigation.findNavController(requireActivity(), R.id.nav_host).navigate(action)
                }
            },
            { mediaEntity, position ->
                observeSaving()
                val action = MainFragmentDirections.editMediaAction(mediaEntity, position)
                Navigation.findNavController(requireActivity(), R.id.nav_host).navigate(action)
            },
            { mediaEntity, position ->
                viewModel.updateSelectionState(true)
                selectMedia(mediaEntity, position)
            })

        recyclerView.adapter = adapter
    }

    private fun selectMedia(mediaWithThumbnail: MediaWithThumbnail, position: Int) {
        val selectItem = SelectItem(mediaWithThumbnail, position)
        if (selectedMedia.contains(selectItem)) {
            selectedMedia.remove(selectItem)
        } else {
            selectedMedia.add(selectItem)
        }

        selectedItemsText.text =
            resources.getString(R.string.selected_items_count, selectedMedia.count())
        adapter.notifyItemChanged(position, selectedMedia.contains(selectItem))
    }

    private fun cancelSelection() {
        selectedMedia.forEach { selectedItem ->
            adapter.notifyItemChanged(selectedItem.position, false)
        }
        selectedMedia.clear()
    }

    private fun adapterLoadStateListening(loadState: CombinedLoadStates) {

        recyclerViewFooterProgress.isVisible = loadState.source.append is LoadState.Loading
        recyclerViewHeaderProgress.isVisible = loadState.source.prepend is LoadState.Loading

        recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
        emptyText.visibility = View.GONE

        if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        }

        if (loadState.source.refresh is LoadState.Error) {
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
            emptyText.text = getString(R.string.an_error_occurred_while_loading_media)
        }
    }

    private fun observeSaving() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveProgress.collect {
                    it.onSuccess { msg ->
                        if (msg.isNotEmpty())
                            requireView().snackBar(msg)
                    }
                    it.onFailure { ex ->
                        ex.message?.let { msg ->
                            requireView().snackBar(msg)
                        }
                    }
                }
            }
        }
    }

    private fun observeSelection() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectionState.collect {
                    if (it) {
                        isSelectionEnabled = true
                        editActionSelect.toggleVisibility()
                        editActionCancel.toggleVisibility()

                        editActionFavorite.isEnabled = false
                        editActionSort.isEnabled = false
                        editActionRemove.isEnabled = true
                    } else {
                        if (!isSelectionEnabled)
                            return@collect
                        isSelectionEnabled = false
                        editActionSelect.toggleVisibility()
                        editActionCancel.toggleVisibility()
                        selectedItemsText.text = ""
                        cancelSelection()


                        editActionFavorite.isEnabled = true
                        editActionSort.isEnabled = true
                        editActionRemove.isEnabled = false
                    }
                }
            }
        }
    }

    private fun observeDownloads() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadsMedia.collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun observeDownloadProgress() {
        lifecycleScope.launch(Dispatchers.IO){
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.downloadInProgress.collect { inProgressDownload ->
                    withContext(Dispatchers.Main){
                        if(inProgressDownload.isNotEmpty()){
                            updateDownloadProgress(inProgressDownload.first())
                        }else{
                            updateDownloadEnd()
                        }
                    }
                }
            }
        }
    }

    private fun updateDownloadEnd() {
        val downloadProgress =
            rootView.findViewById<CircularProgressIndicator>(R.id.download_circle_progress)

        downloadProgress.visibility = View.INVISIBLE
    }

    private fun updateDownloadProgress(downloadHistoryWithExtras: DownloadHistoryWithExtras) {
        val downloadProgress =
            rootView.findViewById<CircularProgressIndicator>(R.id.download_circle_progress)
        downloadProgress.visibility = View.VISIBLE

        val size = downloadHistoryWithExtras.calculateSize()
        val downloadedSize = downloadHistoryWithExtras.calculateDownloadedSize()

        if (size > 0) {
            val progress = (downloadedSize * 100f / size).roundToInt()
            downloadProgress.isIndeterminate = false

            downloadProgress.progress = progress
            return
        }

        downloadProgress.isIndeterminate = true
    }

}