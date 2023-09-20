package com.bluetech.vidown.ui.downloadhistoryscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetech.vidown.core.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.db.entities.DownloadStatus
import com.bluetech.vidown.core.db.entities.updateDownloadStatus
import com.bluetech.vidown.core.domain.RemoveDownloadHistoryItemUseCase
import com.bluetech.vidown.core.repos.DBRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadHistoryViewModel @Inject constructor(
    private var dbRepo: DBRepo,
    private var removeDownloadHistoryItemUseCase: RemoveDownloadHistoryItemUseCase
) : ViewModel(){

    val getDownloadHistory = dbRepo.getAllDownloadHistory()

    fun removeDownloadHistoryItem(downloadHistoryWithExtras: DownloadHistoryWithExtras){
        viewModelScope.launch(Dispatchers.IO){
            removeDownloadHistoryItemUseCase(downloadHistoryWithExtras)
        }
    }

    fun cancelDownload(downloadHistoryWithExtras: DownloadHistoryWithExtras){
        viewModelScope.launch(Dispatchers.IO){
            val cancelledDownload = downloadHistoryWithExtras.updateDownloadStatus(DownloadStatus.CANCELLED)
            dbRepo.updateDownloadHistoryItem(cancelledDownload)
        }
    }
}