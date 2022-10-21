package com.bluetech.vidown.core.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MediaEntity::class],
    version = 1
)
abstract class AppLocalDB : RoomDatabase() {
    abstract fun mediaDao() : MediaDao
}