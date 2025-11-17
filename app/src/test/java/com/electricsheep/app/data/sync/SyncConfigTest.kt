package com.electricsheep.app.data.sync

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for SyncConfig.
 * Tests sync interval configuration and validation.
 */
class SyncConfigTest {
    
    @Test
    fun `should return default sync interval when no custom interval provided`() {
        // Act
        val interval = SyncConfig.getSyncIntervalMinutes()
        
        // Assert
        assertEquals(SyncConfig.DEFAULT_SYNC_INTERVAL_MINUTES, interval)
    }
    
    @Test
    fun `should return custom interval when within valid range`() {
        // Arrange
        val customInterval = 30L
        
        // Act
        val interval = SyncConfig.getSyncIntervalMinutes(customInterval)
        
        // Assert
        assertEquals(30L, interval)
    }
    
    @Test
    fun `should clamp interval to minimum when below minimum`() {
        // Arrange
        val customInterval = 2L // Below minimum of 5
        
        // Act
        val interval = SyncConfig.getSyncIntervalMinutes(customInterval)
        
        // Assert
        assertEquals(SyncConfig.MIN_SYNC_INTERVAL_MINUTES, interval)
    }
    
    @Test
    fun `should clamp interval to maximum when above maximum`() {
        // Arrange
        val customInterval = 120L // Above maximum of 60
        
        // Act
        val interval = SyncConfig.getSyncIntervalMinutes(customInterval)
        
        // Assert
        assertEquals(SyncConfig.MAX_SYNC_INTERVAL_MINUTES, interval)
    }
    
    @Test
    fun `should return interval at minimum boundary`() {
        // Arrange
        val customInterval = SyncConfig.MIN_SYNC_INTERVAL_MINUTES
        
        // Act
        val interval = SyncConfig.getSyncIntervalMinutes(customInterval)
        
        // Assert
        assertEquals(SyncConfig.MIN_SYNC_INTERVAL_MINUTES, interval)
    }
    
    @Test
    fun `should return interval at maximum boundary`() {
        // Arrange
        val customInterval = SyncConfig.MAX_SYNC_INTERVAL_MINUTES
        
        // Act
        val interval = SyncConfig.getSyncIntervalMinutes(customInterval)
        
        // Assert
        assertEquals(SyncConfig.MAX_SYNC_INTERVAL_MINUTES, interval)
    }
    
    @Test
    fun `should convert minutes to milliseconds correctly`() {
        // Arrange
        val minutes = 15L
        
        // Act
        val milliseconds = SyncConfig.getSyncIntervalMillis(minutes)
        
        // Assert
        assertEquals(15L * 60 * 1000, milliseconds)
    }
    
    @Test
    fun `should use default interval for milliseconds conversion`() {
        // Act
        val milliseconds = SyncConfig.getSyncIntervalMillis()
        
        // Assert
        val expectedMillis = SyncConfig.DEFAULT_SYNC_INTERVAL_MINUTES * 60 * 1000
        assertEquals(expectedMillis, milliseconds)
    }
}

