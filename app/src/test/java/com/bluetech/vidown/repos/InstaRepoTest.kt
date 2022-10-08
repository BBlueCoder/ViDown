package com.bluetech.vidown.repos

import com.bluetech.vidown.pojoclasses.ResultItem
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class InstaRepoTest{
    private lateinit var instaRepo: InstaRepo

    @Before
    fun setup(){
        instaRepo = InstaRepo()
    }

    @Test
    fun `return results of valid instagram video post`() = runTest {
        val result = mutableListOf<ResultItem>()

        launch(UnconfinedTestDispatcher()){
            instaRepo.getResultsAsFlow("https://www.instagram.com/p/Ci77zUFj1IZ/").collectLatest {
                result.addAll(it)
            }
        }

        val resultCount = result.size
        assertThat(resultCount).isEqualTo(5)
    }

    @Test
    fun `return results of valid instagram image post`() = runTest {
        val result = mutableListOf<ResultItem>()

        launch(UnconfinedTestDispatcher()){
            instaRepo.getResultsAsFlow("https://www.instagram.com/p/CjTcobKh0Z7/").collectLatest {
                result.addAll(it)
            }
        }

        val resultCount = result.size
        assertThat(resultCount).isEqualTo(16)
    }

    @Test
    fun `return results of valid instagram mixed post`() = runTest {
        val result = mutableListOf<ResultItem>()

        launch(UnconfinedTestDispatcher()){
            instaRepo.getResultsAsFlow("https://www.instagram.com/p/Ci-vS91o1YO/").collectLatest {
                result.addAll(it)
            }
        }

        val resultCount = result.size
        assertThat(resultCount).isEqualTo(20)
    }
}