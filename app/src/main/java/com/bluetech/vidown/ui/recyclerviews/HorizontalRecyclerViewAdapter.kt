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
import com.bumptech.glide.Glide
import java.io.File

class HorizontalRecyclerViewAdapter(var recentDownloadList : List<MediaEntity>, private val context: Context)
    : RecyclerView.Adapter<HorizontalRecyclerViewAdapter.RecentDownloadAdapterHolder>()
{

    inner class RecentDownloadAdapterHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        fun bind(mediaEntity: MediaEntity,context : Context){
            val thumbnail = itemView.findViewById<ImageView>(R.id.recent_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.recent_title)

            title.text = mediaEntity.title
            val file = File(context.filesDir,mediaEntity.name)
            if (file.exists()){
                when(mediaEntity.mediaType){
                    MediaType.Audio->{
                        thumbnail.setImageResource(R.drawable.ic_audio_gray)
                    }
                    MediaType.Image->{
//                        playIcon.visibility = View.INVISIBLE
                        //overView.visibility = View.INVISIBLE
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