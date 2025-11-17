package com.electricsheep.app.data.sync

import android.content.Context
import androidx.work.WorkerParameters
import com.electricsheep.app.data.repository.MoodRepository

/**
 * Factory for creating MoodSyncWorker instances with dependency injection.
 * WorkManager requires a factory to inject dependencies into workers.
 */
class MoodSyncWorkerFactory(
    private val moodRepository: MoodRepository
) : androidx.work.WorkerFactory() {
    
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): androidx.work.ListenableWorker? {
        return when (workerClassName) {
            MoodSyncWorker::class.java.name -> {
                MoodSyncWorker(appContext, workerParameters, moodRepository)
            }
            else -> null
        }
    }
}

