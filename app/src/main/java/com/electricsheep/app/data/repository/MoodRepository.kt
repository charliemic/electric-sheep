package com.electricsheep.app.data.repository

import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.data.local.MoodDao
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.data.remote.SupabaseDataSource
import com.electricsheep.app.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Repository for Mood data management.
 * Implements offline-first pattern: local storage is source of truth,
 * remote sync happens in background.
 * All operations are scoped to the current authenticated user.
 */
class MoodRepository(
    private val moodDao: MoodDao,
    private val remoteDataSource: SupabaseDataSource?,
    private val featureFlagManager: FeatureFlagManager,
    private val userManager: UserManager
) {
    /**
     * Get all moods for the current user (from local storage, which is always available)
     */
    fun getAllMoods(): Flow<List<Mood>> {
        val userId = userManager.requireUserId()
        Logger.debug("MoodRepository", "Getting all moods for user: $userId")
        return moodDao.getAllMoods(userId)
    }
    
    /**
     * Get moods within a date range for the current user (from local storage)
     */
    fun getMoodsByDateRange(startTime: Long, endTime: Long): Flow<List<Mood>> {
        val userId = userManager.requireUserId()
        Logger.debug("MoodRepository", "Getting moods by date range for user: $userId")
        return moodDao.getMoodsByDateRange(userId, startTime, endTime)
    }
    
    /**
     * Save a mood entry (offline-first: save locally, sync to remote in background)
     */
    suspend fun saveMood(mood: Mood): Result<Mood> {
        return try {
            // Validate mood
            if (!mood.isValid()) {
                Logger.warn("MoodRepository", "Invalid mood data: score=${mood.score}, timestamp=${mood.timestamp}")
                return Result.failure(IllegalArgumentException("Invalid mood data"))
            }
            
            // Ensure mood has userId (use current user if not set)
            val userId = userManager.requireUserId()
            val moodWithUser = if (mood.userId.isBlank()) {
                mood.copy(userId = userId)
            } else {
                // Verify userId matches current user (security check)
                require(mood.userId == userId) { "Mood userId must match current user" }
                mood
            }
            
            // Generate ID if not provided (should always be provided, but handle gracefully)
            val moodWithId = if (moodWithUser.id.isBlank()) {
                moodWithUser.copy(id = UUID.randomUUID().toString())
            } else {
                moodWithUser
            }
            
            // Set timestamps (createdAt and updatedAt)
            val moodWithTimestamps = moodWithId.withUpdatedTimestamp()
            
            // Save to local storage first (offline-first)
            Logger.info("MoodRepository", "Saving mood locally: score=${moodWithTimestamps.score}, userId=${moodWithTimestamps.userId}")
            
            // Wrap Room operation to handle database-specific errors
            val insertResult = com.electricsheep.app.data.local.RoomErrorHandler.wrapRoomOperation(
                operation = { moodDao.insertMood(moodWithTimestamps) },
                context = "insertMood(id=${moodWithTimestamps.id})"
            )
            
            insertResult.getOrThrow() // Throw DataError if insert fails
            
            // Try to sync to remote (non-blocking, failures are logged but don't affect local save)
            // Skip remote sync if offline-only mode is enabled or remote data source is unavailable
            if (!featureFlagManager.isOfflineOnly() && remoteDataSource != null) {
                try {
                    remoteDataSource.insertMood(moodWithTimestamps)
                    Logger.info("MoodRepository", "Mood synced to remote: id=${moodWithTimestamps.id}")
                } catch (e: com.electricsheep.app.error.NetworkError) {
                    // Network errors are recoverable - mood is saved locally, will sync later
                    e.log("MoodRepository", "Failed to sync mood to remote (will retry later)")
                    // Mood is saved locally, sync will happen later via background sync
                } catch (e: Exception) {
                    // Other errors - log and continue
                    Logger.warn("MoodRepository", "Failed to sync mood to remote, will retry later", e)
                    // Mood is saved locally, sync will happen later
                }
            } else {
                Logger.debug("MoodRepository", "Skipping remote sync (offline-only mode enabled or remote unavailable)")
            }
            
            Result.success(moodWithTimestamps)
        } catch (e: com.electricsheep.app.error.NetworkError) {
            // Network errors are already handled above - this shouldn't happen
            e.log("MoodRepository", "Unexpected network error in saveMood")
            Result.failure(e)
        } catch (e: com.electricsheep.app.error.DataError) {
            e.log("MoodRepository", "Data error in saveMood")
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            // Validation errors - re-throw as DataError
            val dataError = com.electricsheep.app.error.DataError.InvalidData(
                field = "mood",
                errorCause = e
            )
            dataError.log("MoodRepository", "Invalid mood data")
            Result.failure(dataError)
        } catch (e: Exception) {
            Logger.error("MoodRepository", "Failed to save mood", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update a mood entry.
     * Automatically sets updatedAt timestamp to current time.
     * If multiple edits are made offline, only the latest (by updatedAt) will be synced.
     */
    suspend fun updateMood(mood: Mood): Result<Mood> {
        return try {
            require(mood.id.isNotBlank()) { "Mood must have an id to update" }
            
            // Verify userId matches current user (security check)
            val userId = userManager.requireUserId()
            require(mood.userId == userId) { "Mood userId must match current user" }
            
            if (!mood.isValid()) {
                Logger.warn("MoodRepository", "Invalid mood data for update")
                return Result.failure(IllegalArgumentException("Invalid mood data"))
            }
            
            // Set updatedAt timestamp to current time
            val moodWithTimestamp = mood.withUpdatedTimestamp()
            
            Logger.info("MoodRepository", "Updating mood: id=${moodWithTimestamp.id}, userId=$userId")
            
            // Update local storage first - wrap Room operation to handle database-specific errors
            val updateResult = com.electricsheep.app.data.local.RoomErrorHandler.wrapRoomOperation(
                operation = { moodDao.updateMood(moodWithTimestamp) },
                context = "updateMood(id=${moodWithTimestamp.id})"
            )
            
            updateResult.getOrThrow() // Throw DataError if update fails
            
            // Try to sync to remote (skip if offline-only mode is enabled or remote unavailable)
            if (!featureFlagManager.isOfflineOnly() && remoteDataSource != null) {
                try {
                    remoteDataSource.updateMood(moodWithTimestamp)
                    Logger.info("MoodRepository", "Mood update synced to remote")
                } catch (e: com.electricsheep.app.error.NetworkError) {
                    // Network errors are recoverable - mood is updated locally, will sync later
                    e.log("MoodRepository", "Failed to sync mood update to remote (will retry later)")
                    // Mood is updated locally, sync will happen later via background sync
                } catch (e: Exception) {
                    // Other errors - log and continue
                    Logger.warn("MoodRepository", "Failed to sync mood update to remote", e)
                    // Mood is updated locally, sync will happen later
                }
            } else {
                Logger.debug("MoodRepository", "Skipping remote sync (offline-only mode enabled or remote unavailable)")
            }
            
            Result.success(moodWithTimestamp)
        } catch (e: com.electricsheep.app.error.NetworkError) {
            // Network errors are already handled above - this shouldn't happen
            e.log("MoodRepository", "Unexpected network error in updateMood")
            Result.failure(e)
        } catch (e: com.electricsheep.app.error.DataError) {
            e.log("MoodRepository", "Data error in updateMood")
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            // Validation errors - re-throw as DataError
            val dataError = com.electricsheep.app.error.DataError.InvalidData(
                field = "mood",
                errorCause = e
            )
            dataError.log("MoodRepository", "Invalid mood data")
            Result.failure(dataError)
        } catch (e: Exception) {
            Logger.error("MoodRepository", "Failed to update mood", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a mood entry
     */
    suspend fun deleteMood(id: String): Result<Unit> {
        return try {
            val userId = userManager.requireUserId()
            Logger.info("MoodRepository", "Deleting mood: id=$id, userId=$userId")
            
            // Verify mood belongs to current user before deleting
            val mood = moodDao.getMoodById(id, userId)
            if (mood == null) {
                Logger.warn("MoodRepository", "Mood not found or doesn't belong to user: id=$id")
                return Result.failure(IllegalArgumentException("Mood not found"))
            }
            
            // Delete from local storage first - wrap Room operation to handle database-specific errors
            val deleteResult = com.electricsheep.app.data.local.RoomErrorHandler.wrapRoomOperation(
                operation = { moodDao.deleteMoodById(id) },
                context = "deleteMood(id=$id)"
            )
            
            deleteResult.getOrThrow() // Throw DataError if delete fails
            
            // Try to sync deletion to remote (skip if offline-only mode is enabled or remote unavailable)
            if (!featureFlagManager.isOfflineOnly() && remoteDataSource != null) {
                try {
                    remoteDataSource.deleteMood(id)
                    Logger.info("MoodRepository", "Mood deletion synced to remote")
                } catch (e: Exception) {
                    Logger.warn("MoodRepository", "Failed to sync mood deletion to remote", e)
                }
            } else {
                Logger.debug("MoodRepository", "Skipping remote sync (offline-only mode enabled or remote unavailable)")
            }
            
            Result.success(Unit)
        } catch (e: com.electricsheep.app.error.NetworkError) {
            // Network errors are already handled above - this shouldn't happen
            e.log("MoodRepository", "Unexpected network error in deleteMood")
            Result.failure(e)
        } catch (e: com.electricsheep.app.error.DataError) {
            e.log("MoodRepository", "Data error in deleteMood")
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            // Validation errors - re-throw as DataError
            val dataError = com.electricsheep.app.error.DataError.NotFound(
                resource = "mood",
                errorCause = e
            )
            dataError.log("MoodRepository", "Mood not found")
            Result.failure(dataError)
        } catch (e: Exception) {
            Logger.error("MoodRepository", "Failed to delete mood", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sync local data with remote (bidirectional sync with conflict resolution).
     * 
     * Sync strategy:
     * 1. Push local changes to remote (upsert)
     * 2. Pull remote changes and merge with local
     * 3. Conflict resolution: latest edit wins (based on updatedAt timestamp)
     * 
     * Since only this app can create/edit moods, conflicts only occur when:
     * - Multiple edits made offline to the same mood
     * - Latest edit (by updatedAt) wins
     */
    suspend fun syncWithRemote(): Result<SyncResult> {
        return try {
            // Check if offline-only mode is enabled
            if (featureFlagManager.isOfflineOnly()) {
                Logger.info("MoodRepository", "Sync skipped: offline-only mode is enabled")
                return Result.success(
                    SyncResult(
                        pushedCount = 0,
                        pulledCount = 0,
                        mergedCount = 0,
                        conflictsResolved = 0
                    )
                )
            }
            
            Logger.info("MoodRepository", "Starting bidirectional sync with remote")
            
            // Step 1: Get all local moods for current user
            val userId = userManager.requireUserId()
            val localMoods = moodDao.getAllMoods(userId).first()
            Logger.debug("MoodRepository", "Found ${localMoods.size} local moods for user: $userId")
            
            // Check if remote data source is available
            if (remoteDataSource == null) {
                Logger.info("MoodRepository", "Sync skipped: remote data source unavailable")
                return Result.success(
                    SyncResult(
                        pushedCount = 0,
                        pulledCount = 0,
                        mergedCount = 0,
                        conflictsResolved = 0
                    )
                )
            }
            
            // Step 2: Push local changes to remote (upsert)
            var pushedCount = 0
            try {
                if (localMoods.isNotEmpty()) {
                    remoteDataSource.upsertMoods(localMoods)
                    pushedCount = localMoods.size
                    Logger.info("MoodRepository", "Pushed $pushedCount moods to remote")
                }
            } catch (e: com.electricsheep.app.error.NetworkError) {
                // Network errors are recoverable - log and continue
                e.log("MoodRepository", "Failed to push local changes to remote (will retry later)")
                // Continue with pull even if push fails
            } catch (e: Exception) {
                Logger.warn("MoodRepository", "Failed to push local changes to remote", e)
                // Continue with pull even if push fails
            }
            
            // Step 3: Pull remote changes for current user
            val remoteMoods = try {
                remoteDataSource.getAllMoods(userId)
            } catch (e: com.electricsheep.app.error.NetworkError) {
                // Network errors are recoverable - return partial success
                e.log("MoodRepository", "Failed to pull remote changes (will retry later)")
                // If pull fails but push succeeded, return partial success
                return Result.success(
                    SyncResult(
                        pushedCount = pushedCount,
                        pulledCount = 0,
                        mergedCount = 0,
                        conflictsResolved = 0
                    )
                )
            } catch (e: Exception) {
                Logger.error("MoodRepository", "Failed to pull remote changes", e)
                // If pull fails but push succeeded, return partial success
                return Result.success(
                    SyncResult(
                        pushedCount = pushedCount,
                        pulledCount = 0,
                        mergedCount = 0,
                        conflictsResolved = 0
                    )
                )
            }
            
            Logger.debug("MoodRepository", "Fetched ${remoteMoods.size} moods from remote")
            
            // Step 4: Merge remote with local (conflict resolution: latest wins)
            val (moodsToSave, mergeResult) = mergeMoods(localMoods, remoteMoods)
            
            // Step 5: Save merged moods to local storage (wrap Room operations)
            if (moodsToSave.isNotEmpty()) {
                val insertResult = com.electricsheep.app.data.local.RoomErrorHandler.wrapRoomOperation(
                    operation = { moodDao.insertMoods(moodsToSave) },
                    context = "syncWithRemote - insert merged moods"
                )
                insertResult.getOrThrow() // Throw DataError if insert fails
            }
            
            Logger.info(
                "MoodRepository",
                "Sync completed: pushed=$pushedCount, pulled=${remoteMoods.size}, " +
                "merged=${mergeResult.mergedCount}, conflicts=${mergeResult.conflictsResolved}"
            )
            
            Result.success(mergeResult.copy(pushedCount = pushedCount))
        } catch (e: com.electricsheep.app.error.DataError) {
            // Data errors (e.g., database corruption) - log and fail
            e.log("MoodRepository", "Failed to sync with remote - data error")
            Result.failure(e)
        } catch (e: Exception) {
            Logger.error("MoodRepository", "Failed to sync with remote", e)
            Result.failure(e)
        }
    }
    
    /**
     * Merge remote moods with local moods, resolving conflicts.
     * Conflict resolution: latest edit wins (based on updatedAt timestamp).
     * 
     * @param localMoods Current local moods
     * @param remoteMoods Moods from remote
     * @return Pair of (moods to save, sync result statistics)
     */
    private suspend fun mergeMoods(
        localMoods: List<Mood>,
        remoteMoods: List<Mood>
    ): Pair<List<Mood>, SyncResult> {
        val localMap = localMoods.associateBy { it.id }
        val remoteMap = remoteMoods.associateBy { it.id }
        
        val allIds = (localMap.keys + remoteMap.keys).toSet()
        val moodsToSave = mutableListOf<Mood>()
        var conflictsResolved = 0
        
        for (id in allIds) {
            val local = localMap[id]
            val remote = remoteMap[id]
            
            when {
                // New remote mood (not in local)
                local == null && remote != null -> {
                    moodsToSave.add(remote)
                }
                // Local-only mood (not in remote) - keep local
                local != null && remote == null -> {
                    moodsToSave.add(local)
                }
                // Conflict: both exist - latest edit wins
                local != null && remote != null -> {
                    val winner = if (local.isNewerThan(remote)) {
                        local
                    } else {
                        remote
                    }
                    moodsToSave.add(winner)
                    
                    // Count as conflict if they're different
                    if (local.score != remote.score || local.timestamp != remote.timestamp) {
                        conflictsResolved++
                        Logger.debug(
                            "MoodRepository",
                            "Conflict resolved for mood $id: local updatedAt=${local.updatedAt}, " +
                            "remote updatedAt=${remote.updatedAt}, winner=${if (local.isNewerThan(remote)) "local" else "remote"}"
                        )
                    }
                }
            }
        }
        
        // Note: Saving is handled by caller with error handling wrapper
        val syncResult = SyncResult(
            pushedCount = 0, // Will be set by caller
            pulledCount = remoteMoods.size,
            mergedCount = moodsToSave.size,
            conflictsResolved = conflictsResolved
        )
        return Pair(moodsToSave, syncResult)
    }
}

/**
 * Result of a sync operation with statistics.
 */
data class SyncResult(
    val pushedCount: Int,
    val pulledCount: Int,
    val mergedCount: Int,
    val conflictsResolved: Int
)

