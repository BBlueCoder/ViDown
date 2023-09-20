package com.bluetech.vidown.ui.downloadscreen

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.entities.MediaWithThumbnail
import com.bluetech.vidown.utils.formatDurationToReadableFormat
import com.bluetech.vidown.utils.toggleVisibility
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import java.io.File


sealed class DownloadsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(
        mediaWithThumbnail: MediaWithThumbnail,
        context: Context,
        position: Int,
        itemClickListener: ((mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit),
        editClickListener: ((mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit),
        longClickListener: ((mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit)
    )

    abstract fun renameItem(title: String)

    abstract fun toggleItemSelection(isItemSelected: Boolean)

    class ImageMediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(
            mediaWithThumbnail: MediaWithThumbnail,
            context: Context,
            position: Int,
            itemClickListener: (mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit,
            editClickListener: (mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit,
            longClickListener: (mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit
        ) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_image_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_image_title)
            val cardView = itemView.findViewById<MaterialCardView>(R.id.media_image_card_view)

            cardView.strokeWidth = 0

            val editDots = itemView.findViewById<ImageView>(R.id.media_image_edit)

            title.text = mediaWithThumbnail.mediaEntity.title

            val file = File(context.filesDir, mediaWithThumbnail.mediaEntity.savedName)
            if (file.exists()) {

                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaWithThumbnail, position)
                }

                thumbnail.setOnLongClickListener {
                    longClickListener.invoke(mediaWithThumbnail, position)
                    true
                }

                editDots.setOnClickListener {
                    editClickListener.invoke(mediaWithThumbnail, position)
                }

                Glide.with(context)
                    .load(Uri.fromFile(file))
                    .error(R.drawable.ic_video_corrupted)
                    .into(thumbnail)

            } else {
                Glide.with(context)
                    .load(R.drawable.ic_video_corrupted)
                    .into(thumbnail)
            }
        }

        override fun renameItem(title: String) {
            val titleTxt = itemView.findViewById<TextView>(R.id.media_image_title)
            titleTxt.text = title
        }

        override fun toggleItemSelection(isItemSelected: Boolean) {
            val cardView = itemView.findViewById<MaterialCardView>(R.id.media_image_card_view)
            val selectedOverlay = itemView.findViewById<View>(R.id.media_image_selected_overlay)
            if (isItemSelected) {
                cardView.strokeWidth = 3
                selectedOverlay.visibility = View.VISIBLE
            } else {
                cardView.strokeWidth = 0
                selectedOverlay.visibility = View.GONE
            }
        }
    }

    class MediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(
            mediaWithThumbnail: MediaWithThumbnail,
            context: Context,
            position: Int,
            itemClickListener: (mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit,
            editClickListener: (mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit,
            longClickListener: (mediaWithThumbnail: MediaWithThumbnail, position: Int) -> Unit
        ) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_title)
            val durationText = itemView.findViewById<TextView>(R.id.media_duration)
            val audioIcon = itemView.findViewById<ImageView>(R.id.media_audio_icon)

            if(mediaWithThumbnail.mediaEntity.mediaType != MediaType.Audio)
                audioIcon.toggleVisibility()

            val editDots = itemView.findViewById<ImageView>(R.id.media_more)

            mediaWithThumbnail.mediaThumbnail.thumbnailSavedName.let {
                val file = File(context.filesDir, it)
                if (file.exists()) {
                    Glide.with(context)
                        .load(Uri.fromFile(file))
                        .into(thumbnail)
                }
            }

            title.text = mediaWithThumbnail.mediaEntity.title
            durationText.text = mediaWithThumbnail.mediaEntity.duration.formatDurationToReadableFormat()
            val file = File(context.filesDir, mediaWithThumbnail.mediaEntity.savedName)

            if (file.exists()) {

                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaWithThumbnail, position)
                }

                thumbnail.setOnLongClickListener {
                    longClickListener.invoke(mediaWithThumbnail, position)
                    true
                }

                editDots.setOnClickListener {
                    editClickListener.invoke(mediaWithThumbnail, position)
                }

            } else {
                Glide.with(context)
                    .load(R.drawable.ic_video_corrupted)
                    .into(thumbnail)
            }


        }

        override fun renameItem(title: String) {
            val titleTxt = itemView.findViewById<TextView>(R.id.media_title)
            titleTxt.text = title
        }

        override fun toggleItemSelection(isItemSelected: Boolean) {
            val cardView = itemView.findViewById<MaterialCardView>(R.id.media_card_view)
            val selectedOverlay = itemView.findViewById<View>(R.id.media_selected_overlay)
            if (isItemSelected) {
                cardView.strokeWidth = 3
                selectedOverlay.visibility = View.VISIBLE
            } else {
                cardView.strokeWidth = 0
                selectedOverlay.visibility = View.GONE
            }
        }
    }

}