package com.electricsheep.app.auth

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for User data model.
 * Tests validation logic and user properties.
 */
class UserTest {
    
    @Test
    fun `should create valid user with all fields`() {
        // Arrange
        val id = "user-123"
        val email = "test@example.com"
        val displayName = "Test User"
        val createdAt = System.currentTimeMillis()
        
        // Act
        val user = User(
            id = id,
            email = email,
            displayName = displayName,
            createdAt = createdAt
        )
        
        // Assert
        assertEquals(id, user.id)
        assertEquals(email, user.email)
        assertEquals(displayName, user.displayName)
        assertEquals(createdAt, user.createdAt)
        assertTrue(user.isValid())
    }
    
    @Test
    fun `should create valid user with minimal fields`() {
        // Arrange & Act
        val user = User(
            id = "user-123",
            email = "test@example.com"
        )
        
        // Assert
        assertEquals("user-123", user.id)
        assertEquals("test@example.com", user.email)
        assertNull(user.displayName)
        assertNull(user.createdAt)
        assertTrue(user.isValid())
    }
    
    @Test
    fun `should return false for invalid blank id`() {
        // Arrange & Act
        val user = User(
            id = "",
            email = "test@example.com"
        )
        
        // Assert
        assertFalse(user.isValid())
    }
    
    @Test
    fun `should return false for invalid blank email`() {
        // Arrange & Act
        val user = User(
            id = "user-123",
            email = ""
        )
        
        // Assert
        assertFalse(user.isValid())
    }
    
    @Test
    fun `should return false for invalid whitespace id`() {
        // Arrange & Act
        val user = User(
            id = "   ",
            email = "test@example.com"
        )
        
        // Assert
        assertFalse(user.isValid())
    }
    
    @Test
    fun `should return false for invalid whitespace email`() {
        // Arrange & Act
        val user = User(
            id = "user-123",
            email = "   "
        )
        
        // Assert
        assertFalse(user.isValid())
    }
}

