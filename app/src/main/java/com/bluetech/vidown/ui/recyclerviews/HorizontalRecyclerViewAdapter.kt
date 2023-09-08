package com.bluetech.vidown.ui.recyclerviews

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.utils.toggleVisibility
import com.bumptech.glide.Glide
import java.io.File

class HorizontalRecyclerViewAdapter(
    var recentDownloadList : List<MediaEntity>,
    private val context: Context,
    private val itemClickListener : ((mediaEntity : MediaEntity) -> Unit)
    )
    : RecyclerView.Adapter<HorizontalRecyclerViewAdapter.RecentDownloadAdapterHolder>()
{

    inner class RecentDownloadAdapterHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        fun bind(mediaEntity: MediaEntity,context : Context){
            val thumbnail = itemView.findViewById<ImageView>(R.id.hor_thumbnail)
            val playIcon = itemView.findViewById<ImageView>(R.id.hor_play_ic)
            val musicIcon = itemView.findViewById<ImageView>(R.id.hor_music_ic)

            val file = File(context.filesDir,mediaEntity.name)
            if (file.exists()){
                when(mediaEntity.mediaType){
                    MediaType.Audio->{
                        mediaEntity.thumbnail?.let {thumbnailName ->
                            val fileThumbnail = File(context.filesDir, thumbnailName)
                            if (file.exists()) {
                                Glide.with(context)
                                    .load(Uri.fromFile(fileThumbnail))
                                    .error(R.drawable.ic_audio_gray)
                                    .into(thumbnail)
                            }
                        }
                        musicIcon.toggleVisibility()
                    }
                    MediaType.Image->{
                        playIcon.visibility = View.INVISIBLE
                        musicIcon.visibility = View.INVISIBLE
                        Glide.with(context)
                            .load(Uri.fromFile(file))
                            .into(thumbnail)
                    }
                    MediaType.Video->{
                        Glide.with(context)
                            .asBitmap()
                            .load(Uri.fromFile(file))
                            .into(thumbnail)
                    }
                }
                thumbnail.setOnClickListener {
                    itemClickListener.invoke(mediaEntity)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentDownloadAdapterHolder {
        return RecentDownloadAdapterHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.horizontal_recycler_view_item_layout,parent,false
        ))
    }

    override fun onBindViewHolder(holder: RecentDownloadAdapterHolder, position: Int) {
        holder.bind(recentDownloadList[position],context)
    }

    override fun getItemCount(): Int {
        return recentDownloadList.size
    }

}