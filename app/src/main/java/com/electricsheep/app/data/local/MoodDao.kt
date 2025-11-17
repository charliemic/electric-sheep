package com.electricsheep.app.data.local

import androidx.room.*
import com.electricsheep.app.data.model.Mood
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Mood entities in local Room database.
 * Provides offline-first local storage for mood data.
 * All queries are scoped to the current user.
 */
@Dao
interface MoodDao {
    @Query("SELECT * FROM moods WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMoods(userId: String): Flow<List<Mood>>
    
    @Query("SELECT * FROM moods WHERE id = :id AND userId = :userId")
    suspend fun getMoodById(id: String, userId: String): Mood?
    
    @Query("SELECT * FROM moods WHERE userId = :userId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getMoodsByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<Mood>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: Mood): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoods(moods: List<Mood>)
    
    @Update
    suspend fun updateMood(mood: Mood)
    
    @Delete
    suspend fun deleteMood(mood: Mood)
    
    @Query("DELETE FROM moods WHERE id = :id")
    suspend fun deleteMoodById(id: String)
    
    @Query("SELECT COUNT(*) FROM moods WHERE userId = :userId")
    suspend fun getMoodCount(userId: String): Int
}

