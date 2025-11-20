package com.electricsheep.app.data.fixtures

import com.electricsheep.app.auth.User
import com.electricsheep.app.data.local.MoodDao
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.util.Logger
import kotlinx.coroutines.flow.first

/**
 * Loads test fixture data into the database for testing purposes.
 * Provides methods to populate test users and their mood entries.
 */
class TestDataLoader(
    private val moodDao: MoodDao
) {
    
    /**
     * Load test data for a specific persona combination
     * 
     * @param techLevel Technical skill level
     * @param moodPattern Mood pattern
     * @param days Number of days of data to generate
     * @param entriesPerDay Number of entries per day
     */
    suspend fun loadTestData(
        techLevel: TechLevel,
        moodPattern: MoodPattern,
        days: Int = 30,
        entriesPerDay: Int = 1
    ): Result<TestDataLoadResult> {
        return try {
            Logger.info("TestDataLoader", "Loading test data: $techLevel + $moodPattern")
            
            val user = TestUserFixtures.getTestUser(techLevel, moodPattern)
            val moods = TestUserFixtures.generateMoodEntries(techLevel, moodPattern, days, entriesPerDay)
            
            // Insert moods into database
            moodDao.insertMoods(moods)
            
            Logger.info("TestDataLoader", "Loaded ${moods.size} mood entries for user ${user.id}")
            
            Result.success(
                TestDataLoadResult(
                    user = user,
                    moodCount = moods.size,
                    days = days,
                    entriesPerDay = entriesPerDay
                )
            )
        } catch (e: Exception) {
            Logger.error("TestDataLoader", "Failed to load test data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if test data exists for a user
     */
    suspend fun hasTestData(userId: String): Boolean {
        return try {
            val count = moodDao.getMoodCount(userId)
            count > 0
        } catch (e: Exception) {
            Logger.error("TestDataLoader", "Failed to check test data", e)
            false
        }
    }
    
    /**
     * Clear test data for a user
     */
    suspend fun clearTestData(userId: String): Result<Unit> {
        return try {
            // Get all moods for user
            val moods = moodDao.getAllMoods(userId).first()
            
            // Delete each mood
            moods.forEach { mood ->
                moodDao.deleteMood(mood)
            }
            
            Logger.info("TestDataLoader", "Cleared ${moods.size} mood entries for user $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("TestDataLoader", "Failed to clear test data", e)
            Result.failure(e)
        }
    }
    
    /**
     * Load all persona combinations (for comprehensive testing)
     */
    suspend fun loadAllPersonaCombinations(days: Int = 30): Result<Map<String, TestDataLoadResult>> {
        val results = mutableMapOf<String, TestDataLoadResult>()
        
        for (techLevel in TechLevel.values()) {
            for (moodPattern in MoodPattern.values()) {
                val key = "${techLevel.name}_${moodPattern.name}"
                val result = loadTestData(techLevel, moodPattern, days)
                
                result.onSuccess { loadResult ->
                    results[key] = loadResult
                }.onFailure { error ->
                    Logger.error("TestDataLoader", "Failed to load $key", error)
                }
            }
        }
        
        return if (results.size == TechLevel.values().size * MoodPattern.values().size) {
            Result.success(results)
        } else {
            Result.failure(Exception("Failed to load all persona combinations"))
        }
    }
}

/**
 * Result of loading test data
 */
data class TestDataLoadResult(
    val user: User,
    val moodCount: Int,
    val days: Int,
    val entriesPerDay: Int
)

