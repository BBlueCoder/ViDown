package com.bluetech.vidown.ui.resultsheetscreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bluetech.vidown.R
import com.bluetech.vidown.data.repos.pojoclasses.ResultItem

class ResultsAdapter(var resultsList : List<ResultItem>,
                     private var itemClickListener : ((resultItem : ResultItem.ItemData)->Unit)? = null) : ListAdapter<ResultItem, ResultsAdapterHolder>(
    COMPARATOR
)
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsAdapterHolder {
        return when(viewType){
            R.layout.result_category_title -> {
                ResultsAdapterHolder.TitleViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            R.layout.result_list_item -> {
                ResultsAdapterHolder.ResultsViewHolder(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ResultsAdapterHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        val item = getItem(position)
        when(holder){
            is ResultsAdapterHolder.TitleViewHolder -> holder.bind(item as ResultItem.CategoryTitle)
            is ResultsAdapterHolder.ResultsViewHolder -> holder.bind(item as ResultItem.ItemData)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is ResultItem.CategoryTitle -> R.layout.result_category_title
            is ResultItem.ItemData -> R.layout.result_list_item
            is ResultItem.ItemInfo -> R.layout.result_category_title
        }
    }

    fun getListData(): MutableList<ResultItem> {
        val itemsList = mutableListOf<ResultItem>()
        for(i in 0 until itemCount){
            getItem(i)?.let {
                itemsList.add(it)
            }
        }
        return itemsList
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