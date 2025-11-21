package com.electricsheep.app.data.fixtures

import com.electricsheep.app.auth.User
import com.electricsheep.app.config.MoodConfig
import com.electricsheep.app.data.model.Mood
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Test user fixtures with predefined mood data for testing chart visualizations.
 * Each fixture represents a different persona combination (tech level + mood pattern).
 */
object TestUserFixtures {
    
    /**
     * Base user IDs for test fixtures (version controlled)
     */
    object UserIds {
        const val TECH_NOVICE_HIGH_STABLE = "test-user-tech-novice-high-stable"
        const val TECH_NOVICE_LOW_STABLE = "test-user-tech-novice-low-stable"
        const val TECH_NOVICE_HIGH_UNSTABLE = "test-user-tech-novice-high-unstable"
        const val TECH_NOVICE_LOW_UNSTABLE = "test-user-tech-novice-low-unstable"
        const val TECH_SAVVY_HIGH_STABLE = "test-user-tech-savvy-high-stable"
        const val TECH_SAVVY_LOW_STABLE = "test-user-tech-savvy-low-stable"
        const val TECH_SAVVY_HIGH_UNSTABLE = "test-user-tech-savvy-high-unstable"
        const val TECH_SAVVY_LOW_UNSTABLE = "test-user-tech-savvy-low-unstable"
    }
    
    /**
     * Get test user for a persona combination
     */
    fun getTestUser(
        techLevel: TechLevel,
        moodPattern: MoodPattern
    ): User {
        val userId = getUserId(techLevel, moodPattern)
        val email = getEmail(techLevel, moodPattern)
        val displayName = getDisplayName(techLevel, moodPattern)
        
        return User(
            id = userId,
            email = email,
            displayName = displayName,
            createdAt = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000) // 90 days ago
        )
    }
    
    /**
     * Generate mood entries for a persona combination
     * 
     * @param techLevel Technical skill level
     * @param moodPattern Mood pattern (high/low, stable/unstable)
     * @param days Number of days of data to generate (default: 30)
     * @param entriesPerDay Number of entries per day (default: 1)
     */
    fun generateMoodEntries(
        techLevel: TechLevel,
        moodPattern: MoodPattern,
        days: Int = 30,
        entriesPerDay: Int = 1
    ): List<Mood> {
        val userId = getUserId(techLevel, moodPattern)
        val baseTime = System.currentTimeMillis()
        val entries = mutableListOf<Mood>()
        
        for (dayOffset in 0 until days) {
            val date = LocalDate.now().minusDays(dayOffset.toLong())
            val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            for (entryIndex in 0 until entriesPerDay) {
                val timestamp = dayStart + (entryIndex * (24 * 60 * 60 * 1000L / entriesPerDay))
                val score = generateMoodScore(moodPattern, dayOffset, entryIndex)
                
                entries.add(
                    Mood(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        score = score,
                        timestamp = timestamp,
                        createdAt = timestamp,
                        updatedAt = timestamp
                    )
                )
            }
        }
        
        // Sort by timestamp descending (newest first)
        return entries.sortedByDescending { it.timestamp }
    }
    
    /**
     * Generate a mood score based on pattern
     */
    private fun generateMoodScore(
        pattern: MoodPattern,
        dayOffset: Int,
        entryIndex: Int
    ): Int {
        return when (pattern) {
            MoodPattern.HIGH_STABLE -> {
                // High stable: 7-9 range, low variance
                val base = 8.0
                val variance = (Math.sin(dayOffset * 0.1) * 0.5) + (Math.random() * 0.5 - 0.25)
                (base + variance).toInt().coerceIn(MoodConfig.MIN_SCORE, MoodConfig.MAX_SCORE)
            }
            MoodPattern.LOW_STABLE -> {
                // Low stable: 2-4 range, low variance
                val base = 3.0
                val variance = (Math.sin(dayOffset * 0.1) * 0.5) + (Math.random() * 0.5 - 0.25)
                (base + variance).toInt().coerceIn(MoodConfig.MIN_SCORE, MoodConfig.MAX_SCORE)
            }
            MoodPattern.HIGH_UNSTABLE -> {
                // High unstable: 4-10 range, high variance
                val base = 7.0
                val variance = (Math.sin(dayOffset * 0.5) * 2.5) + (Math.random() * 2.0 - 1.0)
                (base + variance).toInt().coerceIn(MoodConfig.MIN_SCORE, MoodConfig.MAX_SCORE)
            }
            MoodPattern.LOW_UNSTABLE -> {
                // Low unstable: 1-6 range, high variance
                val base = 3.5
                val variance = (Math.sin(dayOffset * 0.5) * 2.5) + (Math.random() * 2.0 - 1.0)
                (base + variance).toInt().coerceIn(MoodConfig.MIN_SCORE, MoodConfig.MAX_SCORE)
            }
        }
    }
    
    internal fun getUserId(techLevel: TechLevel, moodPattern: MoodPattern): String {
        return when {
            techLevel == TechLevel.NOVICE && moodPattern == MoodPattern.HIGH_STABLE -> UserIds.TECH_NOVICE_HIGH_STABLE
            techLevel == TechLevel.NOVICE && moodPattern == MoodPattern.LOW_STABLE -> UserIds.TECH_NOVICE_LOW_STABLE
            techLevel == TechLevel.NOVICE && moodPattern == MoodPattern.HIGH_UNSTABLE -> UserIds.TECH_NOVICE_HIGH_UNSTABLE
            techLevel == TechLevel.NOVICE && moodPattern == MoodPattern.LOW_UNSTABLE -> UserIds.TECH_NOVICE_LOW_UNSTABLE
            techLevel == TechLevel.SAVVY && moodPattern == MoodPattern.HIGH_STABLE -> UserIds.TECH_SAVVY_HIGH_STABLE
            techLevel == TechLevel.SAVVY && moodPattern == MoodPattern.LOW_STABLE -> UserIds.TECH_SAVVY_LOW_STABLE
            techLevel == TechLevel.SAVVY && moodPattern == MoodPattern.HIGH_UNSTABLE -> UserIds.TECH_SAVVY_HIGH_UNSTABLE
            techLevel == TechLevel.SAVVY && moodPattern == MoodPattern.LOW_UNSTABLE -> UserIds.TECH_SAVVY_LOW_UNSTABLE
            else -> UUID.randomUUID().toString()
        }
    }
    
    internal fun getEmail(techLevel: TechLevel, moodPattern: MoodPattern): String {
        val tech = techLevel.name.lowercase()
        val mood = moodPattern.name.lowercase().replace("_", "-")
        return "test-$tech-$mood@electric-sheep.test"
    }
    
    internal fun getDisplayName(techLevel: TechLevel, moodPattern: MoodPattern): String {
        val tech = when (techLevel) {
            TechLevel.NOVICE -> "Tech Novice"
            TechLevel.SAVVY -> "Tech Savvy"
        }
        val mood = when (moodPattern) {
            MoodPattern.HIGH_STABLE -> "High Stable Mood"
            MoodPattern.LOW_STABLE -> "Low Stable Mood"
            MoodPattern.HIGH_UNSTABLE -> "High Unstable Mood"
            MoodPattern.LOW_UNSTABLE -> "Low Unstable Mood"
        }
        return "$tech - $mood"
    }
}

/**
 * Technical skill level for personas
 */
enum class TechLevel {
    NOVICE,
    SAVVY
}

/**
 * Mood pattern for personas
 */
enum class MoodPattern {
    HIGH_STABLE,      // High average mood (7-9), low variance
    LOW_STABLE,       // Low average mood (2-4), low variance
    HIGH_UNSTABLE,    // High average mood (4-10), high variance
    LOW_UNSTABLE      // Low average mood (1-6), high variance
}

