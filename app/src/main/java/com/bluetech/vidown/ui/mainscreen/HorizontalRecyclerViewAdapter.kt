package com.bluetech.vidown.ui.mainscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.data.db.entities.MediaType
import com.bluetech.vidown.data.db.entities.MediaWithThumbnail
import com.bluetech.vidown.utils.toggleVisibility
import com.bumptech.glide.Glide
import java.io.File

class HorizontalRecyclerViewAdapter(
    var recentDownloadList: List<MediaWithThumbnail>,
    private val context: Context,
    private val itemClickListener: ((mediaEntity: MediaWithThumbnail) -> Unit)
) : RecyclerView.Adapter<HorizontalRecyclerViewAdapter.RecentDownloadAdapterHolder>() {

    inner class RecentDownloadAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(mediaWithThumbnail: MediaWithThumbnail, context: Context) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.hor_thumbnail)
            val playIcon = itemView.findViewById<ImageView>(R.id.hor_play_ic)
            val musicIcon = itemView.findViewById<ImageView>(R.id.hor_music_ic)

            val file = File(context.filesDir, mediaWithThumbnail.mediaEntity.savedName)
            if (file.exists()) {

                Glide.with(context)
                    .load(
                        File(
                            context.filesDir,
                            mediaWithThumbnail.mediaThumbnail.thumbnailSavedName
                        )
                    )
                    .into(thumbnail)

                if (mediaWithThumbnail.mediaEntity.mediaType == MediaType.Audio)
                    musicIcon.toggleVisibility()

                if (mediaWithThumbnail.mediaEntity.mediaType == MediaType.Image) {
                    playIcon.visibility = View.INVISIBLE
                    musicIcon.visibility = View.INVISIBLE
                }


                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaWithThumbnail)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentDownloadAdapterHolder {
        return RecentDownloadAdapterHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.horizontal_recycler_view_item_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecentDownloadAdapterHolder, position: Int) {
        holder.bind(recentDownloadList[position], context)
    }

    override fun getItemCount(): Int {
        return recentDownloadList.size
    }

}