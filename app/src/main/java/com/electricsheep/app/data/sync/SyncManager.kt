package com.electricsheep.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.util.Logger
import java.util.concurrent.TimeUnit

/**
 * Manages background sync scheduling using WorkManager.
 * Provides methods to start, stop, and configure periodic sync.
 */
class SyncManager(
    private val context: Context,
    private val featureFlagManager: FeatureFlagManager,
    private val syncIntervalMinutes: Long = SyncConfig.DEFAULT_SYNC_INTERVAL_MINUTES
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Start periodic background sync.
     * Sync will run at the configured interval when network is available.
     * Skips scheduling if offline-only mode is enabled.
     */
    fun startPeriodicSync() {
        // Check if offline-only mode is enabled
        if (featureFlagManager.isOfflineOnly()) {
            Logger.info("SyncManager", "Periodic sync not started: offline-only mode is enabled")
            return
        }
        
        val interval = SyncConfig.getSyncIntervalMinutes(syncIntervalMinutes)
        val intervalMillis = interval * 60 * 1000L
        
        Logger.info("SyncManager", "Starting periodic sync with interval: $interval minutes")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWork = PeriodicWorkRequestBuilder<MoodSyncWorker>(
            interval,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            MoodSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            syncWork
        )
        
        Logger.info("SyncManager", "Periodic sync scheduled")
    }
    
    /**
     * Stop periodic background sync.
     */
    fun stopPeriodicSync() {
        Logger.info("SyncManager", "Stopping periodic sync")
        workManager.cancelUniqueWork(MoodSyncWorker.WORK_NAME)
    }
    
    /**
     * Trigger an immediate sync (one-time work).
     * Useful for manual sync or when app comes online.
     * Skips if offline-only mode is enabled.
     */
    fun triggerImmediateSync() {
        // Check if offline-only mode is enabled
        if (featureFlagManager.isOfflineOnly()) {
            Logger.info("SyncManager", "Immediate sync skipped: offline-only mode is enabled")
            return
        }
        
        Logger.info("SyncManager", "Triggering immediate sync")
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWork = androidx.work.OneTimeWorkRequestBuilder<MoodSyncWorker>()
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()
        
        workManager.enqueue(syncWork)
    }
    
    companion object {
        private const val WORK_TAG = "mood_sync"
    }
}

