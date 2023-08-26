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
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DownloadViewModel @Inject constructor(private var dbRepo: DBRepo) : ViewModel(){

    private val _removeMediaStateFlow = MutableStateFlow<Int?>(null)
    val removeMediaStateFlow = _removeMediaStateFlow.asStateFlow()

    private val _renameMediaStateFlow = MutableStateFlow<Result<DownloadItemPayload?>>(Result.success(null))
    val renameMediaStateFlow = _renameMediaStateFlow.asStateFlow()

    private val _savingProgress = MutableStateFlow(Result.success(""))
    val saveProgress = _savingProgress.asStateFlow()

    var currentWorkId  = MutableStateFlow<UUID?>(null)
        private set

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

    fun updateMediaFavorite(id : Int,favorite : Boolean){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.updateMediaFavorite(id,favorite)
        }
    }

    fun updateRequestUUID(uuid: UUID?){
        currentWorkId.value = uuid
    }

    fun removeMedia(mediaEntity: MediaEntity,context: Context,position : Int){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.removeMedia(mediaEntity,context).collect{
                it.onSuccess {
                    _removeMediaStateFlow.emit(position)
                }
                it.onFailure {
                    _removeMediaStateFlow.emit(null)
                }
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