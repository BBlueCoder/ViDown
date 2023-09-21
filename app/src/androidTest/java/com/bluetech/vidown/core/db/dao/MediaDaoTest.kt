package com.bluetech.vidown.core.db.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bluetech.vidown.data.db.entities.MediaType
import com.bluetech.vidown.data.db.AppLocalDB
import com.bluetech.vidown.data.db.entities.MediaEntity
import com.bluetech.vidown.data.db.entities.MediaThumbnail
import com.bluetech.vidown.data.db.entities.MediaWithThumbnail
import com.bluetech.vidown.data.db.dao.MediaDao
import com.google.common.truth.Truth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MediaDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Named("test_db")
    @Inject
    lateinit var db : AppLocalDB

    private lateinit var mediaDao: MediaDao

    private val fakeMediaEntity = MediaEntity(1,"savedName", MediaType.Audio,"title",0)
    private val fakeMediaThumbnail = MediaThumbnail(1,1,"name","blur")
    private val fakeMediaWithThumbnail = MediaWithThumbnail(fakeMediaEntity,fakeMediaThumbnail)
    @Before
    fun setUp(){
        hiltRule.inject()
        mediaDao = db.mediaDao()
    }

    @Test
    fun mediaDao_insertMediaEntityTest() = runBlocking {

        mediaDao.addMedia(fakeMediaEntity)
        mediaDao.addThumbnail(fakeMediaThumbnail)

        val list = mediaDao.getAllMedia(25,0)
        Truth.assertThat(list).contains(fakeMediaWithThumbnail)
    }

    @Test
    fun mediaDao_deleteMediaEntityTest() = runBlocking {
        mediaDao.addMedia(fakeMediaEntity)
        mediaDao.addThumbnail(fakeMediaThumbnail)

        mediaDao.deleteMedia(fakeMediaEntity)

        val list = mediaDao.getAllMedia(25,0)
        Truth.assertThat(list).isEmpty()
    }
}