package com.bluetech.vidown.core.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bluetech.vidown.core.MediaType

@Dao
interface MediaDao {

    @Query("Select * from mediaentity order by uid asc limit :limit offset :offset")
    fun getAllMedia(limit: Int,offset : Int): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMedia(mediaEntity: MediaEntity)

    @Delete
    fun deleteMedia(mediaEntity: MediaEntity)

    @Query("Select * from mediaentity order by uid desc limit 7")
    fun getLastSevenRecords(): List<MediaEntity>
}