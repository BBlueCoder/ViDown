package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwRepo @Inject constructor(): BaseRepo(){

    override suspend fun getResultsAsFlow(url: String): Flow<Result<List<ResultItem>>> = flow {
        val results = mutableListOf<ResultItem>()

        try {
            val splitUrl = url.split("/")

            val sssUrl =
                "${Constants.TWITTER_SSS_BASE_URL}${splitUrl[3]}/${splitUrl[4]}/${splitUrl[5]}"

            val doc = Jsoup.connect(sssUrl).post()

            val resultClassHTML = doc.getElementsByClass("result_overlay")
            if (resultClassHTML.isEmpty())
                throw IllegalArgumentException("This url is invalid")

            val firstResultClassHTML = resultClassHTML.first()

            if (!isTweetContainsMedia(firstResultClassHTML))
                throw NullPointerException("This url does not contain any media")

            val typeOfTweet = tweetMediaType(firstResultClassHTML)

            val elementsP = firstResultClassHTML.getElementsByTag("p")
            val elementsImg = firstResultClassHTML.getElementsByTag("img")

            results.add(ResultItem.CategoryTitle(typeOfTweet))

            if (typeOfTweet == "Image") {
                val imgSource = elementsImg.first().attr("src")
                results.add(ResultItem.ItemInfo("", imgSource))
                results.add(ResultItem.ItemData(1, MediaType.Image, "",imgSource))
                emit(Result.success(results.toList()))
                return@flow
            }

            results.add(
                ResultItem.ItemInfo(
                    elementsP.first().html(),
                    elementsImg.first().attr("src")
                )
            )

            emit(Result.success(results.toList()))

            val elementsLinks = firstResultClassHTML.getElementsByTag("a")

            for ((id, element) in elementsLinks.withIndex()) {
                results.add(
                    ResultItem.ItemData(id, MediaType.Video, element.html().substring(
                        element.html().indexOf("x")
                    ),element.attr("href"))
                )
            }

            emit(Result.success(results.toList()))
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }

    private fun isTweetContainsMedia(resultElement : Element) : Boolean {
        val pElements = resultElement.getElementsByTag("p")

        return pElements.isNotEmpty()
    }

    private fun tweetMediaType(resultElement : Element) : String{
        val pElements = resultElement.getElementsByTag("p")

        if(pElements.first().html().lowercase().contains("but it contains some image instead")){
            return "Image"
        }

        return "Video"
    }


}