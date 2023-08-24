package com.bluetech.vidown.ui.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.pojoclasses.SelectItem
import com.bluetech.vidown.core.services.DownloadFileService
import com.bluetech.vidown.core.workers.DownloadFileWorker
import com.bluetech.vidown.ui.recyclerviews.DownloadsAdapter
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bluetech.vidown.utils.CustomWorkerFactory
import com.bluetech.vidown.utils.formatSizeToReadableFormat
import com.bluetech.vidown.utils.snackBar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadFragment : Fragment() {

    private lateinit var viewModel: DownloadViewModel
    private lateinit var adapter: DownloadsAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var emptyText: TextView
    private lateinit var recyclerViewFooterProgress: CircularProgressIndicator
    private lateinit var recyclerViewHeaderProgress: CircularProgressIndicator

    private lateinit var downloadProgress: LinearProgressIndicator
    private lateinit var downloadTextProgress: TextView
    private lateinit var downloadSizeProgress: TextView

    private lateinit var saveProgress: LinearProgressIndicator

    private var isDownloadServiceBound = false

    private lateinit var downloadFileService: DownloadFileService

    private lateinit var selectBtn: MaterialButton
    private lateinit var cancelSelectionBtn: MaterialButton
    private lateinit var editImageView: ImageView


    private var selectionFlow = MutableStateFlow(false)
    private var isSelectionEnabled = false
    private val selectedMedia = mutableListOf<SelectItem>()

    private lateinit var selectedItemsText: TextView

    private lateinit var rootView: View

    private lateinit var workManager: WorkManager

    private var mediaTitle = ""

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
            downloadProgress = findViewById(R.id.download_media_progress)
            downloadTextProgress = findViewById(R.id.download_media_progress_text)
            downloadSizeProgress = findViewById(R.id.download_media_size)
            selectedItemsText = findViewById(R.id.download_select_text)

            saveProgress = findViewById(R.id.download_save_progress)

            selectBtn = findViewById(R.id.download_select_btn)
            cancelSelectionBtn = findViewById(R.id.download_cancel_btn)
            editImageView = findViewById(R.id.download_edits)
        }

        val cancelBtn = view.findViewById<ImageView>(R.id.download_media_cancel)

        workManager = WorkManager.getInstance(requireContext())

        selectBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                selectionFlow.emit(true)
            }
        }

        cancelSelectionBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                selectionFlow.emit(false)
            }
        }

        cancelBtn.setOnClickListener {
            workManager.cancelAllWork()
        }

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
                lifecycleScope.launch(Dispatchers.IO) {
                    selectionFlow.emit(true)
                }
                selectMedia(mediaEntity, position)
            })

        recyclerView.adapter = adapter
        observeDownloads()

        adapter.addLoadStateListener { loadState ->
            adapterLoadStateListening(loadState)
        }

        editImageView.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.popup_download_edit, popupMenu.menu)
            val orderMenuItem = popupMenu.menu.findItem(R.id.popup_edit_order_by)
            val favoritesMenuItem = popupMenu.menu.findItem(R.id.popup_edit_display_favorites)

            if (viewModel.fetchArgs.orderByNewest)
                orderMenuItem.title = "Order by oldest"
            else
                orderMenuItem.title = "Order by newest"

            if (viewModel.fetchArgs.onlyFavorites)
                favoritesMenuItem.title = "Show all"
            else
                favoritesMenuItem.title = "Show only favorites"

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.popup_edit_order_by -> {
                        viewModel.fetchArgs.orderByNewest = !viewModel.fetchArgs.orderByNewest
                        adapter.refresh()
                    }

                    R.id.popup_edit_display_favorites -> {
                        viewModel.fetchArgs.onlyFavorites = !viewModel.fetchArgs.onlyFavorites
                        adapter.refresh()
                    }
                }
                true
            }
            popupMenu.show()
        }

        observeDownloadProgress(view)
        observeRemovingMedia()
        observeRenamingMedia()
        observeSelection()


    }

    private fun selectMedia(mediaEntity: MediaEntity, position: Int) {
        val selectItem = SelectItem(mediaEntity, position)
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
            emptyText.text = "An error occurred while loading media"
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
                selectionFlow.collect {
                    if (it) {
                        isSelectionEnabled = true
                        selectBtn.visibility = View.GONE
                        editImageView.visibility = View.INVISIBLE
                        cancelSelectionBtn.visibility = View.VISIBLE
                        selectedItemsText.visibility = View.VISIBLE
                        selectedItemsText.text = resources.getString(
                            R.string.selected_items_count,
                            selectedMedia.count()
                        )
                    } else {
                        isSelectionEnabled = false
                        cancelSelection()
                        cancelSelectionBtn.visibility = View.GONE
                        editImageView.visibility = View.VISIBLE
                        selectBtn.visibility = View.VISIBLE
                        selectedItemsText.visibility = View.GONE
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

    private fun observeDownloadProgress(view: View) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentWorkId.collect { uuid ->

                    if(uuid == null){
                        updateDownloadEnd(view)
                    }

                    uuid?.let { id ->
                        workManager.getWorkInfoByIdLiveData(id)
                            .observe(viewLifecycleOwner) { workInfo ->
                                if(workInfo?.state == WorkInfo.State.SUCCEEDED || workInfo?.state == WorkInfo.State.FAILED){
                                    viewModel.updateRequestUUID(null)
                                }
                                workInfo?.let {
                                    updateDownloadedMediaInfo(view, workInfo)
                                    updateDownloadProgress(workInfo)
                                }
                            }
                    }
                }
            }
        }
    }

    private fun updateDownloadEnd(view: View){
        val card = view.findViewById<MaterialCardView>(R.id.download_progress_card)

        card.visibility = View.GONE
        adapter.refresh()
    }

    private fun updateDownloadedMediaInfo(view: View,workInfo: WorkInfo){
        val mediaTitle = workInfo.progress.getString(DownloadFileWorker.PARAMS_MEDIA_TITLE)
        val mediaThumbnail = workInfo.progress.getString(DownloadFileWorker.PARAMS_MEDIA_THUMBNAIL)

        if(mediaTitle == null || mediaThumbnail == null)
            return

        val thumbnail = view.findViewById<ImageView>(R.id.download_media_thumbnail)
        val title = view.findViewById<TextView>(R.id.download_media_title)
        val card = view.findViewById<MaterialCardView>(R.id.download_progress_card)

        title.text = mediaTitle
        Glide.with(requireContext())
            .load(mediaThumbnail)
            .into(thumbnail)
        card.visibility = View.VISIBLE
    }
    private fun updateDownloadProgress(workInfo: WorkInfo) {
        val progress = workInfo.progress.getInt(DownloadFileWorker.KEY_DOWNLOAD_PROGRESS, -1)
        val fileSize = workInfo.progress.getLong(DownloadFileWorker.KEY_FILE_SIZE_IN_BYTES, 0)
        val downloadedSize =
            workInfo.progress.getLong(DownloadFileWorker.KEY_DOWNLOADED_SIZE_IN_BYTES, 0)

        if (progress != -1) {
            downloadProgress.isIndeterminate = false
            downloadSizeProgress.text =
                "${downloadedSize.formatSizeToReadableFormat()}/${fileSize.formatSizeToReadableFormat()}"

            downloadProgress.progress = progress
            downloadTextProgress.text = "$progress%"

            return
        }

        downloadProgress.isIndeterminate = true
        downloadTextProgress.text = ""
        downloadSizeProgress.text = downloadedSize.formatSizeToReadableFormat()

    }

    private fun bindToDownloadService() {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                println("---------------------- bind to service")
                isDownloadServiceBound = true
                downloadFileService =
                    (service as DownloadFileService.DownloadFileServiceBinder).service
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                println("---------------------- unbind to service")
                isDownloadServiceBound = false
            }

        }

        Intent(requireContext(), DownloadFileService::class.java).also {
            requireContext().bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun observeRemovingMedia() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.removeMediaStateFlow.collect { result ->
                    result.onSuccess {
                        if (it != null)
                            adapter.refresh()
                    }
                }
            }
        }
    }

    private fun observeRenamingMedia() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.renameMediaStateFlow.collect { result ->
                    result.onSuccess { itemPayload ->
                        if (itemPayload != null) {
                            adapter.notifyItemChanged(itemPayload.position, itemPayload.title)
                        }
                    }
                    result.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }
    }
}