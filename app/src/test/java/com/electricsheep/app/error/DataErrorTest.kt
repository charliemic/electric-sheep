package com.electricsheep.app.error

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for DataError and its error types.
 */
class DataErrorTest {

    @Test
    fun `ConstraintViolation should have correct properties`() {
        val cause = Exception("Duplicate key")
        val error = DataError.ConstraintViolation("unique_mood_id", cause)
        
        assertEquals("Database constraint violation: unique_mood_id", error.message)
        assertEquals("This data already exists. Please check and try again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry)
        assertEquals("unique_mood_id", error.constraint)
    }

    @Test
    fun `ConstraintViolation should handle null constraint`() {
        val error = DataError.ConstraintViolation(null)
        
        assertEquals("Database constraint violation", error.message)
        assertNull(error.constraint)
    }

    @Test
    fun `NotFound should have correct properties`() {
        val cause = Exception("Not found")
        val error = DataError.NotFound("mood", cause)
        
        assertEquals("mood not found", error.message)
        assertEquals("The requested item could not be found.", error.userMessage)
        assertFalse(error.isRecoverable) // Data not found is typically non-recoverable
        assertFalse(error.shouldRetry)
        assertEquals("mood", error.resource)
    }

    @Test
    fun `NotFound should use default resource name`() {
        val error = DataError.NotFound()
        
        assertEquals("resource not found", error.message)
        assertEquals("resource", error.resource)
    }

    @Test
    fun `InvalidData should have correct properties with field`() {
        val cause = IllegalArgumentException("Invalid score")
        val error = DataError.InvalidData("score", cause)
        
        assertEquals("Invalid data in field: score", error.message)
        assertEquals("Invalid score. Please check your input.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry)
        assertEquals("score", error.field)
    }

    @Test
    fun `InvalidData should have correct properties without field`() {
        val cause = IllegalArgumentException("Invalid data")
        val error = DataError.InvalidData(null, cause)
        
        assertEquals("Invalid data", error.message)
        assertEquals("Invalid data. Please check your input and try again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertNull(error.field)
    }

    @Test
    fun `Corruption should have correct properties`() {
        val cause = Exception("Database corruption")
        val error = DataError.Corruption(cause)
        
        assertEquals("Database corruption detected", error.message)
        assertEquals("Data integrity issue detected. The app may need to be reset.", error.userMessage)
        assertFalse(error.isRecoverable) // Corruption is non-recoverable
        assertFalse(error.shouldRetry)
    }

    @Test
    fun `log should exist for all DataError types`() {
        val constraintError = DataError.ConstraintViolation()
        val notFoundError = DataError.NotFound()
        val invalidDataError = DataError.InvalidData()
        val corruptionError = DataError.Corruption()
        val unknownError = DataError.Unknown()
        
        // Test that log method exists and doesn't throw
        try {
            constraintError.log("TestTag")
            notFoundError.log("TestTag")
            invalidDataError.log("TestTag")
            corruptionError.log("TestTag")
            unknownError.log("TestTag")
        } catch (e: Exception) {
            fail("log() should not throw exceptions: ${e.message}")
        }
    }
    
    @Test
    fun `Unknown should have correct properties`() {
        val cause = Exception("test")
        val error = DataError.Unknown(cause)
        
        assertEquals("Unknown database error", error.message)
        assertEquals("A database error occurred. Please try again.", error.userMessage)
        assertTrue(error.isRecoverable)
        assertFalse(error.shouldRetry)
        assertEquals(cause, error.cause)
    }
}

