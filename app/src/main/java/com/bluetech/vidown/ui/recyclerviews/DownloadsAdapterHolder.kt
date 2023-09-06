package com.bluetech.vidown.ui.recyclerviews

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.utils.formatDurationToReadableFormat
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.util.*


sealed class DownloadsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(
        mediaEntity: MediaEntity,
        context: Context,
        position: Int,
        itemClickListener: ((mediaEntity: MediaEntity, position: Int) -> Unit),
        editClickListener: ((mediaEntity: MediaEntity, position: Int) -> Unit),
        longClickListener: ((mediaEntity: MediaEntity, position: Int) -> Unit)
    )

    abstract fun renameItem(title: String)

    abstract fun toggleItemSelection(isItemSelected: Boolean)

    class VideoMediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(
            mediaEntity: MediaEntity,
            context: Context,
            position: Int,
            itemClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit,
            editClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit,
            longClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit
        ) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_video_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_video_title)
            val durationText = itemView.findViewById<TextView>(R.id.media_video_duration)
            val editDots = itemView.findViewById<ImageView>(R.id.media_video_edit)

            val file = File(context.filesDir, mediaEntity.name)

            title.text = mediaEntity.title
            durationText.text = mediaEntity.duration.formatDurationToReadableFormat()

            if (file.exists()) {

                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaEntity, position)
                }

                thumbnail.setOnLongClickListener {
                    longClickListener.invoke(mediaEntity, position)
                    true
                }

                editDots.setOnClickListener {
                    editClickListener.invoke(mediaEntity, position)
                }

                Glide.with(context)
                    .asBitmap()
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
            val titleTxt = itemView.findViewById<TextView>(R.id.media_video_title)
            titleTxt.text = title
        }

        override fun toggleItemSelection(isItemSelected: Boolean) {
            val cardView = itemView.findViewById<MaterialCardView>(R.id.media_video_card_view)
            val selectedOverlay = itemView.findViewById<View>(R.id.media_video_selected_overlay)
            if (isItemSelected) {
                cardView.strokeWidth = 3
                selectedOverlay.visibility = View.VISIBLE
            } else {
                cardView.strokeWidth = 0
                selectedOverlay.visibility = View.GONE
            }
        }
    }

    class ImageMediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(
            mediaEntity: MediaEntity,
            context: Context,
            position: Int,
            itemClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit,
            editClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit,
            longClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit
        ) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_image_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_image_title)
            val cardView = itemView.findViewById<MaterialCardView>(R.id.media_image_card_view)

            cardView.strokeWidth = 0

            val editDots = itemView.findViewById<ImageView>(R.id.media_image_edit)

            title.text = mediaEntity.title

            val file = File(context.filesDir, mediaEntity.name)
            if (file.exists() && !mediaEntity.isMediaCorrupted) {

                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaEntity, position)
                }

                thumbnail.setOnLongClickListener {
                    longClickListener.invoke(mediaEntity, position)
                    true
                }

                editDots.setOnClickListener {
                    editClickListener.invoke(mediaEntity, position)
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
            mediaEntity: MediaEntity,
            context: Context,
            position: Int,
            itemClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit,
            editClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit,
            longClickListener: (mediaEntity: MediaEntity, position: Int) -> Unit
        ) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_title)
            val durationText = itemView.findViewById<TextView>(R.id.media_duration)

            val editDots = itemView.findViewById<ImageView>(R.id.media_more)

            mediaEntity.thumbnail?.let {
                val file = File(context.filesDir, it)
                if (file.exists()) {
                    Glide.with(context)
                        .load(Uri.fromFile(file))
                        .into(thumbnail)
                }
            }
            if(mediaEntity.thumbnail == null){
                val file = File(context.filesDir, mediaEntity.name)
                Glide.with(context)
                    .asBitmap()
                    .load(Uri.fromFile(file))
                    .error(R.drawable.ic_video_corrupted)
                    .into(thumbnail)
            }

            title.text = mediaEntity.title
            durationText.text = mediaEntity.duration.formatDurationToReadableFormat()
            val file = File(context.filesDir, mediaEntity.name)

            if (file.exists()) {

                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaEntity, position)
                }

                thumbnail.setOnLongClickListener {
                    longClickListener.invoke(mediaEntity, position)
                    true
                }

                editDots.setOnClickListener {
                    editClickListener.invoke(mediaEntity, position)
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