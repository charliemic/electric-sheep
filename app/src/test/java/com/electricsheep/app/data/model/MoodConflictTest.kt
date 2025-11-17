package com.electricsheep.app.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Mood conflict resolution logic.
 * Tests the isNewerThan method and timestamp handling.
 */
class MoodConflictTest {
    
    @Test
    fun `should identify mood with newer updatedAt as newer`() {
        // Arrange
        val older = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            updatedAt = 1000L
        )
        val newer = Mood(
            id = "1",
            userId = "user-1",
            score = 7,
            timestamp = 2000L,
            updatedAt = 2000L
        )
        
        // Act & Assert
        assertTrue(newer.isNewerThan(older))
        assertFalse(older.isNewerThan(newer))
    }
    
    @Test
    fun `should use createdAt when updatedAt is null`() {
        // Arrange
        val older = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            createdAt = 1000L,
            updatedAt = null
        )
        val newer = Mood(
            id = "1",
            userId = "user-1",
            score = 7,
            timestamp = 2000L,
            createdAt = 2000L,
            updatedAt = null
        )
        
        // Act & Assert
        assertTrue(newer.isNewerThan(older))
        assertFalse(older.isNewerThan(newer))
    }
    
    @Test
    fun `should prefer updatedAt over createdAt when comparing`() {
        // Arrange
        val withUpdatedAt = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            createdAt = 500L,
            updatedAt = 2000L
        )
        val onlyCreatedAt = Mood(
            id = "1",
            userId = "user-1",
            score = 7,
            timestamp = 2000L,
            createdAt = 1500L,
            updatedAt = null
        )
        
        // Act & Assert
        // Mood with updatedAt=2000 should be newer than mood with createdAt=1500
        assertTrue(withUpdatedAt.isNewerThan(onlyCreatedAt))
        assertFalse(onlyCreatedAt.isNewerThan(withUpdatedAt))
    }
    
    @Test
    fun `should return false when timestamps are equal`() {
        // Arrange
        val mood1 = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            updatedAt = 1000L
        )
        val mood2 = Mood(
            id = "1",
            userId = "user-1",
            score = 7,
            timestamp = 2000L,
            updatedAt = 1000L
        )
        
        // Act & Assert
        assertFalse(mood1.isNewerThan(mood2))
        assertFalse(mood2.isNewerThan(mood1))
    }
    
    @Test
    fun `should handle moods with no timestamps as oldest`() {
        // Arrange
        val noTimestamps = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            createdAt = null,
            updatedAt = null
        )
        val withTimestamp = Mood(
            id = "1",
            userId = "user-1",
            score = 7,
            timestamp = 2000L,
            createdAt = 1000L,
            updatedAt = null
        )
        
        // Act & Assert
        assertTrue(withTimestamp.isNewerThan(noTimestamps))
        assertFalse(noTimestamps.isNewerThan(withTimestamp))
    }
    
    @Test
    fun `should set updatedAt timestamp when calling withUpdatedTimestamp`() {
        // Arrange
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            createdAt = 500L,
            updatedAt = null
        )
        
        // Act
        val updated = mood.withUpdatedTimestamp()
        
        // Assert
        assertNotNull(updated.updatedAt)
        assertTrue(updated.updatedAt!! > 0)
        assertEquals(mood.createdAt, updated.createdAt) // Should preserve existing createdAt
        assertEquals(mood.id, updated.id)
        assertEquals(mood.score, updated.score)
    }
    
    @Test
    fun `should set createdAt if null when calling withUpdatedTimestamp`() {
        // Arrange
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = 1000L,
            createdAt = null,
            updatedAt = null
        )
        
        // Act
        val updated = mood.withUpdatedTimestamp()
        
        // Assert
        assertNotNull(updated.createdAt)
        assertNotNull(updated.updatedAt)
        assertTrue(updated.createdAt!! > 0)
        assertTrue(updated.updatedAt!! > 0)
    }
}

