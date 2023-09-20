package com.bluetech.vidown.core.domain

import com.bluetech.vidown.core.MediaType
import com.bluetech.vidown.core.db.entities.DownloadData
import com.bluetech.vidown.core.db.entities.DownloadHistoryEntity
import com.bluetech.vidown.core.db.entities.DownloadHistoryItemExtras
import com.bluetech.vidown.core.db.entities.DownloadStatus
import com.bluetech.vidown.core.pojoclasses.ResultItem
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.utils.Constants
import javax.inject.Inject

class AddNewDownloadUseCase @Inject constructor(private var dbRepo: DBRepo) {

    operator fun invoke(itemData : ResultItem.ItemData,itemInfo : ResultItem.ItemInfo,separatedAudioUrl : String?){
        val savedName = Constants.generateFileName()
        val downloadHistoryEntity = DownloadHistoryEntity(
            0,
            itemData.format,
            itemInfo.title,
            savedName,
            itemInfo.link,
            itemData.quality,
            createNewDownloadData(itemData.url),
            Constants.getTimeInMillis()
        )

        val mediaId = dbRepo.addDownloadHistoryItem(downloadHistoryEntity)
        val thumbnailExtra = createDownloadExtra(mediaId,MediaType.Image,itemInfo.thumbnail)
        dbRepo.addDownloadExtras(thumbnailExtra)

        separatedAudioUrl?.let {
            val audioExtra = createDownloadExtra(mediaId,MediaType.Audio,it)

            dbRepo.addDownloadExtras(audioExtra)
        }
    }

    private fun createNewDownloadData(url : String): DownloadData {
        return DownloadData(url,DownloadStatus.PENDING,0,0)
    }

    private fun createDownloadExtra(id : Long, type : MediaType,url : String): DownloadHistoryItemExtras {
        return DownloadHistoryItemExtras(
            0,
            id,
            Constants.generateFileName(),
            type,
            createNewDownloadData(url)
        )
    }
}