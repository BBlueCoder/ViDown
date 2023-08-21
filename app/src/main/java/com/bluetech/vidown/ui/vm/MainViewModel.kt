package com.bluetech.vidown.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetech.vidown.core.db.MediaEntity
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.core.repos.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private var dbRepo: DBRepo): ViewModel(){

    @Inject
    lateinit var youRepo: YouRepo

    @Inject
    lateinit var instaRepo: InstaRepo

    @Inject
    lateinit var ttRepo: TTRepo

    @Inject
    lateinit var twRepo : TwRepo

    private val _lookUpResults = MutableStateFlow<Result<List<ResultItem>>>(Result.success(emptyList()))
    val lookUpResults = _lookUpResults.asStateFlow()

    private val _recentDownloads = MutableStateFlow<Result<List<MediaEntity>>>(Result.success(
        emptyList()
    ))
    val recentDownloads = _recentDownloads.asStateFlow()

    private val _lastFavorites = MutableStateFlow<Result<List<MediaEntity>>>(Result.success(emptyList()))
    val lastFavorites = _lastFavorites.asStateFlow()

    private val _mediaLink = MutableStateFlow("")
    val mediaLink = _mediaLink.asStateFlow()

    fun searchForResult(url : String){
        viewModelScope.launch(Dispatchers.IO){
            val repo = verifyUrlAndMatchItToRepo(url)
            if(repo == null)
                _lookUpResults.emit(Result.failure(Exception("Url is invalid or not supported")))
            repo!!.getResultsAsFlow(url).collect{
                _lookUpResults.emit(it)
            }
        }
    }

    fun updateMediaLink(link : String){
        _mediaLink.value = link
    }

    fun getLastDownloads(){
        viewModelScope.launch(Dispatchers.IO){
            dbRepo.getRecentRecords().collect{
                _recentDownloads.emit(it)
            }
        }
    }

    fun getLastFavorites(){
        viewModelScope.launch(Dispatchers.IO) {
            dbRepo.getLastFavorites().collect{
                _lastFavorites.emit(it)
            }
        }
    }

    private fun verifyUrlAndMatchItToRepo(url : String) : BaseRepo?{
        return when{
            url.contains("youtube") || url.contains("youtu.be") -> youRepo
            url.contains("instagram") || url.contains("instagra") -> instaRepo
            //url.contains("twitter") || url.contains("t.co") -> twRepo
            url.contains("tiktok") -> ttRepo
           else -> null
        }
    }

    init {
        getLastDownloads()
        getLastFavorites()
    }
}