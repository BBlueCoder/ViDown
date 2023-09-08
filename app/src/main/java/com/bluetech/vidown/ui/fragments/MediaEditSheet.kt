package com.bluetech.vidown.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.pojoclasses.DownloadItemPayload
import com.bluetech.vidown.ui.vm.DownloadViewModel
import com.bluetech.vidown.ui.vm.MainViewModel
import com.bluetech.vidown.utils.hideKeyboard
import com.bluetech.vidown.utils.showPermissionRequestExplanation
import com.bluetech.vidown.utils.snackBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MediaEditSheet : BottomSheetDialogFragment() {

    private val args: MediaEditSheetArgs by navArgs()

    private lateinit var currentMedia: MediaEntity

    private lateinit var mainViewModel: MainViewModel
    private lateinit var downloadViewModel: DownloadViewModel

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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


        requestPermissionLauncher = registerForPermission()

        view.apply {
            val favoriteBtn = findViewById<Button>(R.id.media_edit_favorite_btn)
            val favoriteIcon = findViewById<ImageView>(R.id.media_edit_favorite_ic)
            val saveBtn = findViewById<Button>(R.id.media_edit_save_btn)
            val renameBtn = findViewById<Button>(R.id.media_edit_rename_btn)
            val deleteBtn = findViewById<Button>(R.id.media_edit_delete_btn)

            toggleFavoriteBtnAndIcon(favoriteBtn, favoriteIcon)
            favoriteBtn.setOnClickListener {
                downloadViewModel.updateMediaFavorite(currentMedia.uid, !currentMedia.favorite)
                currentMedia.favorite = !currentMedia.favorite
                toggleFavoriteBtnAndIcon(favoriteBtn, favoriteIcon)
                dismiss()
            }

            saveBtn.setOnClickListener {
                try {
                    saveMediaToDevice()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    downloadViewModel.updateSaveProgress(Result.failure(ex))
                }
            }

            renameBtn.setOnClickListener {
                renameMedia()
            }

            deleteBtn.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                    .setTitle("Remove Media")
                    .setMessage("Are you sure you want to remove media?")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("YES") { dialog, _ ->
                        dialog.dismiss()
                        downloadViewModel.removeMedia(currentMedia, requireContext(),args.position)
                        dismiss()
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

    private fun renameMedia() {
        val inflater = LayoutInflater.from(requireContext())
        val alertDialogView = inflater.inflate(R.layout.rename_media_alert_dialog_layout, null)

        val renameEditText = alertDialogView.findViewById<EditText>(R.id.rename_media_edit_text)

        renameEditText.setText(currentMedia.title)

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Rename Media")
            .setView(alertDialogView)
            .setNegativeButton("CANCEL") { dialog, _ ->
                requireActivity().hideKeyboard(alertDialogView)
                dialog.dismiss()
            }
            .setPositiveButton("OK") { dialog, _ ->
                if (renameEditText.text.isEmpty() && renameEditText.text.toString() == currentMedia.title)
                    return@setPositiveButton

                val newTitle = renameEditText.text.toString()
                downloadViewModel.renameMedia(
                    currentMedia.uid, newTitle,
                    DownloadItemPayload(args.position, newTitle)
                )
                requireActivity().hideKeyboard(alertDialogView)
                dialog.dismiss()
                dismiss()
            }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun saveMediaToDevice() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !isAppHaveWriteExternalStoragePermission()) {
            requestWriteStoragePermission()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            when (currentMedia.mediaType) {
                MediaType.Video -> saveVideoToDevice()
                MediaType.Audio -> saveAudioToDevice()
                MediaType.Image -> saveImageToDevice()
            }
        }

    }

    private fun saveVideoToDevice() {
        val name = "ViDown_${currentMedia.name}"

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.DIRECTORY_DCIM
        } else {
            val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath)
            if (!file.exists())
                file.mkdir()
            File(file, name).absolutePath
        }

        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.MediaColumns.DATA
        }

        var outputStream: OutputStream?
        val inputStream = File(requireContext().filesDir, name).inputStream()

        requireContext().contentResolver.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, currentMedia.title)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/*")
                put(pathColumn, path)
            }
            val uri = resolver.insert(
                getUrl(),
                contentValues
            )

            outputStream = uri?.let {
                resolver.openOutputStream(it)
            }
        }

        outputStream?.use {
            inputStream.copyTo(it)
        }

        downloadViewModel.updateSaveProgress(Result.success("Video saved!"))
        dismiss()
    }

    private fun saveAudioToDevice() {
        val name = "ViDown_${currentMedia.name}"

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.DIRECTORY_MUSIC
        } else {
            val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath)
            if (!file.exists())
                file.mkdir()
            File(file, currentMedia.name).absolutePath
        }

        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.MediaColumns.DATA
        }

        var outputStream: OutputStream?
        val inputStream = File(requireContext().filesDir, currentMedia.name).inputStream()

        requireContext().contentResolver.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, currentMedia.title)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/*")
                put(pathColumn, path)
            }
            val uri = resolver.insert(
                getUrl(),
                contentValues
            )

            outputStream = uri?.let {
                resolver.openOutputStream(it)
            }
        }

        outputStream?.use {
            inputStream.copyTo(it)
        }

        downloadViewModel.updateSaveProgress(Result.success("Audio saved!"))
        dismiss()
    }

    private fun saveImageToDevice() {
        val name = "ViDown_${currentMedia.name}"

        val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.DIRECTORY_PICTURES
        } else {
            val file =
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath)
            if (!file.exists())
                file.mkdir()
            File(file, currentMedia.name).absolutePath
        }

        val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            MediaStore.MediaColumns.DATA
        }

        var outputStream: OutputStream?
        val inputStream = File(requireContext().filesDir, currentMedia.name).inputStream()

        requireContext().contentResolver.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, currentMedia.title)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/*")
                put(pathColumn, path)
            }
            val uri = resolver.insert(
                getUrl(),
                contentValues
            )

            outputStream = uri?.let {
                resolver.openOutputStream(it)
            }
        }

        outputStream?.use {
            inputStream.copyTo(it)
        }

        downloadViewModel.updateSaveProgress(Result.success("Image saved!"))
        dismiss()
    }

    private fun getUrl(): Uri {
        return when (currentMedia.mediaType) {
            MediaType.Video -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            }
            MediaType.Audio -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
            }
            MediaType.Image -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
            }
        }
    }


    private fun isAppHaveWriteExternalStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun registerForPermission(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                saveMediaToDevice()
            } else {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    val sharedPreferences = requireContext().getSharedPreferences(
                        getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE
                    )
                    with(sharedPreferences.edit()){
                        putBoolean(getString(R.string.write_permission_key),true)
                        apply()
                    }
                }
                downloadViewModel.updateSaveProgress(Result.failure(Exception("Permission Denied, failed to save media")))
                dismiss()
            }
        }
    }

    private fun requestWriteStoragePermission() {
        val sharedPreferences = requireContext().getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

        val isPermissionDeniedForGood = sharedPreferences.getBoolean(getString(R.string.write_permission_key),false)
        if(isPermissionDeniedForGood){
            requireContext().showPermissionRequestExplanation(
                getString(R.string.storage_permission_dialog_title),
                getString(R.string.storage_permission_denied_exp)
            ){
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",context?.packageName,null)
                intent.data = uri
                startActivity(intent)
            }
            return
        }

        if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requireContext().showPermissionRequestExplanation(
                getString(R.string.storage_permission_dialog_title),
                getString(R.string.storage_permission_exp)
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            return
        }
        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}
