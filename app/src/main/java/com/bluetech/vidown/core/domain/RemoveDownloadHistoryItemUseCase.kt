package com.bluetech.vidown.core.domain

import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.repos.DBRepo
import javax.inject.Inject

class RemoveDownloadHistoryItemUseCase @Inject constructor(
    private var dbRepo: DBRepo
) {

    operator fun invoke(downloadHistoryWithExtras: DownloadHistoryWithExtras) {
        println("********************************* delete history called")
        downloadHistoryWithExtras.downloadHistoryItemExtras.forEach {
            dbRepo.deleteDownloadExtra(it)
        }
        dbRepo.deleteDownloadHistoryItem(downloadHistoryWithExtras.downloadHistoryEntity)

    }
}