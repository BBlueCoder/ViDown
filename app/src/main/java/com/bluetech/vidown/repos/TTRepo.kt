package com.bluetech.vidown.repos

import com.bluetech.vidown.api.ApplicationApi
import com.bluetech.vidown.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constants.TT_BASE_URL_API
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTRepo (var api : ApplicationApi): BaseRepo() {

    override fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow {
        val results = mutableListOf<ResultItem>()

        try {
            val resp = api.getTTMedia("$TT_BASE_URL_API$url")
            if (!resp.isSuccessful || resp.body()?.type == null)
                throw Exception("Url is an invalid")

            val respBody = resp.body()!!
            when(respBody.type){
                "video" -> {
                    results.add(ResultItem.CategoryTitle("Video"))
                    results.add(ResultItem.ItemInfo(respBody.videoTitle!!,respBody.videoThumbnail!!))
                    results.add(ResultItem.ItemData(results.size,"video",respBody.videoUrl!!))
                    results.add(ResultItem.CategoryTitle("Video without watermark"))
                    results.add(ResultItem.ItemData(results.size,"video",respBody.videoUrlWithoutWatermark!!))
                    results.add(ResultItem.CategoryTitle("Music in video : ${respBody.videoMusicTitle!!}"))
                    results.add(ResultItem.ItemData(results.size,"audio",respBody.videoMusicUrl!!))
                    emit(Result.success(results))
                }
                "album" -> {
                    results.add(ResultItem.CategoryTitle("Image"))
                    results.add(ResultItem.ItemInfo(respBody.albumTitle!!,respBody.albumUrls!!.first()))
                    for ((id,thumbnail) in respBody.albumUrls.withIndex()){
                        results.add(ResultItem.ItemData(id,"image",thumbnail))
                    }
                    results.add(ResultItem.CategoryTitle("Music in album : ${respBody.albumMusicTitle!!}"))
                    results.add(ResultItem.ItemData(results.size,"audio",respBody.albumMusicUrl!!))
                    emit(Result.success(results))
                }
                else -> throw Exception("Type unsupported")
            }

        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }


}