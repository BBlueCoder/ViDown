package com.bluetech.vidown.utils

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.bluetech.vidown.core.db.MediaDao
import com.bluetech.vidown.core.workers.DownloadFileWorker
import javax.inject.Inject

class CustomWorkerFactory @Inject constructor(
    private val mediaDao: MediaDao
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = DownloadFileWorker(mediaDao,appContext,workerParameters)
}