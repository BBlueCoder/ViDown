package com.bluetech.vidown

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.pojoclasses.ResultItem
import org.w3c.dom.Text

class ResultsAdapter(var resultsList : MutableList<ResultItem>) : RecyclerView.Adapter<ResultsAdapter.ResultsAdapterHolder>()
{
    inner class ResultsAdapterHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAdapterHolder {
        return ResultsAdapterHolder(LayoutInflater.from(parent.context).inflate(R.layout.download_item,parent,false))
    }

    override fun onBindViewHolder(holder: ResultsAdapterHolder, position: Int) {
        holder.itemView.apply {
            val title = this.findViewById<TextView>(R.id.title)
            val format = this.findViewById<TextView>(R.id.format)

            title.text = resultsList[position].title
            format.text = resultsList[position].format
        }
    }

    override fun getItemCount(): Int {
        return resultsList.size
    }
}