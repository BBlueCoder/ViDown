package com.bluetech.vidown.core.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bluetech.vidown.core.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Dao
interface DownloadHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDownloadHistoryItem(downloadHistoryEntity: DownloadHistoryEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDownloadExtras(downloadHistoryItemExtras: DownloadHistoryItemExtras)

    @Delete
    fun deleteDownloadExtras(downloadHistoryItemExtras: DownloadHistoryItemExtras)

    @Update
    fun updateDownloadHistoryItem(downloadHistoryEntity: DownloadHistoryEntity)

    @Update
    fun updateDownloadItemExtra(downloadHistoryItemExtras: DownloadHistoryItemExtras)

    @Transaction
    @Query("Select * from DownloadHistoryEntity order by date desc")
    fun getAllDownloadHistory() : Flow<List<DownloadHistoryWithExtras>>

    @Transaction
    @Query("SELECT * from DownloadHistoryEntity where downloadStatus = 'PENDING' or downloadStatus = 'INPROGRESS'")
    fun getPendingDownloads() : List<DownloadHistoryWithExtras>

    @Transaction
    @Query("Select * from DownloadHistoryEntity where downloadStatus = 'INPROGRESS'")
    fun getDownloadInProgressStream() : Flow<List<DownloadHistoryWithExtras>>

    @Query("Select * from DownloadHistoryEntity where uid = :id")
    fun getDownloadHistoryEntity(id : Long) : DownloadHistoryEntity

    @Query("Select count(uid) from DownloadHistoryEntity where downloadStatus = 'PENDING' or downloadStatus = 'INPROGRESS'")
    fun getCountOfUncompletedDownloads() : Int

    @Query("Delete from DownloadHistoryEntity where uid = :id")
    fun deleteDownloadHistoryItem(id : Long)
}