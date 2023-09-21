package com.bluetech.vidown.core.workers

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.bluetech.vidown.workers.BlurImage
import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class BlurImageTest {

    private lateinit var context : Context
    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun blurImage_test() = runBlocking {
        val imageName = "image.jpg"
        val blurImage = BlurImage(context,imageName)()
        Truth.assertThat(blurImage).isEqualTo("blurred_image.jpg")
    }
}