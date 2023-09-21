package com.bluetech.vidown

import com.bluetech.vidown.data.repos.pojoclasses.ResultItem
import com.bluetech.vidown.data.repos.InstaRepo
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class InstaRepoTest {

    private lateinit var instaRepo: InstaRepo

    @Before
    fun setup(){
        //instaRepo = InstaRepo()
    }

    @Test
    fun return_results_of_valid_instagram_video_post() = runBlocking {

    }

    @Test
    fun return_results_of_valid_instagram_image_post() = runBlocking {
        val result = mutableListOf<ResultItem>()
  }

    @Test
    fun `return_results_of_valid_instagram_mixed_post`() = runBlocking {
//        val result = mutableListOf<ResultItem>()
//
//        val job = launch(){
//            instaRepo.getResultsAsFlow("https://www.instagram.com/p/Ci-vS91o1YO/").collectLatest {
//                result.addAll(it)
//            }
//        }
//        job.join()
//        val resultCount = result.size
//        assertThat(resultCount).isEqualTo(5)
    }
}