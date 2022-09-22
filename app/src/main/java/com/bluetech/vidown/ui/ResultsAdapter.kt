package com.bluetech.vidown.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluetech.vidown.R
import com.bluetech.vidown.pojoclasses.ResultItem
import com.bluetech.vidown.ui.recyclerviews.ResultsAdapterHolder

class ResultsAdapter(var resultsList : MutableList<ResultItem>) : ListAdapter<ResultItem,ResultsAdapterHolder>(
    COMPARATOR)
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAdapterHolder {
        return when(viewType){
            R.layout.result_category_title -> {
                ResultsAdapterHolder.TitleViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType,parent,false)
                )
            }
            R.layout.download_item -> {
                ResultsAdapterHolder.ResultsViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType,parent,false)
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ResultsAdapterHolder, position: Int) {
        val item = getItem(position)
        when(holder){
            is ResultsAdapterHolder.TitleViewHolder -> holder.bind(item as ResultItem.CategoryTitle)
            is ResultsAdapterHolder.ResultsViewHolder -> holder.bind(item as ResultItem.ItemData)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is ResultItem.CategoryTitle -> R.layout.result_category_title
            is ResultItem.ItemData -> R.layout.download_item
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<ResultItem>(){
            override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
                return when {
                    oldItem is ResultItem.ItemData && newItem is ResultItem.ItemData -> {
                        oldItem.id == newItem.id
                    }
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean {
                return when {
                    oldItem is ResultItem.ItemData && newItem is ResultItem.ItemData -> {
                        oldItem == newItem
                    }
                    else -> false
                }
            }

        }
    }
}