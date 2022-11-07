package com.bluetech.vidown.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.paging.MediaPagingSource
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.core.repos.DownloadRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(private var dbRepo: DBRepo) : ViewModel(){

//    private val _downloadMedia = MutableStateFlow<Result<List<MediaEntity>>>(Result.success(emptyList()))
//    val downloadMedia = _downloadMedia.asStateFlow()
//
//    init {
//        refreshDownload()
//    }
//
//    fun refreshDownload(){
//        viewModelScope.launch(Dispatchers.IO) {
//            downloadRepo.getDownloadFiles().collect{
//                _downloadMedia.emit(it)
//            }
//        }
//    }


    val downloadsMedia = Pager(
        PagingConfig(
            pageSize = 30,
            enablePlaceholders = false,
            maxSize = 100
        ),){
        MediaPagingSource(dbRepo)
    }.flow.cachedIn(viewModelScope)

}