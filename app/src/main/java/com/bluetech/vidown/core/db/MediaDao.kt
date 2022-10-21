package com.bluetech.vidown.core.db

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bluetech.vidown.core.MediaType

interface MediaDao {

    @Query("Select * from mediaentity")
    fun getAllMedia(): List<MediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMedia(mediaEntity: MediaEntity)

    @Delete
    fun deleteMedia(mediaEntity: MediaEntity)
}