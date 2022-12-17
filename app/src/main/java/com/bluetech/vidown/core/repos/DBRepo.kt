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
            emit(Result.success(mediaDao.getLastSevenRecords()))
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }

    fun getMedia(limit : Int,offset : Int) = mediaDao.getAllMedia(limit,offset)

    fun getLastFavorites() = flow{
        try {
            emit(Result.success(mediaDao.getLastFavorites()))
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }

    fun updateMediaFavorite(id : Int,favorite : Boolean){
        mediaDao.updateMediaFavorite(id,favorite)
    }

}