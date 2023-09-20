package com.bluetech.vidown.core.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.bluetech.vidown.core.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras

data class DownloadHistoryWithExtras(
    @Embedded val downloadHistoryEntity: DownloadHistoryEntity,
    @Relation(
        parentColumn = "uid",
        entityColumn = "mainItemId"
    )
    val downloadHistoryItemExtras : List<DownloadHistoryItemExtras>
)
