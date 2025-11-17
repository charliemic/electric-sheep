package com.electricsheep.app.config

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for FeatureFlagManager.
 * Tests feature flag retrieval and offline-only mode detection.
 */
class FeatureFlagManagerTest {
    
    private lateinit var mockProvider: FeatureFlagProvider
    private lateinit var featureFlagManager: FeatureFlagManager
    
    @Before
    fun setUp() {
        mockProvider = mock()
        featureFlagManager = FeatureFlagManager(mockProvider)
    }
    
    @Test
    fun `should return true when offline-only mode is enabled`() {
        // Arrange
        whenever(mockProvider.isEnabled(FeatureFlag.OFFLINE_ONLY, false)).thenReturn(true)
        
        // Act
        val result = featureFlagManager.isOfflineOnly()
        
        // Assert
        assertTrue(result)
        verify(mockProvider).isEnabled(FeatureFlag.OFFLINE_ONLY, false)
    }
    
    @Test
    fun `should return false when offline-only mode is disabled`() {
        // Arrange
        whenever(mockProvider.isEnabled(FeatureFlag.OFFLINE_ONLY, false)).thenReturn(false)
        
        // Act
        val result = featureFlagManager.isOfflineOnly()
        
        // Assert
        assertFalse(result)
        verify(mockProvider).isEnabled(FeatureFlag.OFFLINE_ONLY, false)
    }
    
    @Test
    fun `should delegate boolean flag retrieval to provider`() {
        // Arrange
        whenever(mockProvider.getBoolean("test_flag", true)).thenReturn(true)
        
        // Act
        val result = featureFlagManager.getBoolean("test_flag", true)
        
        // Assert
        assertTrue(result)
        verify(mockProvider).getBoolean("test_flag", true)
    }
    
    @Test
    fun `should delegate string flag retrieval to provider`() {
        // Arrange
        whenever(mockProvider.getString("test_flag", "default")).thenReturn("value")
        
        // Act
        val result = featureFlagManager.getString("test_flag", "default")
        
        // Assert
        assertEquals("value", result)
        verify(mockProvider).getString("test_flag", "default")
    }
    
    @Test
    fun `should delegate integer flag retrieval to provider`() {
        // Arrange
        whenever(mockProvider.getInt("test_flag", 42)).thenReturn(42)
        
        // Act
        val result = featureFlagManager.getInt("test_flag", 42)
        
        // Assert
        assertEquals(42, result)
        verify(mockProvider).getInt("test_flag", 42)
    }
    
    @Test
    fun `should delegate isEnabled to provider`() {
        // Arrange
        whenever(mockProvider.isEnabled("test_flag", false)).thenReturn(true)
        
        // Act
        val result = featureFlagManager.isEnabled("test_flag", false)
        
        // Assert
        assertTrue(result)
        verify(mockProvider).isEnabled("test_flag", false)
    }
}

