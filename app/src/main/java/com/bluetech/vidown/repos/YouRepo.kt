package com.bluetech.vidown.repos

import com.bluetech.vidown.pojoclasses.ResultItem
import com.dabluecoder.youdownloaderlib.YouClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class YouRepo : BaseRepo() {

    override fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow{

        val results = mutableListOf<ResultItem>()
        try {
            val youClient = YouClient(url)

            val videoTitle = youClient.getVideoTitle()
            val videoThumbnail = youClient.getVideoThumbnail()

            results.add(ResultItem.CategoryTitle("Video"))
            results.add(
                ResultItem.ItemInfo(
                    videoTitle,
                    videoThumbnail
                )
            )

            emit(Result.success(results.toList()))

            val vidResp = youClient.getVideoAllData()

            vidResp.streamingData.mixedFormats!!.forEach {
                results.add(
                    ResultItem.ItemData(
                        results.size,
                        "video",
                        it.url!!
                    )
                )
            }

            vidResp.streamingData.adaptiveFormats!!.forEach {
                results.add(
                    ResultItem.ItemData(
                        results.size,
                        if (it.mimeType.contains("audio")) "audio" else "video",
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