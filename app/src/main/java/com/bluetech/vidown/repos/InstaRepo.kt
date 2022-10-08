package com.bluetech.vidown.repos

import com.bluetech.vidown.api.ApplicationApi
import com.bluetech.vidown.pojoclasses.ResultItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class InstaRepo : BaseRepo() {

    override fun getResultsAsFlow(url: String): Flow<MutableList<ResultItem>>  = flow {

        val results = mutableListOf<ResultItem>()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.instagram.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApplicationApi::class.java)

        val resp = api.getInstaPostJSONData("$url?__a=1&__d=dis")
        if(!resp.isSuccessful)
            throw Exception("Error : ${resp.errorBody()!!.charStream()}")

        val instaItem = resp.body()!!.graphql.InstaMedia

        println("type : ${instaItem.typeName}")

        when(instaItem.typeName){
            "GraphImage" -> { // post is an image
                results.add(ResultItem.CategoryTitle("Image"))
                results.add(ResultItem.ItemInfo(
                    instaItem.mediaCaption.edges.first().node.text,
                    instaItem.thumbnail))
                results.add(ResultItem.ItemData(results.size,"image",instaItem.thumbnail))
                emit(results)
            }
            "GraphVideo" -> { // post is a video
                results.add(ResultItem.CategoryTitle("Video"))
                results.add(ResultItem.ItemInfo(
                    instaItem.mediaCaption.edges.first().node.text,
                    instaItem.thumbnail
                ))
                results.add(ResultItem.ItemData(results.size,"video",instaItem.videoUrl!!))
                emit(results)
            }
            "GraphSidecar" -> { // post is multiples
                results.add(ResultItem.ItemInfo(
                    instaItem.mediaCaption.edges.first().node.text,
                    instaItem.thumbnail
                ))
                for(media in instaItem.mediaSideCar.edges){
                    when(media.node.typeName){
                        "GraphImage" -> {
                            results.add(ResultItem.CategoryTitle("Image"))
                            results.add(ResultItem.ItemData(results.size,"image",media.node.thumbnail))
                        }
                        "GraphVideo" -> {
                            results.add(ResultItem.CategoryTitle("Video"))
                            results.add(ResultItem.ItemData(results.size,"video",media.node.videoUrl!!))
                        }
                    }
                }
                emit(results)
            }
            else -> throw Exception("Media type not supported")
        }
    }
}