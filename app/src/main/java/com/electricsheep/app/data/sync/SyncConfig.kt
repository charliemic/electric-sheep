package com.electricsheep.app.data.sync

/**
 * Configuration for background sync operations.
 * Provides sensible defaults while allowing customisation.
 */
object SyncConfig {
    /**
     * Default sync interval in minutes.
     * Set to 15 minutes to balance freshness with backend load.
     * This means sync will run at most 96 times per day, which is reasonable.
     */
    const val DEFAULT_SYNC_INTERVAL_MINUTES = 15L
    
    /**
     * Minimum sync interval in minutes.
     * Prevents excessive syncing that could drain battery or spam backend.
     */
    const val MIN_SYNC_INTERVAL_MINUTES = 5L
    
    /**
     * Maximum sync interval in minutes.
     * Ensures data doesn't get too stale.
     */
    const val MAX_SYNC_INTERVAL_MINUTES = 60L
    
    /**
     * Get sync interval in minutes, clamped to valid range.
     */
    fun getSyncIntervalMinutes(customInterval: Long? = null): Long {
        val interval = customInterval ?: DEFAULT_SYNC_INTERVAL_MINUTES
        return interval.coerceIn(MIN_SYNC_INTERVAL_MINUTES, MAX_SYNC_INTERVAL_MINUTES)
    }
    
    /**
     * Get sync interval in milliseconds for WorkManager.
     */
    fun getSyncIntervalMillis(customInterval: Long? = null): Long {
        return getSyncIntervalMinutes(customInterval) * 60 * 1000
    }
}

