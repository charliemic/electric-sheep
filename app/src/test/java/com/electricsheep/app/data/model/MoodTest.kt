package com.electricsheep.app.data.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for Mood data model.
 * Tests validation logic and utility methods.
 */
class MoodTest {
    
    @Test
    fun `should create valid mood with all fields`() {
        // Arrange
        val id = "test-id"
        val userId = "user-1"
        val score = 5
        val timestamp = System.currentTimeMillis()
        val createdAt = timestamp
        val updatedAt = timestamp
        
        // Act
        val mood = Mood(
            id = id,
            userId = userId,
            score = score,
            timestamp = timestamp,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
        
        // Assert
        assertEquals(id, mood.id)
        assertEquals(userId, mood.userId)
        assertEquals(score, mood.score)
        assertEquals(timestamp, mood.timestamp)
        assertEquals(createdAt, mood.createdAt)
        assertEquals(updatedAt, mood.updatedAt)
        assertTrue(mood.isValid())
    }
    
    @Test
    fun `should create valid mood with minimal fields`() {
        // Arrange & Act
        val mood = Mood(
            id = "test-id",
            userId = "user-1",
            score = 7,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertEquals("test-id", mood.id)
        assertEquals("user-1", mood.userId)
        assertEquals(7, mood.score)
        assertTrue(mood.isValid())
    }
    
    @Test
    fun `should return false for invalid blank userId`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "",
            score = 5,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertFalse(mood.isValid())
    }
    
    @Test
    fun `should return false for invalid whitespace userId`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "   ",
            score = 5,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertFalse(mood.isValid())
    }
    
    @Test
    fun `should return false for invalid score below minimum`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 0,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertFalse(mood.isValid())
    }
    
    @Test
    fun `should return false for invalid score above maximum`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 11,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertFalse(mood.isValid())
    }
    
    @Test
    fun `should return false for invalid negative timestamp`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = -1
        )
        
        // Assert
        assertFalse(mood.isValid())
    }
    
    @Test
    fun `should return false for invalid zero timestamp`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 0
        )
        
        // Assert
        assertFalse(mood.isValid())
    }
    
    @Test
    fun `should return true for valid score at minimum boundary`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 1,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertTrue(mood.isValid())
    }
    
    @Test
    fun `should return true for valid score at maximum boundary`() {
        // Arrange & Act
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 10,
            timestamp = System.currentTimeMillis()
        )
        
        // Assert
        assertTrue(mood.isValid())
    }
    
    @Test
    fun `should create mood with updated timestamp`() {
        // Arrange
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = System.currentTimeMillis()
        )
        
        // Act
        val updatedMood = mood.withUpdatedTimestamp()
        
        // Assert
        assertNotNull(updatedMood.createdAt)
        assertNotNull(updatedMood.updatedAt)
        assertEquals(updatedMood.createdAt, updatedMood.updatedAt)
    }
    
    @Test
    fun `should preserve existing createdAt when updating timestamp`() {
        // Arrange
        val originalCreatedAt = System.currentTimeMillis() - 10000
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = System.currentTimeMillis(),
            createdAt = originalCreatedAt
        )
        
        // Act
        val updatedMood = mood.withUpdatedTimestamp()
        
        // Assert
        assertEquals(originalCreatedAt, updatedMood.createdAt)
        assertNotNull(updatedMood.updatedAt)
        assertTrue(updatedMood.updatedAt!! > originalCreatedAt)
    }
    
    @Test
    fun `should correctly identify newer mood`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val olderMood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = baseTime,
            updatedAt = baseTime
        )
        val newerMood = Mood(
            id = "2",
            userId = "user-1",
            score = 7,
            timestamp = baseTime,
            updatedAt = baseTime + 1000
        )
        
        // Act & Assert
        assertTrue(newerMood.isNewerThan(olderMood))
        assertFalse(olderMood.isNewerThan(newerMood))
    }
}

