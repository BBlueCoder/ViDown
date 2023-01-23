package com.bluetech.vidown.core.repos

import android.content.Context
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constants.YOUTUBE
import com.dabluecoder.youdownloaderlib.YouClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouRepo @Inject constructor(@ApplicationContext val context : Context): BaseRepo() {

    private val TAG = "YouRepo"

    override suspend fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow{
        val results = mutableListOf<ResultItem>()
        try {

            val youClient = YouClient(url,context)
            val vidResp = youClient.getVideoData()

            val videoTitle = vidResp.videoDetails.title

            val videoThumbnail = vidResp.videoDetails.thumbnail.thumbnails.first().url

            results.add(ResultItem.CategoryTitle("Video"))
            results.add(
                ResultItem.ItemInfo(
                    url,
                    videoTitle,
                    videoThumbnail,
                    YOUTUBE
                )
            )

            //emit(Result.success(results.toList()))



            vidResp.streamingData.mixedFormats!!.forEach {
                results.add(
                    ResultItem.ItemData(
                        results.size,
                        MediaType.Video,
                        it.qualityLabel?:it.quality,
                        it.url!!,
                        true
                    )
                )
            }

            vidResp.streamingData.adaptiveFormats!!.filter { format -> !format.mimeType.contains("audio")
                    && format.mimeType.lowercase().contains("avc1")}.forEach {
                if(!results.any { item -> item is ResultItem.ItemData && (item.quality == it.qualityLabel
                            || item.quality == it.quality) }){
                    results.add(
                        ResultItem.ItemData(
                            results.size,
                            MediaType.Video,
                            it.qualityLabel?:it.quality,
                            it.url!!,
                            false
                        )
                    )
                }

            }

            results.sortWith(compareByDescending {
                when(it){
                    is ResultItem.ItemData -> it.quality.replace("p","").toInt()
                    else -> Int.MAX_VALUE
                }
            })

            results.add(ResultItem.CategoryTitle("Audio"))
            vidResp.streamingData.adaptiveFormats!!.filter { format -> format.mimeType.contains("audio")
                    && format.mimeType.contains("mp4a")}.forEach {
                results.add(
                    ResultItem.ItemData(
                        results.size,
                        MediaType.Audio,
                        it.qualityLabel?: it.audioQuality?:it.quality,
                        it.url!!
                    )
                )
            }



            emit(Result.success(results.toList()))
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }
}