package com.bluetech.vidown.ui.downloadscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.entities.MediaWithThumbnail

class DownloadsAdapter(
    private val context: Context,
    private val itemClickListener: ((mediaWithThumbnail: MediaWithThumbnail, position : Int) -> Unit),
    private val editClickListener: ((mediaWithThumbnail: MediaWithThumbnail, position : Int) -> Unit),
    private val longClickListener : ((mediaWithThumbnail: MediaWithThumbnail, position : Int) -> Unit)
) : PagingDataAdapter<MediaWithThumbnail, DownloadsAdapterHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsAdapterHolder {
        return when (viewType) {
            R.layout.download_item_image_layout -> {
                DownloadsAdapterHolder.ImageMediaViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            R.layout.download_item_media_layout -> {
                DownloadsAdapterHolder.MediaViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: DownloadsAdapterHolder, position: Int) {
        val item = getItem(position) as MediaWithThumbnail

        holder.bind(item, context,position, itemClickListener,editClickListener,longClickListener)
    }

    override fun onBindViewHolder(
        holder: DownloadsAdapterHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isNotEmpty()){
            if(payloads.first() is String){
                holder.renameItem(payloads.first() as String)
            }
            if(payloads.first() is Boolean){
                holder.toggleItemSelection(payloads.first() as Boolean)
            }
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)!!.mediaEntity.mediaType) {
            MediaType.Image -> R.layout.download_item_image_layout
            else -> R.layout.download_item_media_layout
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<MediaWithThumbnail>() {
            override fun areItemsTheSame(oldItem: MediaWithThumbnail, newItem: MediaWithThumbnail): Boolean {
                return oldItem.mediaEntity.uid == newItem.mediaEntity.uid
            }

            override fun areContentsTheSame(oldItem: MediaWithThumbnail, newItem: MediaWithThumbnail): Boolean {
                return oldItem.mediaEntity == newItem.mediaEntity
            }

        }
    }
}