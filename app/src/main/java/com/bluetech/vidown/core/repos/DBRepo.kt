package com.bluetech.vidown.core.repos

import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DBRepo @Inject constructor(private var mediaDao: MediaDao){

    fun getRecentRecords() = flow{
        try {
            val lastRecords = mediaDao.getLastSevenRecords()
            emit(Result.success(lastRecords))
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }

    fun getMedia(limit : Int,offset : Int) : List<MediaEntity> {
        val list  = mediaDao.getAllMedia(limit,offset)
        val _list = mutableListOf<MediaEntity>()
        for(i in 0..20){
            _list.add(list.first())
        }
        return _list.toList()
    }

}