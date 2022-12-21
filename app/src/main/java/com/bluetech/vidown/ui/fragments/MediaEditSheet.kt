package com.bluetech.vidown.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bluetech.vidown.R
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.pojoclasses.DownloadItemPayload
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bluetech.vidown.ui.vm.MainViewModel
import com.bluetech.vidown.utils.snackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaEditSheet : BottomSheetDialogFragment() {

    private val args: MediaEditSheetArgs by navArgs()

    private lateinit var currentMedia: MediaEntity

    private lateinit var mainViewModel: MainViewModel
    private lateinit var downloadViewModel: DownloadViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        downloadViewModel = ViewModelProvider(requireActivity())[DownloadViewModel::class.java]

        return inflater.inflate(R.layout.fragment_media_edit_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentMedia = args.mediaEntity

        view.apply {
            val favoriteBtn = findViewById<Button>(R.id.media_edit_favorite_btn)
            val favoriteIcon = findViewById<ImageView>(R.id.media_edit_favorite_ic)
            val renameBtn = findViewById<Button>(R.id.media_edit_rename_btn)
            val deleteBtn = findViewById<Button>(R.id.media_edit_delete_btn)

            toggleFavoriteBtnAndIcon(favoriteBtn, favoriteIcon)
            favoriteBtn.setOnClickListener {
                downloadViewModel.updateMediaFavorite(currentMedia.uid, !currentMedia.favorite)
                currentMedia.favorite = !currentMedia.favorite
                toggleFavoriteBtnAndIcon(favoriteBtn, favoriteIcon)
            }

            renameBtn.setOnClickListener {
                renameMedia()
            }

            deleteBtn.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                    .setTitle("Remove Media")
                    .setMessage("Are you sure you want to remove the media")
                    .setNegativeButton("Cancel"){ dialog,_->
                        dialog.dismiss()
                    }
                    .setPositiveButton("YES"){dialog,_->
                        observeRemovingMedia(this)
                        downloadViewModel.removeMedia(currentMedia,requireContext())
                        dialog.dismiss()
                    }
                val dialog = dialogBuilder.create()
                dialog.show()
            }
        }
    }

    private fun toggleFavoriteBtnAndIcon(favoriteBtn: Button, favoriteIcon: ImageView) {
        if (currentMedia.favorite) {
            favoriteBtn.text =
                resources.getText(R.string.remove_from_favorite_media_edit_sheet_btn_text)
            favoriteIcon.setImageResource(R.drawable.ic_favorite_purple)
        } else {
            favoriteBtn.text = resources.getText(R.string.add_to_favorite_media_edit_sheet_btn)
            favoriteIcon.setImageResource(R.drawable.ic_favorite_gray)
        }
    }

    private fun observeRemovingMedia(view : View) {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                downloadViewModel.removeMediaStateFlow.collect{ result ->
                    result.onSuccess {
                        if(it != null){
                            view.snackBar("Media removed")
                            dismiss()
                        }
                    }
                    result.onFailure {
                        view.snackBar("Could not remove this media")
                    }
                }
            }
        }
    }

    private fun renameMedia(){
        val inflater = LayoutInflater.from(requireContext())
        val alertDialogView = inflater.inflate(R.layout.rename_media_alert_dialog_layout,null)

        val renameEditText = alertDialogView.findViewById<EditText>(R.id.rename_media_edit_text)

        renameEditText.setText(currentMedia.title)

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Rename Media")
            .setView(alertDialogView)
            .setNegativeButton("CANCEL"){dialog,_->
                dialog.dismiss()
            }
            .setPositiveButton("OK"){dialog,_->
                if(renameEditText.text.isEmpty() && renameEditText.text.toString() == currentMedia.title)
                    return@setPositiveButton

                val newTitle = renameEditText.text.toString()
                downloadViewModel.renameMedia(currentMedia.uid,newTitle,
                    DownloadItemPayload(args.position,newTitle)
                )
                dialog.dismiss()
                dismiss()
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}

