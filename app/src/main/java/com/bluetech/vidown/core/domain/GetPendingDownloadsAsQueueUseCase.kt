package com.bluetech.vidown.core.domain

import com.bluetech.vidown.core.db.entities.DownloadHistoryWithExtras
import com.bluetech.vidown.core.repos.DBRepo
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

class GetPendingDownloadsAsQueueUseCase @Inject constructor(
    private var dbRepo: DBRepo
) {

    operator fun invoke(): Queue<DownloadHistoryWithExtras> {
        val pendingDownloads = dbRepo.getPendingDownloads()
        val queue : Queue<DownloadHistoryWithExtras> = LinkedList()
        queue.addAll(pendingDownloads)
        return queue
    }
}