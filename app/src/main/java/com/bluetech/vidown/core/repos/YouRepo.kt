package com.bluetech.vidown.core.repos

import android.content.Context
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.pojoclasses.ResultItem
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

            val videoTitle = youClient.getVideoTitle()

            val videoThumbnail = youClient.getVideoThumbnail()

            results.add(ResultItem.CategoryTitle("Video"))
            results.add(
                ResultItem.ItemInfo(
                    url,
                    videoTitle,
                    videoThumbnail
                )
            )

            //emit(Result.success(results.toList()))

            val vidResp = youClient.getVideoAllData()

            vidResp.streamingData.mixedFormats!!.forEach {
                results.add(
                    ResultItem.ItemData(
                        results.size,
                        MediaType.Video,
                        it.qualityLabel?:it.quality,
                        it.url!!
                    )
                )
            }

            vidResp.streamingData.adaptiveFormats!!.filter { format -> !format.mimeType.contains("audio") }.forEach {
                println("---------------------------------${it.qualityLabel}/${it.contentLength}")
                results.add(
                    ResultItem.ItemData(
                        results.size,
                        MediaType.Video,
                        it.qualityLabel?:it.quality,
                        it.url!!
                    )
                )
            }
            results.add(ResultItem.CategoryTitle("Audio"))
            vidResp.streamingData.adaptiveFormats!!.filter { format -> format.mimeType.contains("audio") }.forEach {
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