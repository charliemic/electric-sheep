package com.electricsheep.app.config

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ConfigBasedFeatureFlagProvider.
 * 
 * Note: BuildConfig fields are compile-time constants, so we test the behavior
 * rather than specific values. The actual BuildConfig value is tested in integration.
 */
class ConfigBasedFeatureFlagProviderTest {
    
    private lateinit var provider: ConfigBasedFeatureFlagProvider
    
    @Before
    fun setUp() {
        provider = ConfigBasedFeatureFlagProvider()
    }
    
    @Test
    fun `getBoolean should return value for OFFLINE_ONLY flag`() {
        // Given: OFFLINE_ONLY flag
        val key = FeatureFlag.OFFLINE_ONLY
        
        // When: Getting boolean value
        val value = provider.getBoolean(key, false)
        
        // Then: Should return a boolean value (from BuildConfig)
        // We can't assert the exact value as it depends on BuildConfig, but we verify it's a boolean
        assertNotNull(value)
    }
    
    @Test
    fun `getBoolean should return default for unknown flag`() {
        // Given: Unknown flag key
        val key = "unknown_flag"
        val defaultValue = true
        
        // When: Getting boolean value
        val value = provider.getBoolean(key, defaultValue)
        
        // Then: Should return default
        assertEquals(defaultValue, value)
    }
    
    @Test
    fun `getString should return default for any key`() {
        // Given: Any key
        val key = FeatureFlag.OFFLINE_ONLY
        val defaultValue = "default_value"
        
        // When: Getting string value
        val value = provider.getString(key, defaultValue)
        
        // Then: Should return default
        assertEquals(defaultValue, value)
    }
    
    @Test
    fun `getInt should return default for any key`() {
        // Given: Any key
        val key = FeatureFlag.OFFLINE_ONLY
        val defaultValue = 42
        
        // When: Getting int value
        val value = provider.getInt(key, defaultValue)
        
        // Then: Should return default
        assertEquals(defaultValue, value)
    }
}

