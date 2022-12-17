package com.bluetech.vidown.ui.recyclerviews

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaEntity
import com.bumptech.glide.Glide
import java.io.File

class DownloadsAdapter(
    private val context: Context,
    private val itemClickListener: ((mediaEntity: MediaEntity) -> Unit)?,
    private val favoriteClickListener: ((mediaEntity: MediaEntity) -> Unit)?
) : PagingDataAdapter<MediaEntity, DownloadsAdapterHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsAdapterHolder {
        return when (viewType) {
            R.layout.media_audio_layout -> {
                DownloadsAdapterHolder.AudioMediaViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            R.layout.media_image_layout -> {
                DownloadsAdapterHolder.ImageMediaViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            R.layout.media_video_layout -> {
                DownloadsAdapterHolder.VideoMediaViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: DownloadsAdapterHolder, position: Int) {
        val item = getItem(position) as MediaEntity
        holder.bind(item, context, itemClickListener,favoriteClickListener)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)!!.mediaType) {
            MediaType.Audio -> R.layout.media_audio_layout
            MediaType.Image -> R.layout.media_image_layout
            MediaType.Video -> R.layout.media_video_layout
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<MediaEntity>() {
            override fun areItemsTheSame(oldItem: MediaEntity, newItem: MediaEntity): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(oldItem: MediaEntity, newItem: MediaEntity): Boolean {
                return oldItem == newItem
            }

        }
    }
}