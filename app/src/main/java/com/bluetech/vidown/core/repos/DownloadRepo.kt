package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.db.AppLocalDB
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepo @Inject constructor(var mediaDao: MediaDao){

    fun getDownloadFiles() = flow {
        val media = mediaDao.getAllMedia()
        emit(Result.success(media))
    }

}