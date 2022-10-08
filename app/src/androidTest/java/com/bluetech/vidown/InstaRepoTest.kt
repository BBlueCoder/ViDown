package com.bluetech.vidown

import com.bluetech.vidown.pojoclasses.ResultItem
import com.bluetech.vidown.repos.InstaRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class InstaRepoTest {

    private lateinit var instaRepo: InstaRepo

    @Before
    fun setup(){
        instaRepo = InstaRepo()
    }

    @Test
    fun return_results_of_valid_instagram_video_post() = runBlocking {
        val result = mutableListOf<ResultItem>()

        val job = launch{
            instaRepo.getResultsAsFlow("https://www.instagram.com/p/Ci77zUFj1IZ/").collectLatest {
                result.addAll(it)
            }
        }
        job.join()
        val videoUrl = (result[2] as ResultItem.ItemData).url
        println("video url = $videoUrl")

        assertThat(result.size).isEqualTo(3)
    }

    @Test
    fun return_results_of_valid_instagram_image_post() = runBlocking {
        val result = mutableListOf<ResultItem>()

        val job = launch{
            instaRepo.getResultsAsFlow("https://www.instagram.com/p/CjTcobKh0Z7/").collectLatest {
                result.addAll(it)
                println("inside")
            }
        }
        job.join()
        println("outside")
        val videoUrl = (result[2] as ResultItem.ItemData).url
        assertThat(videoUrl).isEqualTo("https://instagram.frba2-2.fna.fbcdn.net/v/t51.2885-15/310829607_1088962215318743_645326645052773876_n.jpg?stp=dst-jpg_e35_p1080x1080&_nc_ht=instagram.frba2-2.fna.fbcdn.net&_nc_cat=1&_nc_ohc=7vLilSFM62sAX-WaAjG&edm=AABBvjUBAAAA&ccb=7-5&oh=00_AT-FSGEfSeprUyBywAkH4bOa8kLH8TKRIjRoP9QcapgW4A&oe=63482420&_nc_sid=83d603")
    }

    @Test
    fun `return_results_of_valid_instagram_mixed_post`() = runBlocking {
        val result = mutableListOf<ResultItem>()

        val job = launch(){
            instaRepo.getResultsAsFlow("https://www.instagram.com/p/Ci-vS91o1YO/").collectLatest {
                result.addAll(it)
            }
        }
        job.join()
        val resultCount = result.size
        assertThat(resultCount).isEqualTo(5)
    }
}