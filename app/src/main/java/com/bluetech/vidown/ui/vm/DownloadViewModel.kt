package com.bluetech.vidown.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.repos.DownloadRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(private var downloadRepo: DownloadRepo) : ViewModel(){

    private val _downloadMedia = MutableStateFlow<Result<List<MediaEntity>>>(Result.success(emptyList()))
    val downloadMedia = _downloadMedia.asStateFlow()

    init {
        refreshDownload()
    }

    fun refreshDownload(){
        viewModelScope.launch(Dispatchers.IO) {
            downloadRepo.getDownloadFiles().collect{
                _downloadMedia.emit(it)
            }
        }
    }
}