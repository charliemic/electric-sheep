package com.electricsheep.app.config

/**
 * Configuration for mood-related settings.
 * These values can be moved to BuildConfig or remote configuration in the future.
 */
object MoodConfig {
    /**
     * Minimum valid mood score
     */
    const val MIN_SCORE = 1
    
    /**
     * Maximum valid mood score
     */
    const val MAX_SCORE = 10
    
    /**
     * Validate that a score is within the valid range
     */
    fun isValidScore(score: Int): Boolean {
        return score in MIN_SCORE..MAX_SCORE
    }
}

