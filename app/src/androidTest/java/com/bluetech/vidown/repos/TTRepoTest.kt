package com.bluetech.vidown.repos

import com.bluetech.vidown.api.ApplicationApi
import com.bluetech.vidown.pojoclasses.ResultItem
import com.google.common.truth.Truth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class TTRepoTest{

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Named("test_api")
    @Inject
    lateinit var api : ApplicationApi

    private lateinit var ttRepo: TTRepo

    @Before
    fun setUp(){
        hiltRule.inject()
        ttRepo = TTRepo(api)
    }

    @Test
    fun testAValidVideoLink() = runBlocking {
        var url = ""
        val job = launch() {
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
        Truth.assertThat(url).isEqualTo("https://sf16-ies-music-sg.tiktokcdn.com/obj/tos-alisg-ve-2774/91813d1276984ce89ec7d3385b56607e")

    }
}