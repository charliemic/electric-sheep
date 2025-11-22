package com.electricsheep.app.auth

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SignInResult sealed class.
 * Tests the different result types and their properties.
 */
class SignInResultTest {
    
    @Test
    fun `should create Success result with user`() {
        // Arrange
        val user = User(id = "user-1", email = "test@example.com")
        
        // Act
        val result = SignInResult.Success(user)
        
        // Assert
        assertTrue(result is SignInResult.Success)
        assertEquals(user, (result as SignInResult.Success).user)
    }
    
    @Test
    fun `should create MfaChallenge result with challenge and user ID`() {
        // Arrange
        val challengeId = "challenge-123"
        val userId = "user-456"
        
        // Act
        val result = SignInResult.MfaChallenge(challengeId, userId)
        
        // Assert
        assertTrue(result is SignInResult.MfaChallenge)
        assertEquals(challengeId, (result as SignInResult.MfaChallenge).challengeId)
        assertEquals(userId, (result as SignInResult.MfaChallenge).userId)
    }
    
    @Test
    fun `should create Error result with auth error`() {
        // Arrange
        val error = AuthError.InvalidCredentials()
        
        // Act
        val result = SignInResult.Error(error)
        
        // Assert
        assertTrue(result is SignInResult.Error)
        assertEquals(error, (result as SignInResult.Error).error)
    }
}

