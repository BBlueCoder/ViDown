package com.bluetech.vidown.repos

import com.bluetech.vidown.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constantes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class TwRepository : BaseRepo(){

    override fun getResultsAsFlow(url : String): Flow<MutableList<ResultItem>> = flow {
        val results = mutableListOf<ResultItem>()
        val splitUrl = url.split("/")

        val sssUrl = "${Constantes.TWITTER_SSS_BASE_URL}${splitUrl[3]}/${splitUrl[4]}/${splitUrl[5]}"

        val doc = Jsoup.connect(sssUrl).post()

        val resultClassHTML = doc.getElementsByClass("result_overlay")
        if(resultClassHTML.isEmpty())
            throw IllegalArgumentException("This url is invalid")

        val firstResultClassHTML = resultClassHTML.first()

        if(!isTweetContainsMedia(firstResultClassHTML))
            throw NullPointerException("This url does not contain any media")

        val typeOfTweet = tweetMediaType(firstResultClassHTML)

        val elementsP = firstResultClassHTML.getElementsByTag("p")
        val elementsImg = firstResultClassHTML.getElementsByTag("img")

        results.add(ResultItem.CategoryTitle(typeOfTweet))

        if(typeOfTweet == "Image"){
            val imgSource = elementsImg.first().attr("src")
            results.add(ResultItem.ItemInfo("",imgSource))
            results.add(ResultItem.ItemData(1,"image",imgSource))
            emit(results)
            return@flow
        }

        results.add(ResultItem.ItemInfo(elementsP.first().html(),elementsImg.first().attr("src")))

        emit(results)

        val elementsLinks = firstResultClassHTML.getElementsByTag("a")

        for((id, element) in elementsLinks.withIndex()){
            results.add(
                ResultItem.ItemData(id,"video",element.attr("href"))
            )
        }

        emit(results)
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