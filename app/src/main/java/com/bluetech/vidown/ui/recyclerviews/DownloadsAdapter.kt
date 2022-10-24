package com.bluetech.vidown.ui.recyclerviews

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaEntity
import com.bumptech.glide.Glide
import java.io.File

class DownloadsAdapter(var downloadsList : List<MediaEntity>) : RecyclerView.Adapter<DownloadsAdapter.DownloadsAdapterHolder>() {

    inner class DownloadsAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadsAdapterHolder {
        return DownloadsAdapterHolder(LayoutInflater.from(parent.context).inflate(R.layout.download_item,parent,false))
    }

    override fun onBindViewHolder(holder: DownloadsAdapterHolder, position: Int) {
        holder.itemView.apply {
            val thumbnail = this.findViewById<ImageView>(R.id.download_item_thumbnail)
            val playIcon = this.findViewById<ImageView>(R.id.download_thumbnail_play_btn)
            val overView = this.findViewById<View>(R.id.download_item_overview)

            val file = File(context.filesDir,downloadsList[position].name)
            if(file.exists()){
                when(downloadsList[position].mediaType){
                    MediaType.Audio->{
                        thumbnail.setImageResource(R.drawable.ic_audio_gray)
                    }
                    MediaType.Image->{
                        playIcon.visibility = View.INVISIBLE
                        overView.visibility = View.INVISIBLE
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

    override fun getItemCount(): Int {
        return downloadsList.size
    }
}