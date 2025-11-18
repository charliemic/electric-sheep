package com.electricsheep.app.auth

import org.junit.Assert.*
import org.junit.Test
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Unit tests for AuthError error parsing logic.
 */
class AuthErrorTest {
    
    @Test
    fun `fromException should parse InvalidCredentials error`() {
        // Given: Exception with invalid credentials message
        val exception = Exception("Invalid login credentials")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be InvalidCredentials
        assertTrue(error is AuthError.InvalidCredentials)
        assertEquals("Invalid email or password. Please check your credentials and try again.", error.message)
    }
    
    @Test
    fun `fromException should parse UserAlreadyExists error`() {
        // Given: Exception with user already exists message
        val exception = Exception("User already registered")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be UserAlreadyExists
        assertTrue(error is AuthError.UserAlreadyExists)
        assertEquals("An account with this email already exists. Please sign in instead.", error.message)
    }
    
    @Test
    fun `fromException should parse EmailNotConfirmed error`() {
        // Given: Exception with email not confirmed message
        val exception = Exception("Email not confirmed")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be EmailNotConfirmed
        assertTrue(error is AuthError.EmailNotConfirmed)
        assertEquals("Please check your email and confirm your account before signing in.", error.message)
    }
    
    @Test
    fun `fromException should parse WeakPassword error`() {
        // Given: Exception with weak password message
        val exception = Exception("Password too short")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be WeakPassword
        assertTrue(error is AuthError.WeakPassword)
        assertEquals("Password is too weak. Please use at least 6 characters.", error.message)
    }
    
    @Test
    fun `fromException should parse InvalidEmail error`() {
        // Given: Exception with invalid email message
        val exception = Exception("Invalid email format")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be InvalidEmail
        assertTrue(error is AuthError.InvalidEmail)
        assertEquals("Please enter a valid email address.", error.message)
    }
    
    @Test
    fun `fromException should parse NetworkError from UnknownHostException`() {
        // Given: Network exception
        val exception = UnknownHostException("Unable to resolve host")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be NetworkError
        assertTrue(error is AuthError.NetworkError)
        assertEquals("Unable to connect. Please check your internet connection and try again.", error.message)
    }
    
    @Test
    fun `fromException should parse NetworkError from ConnectException`() {
        // Given: Connection exception
        val exception = ConnectException("Connection refused")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be NetworkError
        assertTrue(error is AuthError.NetworkError)
        assertEquals("Unable to connect. Please check your internet connection and try again.", error.message)
    }
    
    @Test
    fun `fromException should parse NetworkError from network message`() {
        // Given: Exception with network message
        val exception = Exception("Network timeout")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be NetworkError
        assertTrue(error is AuthError.NetworkError)
    }
    
    @Test
    fun `fromException should parse OAuthError`() {
        // Given: Exception with OAuth message
        val exception = Exception("OAuth authentication failed")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be OAuthError
        assertTrue(error is AuthError.OAuthError)
        assertEquals("Google sign-in failed. Please try again.", error.message)
    }
    
    @Test
    fun `fromException should parse UserNotFound error`() {
        // Given: Exception with user not found message
        val exception = Exception("User not found")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be UserNotFound
        assertTrue(error is AuthError.UserNotFound)
        assertEquals("No account found with this email. Please sign up first.", error.message)
    }
    
    @Test
    fun `fromException should parse Generic error for unknown exceptions`() {
        // Given: Exception with unknown message
        val exception = Exception("Something went wrong")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be Generic with capitalized message
        assertTrue(error is AuthError.Generic)
        assertEquals("Something went wrong", error.message)
    }
    
    @Test
    fun `fromException should use generic message for very long messages`() {
        // Given: Exception with very long message
        val longMessage = "A".repeat(150)
        val exception = Exception(longMessage)
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be Generic with default message
        assertTrue(error is AuthError.Generic)
        assertEquals("An error occurred. Please try again.", error.message)
    }
    
    @Test
    fun `fromException should use generic message for empty message`() {
        // Given: Exception with null message
        val exception = Exception()
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should be Generic with default message
        assertTrue(error is AuthError.Generic)
        assertEquals("An error occurred. Please try again.", error.message)
    }
    
    @Test
    fun `fromException should preserve cause`() {
        // Given: Exception with cause
        val cause = Exception("Root cause")
        val exception = Exception("Wrapper exception", cause)
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should preserve cause
        assertEquals(cause, error.cause)
    }
    
    @Test
    fun `fromException should handle case insensitive matching`() {
        // Given: Exception with uppercase message
        val exception = Exception("INVALID LOGIN CREDENTIALS")
        
        // When: Parsing exception
        val error = AuthError.fromException(exception)
        
        // Then: Should still parse correctly
        assertTrue(error is AuthError.InvalidCredentials)
    }
}

