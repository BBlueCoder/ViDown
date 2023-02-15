package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.api.ApplicationApi
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constants.TIKTOK
import com.bluetech.vidown.utils.Constants.TT_BASE_URL_API
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTRepo @Inject constructor(private val api : ApplicationApi): BaseRepo() {

    override suspend fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow {
        val results = mutableListOf<ResultItem>()

        try {
            val resp = api.getTTMedia("$TT_BASE_URL_API$url")
            if (!resp.isSuccessful)
                throw Exception("Url is an invalid, ${resp.errorBody()?.string()}")

            val respBody = resp.body()!!
            when(respBody.type){
                "video" -> {
                    results.add(ResultItem.CategoryTitle("Video"))
                    results.add(ResultItem.ItemInfo(url,respBody.desc,respBody.thumbnails.source.urlList.first(),TIKTOK
                    ))
                    results.add(ResultItem.ItemData(results.size,MediaType.Video,"HQ",respBody.videoData!!.videoHQ))
                    results.add(ResultItem.CategoryTitle("Video without watermark"))
                    results.add(ResultItem.ItemData(results.size,MediaType.Video,"HQ",respBody.videoData.videoWithoutWatermarkHQ))
                    results.add(ResultItem.CategoryTitle("Music in video : ${respBody.music.title}"))
                    results.add(ResultItem.ItemData(results.size,MediaType.Audio,"normal",respBody.music.source.urlList.first()))
                    emit(Result.success(results))
                }
                "image" -> {
                    results.add(ResultItem.CategoryTitle("Image"))
                    results.add(ResultItem.ItemInfo(url,respBody.desc,respBody.thumbnails.source.urlList.first(),TIKTOK))
                    for ((id,thumbnail) in respBody.imageData!!.imagesWithoutWatermark.withIndex()){
                        results.add(ResultItem.ItemData(id,MediaType.Image,"HQ",thumbnail))
                    }
                    results.add(ResultItem.CategoryTitle("Music in album : ${respBody.music.title}}"))
                    results.add(ResultItem.ItemData(results.size,MediaType.Video,"normal",respBody.music.source.urlList.first()))
                    emit(Result.success(results))
                }
                else -> throw Exception("Type unsupported")
            }

        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }


}