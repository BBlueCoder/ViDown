package com.bluetech.vidown.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bluetech.vidown.data.db.dao.DownloadHistoryDao
import com.bluetech.vidown.domain.GetPendingDownloadsAsQueueUseCase
import com.bluetech.vidown.data.repos.DBRepo
import com.bluetech.vidown.workers.DownloadFileWorker
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