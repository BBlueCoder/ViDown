package com.bluetech.vidown.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.paging.MediaPagingSource
import com.bluetech.vidown.core.pojoclasses.DownloadMediaProgress
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.core.repos.DownloadRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DownloadViewModel @Inject constructor(private var dbRepo: DBRepo) : ViewModel(){

    private val _downloadProgress = MutableStateFlow(DownloadMediaProgress(0,0,0))
    val downloadProgress = _downloadProgress.asStateFlow()

    private val _downloadItemInfo = MutableStateFlow<ResultItem.ItemInfo?>(null)
    val downloadItemInfo = _downloadItemInfo.asStateFlow()


    var title : String = ""
    var thumbnail : String = ""

    val downloadsMedia = Pager(
        PagingConfig(
            pageSize = 30,
            enablePlaceholders = false,
            maxSize = 100
        ),){
        MediaPagingSource(dbRepo)
    }.flow.cachedIn(viewModelScope)

    fun updateProgress(progress : DownloadMediaProgress){
        viewModelScope.launch(Dispatchers.Default){
            _downloadProgress.emit(progress)
        }
    }

    fun updateItemInfo(itemInfo: ResultItem.ItemInfo?){
        viewModelScope.launch {
            if(itemInfo == null)
                delay(1500)
            _downloadItemInfo.emit(itemInfo)
        }
    }

}