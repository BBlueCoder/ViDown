package com.bluetech.vidown.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabluecoder.youdownloaderlib.OnVideoInfoListener
import com.dabluecoder.youdownloaderlib.YouClient
import com.dabluecoder.youdownloaderlib.pojoclasses.VideoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel(){

    private val _lookUpResults = MutableStateFlow<List<String>>(emptyList())
    val lookUpResults = _lookUpResults.asStateFlow()

    fun lookUp(url : String){
        viewModelScope.launch(Dispatchers.Default) {
            val client = YouClient()
            client.videoUrl = url

            client.getVideoInfo(object : OnVideoInfoListener{
                override fun onError(message: String) {
                    throw Exception(message)
                }

                override fun onSuccess(videoInfo: VideoResponse) {
                    val list = mutableListOf<String>()

                    videoInfo.streamingData.adaptiveFormats?.forEach {
                        list.add("${it.qualityLabel}${it.audioQuality} - ")
                    }

//                    _lookUpResults.emit(list)
                }

            })
        }
    }
}