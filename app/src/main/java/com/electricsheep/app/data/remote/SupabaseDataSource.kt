package com.electricsheep.app.data.remote

import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.error.NetworkError
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Remote data source for Mood entities using Supabase.
 * Handles all remote operations and sync with Supabase backend.
 * 
 * All methods throw NetworkError exceptions that can be caught and handled appropriately.
 */
class SupabaseDataSource(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "moods"
    
    /**
     * Wrap exceptions in NetworkError with proper classification.
     */
    private fun handleException(operation: String, exception: Exception): Nothing {
        val networkError = NetworkError.fromException(exception)
        networkError.log("SupabaseDataSource", "Operation: $operation")
        throw networkError
    }
    
    /**
     * Fetch all moods for a user from remote
     */
    suspend fun getAllMoods(userId: String): List<Mood> {
        return try {
            Logger.debug("SupabaseDataSource", "Fetching all moods from remote for user: $userId")
            val moods = supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Mood>()
                .sortedByDescending { it.timestamp }
            Logger.info("SupabaseDataSource", "Fetched ${moods.size} moods from remote for user: $userId")
            moods
        } catch (e: Exception) {
            handleException("getAllMoods(userId=$userId)", e)
        }
    }
    
    /**
     * Fetch moods within a date range for a user from remote
     */
    suspend fun getMoodsByDateRange(userId: String, startTime: Long, endTime: Long): List<Mood> {
        return try {
            Logger.debug("SupabaseDataSource", "Fetching moods by date range: $startTime to $endTime for user: $userId")
            val moods = supabaseClient.from(tableName)
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        gte("timestamp", startTime)
                        lte("timestamp", endTime)
                    }
                }
                .decodeList<Mood>()
                .sortedByDescending { it.timestamp }
            Logger.debug("SupabaseDataSource", "Fetched ${moods.size} moods in date range for user: $userId")
            moods
        } catch (e: Exception) {
            handleException("getMoodsByDateRange(userId=$userId, startTime=$startTime, endTime=$endTime)", e)
        }
    }
    
    /**
     * Insert a mood to remote
     */
    suspend fun insertMood(mood: Mood): Mood {
        return try {
            Logger.debug("SupabaseDataSource", "Inserting mood to remote: score=${mood.score}, id=${mood.id}")
            val inserted = supabaseClient.from(tableName)
                .insert(mood)
                .decodeSingle<Mood>()
            Logger.info("SupabaseDataSource", "Mood inserted to remote with id: ${inserted.id}")
            inserted
        } catch (e: Exception) {
            handleException("insertMood(id=${mood.id}, score=${mood.score})", e)
        }
    }
    
    /**
     * Update a mood in remote
     */
    suspend fun updateMood(mood: Mood): Mood {
        return try {
            require(mood.id.isNotBlank()) { "Mood must have an id to update" }
            Logger.debug("SupabaseDataSource", "Updating mood in remote: id=${mood.id}")
            val updated = supabaseClient.from(tableName)
                .update(mood) {
                    filter {
                        eq("id", mood.id)
                    }
                }
                .decodeSingle<Mood>()
            Logger.info("SupabaseDataSource", "Mood updated in remote: id=${updated.id}")
            updated
        } catch (e: IllegalArgumentException) {
            // Re-throw validation errors as-is
            throw e
        } catch (e: Exception) {
            handleException("updateMood(id=${mood.id})", e)
        }
    }
    
    /**
     * Delete a mood from remote
     */
    suspend fun deleteMood(id: String) {
        try {
            Logger.debug("SupabaseDataSource", "Deleting mood from remote: id=$id")
            supabaseClient.from(tableName)
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Logger.info("SupabaseDataSource", "Mood deleted from remote: id=$id")
        } catch (e: Exception) {
            handleException("deleteMood(id=$id)", e)
        }
    }
    
    /**
     * Upsert multiple moods (for sync operations)
     */
    suspend fun upsertMoods(moods: List<Mood>) {
        if (moods.isEmpty()) return
        
        try {
            Logger.debug("SupabaseDataSource", "Upserting ${moods.size} moods to remote")
            supabaseClient.from(tableName)
                .upsert(moods)
            Logger.info("SupabaseDataSource", "Upserted ${moods.size} moods to remote")
        } catch (e: Exception) {
            handleException("upsertMoods(count=${moods.size})", e)
        }
    }
}

