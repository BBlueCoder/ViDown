package com.bluetech.vidown.ui.downloadscreen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.bluetech.vidown.data.db.entities.MediaEntity
import com.bluetech.vidown.paging.MediaPagingSource
import com.bluetech.vidown.data.repos.pojoclasses.DownloadItemPayload
import com.bluetech.vidown.data.repos.pojoclasses.FetchArgs
import com.bluetech.vidown.data.repos.pojoclasses.SelectItem
import com.bluetech.vidown.data.repos.DBRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(private var dbRepo: DBRepo) : ViewModel(){
    private val _savingProgress = MutableStateFlow(Result.success(""))
    val saveProgress = _savingProgress.asStateFlow()

    val allMediaStream = dbRepo.getMediaStream()

    val downloadInProgress = dbRepo.getDownloadInProgressStream()

    var selectionState = MutableStateFlow(false)
        private set

    var fetchArgs = FetchArgs()

    val downloadsMedia = Pager(
        PagingConfig(
            pageSize = 30,
            enablePlaceholders = false,
            maxSize = 100
        ),){
        MediaPagingSource(dbRepo,fetchArgs)
    }.flow.cachedIn(viewModelScope)

    fun updateMediaFavorite(id : Long,favorite : Boolean){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.updateMediaFavorite(id,favorite)
        }
    }

    fun updateSelectionState(state : Boolean){
        selectionState.value = state
    }

    fun removeMedia(mediaEntity: MediaEntity, context: Context, position : Int){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.removeMedia(mediaEntity,context)
        }
    }

    fun removeMedia(media : List<SelectItem>, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            dbRepo.removeMedia(media.map { it.mediaWithThumbnail.mediaEntity },context)
        }
    }

    fun renameMedia(id : Long,title: String){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.renameMedia(id,title)
        }
    }

    fun updateSaveProgress(result : Result<String>){
        viewModelScope.launch(Dispatchers.IO){
            _savingProgress.emit(result)
        }
    }

}