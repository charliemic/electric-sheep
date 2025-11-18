package com.electricsheep.app.error

import com.electricsheep.app.util.Logger
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for AppError base class and logging functionality.
 */
class AppErrorTest {

    @Test
    fun `AppError should have default recoverable flag`() {
        // NetworkError defaults to recoverable
        val networkError = NetworkError.NoConnection()
        assertTrue(networkError.isRecoverable)
        
        // SystemError defaults to non-recoverable
        val systemError = SystemError.Unknown()
        assertFalse(systemError.isRecoverable)
    }

    @Test
    fun `AppError should preserve cause`() {
        val originalException = Exception("Original error")
        val networkError = NetworkError.NoConnection(originalException)
        
        assertEquals(originalException, networkError.cause)
    }

    @Test
    fun `AppError should have user-friendly message`() {
        val networkError = NetworkError.NoConnection()
        
        assertNotNull(networkError.userMessage)
        assertNotEquals("", networkError.userMessage)
        assertTrue(networkError.userMessage.length > 0)
    }

    @Test
    fun `log should not throw for recoverable errors`() {
        val error = NetworkError.NoConnection()
        
        // Should not throw - actual logging is tested in integration
        try {
            error.log("TestTag", "Test context")
        } catch (e: Exception) {
            fail("log() should not throw exceptions: ${e.message}")
        }
    }

    @Test
    fun `log should not throw for non-recoverable errors`() {
        val error = SystemError.Unknown()
        
        // Should not throw - actual logging is tested in integration
        try {
            error.log("TestTag", "Test context")
        } catch (e: Exception) {
            fail("log() should not throw exceptions: ${e.message}")
        }
    }

    @Test
    fun `AppError should extend Exception`() {
        val error = NetworkError.NoConnection()
        
        assertTrue(error is Exception)
    }

    @Test
    fun `AppError should have message and cause`() {
        val cause = Exception("Cause")
        val error = NetworkError.NoConnection(cause)
        
        assertNotNull(error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `retryDelayMillis should be set appropriately`() {
        val noConnection = NetworkError.NoConnection()
        assertEquals(3000L, noConnection.retryDelayMillis)
        
        val timeout = NetworkError.Timeout(30)
        assertEquals(5000L, timeout.retryDelayMillis)
        
        val serverError = NetworkError.ServerError(500)
        assertEquals(10000L, serverError.retryDelayMillis)
    }

    @Test
    fun `shouldRetry should be set appropriately`() {
        val noConnection = NetworkError.NoConnection()
        assertTrue(noConnection.shouldRetry) // Network errors should retry
        
        val unauthorized = NetworkError.Unauthorized()
        assertFalse(unauthorized.shouldRetry) // Auth errors require user action
        
        val clientError = NetworkError.ClientError(400)
        assertFalse(clientError.shouldRetry) // Client errors don't auto-retry
    }
}

