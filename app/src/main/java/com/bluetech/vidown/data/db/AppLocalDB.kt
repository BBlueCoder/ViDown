package com.bluetech.vidown.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bluetech.vidown.data.db.dao.DownloadHistoryDao
import com.bluetech.vidown.data.db.dao.MediaDao
import com.bluetech.vidown.data.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.data.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.data.db.entities.MediaEntity
import com.bluetech.vidown.data.db.entities.MediaThumbnail

@Database(
    entities = [MediaEntity::class, DownloadHistoryEntity::class, MediaThumbnail::class, DownloadHistoryItemExtras::class],
    version = 2
)
abstract class AppLocalDB : RoomDatabase() {
    abstract fun mediaDao() : MediaDao

    abstract fun downloadHistoryDao() : DownloadHistoryDao
}