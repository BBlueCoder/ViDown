package com.bluetech.vidown.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bluetech.vidown.core.db.dao.DownloadHistoryDao
import com.bluetech.vidown.core.db.dao.MediaDao
import com.bluetech.vidown.core.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.core.db.entities.MediaEntity
import com.bluetech.vidown.core.db.entities.MediaThumbnail

@Database(
    entities = [MediaEntity::class, DownloadHistoryEntity::class, MediaThumbnail::class,DownloadHistoryItemExtras::class],
    version = 2
)
abstract class AppLocalDB : RoomDatabase() {
    abstract fun mediaDao() : MediaDao

    abstract fun downloadHistoryDao() : DownloadHistoryDao
}