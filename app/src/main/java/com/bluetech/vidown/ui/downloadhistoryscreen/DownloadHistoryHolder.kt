package com.bluetech.vidown.ui.downloadhistoryscreen

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.db.entities.DownloadStatus
import com.bluetech.vidown.utils.calculateDownloadedSize
import com.bluetech.vidown.utils.calculateSize
import com.bluetech.vidown.utils.formatSizeToReadableFormat
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

sealed class DownloadHistoryHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(
        downloadHistoryWithExtras: DownloadHistoryWithExtras,
        context : Context
    )

    fun bindTitleAndThumbnail(context: Context,downloadHistoryWithExtras: DownloadHistoryWithExtras,titleView: TextView,thumbnailView : ImageView){
        titleView.text = downloadHistoryWithExtras.downloadHistoryEntity.title

        val thumbnail = downloadHistoryWithExtras.downloadHistoryItemExtras.find { it.mediaType == MediaType.Image }
        thumbnail?.let {
            if(downloadHistoryWithExtras.downloadHistoryEntity.downloadData.downloadStatus != DownloadStatus.COMPLETED){
                Glide.with(context)
                    .load(thumbnail.downloadData.downloadUrl)
                    .into(thumbnailView)
                return
            }
            val thumbnailSavedName = thumbnail.savedName
            val file = File(context.filesDir,thumbnailSavedName)
            Glide.with(context)
                .load(Uri.fromFile(file))
                .error(R.drawable.ic_video_corrupted)
                .into(thumbnailView)
        }
    }

    class DownloadHistoryItem(
        itemView: View,
        private val removeAction : (downloadHistoryWithExtras : DownloadHistoryWithExtras)-> Unit
    ): DownloadHistoryHolder(itemView){
        override fun bind(
            downloadHistoryWithExtras: DownloadHistoryWithExtras,
            context: Context
        ){
            val thumbnailView = itemView.findViewById<ImageView>(R.id.download_history_thumbnail)
            val titleView = itemView.findViewById<TextView>(R.id.download_history_title)
            val dateView = itemView.findViewById<TextView>(R.id.download_history_date)
            val stateView = itemView.findViewById<TextView>(R.id.download_history_state)
            val removeBtn = itemView.findViewById<Button>(R.id.download_history_remove_btn)

            removeBtn.setOnClickListener {
                removeAction.invoke(downloadHistoryWithExtras)
            }

            dateView.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(downloadHistoryWithExtras.downloadHistoryEntity.date))
            stateView.text = downloadHistoryWithExtras.downloadHistoryEntity.downloadData.downloadStatus.name

            bindTitleAndThumbnail(context,downloadHistoryWithExtras,titleView,thumbnailView)


        }
    }

    class DownloadHistoryItemDownloading(
        itemView: View,
        private val cancelAction : (downloadHistoryWithExtras : DownloadHistoryWithExtras)-> Unit
    ) : DownloadHistoryHolder(itemView){
        override fun bind(
            downloadHistoryWithExtras: DownloadHistoryWithExtras,
            context: Context
        ) {
            val thumbnailView =
                itemView.findViewById<ImageView>(R.id.download_history_thumbnail_downloading)
            val titleView = itemView.findViewById<TextView>(R.id.download_history_downloading_title)
            val downloadingSize =
                itemView.findViewById<TextView>(R.id.download_history_downloading_size)
            val progressView = itemView.findViewById<TextView>(R.id.download_history_progress_text)
            val progressIndicator =
                itemView.findViewById<LinearProgressIndicator>(R.id.download_history_progress)
            val cancelBtn =
                itemView.findViewById<ImageView>(R.id.download_history_close_downloading)

            cancelBtn.setOnClickListener {
                cancelAction.invoke(downloadHistoryWithExtras)
            }

            bindTitleAndThumbnail(context, downloadHistoryWithExtras, titleView, thumbnailView)

            val size = downloadHistoryWithExtras.calculateSize()
            val downloadedSize = downloadHistoryWithExtras.calculateDownloadedSize()
            if (size > 0) {
                progressIndicator.isIndeterminate = false
                downloadingSize.text = context.getString(
                    R.string.downloading_size_text,
                    downloadedSize.formatSizeToReadableFormat(),
                    size.formatSizeToReadableFormat()
                )
                val progress = (downloadedSize * 100f / size).roundToInt()
                progressView.text = "$progress"
                progressIndicator.progress = progress
            } else {
                progressIndicator.isIndeterminate = true
                downloadingSize.text = "${downloadedSize.formatSizeToReadableFormat()}"
            }
        }

    }
}