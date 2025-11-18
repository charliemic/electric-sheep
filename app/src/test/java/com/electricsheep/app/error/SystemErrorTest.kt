package com.electricsheep.app.error

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SystemError and its error types.
 */
class SystemErrorTest {

    @Test
    fun `OutOfMemory should have correct properties`() {
        val cause = OutOfMemoryError("Java heap space")
        val error = SystemError.OutOfMemory(cause)
        
        assertEquals("Out of memory", error.message)
        assertEquals("The app is running low on memory. Please close other apps and try again.", error.userMessage)
        assertFalse(error.isRecoverable) // Out of memory is non-recoverable
        assertFalse(error.shouldRetry)
    }

    @Test
    fun `ConfigurationError should have correct properties with config`() {
        val cause = IllegalArgumentException("Invalid configuration")
        val error = SystemError.ConfigurationError("Supabase URL", cause)
        
        assertEquals("Configuration error: Supabase URL", error.message)
        assertEquals("App configuration error. Please contact support.", error.userMessage)
        assertFalse(error.isRecoverable)
        assertFalse(error.shouldRetry)
        assertEquals("Supabase URL", error.config)
    }

    @Test
    fun `ConfigurationError should handle null config`() {
        val error = SystemError.ConfigurationError(null)
        
        assertEquals("Configuration error", error.message)
        assertNull(error.config)
    }

    @Test
    fun `Unknown should have correct properties`() {
        val cause = Exception("Unexpected error")
        val error = SystemError.Unknown(cause)
        
        assertEquals("Unknown system error", error.message)
        assertEquals("An unexpected error occurred. Please restart the app.", error.userMessage)
        assertFalse(error.isRecoverable)
        assertFalse(error.shouldRetry)
    }

    @Test
    fun `log should exist for all SystemError types`() {
        val outOfMemoryError = SystemError.OutOfMemory()
        val configError = SystemError.ConfigurationError()
        val unknownError = SystemError.Unknown()
        
        // Test that log method exists and doesn't throw
        try {
            outOfMemoryError.log("TestTag")
            configError.log("TestTag")
            unknownError.log("TestTag")
        } catch (e: Exception) {
            fail("log() should not throw exceptions: ${e.message}")
        }
    }

    @Test
    fun `SystemError should be non-recoverable by default`() {
        val outOfMemory = SystemError.OutOfMemory()
        val configError = SystemError.ConfigurationError()
        val unknown = SystemError.Unknown()
        
        assertFalse(outOfMemory.isRecoverable)
        assertFalse(configError.isRecoverable)
        assertFalse(unknown.isRecoverable)
    }
}

