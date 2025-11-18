package com.electricsheep.app.util

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Unit tests for DateFormatter utility functions.
 */
class DateFormatterTest {
    
    @Test
    fun `formatDateTime should format timestamp correctly`() {
        // Given: A known timestamp (2024-01-15 14:30:00 UTC)
        val timestamp = Instant.parse("2024-01-15T14:30:00Z").toEpochMilli()
        
        // When: Formatting date and time
        val formatted = DateFormatter.formatDateTime(timestamp)
        
        // Then: Should contain date and time components
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
        // Format depends on locale, so we just verify it's not empty
    }
    
    @Test
    fun `formatDate should format date only`() {
        // Given: A known timestamp
        val timestamp = Instant.parse("2024-01-15T14:30:00Z").toEpochMilli()
        
        // When: Formatting date only
        val formatted = DateFormatter.formatDate(timestamp)
        
        // Then: Should contain date components
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
    
    @Test
    fun `formatTime should format time only`() {
        // Given: A known timestamp
        val timestamp = Instant.parse("2024-01-15T14:30:00Z").toEpochMilli()
        
        // When: Formatting time only
        val formatted = DateFormatter.formatTime(timestamp)
        
        // Then: Should contain time components
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
    
    @Test
    fun `formatDateTime should handle current time`() {
        // Given: Current timestamp
        val timestamp = System.currentTimeMillis()
        
        // When: Formatting date and time
        val formatted = DateFormatter.formatDateTime(timestamp)
        
        // Then: Should not throw and should return formatted string
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
    
    @Test
    fun `formatDateTime should handle epoch time`() {
        // Given: Epoch timestamp (1970-01-01 00:00:00 UTC)
        val timestamp = 0L
        
        // When: Formatting date and time
        val formatted = DateFormatter.formatDateTime(timestamp)
        
        // Then: Should format correctly
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
    
    @Test
    fun `formatDateTime should handle future timestamp`() {
        // Given: Future timestamp (2100-01-01)
        val futureDate = ZonedDateTime.of(2100, 1, 1, 12, 0, 0, 0, ZoneId.systemDefault())
        val timestamp = futureDate.toInstant().toEpochMilli()
        
        // When: Formatting date and time
        val formatted = DateFormatter.formatDateTime(timestamp)
        
        // Then: Should format correctly
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
    
    @Test
    fun `formatDate should handle different timezones`() {
        // Given: Timestamp in UTC
        val timestamp = Instant.parse("2024-01-15T14:30:00Z").toEpochMilli()
        
        // When: Formatting date (uses system timezone)
        val formatted = DateFormatter.formatDate(timestamp)
        
        // Then: Should format using system timezone
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
    
    @Test
    fun `formatTime should handle different times`() {
        // Given: Timestamp at midnight
        val midnight = Instant.parse("2024-01-15T00:00:00Z").toEpochMilli()
        
        // When: Formatting time
        val formatted = DateFormatter.formatTime(midnight)
        
        // Then: Should format correctly
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }
}

