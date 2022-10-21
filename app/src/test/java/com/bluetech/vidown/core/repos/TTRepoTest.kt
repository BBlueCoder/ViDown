package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.api.ApplicationApi
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.utils.Constants
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TTRepoTest(){

    private lateinit var ttRepo: TTRepo

    @Before
    fun setUp(){
        ttRepo = TTRepo(
            Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL_EXAMPLE)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApplicationApi::class.java)
        )
    }

    @Test
    fun `test with a valid video url`() = runBlocking {
        var url = ""
        val job = launch(UnconfinedTestDispatcher()) {
            ttRepo.getResultsAsFlow("https://www.tiktok.com/@mcfckun10/video/7122510297075584261").collectLatest {
                it.onSuccess { results ->
                    println("before test")
                    url = (results.last() as ResultItem.ItemData).url
                }
                it.onFailure { ex ->
                    println("Error : ${ex.message}")
                }
            }
        }
        println("test")
        job.join()
        assertThat(url).isEqualTo("https://sf16-ies-music-sg.tiktokcdn.com/obj/tos-alisg-ve-2774/91813d1276984ce89ec7d3385b56607e")
    }

    @Test
    fun `test with a valid album link`() = runTest {
        var count = 0
        val job = launch {
            ttRepo.getResultsAsFlow("https://www.tiktok.com/@vastlyreal/video/7148503067300842757").collectLatest {
                it.onSuccess { results ->
                    count = results.size
                }
            }
        }
        job.join()
        assertThat(count).isEqualTo(12)
    }
}