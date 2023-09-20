package com.bluetech.vidown.core.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bluetech.vidown.core.MediaType

@Entity
data class DownloadHistoryItemExtras(
    @PrimaryKey(autoGenerate = true) val uid : Long,
    val mainItemId : Long,
    val savedName : String,
    val mediaType: MediaType,
    @Embedded val downloadData: DownloadData
)
