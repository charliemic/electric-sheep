package com.electricsheep.app.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.electricsheep.app.data.repository.MoodRepository
import com.electricsheep.app.error.NetworkError
import com.electricsheep.app.error.SystemError
import com.electricsheep.app.util.Logger

/**
 * Background worker for syncing mood data with remote server.
 * Runs periodically to keep local and remote data in sync.
 * 
 * Uses WorkManager for reliable background execution even when app is closed.
 * Handles errors appropriately: retries recoverable errors, fails on non-recoverable errors.
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
                    // Handle different error types appropriately
                    when (error) {
                        is NetworkError -> {
                            error.log("MoodSyncWorker", "Background sync failed - network error")
                            if (error.isRecoverable && error.shouldRetry) {
                                // Retry with exponential backoff (WorkManager handles this)
                                Logger.info("MoodSyncWorker", "Retrying sync after ${error.retryDelayMillis}ms")
                                Result.retry()
                            } else {
                                // Non-retryable network error (e.g., unauthorized) - fail
                                Logger.warn("MoodSyncWorker", "Sync failed with non-retryable network error")
                                Result.failure()
                            }
                        }
                        is SystemError -> {
                            error.log("MoodSyncWorker", "Background sync failed - system error")
                            // System errors are non-recoverable - don't retry
                            Result.failure()
                        }
                        else -> {
                            Logger.error("MoodSyncWorker", "Background sync failed with unknown error", error)
                            // Unknown errors - retry once
                            Result.retry()
                        }
                    }
                }
            )
        } catch (e: NetworkError) {
            e.log("MoodSyncWorker", "Unexpected network error during background sync")
            if (e.isRecoverable && e.shouldRetry) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (e: SystemError) {
            e.log("MoodSyncWorker", "Unexpected system error during background sync")
            Result.failure()
        } catch (e: Exception) {
            Logger.error("MoodSyncWorker", "Unexpected error during background sync", e)
            // Unknown errors - retry once
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "mood_sync_work"
    }
}

