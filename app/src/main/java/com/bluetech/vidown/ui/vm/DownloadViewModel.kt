package com.bluetech.vidown.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.paging.MediaPagingSource
import com.bluetech.vidown.core.pojoclasses.DownloadItemPayload
import com.bluetech.vidown.core.pojoclasses.DownloadMediaProgress
import com.bluetech.vidown.core.pojoclasses.FetchArgs
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.core.repos.DownloadRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
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

    private val _removeMediaStateFlow = MutableStateFlow<Result<Boolean?>>(Result.success(null))
    val removeMediaStateFlow = _removeMediaStateFlow.asStateFlow()

    private val _renameMediaStateFlow = MutableStateFlow<Result<DownloadItemPayload?>>(Result.success(null))
    val renameMediaStateFlow = _renameMediaStateFlow.asStateFlow()

    private val _savingProgress = MutableStateFlow(Result.success(""))
    val saveProgress = _savingProgress.asStateFlow()

    var title : String = ""
    var thumbnail : String = ""

    var fetchArgs = FetchArgs()

    val downloadsMedia = Pager(
        PagingConfig(
            pageSize = 30,
            enablePlaceholders = false,
            maxSize = 100
        ),){
        MediaPagingSource(dbRepo,fetchArgs)
    }.flow.cachedIn(viewModelScope)

    fun updateProgress(progress : DownloadMediaProgress){
        viewModelScope.launch(Dispatchers.Default){
            _downloadProgress.emit(progress)
        }
    }

    fun updateItemInfo(itemInfo: ResultItem.ItemInfo?){
        viewModelScope.launch {
            _downloadItemInfo.emit(itemInfo)
        }
    }

    fun updateMediaFavorite(id : Int,favorite : Boolean){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.updateMediaFavorite(id,favorite)
        }
    }

    fun removeMedia(mediaEntity: MediaEntity,context: Context){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.removeMedia(mediaEntity,context).collect{
                _removeMediaStateFlow.emit(it)
            }
        }
    }

    fun renameMedia(id : Int,title: String,downloadItemPayload: DownloadItemPayload){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.renameMedia(id,title,downloadItemPayload).collect{
                _renameMediaStateFlow.emit(it)
            }
        }
    }

    fun updateSaveProgress(result : Result<String>){
        viewModelScope.launch(Dispatchers.IO){
            _savingProgress.emit(result)
        }
    }

}