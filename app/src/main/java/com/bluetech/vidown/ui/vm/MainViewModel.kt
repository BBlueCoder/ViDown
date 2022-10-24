package com.bluetech.vidown.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class MainViewModel @Inject constructor(): ViewModel(){

//    @Inject
//    lateinit var youRepo: YouRepo

    @Inject
    lateinit var instaRepo: InstaRepo

    @Inject
    lateinit var ttRepo: TTRepo

    @Inject
    lateinit var twRepo : TwRepo

    private val _lookUpResults = MutableStateFlow<Result<List<ResultItem>>>(Result.success(emptyList()))
    val lookUpResults = _lookUpResults.asStateFlow()

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

    private fun verifyUrlAndMatchItToRepo(url : String) : BaseRepo?{
        return when{
            url.contains("youtube") || url.contains("youtu.be") -> YouRepo()
            url.contains("instagram") || url.contains("instagra") -> instaRepo
            url.contains("twitter") || url.contains("t.co") -> twRepo
            url.contains("tiktok") -> ttRepo
           else -> null
        }
    }
}