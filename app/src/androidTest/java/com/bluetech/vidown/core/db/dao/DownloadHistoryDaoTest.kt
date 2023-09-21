package com.bluetech.vidown.core.db.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bluetech.vidown.data.db.entities.MediaType
import com.bluetech.vidown.data.db.AppLocalDB
import com.bluetech.vidown.data.db.entities.DownloadData
import com.bluetech.vidown.data.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.data.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.data.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.data.db.entities.DownloadStatus
import com.bluetech.vidown.data.db.dao.DownloadHistoryDao
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
class DownloadHistoryDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var dao : DownloadHistoryDao

    private val fakeDownloadData = DownloadData("url", DownloadStatus.PENDING,0,0,)
    private val fakeDownloadHistoryItem = DownloadHistoryEntity(
        1,
        MediaType.Audio,
        "title",
        "savedName",
        "url",
        "",
        fakeDownloadData,
        0
    )

    @Named("test_db")
    @Inject
    lateinit var db : AppLocalDB

    @Before
    fun setUp(){
        hiltRule.inject()
        dao = db.downloadHistoryDao()
    }

    @Test
    fun downloadHistoryDao_insertDownloadHistoryItemTest() = runBlocking {
        dao.addDownloadHistoryItem(fakeDownloadHistoryItem)
        val downloadHistoryWithExtras = DownloadHistoryWithExtras(
            fakeDownloadHistoryItem,
            emptyList()
        )
        val list = dao.getPendingDownloads()
        Truth.assertThat(list).contains(downloadHistoryWithExtras)
    }

    @Test
    fun downloadHistoryDao_insertDownloadHistoryItemWithExtrasTest() = runBlocking {
        val downloadHistoryItemExtras = DownloadHistoryItemExtras(1,1,"savedName",
            MediaType.Image,fakeDownloadData)

        dao.addDownloadHistoryItem(fakeDownloadHistoryItem)
        dao.addDownloadExtras(downloadHistoryItemExtras)

        val downloadHistoryWithExtras = DownloadHistoryWithExtras(
            fakeDownloadHistoryItem,
            listOf(downloadHistoryItemExtras)
        )
        val list = dao.getPendingDownloads()
        Truth.assertThat(list).contains(downloadHistoryWithExtras)
    }

    @Test
    fun downloadHistoryDao_deleteDownloadHistoryItemTest() = runBlocking {
        val downloadHistoryItemExtras = DownloadHistoryItemExtras(1,1,"savedName",
            MediaType.Image,fakeDownloadData)

        dao.addDownloadHistoryItem(fakeDownloadHistoryItem)
        dao.addDownloadExtras(downloadHistoryItemExtras)

        dao.deleteDownloadHistoryItem(fakeDownloadHistoryItem.uid)

        val list = dao.getPendingDownloads()
        Truth.assertThat(list).isEmpty()
    }
}