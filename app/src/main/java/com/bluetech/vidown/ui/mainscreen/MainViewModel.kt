package com.bluetech.vidown.ui.mainscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluetech.vidown.domain.AddNewDownloadUseCase
import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import com.bluetech.vidown.workers.WorkerStarter
import com.bluetech.vidown.data.repos.BaseRepo
import com.bluetech.vidown.data.repos.DBRepo
import com.bluetech.vidown.data.repos.InstaRepo
import com.bluetech.vidown.data.repos.TTRepo
import com.bluetech.vidown.data.repos.TwRepo
import com.bluetech.vidown.data.repos.YouRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private var dbRepo: DBRepo,
    private var addNewDownloadUseCase: AddNewDownloadUseCase,
    private var workerStarter: WorkerStarter
): ViewModel(){

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

    val recentDownloads = dbRepo.getRecentRecords()

    val lastFavorites = dbRepo.getLastFavorites()

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

    private fun verifyUrlAndMatchItToRepo(url : String) : BaseRepo?{
        return when{
            url.contains("youtube") || url.contains("youtu.be") -> youRepo
            url.contains("instagram") || url.contains("instagra") -> instaRepo
            //url.contains("twitter") || url.contains("t.co") -> twRepo
            url.contains("tiktok") -> ttRepo
           else -> null
        }
    }

    fun addNewDownload(itemData : ResultItem.ItemData, itemInfo : ResultItem.ItemInfo, separatedAudioUrl : String?) {
        viewModelScope.launch(Dispatchers.IO) {
            addNewDownloadUseCase(itemData,itemInfo,separatedAudioUrl)
        }
    }

    fun isThereAnyUncompletedDownloads() = dbRepo.isThereAnyUncompletedDownloads()

    fun startDownloadWorker(){
        workerStarter()
    }
}