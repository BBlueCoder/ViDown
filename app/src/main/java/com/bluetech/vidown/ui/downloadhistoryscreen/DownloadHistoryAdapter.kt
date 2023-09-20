package com.bluetech.vidown.ui.downloadhistoryscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bluetech.vidown.R
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.db.entities.DownloadStatus

class DownloadHistoryAdapter(
    private val context : Context,
    private val removeActionClick : ((downloadHistoryWithExtra : DownloadHistoryWithExtras)-> Unit),
    private val cancelActionClick : ((downloadHistoryWithExtra : DownloadHistoryWithExtras)-> Unit))
 : ListAdapter<DownloadHistoryWithExtras, DownloadHistoryHolder>(
    COMPARATOR
) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<DownloadHistoryWithExtras>(){
            override fun areItemsTheSame(oldItem: DownloadHistoryWithExtras, newItem: DownloadHistoryWithExtras): Boolean {
                    return oldItem.downloadHistoryEntity.uid == newItem.downloadHistoryEntity.uid
            }

            override fun areContentsTheSame(oldItem: DownloadHistoryWithExtras, newItem: DownloadHistoryWithExtras): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadHistoryHolder {
        return when(viewType){
            R.layout.download_history_item -> {
                DownloadHistoryHolder.DownloadHistoryItem(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                    removeActionClick
                )
            }
            R.layout.download_history_item_downloading -> {
                DownloadHistoryHolder.DownloadHistoryItemDownloading(
                    LayoutInflater.from(parent.context).inflate(viewType, parent, false),
                    cancelActionClick
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: DownloadHistoryHolder, position: Int) {
        holder.bind(
            getItem(position),
            context
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).downloadHistoryEntity.downloadData.downloadStatus){
            DownloadStatus.INPROGRESS -> R.layout.download_history_item_downloading
            else -> R.layout.download_history_item
        }
    }
}