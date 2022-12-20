package com.bluetech.vidown.core.repos

import android.content.Context
import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.db.MediaEntity
import kotlinx.coroutines.flow.flow
import java.io.File
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

    fun removeMedia(mediaEntity : MediaEntity,context : Context) = flow{
        try {
            mediaDao.deleteMedia(mediaEntity)
            val file = File(context.filesDir,mediaEntity.name)
            emit(Result.success(file.delete()))
        }catch (ex : Exception){
            emit(Result.failure(ex))
        }
    }

}