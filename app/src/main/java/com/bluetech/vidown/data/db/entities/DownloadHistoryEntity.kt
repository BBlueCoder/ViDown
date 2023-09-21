package com.bluetech.vidown.data.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class DownloadHistoryEntity(
    @PrimaryKey(autoGenerate = true) val uid : Long,
    val type: MediaType,
    val title: String,
    val savedName : String,
    val originalUrl : String,
    val quality : String?,
    @Embedded val downloadData: DownloadData,
    val date : Long
)
