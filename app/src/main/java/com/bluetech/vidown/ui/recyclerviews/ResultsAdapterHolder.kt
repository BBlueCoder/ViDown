package com.bluetech.vidown.ui.recyclerviews

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.pojoclasses.ResultItem

sealed class ResultsAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    var itemClickListener : ((resultItem : ResultItem.ItemData)->Unit)? = null

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
                MediaType.Video-> icon.setImageResource(R.drawable.ic_video_gray)
                MediaType.Audio-> icon.setImageResource(R.drawable.ic_audio_gray)
                MediaType.Image-> icon.setImageResource(R.drawable.ic_image_gray)
            }

            val title = itemView.findViewById<TextView>(R.id.item_title)
            title.text = resultItem.quality

            val layout = itemView.findViewById<RelativeLayout>(R.id.result_item_view)
            layout.setOnClickListener {
                println("item click...")
                itemClickListener?.invoke(resultItem)
            }
        }
    }

}
