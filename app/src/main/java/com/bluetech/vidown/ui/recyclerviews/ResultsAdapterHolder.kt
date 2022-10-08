package com.bluetech.vidown.ui.recyclerviews

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.pojoclasses.ResultItem

sealed class ResultsAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

    class TitleViewHolder(itemView: View): ResultsAdapterHolder(itemView){
        fun bind(resultItem: ResultItem.CategoryTitle){
            val categoryTitle = itemView.findViewById<TextView>(R.id.result_category_title)
            categoryTitle.text = resultItem.title
        }
    }

    class ResultsViewHolder(itemView: View) : ResultsAdapterHolder(itemView){
        fun bind(resultItem: ResultItem.ItemData){
            val title = itemView.findViewById<TextView>(R.id.title)
            val format = itemView.findViewById<TextView>(R.id.format)

//            title.text = resultItem.title
//            format.text = resultItem.format
        }
    }

}
