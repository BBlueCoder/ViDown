package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.db.MediaDao
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DBRepo @Inject constructor(private var mediaDao: MediaDao){

    fun getRecentRecords() =  flow{
        val lastRecords = mediaDao.getLastSevenRecords()
        emit(Result.success(lastRecords))
    }
}