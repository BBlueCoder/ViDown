package com.bluetech.vidown.core.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bluetech.vidown.core.MediaType
import retrofit2.http.GET

@Dao
interface MediaDao {

    @Query("Select * from mediaentity order by uid desc limit :limit offset :offset")
    fun getAllMedia(limit: Int,offset : Int): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMedia(mediaEntity: MediaEntity)

    @Delete
    fun deleteMedia(mediaEntity: MediaEntity)

    @Query("Select * from mediaentity where favorite = 1 order by uid desc limit 7")
    fun getLastFavorites() : List<MediaEntity>

    @Query("Select * from mediaentity order by uid desc limit 7")
    fun getLastSevenRecords(): List<MediaEntity>

    @Query("update mediaentity set favorite = :favorite where uid = :id")
    fun updateMediaFavorite(id : Int,favorite : Boolean)

}