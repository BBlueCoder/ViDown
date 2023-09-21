package com.bluetech.vidown.data.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadHistoryItemExtras(
    @PrimaryKey(autoGenerate = true) val uid : Long,
    val mainItemId : Long,
    val savedName : String,
    val mediaType: MediaType,
    @Embedded val downloadData: DownloadData
)
