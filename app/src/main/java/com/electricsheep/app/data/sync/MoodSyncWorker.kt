package com.electricsheep.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.electricsheep.app.data.repository.MoodRepository
import com.electricsheep.app.util.Logger

/**
 * Background worker for syncing mood data with remote server.
 * Runs periodically to keep local and remote data in sync.
 * 
 * Uses WorkManager for reliable background execution even when app is closed.
 */
class MoodSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val moodRepository: MoodRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Logger.info("MoodSyncWorker", "Starting background sync")
            
            val syncResult = moodRepository.syncWithRemote()
            
            syncResult.fold(
                onSuccess = { result ->
                    Logger.info(
                        "MoodSyncWorker",
                        "Background sync completed successfully: " +
                        "pushed=${result.pushedCount}, pulled=${result.pulledCount}, " +
                        "merged=${result.mergedCount}, conflicts=${result.conflictsResolved}"
                    )
                    Result.success()
                },
                onFailure = { error ->
                    Logger.error("MoodSyncWorker", "Background sync failed", error)
                    // Return retry result so WorkManager will retry
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Logger.error("MoodSyncWorker", "Unexpected error during background sync", e)
            // Retry on unexpected errors
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "mood_sync_work"
    }
}

