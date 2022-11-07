package com.bluetech.vidown.ui.recyclerviews

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.db.MediaEntity
import com.bumptech.glide.Glide
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

sealed class DownloadsAdapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(mediaEntity: MediaEntity, context: Context)

    class VideoMediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(mediaEntity: MediaEntity, context: Context) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_video_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_video_title)
            val durationText = itemView.findViewById<TextView>(R.id.media_video_duration)

            val file = File(context.filesDir, mediaEntity.name)
            if (file.exists()) {
                Glide.with(context)
                    .asBitmap()
                    .load(Uri.fromFile(file))
                    .into(thumbnail)
            }
            title.text = mediaEntity.title
            val uri = Uri.fromFile(file)
            MediaPlayer.create(context, uri).also {
                val durationDate = Date((it.duration).toLong())
                when {
                    it.duration / 1000 < 3600 -> {
                        val sdf = SimpleDateFormat("m:ss", Locale.getDefault())
                        durationText.text = sdf.format(durationDate)
                    }
                    else -> {
                        val sdf = SimpleDateFormat("h:mm:ss", Locale.getDefault())
                        durationText.text = sdf.format(durationDate)
                    }
                }

                it.reset()
                it.release()
            }

        }
    }

    class ImageMediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(mediaEntity: MediaEntity, context: Context) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_image_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_image_title)

            val file = File(context.filesDir, mediaEntity.name)
            if (file.exists()) {
                Glide.with(context)
                    .load(Uri.fromFile(file))
                    .into(thumbnail)
            }
            title.text = mediaEntity.title

        }
    }

    class AudioMediaViewHolder(itemView: View) : DownloadsAdapterHolder(itemView) {
        override fun bind(mediaEntity: MediaEntity, context: Context) {
            val thumbnail = itemView.findViewById<ImageView>(R.id.media_audio_thumbnail)
            val title = itemView.findViewById<TextView>(R.id.media_audio_title)
            val durationText = itemView.findViewById<TextView>(R.id.media_audio_duration)

//            val file = File(context.filesDir,mediaEntity.name)
//            if(file.exists()){
//                Glide.with(context)
//                    .asBitmap()
//                    .load(Uri.fromFile(file))
//                    .into(thumbnail)
//            }
            title.text = mediaEntity.title

        }
    }

}