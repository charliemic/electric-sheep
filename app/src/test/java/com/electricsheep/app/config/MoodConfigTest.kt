package com.electricsheep.app.config

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for MoodConfig validation.
 */
class MoodConfigTest {
    
    @Test
    fun `isValidScore should return true for minimum score`() {
        // Given: Minimum valid score
        val score = MoodConfig.MIN_SCORE
        
        // When: Validating score
        val isValid = MoodConfig.isValidScore(score)
        
        // Then: Should be valid
        assertTrue(isValid)
    }
    
    @Test
    fun `isValidScore should return true for maximum score`() {
        // Given: Maximum valid score
        val score = MoodConfig.MAX_SCORE
        
        // When: Validating score
        val isValid = MoodConfig.isValidScore(score)
        
        // Then: Should be valid
        assertTrue(isValid)
    }
    
    @Test
    fun `isValidScore should return true for scores in range`() {
        // Given: Scores in valid range
        val scores = listOf(1, 5, 7, 10)
        
        // When/Then: All should be valid
        scores.forEach { score ->
            assertTrue("Score $score should be valid", MoodConfig.isValidScore(score))
        }
    }
    
    @Test
    fun `isValidScore should return false for score below minimum`() {
        // Given: Score below minimum
        val score = MoodConfig.MIN_SCORE - 1
        
        // When: Validating score
        val isValid = MoodConfig.isValidScore(score)
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
    
    @Test
    fun `isValidScore should return false for score above maximum`() {
        // Given: Score above maximum
        val score = MoodConfig.MAX_SCORE + 1
        
        // When: Validating score
        val isValid = MoodConfig.isValidScore(score)
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
    
    @Test
    fun `isValidScore should return false for zero`() {
        // Given: Zero score
        val score = 0
        
        // When: Validating score
        val isValid = MoodConfig.isValidScore(score)
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
    
    @Test
    fun `isValidScore should return false for negative score`() {
        // Given: Negative score
        val score = -1
        
        // When: Validating score
        val isValid = MoodConfig.isValidScore(score)
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
    
    @Test
    fun `MIN_SCORE should be 1`() {
        assertEquals(1, MoodConfig.MIN_SCORE)
    }
    
    @Test
    fun `MAX_SCORE should be 10`() {
        assertEquals(10, MoodConfig.MAX_SCORE)
    }
}

