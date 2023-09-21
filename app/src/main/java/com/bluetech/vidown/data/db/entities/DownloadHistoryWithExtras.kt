package com.bluetech.vidown.data.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.bluetech.vidown.data.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.data.db.entities.DownloadHistoryItemExtras

data class DownloadHistoryWithExtras(
    @Embedded val downloadHistoryEntity: DownloadHistoryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "mainItemId"
    )
    val downloadHistoryItemExtras : List<DownloadHistoryItemExtras>
)
