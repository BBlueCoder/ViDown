package com.bluetech.vidown.repos

import com.bluetech.vidown.api.ApplicationApi
import com.bluetech.vidown.pojoclasses.ResultItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject


class InstaRepo : BaseRepo() {

    @Inject
    lateinit var api : ApplicationApi

    override fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow {

        val results = mutableListOf<ResultItem>()

        try {
            val resp = api.getInstaPostJSONData("$url?__a=1&__d=dis")
            if (!resp.isSuccessful)
                throw Exception("Error : url is invalid")

            val instaItem = resp.body()!!.graphql.InstaMedia

            println("type : ${instaItem.typeName}")

            when (instaItem.typeName) {
                "GraphImage" -> { // post is an image
                    results.add(ResultItem.CategoryTitle("Image"))
                    results.add(
                        ResultItem.ItemInfo(
                            instaItem.mediaCaption.edges.first().node.text,
                            instaItem.thumbnail
                        )
                    )
                    results.add(ResultItem.ItemData(results.size, "image", instaItem.thumbnail))
                    emit(Result.success(results.toList()))
                }
                "GraphVideo" -> { // post is a video
                    results.add(ResultItem.CategoryTitle("Video"))
                    results.add(
                        ResultItem.ItemInfo(
                            instaItem.mediaCaption.edges.first().node.text,
                            instaItem.thumbnail
                        )
                    )
                    results.add(ResultItem.ItemData(results.size, "video", instaItem.videoUrl!!))
                    emit(Result.success(results.toList()))
                }
                "GraphSidecar" -> { // post is multiples
                    results.add(
                        ResultItem.ItemInfo(
                            instaItem.mediaCaption.edges.first().node.text,
                            instaItem.thumbnail
                        )
                    )
                    for (media in instaItem.mediaSideCar.edges) {
                        when (media.node.typeName) {
                            "GraphImage" -> {
                                results.add(ResultItem.CategoryTitle("Image"))
                                results.add(
                                    ResultItem.ItemData(
                                        results.size,
                                        "image",
                                        media.node.thumbnail
                                    )
                                )
                            }
                            "GraphVideo" -> {
                                results.add(ResultItem.CategoryTitle("Video"))
                                results.add(
                                    ResultItem.ItemData(
                                        results.size,
                                        "video",
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
}