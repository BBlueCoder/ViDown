package com.bluetech.vidown.utils

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bluetech.vidown.core.db.dao.DownloadHistoryDao
import com.bluetech.vidown.core.domain.GetPendingDownloadsAsQueueUseCase
import com.bluetech.vidown.core.repos.DBRepo
import com.bluetech.vidown.core.workers.DownloadFileWorker
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val dbRepo: DBRepo,
    private val downloadHistoryDao: DownloadHistoryDao,
    private val getPendingDownloadsAsQueueUseCase: GetPendingDownloadsAsQueueUseCase
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = DownloadFileWorker(dbRepo,downloadHistoryDao,getPendingDownloadsAsQueueUseCase,appContext,workerParameters)
}