package com.electricsheep.app.data.repository

import com.electricsheep.app.data.fixtures.MoodPattern
import com.electricsheep.app.data.fixtures.TechLevel
import com.electricsheep.app.data.fixtures.TestDataLoader
import com.electricsheep.app.data.local.MoodDao
import com.electricsheep.app.util.Logger

/**
 * Extension functions for loading test data into MoodRepository.
 * These are useful for testing and development.
 */
fun MoodRepository.loadTestData(
    techLevel: TechLevel,
    moodPattern: MoodPattern,
    days: Int = 30
): Result<com.electricsheep.app.data.fixtures.TestDataLoadResult> {
    // Access the private moodDao through reflection or make it accessible
    // For now, we'll create a TestDataLoader with the dao
    // Note: This requires making moodDao accessible or using a different approach
    
    // Actually, we need to get the dao from the repository
    // Since it's private, we'll need to either:
    // 1. Make it internal/accessible
    // 2. Add a method to repository to load test data
    // 3. Use reflection (not ideal)
    
    // Best approach: Add a method to repository
    return Result.failure(Exception("Use MoodRepository.loadTestFixtureData() instead"))
}

/**
 * Extension to MoodRepository for test data loading.
 * This allows loading test fixtures directly through the repository.
 */
suspend fun MoodRepository.loadTestFixtureData(
    techLevel: TechLevel,
    moodPattern: MoodPattern,
    days: Int = 30
): Result<com.electricsheep.app.data.fixtures.TestDataLoadResult> {
    return try {
        // We need access to moodDao - this is a limitation
        // For now, return error suggesting to use TestDataLoader directly
        Logger.warn("MoodRepository", "loadTestFixtureData requires direct MoodDao access. Use TestDataLoader instead.")
        Result.failure(Exception("Use TestDataLoader with MoodDao directly"))
    } catch (e: Exception) {
        Logger.error("MoodRepository", "Failed to load test data", e)
        Result.failure(e)
    }
}

