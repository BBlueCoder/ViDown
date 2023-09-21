package com.bluetech.vidown.ui.resultsheetscreen

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import com.google.android.material.button.MaterialButton

sealed class ResultsAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    var itemClickListener : ((resultItem : ResultItem.ItemData)->Unit)? = null

    class TitleViewHolder(itemView: View): ResultsAdapterHolder(itemView){
        fun bind(resultItem: ResultItem.CategoryTitle){
            val categoryTitle = itemView.findViewById<TextView>(R.id.result_category_title)
            val categoryThumbnail = itemView.findViewById<ImageView>(R.id.result_category_icon)
            categoryTitle.text = resultItem.title
            when{
                resultItem.title.contains("Video") -> categoryThumbnail.setImageResource(R.drawable.ic_video)
                resultItem.title.contains("Image") -> categoryThumbnail.setImageResource(R.drawable.ic_image_purple)
                else -> categoryThumbnail.setImageResource(R.drawable.ic_audio)
            }
        }
    }

    class ResultsViewHolder(itemView: View) : ResultsAdapterHolder(itemView){
        fun bind(resultItem: ResultItem.ItemData){

            val title = itemView.findViewById<TextView>(R.id.item_title)
            title.text = resultItem.quality

            val downloadIcon = itemView.findViewById<MaterialButton>(R.id.result_item_download_icon)
            downloadIcon.setOnClickListener {
                itemClickListener?.invoke(resultItem)
            }
        }
    }

}
