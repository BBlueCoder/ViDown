package com.bluetech.vidown.data.repos

import com.bluetech.vidown.data.db.entities.MediaType
import com.bluetech.vidown.data.api.ApplicationApi
import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constants.INSTAGRAM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstaRepo @Inject constructor(private val api: ApplicationApi): BaseRepo() {

    override suspend fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow {

        val results = mutableListOf<ResultItem>()

        try {
            val resp = api.getInstaPostJSONData("${removeUrlParams(url)}?__a=1&__d=dis")
            if (!resp.isSuccessful)
                throw Exception("Error : url is invalid")

            val instaItem = resp.body()!!.graphql.InstaMedia

            println("type : ${instaItem.typeName}")

            when (instaItem.typeName) {
                "GraphImage" -> { // post is an image
                    results.add(ResultItem.CategoryTitle("Image"))
                    results.add(
                        ResultItem.ItemInfo(
                            url,
                            instaItem.mediaCaption.edges.first().node.text,
                            instaItem.thumbnail,
                            INSTAGRAM
                        )
                    )
                    results.add(ResultItem.ItemData(results.size, MediaType.Image, "",instaItem.thumbnail))
                    emit(Result.success(results.toList()))
                }
                "GraphVideo" -> { // post is a video
                    results.add(ResultItem.CategoryTitle("Video"))
                    results.add(
                        ResultItem.ItemInfo(
                            url,
                            instaItem.mediaCaption.edges.first().node.text,
                            instaItem.thumbnail,
                            INSTAGRAM
                        )
                    )
                    results.add(ResultItem.ItemData(results.size, MediaType.Video, "",instaItem.videoUrl!!))
                    emit(Result.success(results.toList()))
                }
                "GraphSidecar" -> { // post is multiples
                    results.add(
                        ResultItem.ItemInfo(
                            url,
                            instaItem.mediaCaption.edges.first().node.text,
                            instaItem.thumbnail,
                            INSTAGRAM
                        )
                    )
                    for (media in instaItem.mediaSideCar.edges) {
                        when (media.node.typeName) {
                            "GraphImage" -> {
                                results.add(ResultItem.CategoryTitle("Image"))
                                results.add(
                                    ResultItem.ItemData(
                                        results.size,
                                        MediaType.Image,
                                        "",
                                        media.node.thumbnail
                                    )
                                )
                            }
                            "GraphVideo" -> {
                                results.add(ResultItem.CategoryTitle("Video"))
                                results.add(
                                    ResultItem.ItemData(
                                        results.size,
                                        MediaType.Video,
                                        "",
                                        media.node.videoUrl!!
                                    )
                                )
                            }
                        }
                    }
                    emit(Result.success(results.toList()))
                }
                else -> throw Exception("Media type not supported")
            }
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }

    private fun removeUrlParams(url : String): String{
        val indexOfTheStartOfTheParams = url.indexOfFirst { it == '?' }
        if(indexOfTheStartOfTheParams == -1)
            return url
        return url.substring(0,url.indexOfFirst { it == '?' })
    }
}