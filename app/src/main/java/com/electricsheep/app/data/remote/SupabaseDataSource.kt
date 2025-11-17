package com.electricsheep.app.data.remote

import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

/**
 * Remote data source for Mood entities using Supabase.
 * Handles all remote operations and sync with Supabase backend.
 */
class SupabaseDataSource(
    private val supabaseClient: SupabaseClient
) {
    private val tableName = "moods"
    
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
            Logger.error("SupabaseDataSource", "Failed to fetch moods from remote", e)
            throw e
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
                        eq("userId", userId)
                        gte("timestamp", startTime)
                        lte("timestamp", endTime)
                    }
                }
                .decodeList<Mood>()
                .sortedByDescending { it.timestamp }
            Logger.debug("SupabaseDataSource", "Fetched ${moods.size} moods in date range for user: $userId")
            moods
        } catch (e: Exception) {
            Logger.error("SupabaseDataSource", "Failed to fetch moods by date range", e)
            throw e
        }
    }
    
    /**
     * Insert a mood to remote
     */
    suspend fun insertMood(mood: Mood): Mood {
        return try {
            Logger.debug("SupabaseDataSource", "Inserting mood to remote: score=${mood.score}")
            val inserted = supabaseClient.from(tableName)
                .insert(mood)
                .decodeSingle<Mood>()
            Logger.info("SupabaseDataSource", "Mood inserted to remote with id: ${inserted.id}")
            inserted
        } catch (e: Exception) {
            Logger.error("SupabaseDataSource", "Failed to insert mood to remote", e)
            throw e
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
        } catch (e: Exception) {
            Logger.error("SupabaseDataSource", "Failed to update mood in remote", e)
            throw e
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
            Logger.error("SupabaseDataSource", "Failed to delete mood from remote", e)
            throw e
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
            Logger.error("SupabaseDataSource", "Failed to upsert moods to remote", e)
            throw e
        }
    }
}

