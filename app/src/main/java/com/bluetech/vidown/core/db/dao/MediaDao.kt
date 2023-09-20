package com.bluetech.vidown.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bluetech.vidown.core.db.entities.MediaWithThumbnail
import com.bluetech.vidown.core.db.entities.MediaEntity
import com.bluetech.vidown.core.db.entities.MediaThumbnail
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Transaction
    @Query("Select * from MediaEntity order by uid desc limit :limit offset :offset")
    fun getAllMedia(limit: Int,offset : Int): List<MediaWithThumbnail>

    @Query("Select * from MediaEntity")
    fun getAllMediaStream() : Flow<List<MediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMedia(mediaEntity: MediaEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addThumbnail(mediaThumbnail: MediaThumbnail)

    @Delete
    fun deleteThumbnail(mediaThumbnail: MediaThumbnail)

    @Delete
    fun deleteMedia(mediaEntity: MediaEntity)

    @Delete
    fun deleteMedias(items : List<MediaEntity>)

    @Transaction
    @Query("Select * from mediaentity where favorite = 1 order by uid asc limit 7")
    fun getLastFavorites() : Flow<List<MediaWithThumbnail>>

    @Transaction
    @Query("Select * from mediaentity order by uid desc limit 7")
    fun getLastSevenRecords(): Flow<List<MediaWithThumbnail>>

    @Transaction
    @Query("update mediaentity set favorite = :favorite where uid = :id")
    fun updateMediaFavorite(id : Long,favorite : Boolean)

    @Query("update mediaentity set title = :title where uid = :id")
    fun updateMediaTitle(title : String,id: Long)

    @Transaction
    @Query("Select * from mediaentity where favorite = 1 limit :limit offset :offset")
    fun getOnlyFavorites(limit: Int,offset: Int): List<MediaWithThumbnail>

    @Transaction
    @Query("Select * from mediaentity where favorite = 1 order by uid asc limit :limit offset :offset")
    fun getOnlyFavoritesByOld(limit: Int,offset: Int): List<MediaWithThumbnail>

    @Transaction
    @Query("Select * from mediaentity order by uid asc limit :limit offset :offset")
    fun getAllMediaByOld(limit: Int,offset: Int): List<MediaWithThumbnail>



}