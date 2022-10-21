package com.bluetech.vidown.core.repos

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class TwRepositoryTest {

    private lateinit var twRepository: TwRepo

    @Before
    fun init(){
        twRepository = TwRepo()
    }

    @Test
    fun `return result of valid url twitter video`() = runTest{
//        var result = mutableListOf<ResultItem>()
//
//        launch(UnconfinedTestDispatcher()){
//            twRepository.getResultsAsFlow("https://twitter.com/NarutoXposts/status/1576885834522894337").collectLatest {
//                result = it
//            }
//        }
//
//        val resultCount = result.size
//
//        assertThat(resultCount).isEqualTo(8)
    }

    @Test
    fun `return result of valid url twitter image`() = runTest {
//        val result = mutableListOf<ResultItem>()
//
//        launch(UnconfinedTestDispatcher()){
//            twRepository.getResultsAsFlow("https://twitter.com/Castro1021/status/1576949622869995520").collectLatest {
//                result.clear()
//                result.addAll(it)
//            }
//        }
//
//        val resultCount = result.size
//
//        assertThat(resultCount).isEqualTo(3)
    }

    @Test
    fun `throw exception if url doesn't contain video or image`() = runTest {
//        val result = mutableListOf<ResultItem>()
//
//        launch(UnconfinedTestDispatcher()){
//            twRepository.getResultsAsFlow("https://twitter.com/WatcherGuru/status/1576973612501716").collectLatest {
//                result.addAll(it)
//            }
//        }
//
//        val resultCount = result.size
//
//        assertThat(resultCount).isEqualTo(3)
    }

}