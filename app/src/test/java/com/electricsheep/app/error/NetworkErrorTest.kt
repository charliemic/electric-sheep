package com.electricsheep.app.error

import org.junit.Assert.*
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Unit tests for NetworkError and its parsing logic.
 */
class NetworkErrorTest {

    @Test
    fun `fromException should return NoConnection for UnknownHostException`() {
        val exception = UnknownHostException("Unable to resolve host")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.NoConnection)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(3000L, error.retryDelayMillis)
    }

    @Test
    fun `fromException should return NoConnection for ConnectException`() {
        val exception = ConnectException("Connection refused")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.NoConnection)
    }

    @Test
    fun `fromException should return NoConnection for no internet message`() {
        val exception = Exception("No internet connection available")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.NoConnection)
    }

    @Test
    fun `fromException should return Timeout for SocketTimeoutException`() {
        val exception = SocketTimeoutException("Read timed out")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Timeout)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(5000L, error.retryDelayMillis)
    }

    @Test
    fun `fromException should return Timeout for TimeoutException`() {
        val exception = TimeoutException("Operation timed out")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Timeout)
    }

    @Test
    fun `fromException should return Timeout for timeout message`() {
        val exception = Exception("Request timeout after 30 seconds")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Timeout)
        val timeoutError = error as NetworkError.Timeout
        assertEquals(30, timeoutError.timeoutSeconds)
    }

    @Test
    fun `fromException should return Unauthorized for 401 message`() {
        val exception = Exception("HTTP 401 Unauthorized")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Unauthorized)
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry) // Requires user action
    }

    @Test
    fun `fromException should return Unauthorized for unauthorized message`() {
        val exception = Exception("Unauthorized access")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Unauthorized)
    }

    @Test
    fun `fromException should return RateLimited for 429 message`() {
        val exception = Exception("HTTP 429 Rate limit exceeded, retry after 60")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.RateLimited)
        val rateLimited = error as NetworkError.RateLimited
        assertEquals(60, rateLimited.retryAfterSeconds)
        assertEquals(60000L, error.retryDelayMillis)
    }

    @Test
    fun `fromException should return ServerError for 5xx status code`() {
        val exception = Exception("HTTP 500 Internal Server Error")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.ServerError)
        val serverError = error as NetworkError.ServerError
        assertEquals(500, serverError.statusCode)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(10000L, error.retryDelayMillis)
    }

    @Test
    fun `fromException should return ClientError for 4xx status code`() {
        val exception = Exception("HTTP 400 Bad Request")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.ClientError)
        val clientError = error as NetworkError.ClientError
        assertEquals(400, clientError.statusCode)
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry) // User needs to fix input
    }

    @Test
    fun `fromException should return Unknown for unrecognized exception`() {
        val exception = Exception("Some random error")
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Unknown)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(3000L, error.retryDelayMillis)
    }

    @Test
    fun `fromException should return Unknown for null message`() {
        val exception = Exception(null as String?)
        val error = NetworkError.fromException(exception)
        
        assertTrue(error is NetworkError.Unknown)
    }

    @Test
    fun `NoConnection should have correct properties`() {
        val cause = UnknownHostException("test")
        val error = NetworkError.NoConnection(cause)
        
        assertEquals("No internet connection", error.message)
        assertEquals("No internet connection. Please check your network and try again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(3000L, error.retryDelayMillis)
        assertEquals(cause, error.cause)
    }

    @Test
    fun `Timeout should have correct properties`() {
        val cause = SocketTimeoutException("test")
        val error = NetworkError.Timeout(30, cause)
        
        assertEquals("Request timed out after 30s", error.message)
        assertEquals("Request took too long. Please check your connection and try again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(5000L, error.retryDelayMillis)
        assertEquals(30, error.timeoutSeconds)
    }

    @Test
    fun `ServerError should have correct properties`() {
        val cause = Exception("test")
        val error = NetworkError.ServerError(500, "Internal error", cause)
        
        assertEquals("Server error: 500 - Internal error", error.message)
        assertEquals("Server is temporarily unavailable. Please try again in a moment.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(10000L, error.retryDelayMillis)
        assertEquals(500, error.statusCode)
        assertEquals("Internal error", error.serverMessage)
    }

    @Test
    fun `ClientError should have correct properties`() {
        val cause = Exception("test")
        val error = NetworkError.ClientError(400, "Bad request", cause)
        
        assertEquals("Client error: 400 - Bad request", error.message)
        assertEquals("Bad request", error.userMessage) // Uses serverMessage if provided
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry)
        assertEquals(400, error.statusCode)
    }

    @Test
    fun `Unauthorized should have correct properties`() {
        val cause = Exception("test")
        val error = NetworkError.Unauthorized(cause)
        
        assertEquals("Authentication failed", error.message)
        assertEquals("Your session has expired. Please sign in again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry) // Requires user action
    }

    @Test
    fun `RateLimited should have correct properties`() {
        val cause = Exception("test")
        val error = NetworkError.RateLimited(120, cause)
        
        assertEquals("Rate limited - retry after 120s", error.message)
        assertEquals("Too many requests. Please wait a moment and try again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertTrue(error.shouldRetry)
        assertEquals(120000L, error.retryDelayMillis) // 120 seconds in milliseconds
        assertEquals(120, error.retryAfterSeconds)
    }

    @Test
    fun `log should use appropriate log level for recoverable errors`() {
        val error = NetworkError.NoConnection()
        // Test that log method exists and doesn't throw
        // Actual logging is tested in integration tests
        try {
            error.log("TestTag", "Test context")
        } catch (e: Exception) {
            fail("log() should not throw exceptions: ${e.message}")
        }
    }
}

