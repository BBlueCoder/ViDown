package com.bluetech.vidown.ui.recyclerviews

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.pojoclasses.ResultItem

sealed class ResultsAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    class TitleViewHolder(itemView: View): ResultsAdapterHolder(itemView){
        fun bind(resultItem: ResultItem.CategoryTitle){
            val categoryTitle = itemView.findViewById<TextView>(R.id.result_category_title)
            categoryTitle.text = resultItem.title
        }
    }

    class ResultsViewHolder(itemView: View) : ResultsAdapterHolder(itemView){
        fun bind(resultItem: ResultItem.ItemData){
            val icon = itemView.findViewById<ImageView>(R.id.item_icon)
            when(resultItem.format){
                "video"-> icon.setImageResource(R.drawable.ic_video_gray)
                "audio"-> icon.setImageResource(R.drawable.ic_audio_gray)
                "image"-> icon.setImageResource(R.drawable.ic_image_gray)
            }

            val title = itemView.findViewById<TextView>(R.id.item_title)
            val quality = itemView.findViewById<TextView>(R.id.item_quality)

            title.text = resultItem.format
            quality.text = resultItem.quality
        }
    }

}
