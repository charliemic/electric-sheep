package com.electricsheep.app.config

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for CompositeFeatureFlagProvider.
 * Tests fallback behavior between primary (Supabase) and fallback (Config) providers.
 */
class CompositeFeatureFlagProviderTest {
    
    private lateinit var mockPrimaryProvider: FeatureFlagProvider
    private lateinit var mockFallbackProvider: FeatureFlagProvider
    private lateinit var mockRemoteProvider: RemoteFeatureFlagProvider
    private lateinit var compositeProvider: CompositeFeatureFlagProvider
    
    @Before
    fun setUp() {
        mockPrimaryProvider = mock()
        mockFallbackProvider = mock()
    }
    
    @Test
    fun `getBoolean should use primary provider when remote provider has flag`() {
        // Given: Primary is remote provider with flag in cache
        mockRemoteProvider = mock()
        whenever(mockRemoteProvider.isReady()).thenReturn(true)
        whenever(mockRemoteProvider.getCachedFlags()).thenReturn(mapOf("test_flag" to true))
        whenever(mockRemoteProvider.getBoolean("test_flag", false)).thenReturn(true)
        
        compositeProvider = CompositeFeatureFlagProvider(mockRemoteProvider, mockFallbackProvider)
        
        // When: Getting boolean flag
        val value = compositeProvider.getBoolean("test_flag", false)
        
        // Then: Should use primary provider
        assertEquals(true, value)
        verify(mockRemoteProvider).getBoolean("test_flag", false)
        verify(mockFallbackProvider, never()).getBoolean(any(), any())
    }
    
    @Test
    fun `getBoolean should use fallback when remote provider not ready`() {
        // Given: Primary is remote provider but flags not initialised
        mockRemoteProvider = mock()
        whenever(mockRemoteProvider.isReady()).thenReturn(false)
        whenever(mockFallbackProvider.getBoolean("test_flag", false)).thenReturn(true)
        
        compositeProvider = CompositeFeatureFlagProvider(mockRemoteProvider, mockFallbackProvider)
        
        // When: Getting boolean flag
        val value = compositeProvider.getBoolean("test_flag", false)
        
        // Then: Should use fallback
        assertEquals(true, value)
        verify(mockFallbackProvider).getBoolean("test_flag", false)
    }
    
    @Test
    fun `getBoolean should use fallback when flag not in remote cache`() {
        // Given: Primary is remote provider but flag not in cache
        mockRemoteProvider = mock()
        whenever(mockRemoteProvider.isReady()).thenReturn(true)
        whenever(mockRemoteProvider.getCachedFlags()).thenReturn(emptyMap())
        whenever(mockFallbackProvider.getBoolean("test_flag", false)).thenReturn(true)
        
        compositeProvider = CompositeFeatureFlagProvider(mockRemoteProvider, mockFallbackProvider)
        
        // When: Getting boolean flag
        val value = compositeProvider.getBoolean("test_flag", false)
        
        // Then: Should use fallback
        assertEquals(true, value)
        verify(mockFallbackProvider).getBoolean("test_flag", false)
    }
    
    @Test
    fun `getBoolean should use fallback when primary provider throws exception`() {
        // Given: Primary provider throws exception
        mockRemoteProvider = mock()
        whenever(mockRemoteProvider.isReady()).thenReturn(true)
        whenever(mockRemoteProvider.getCachedFlags()).thenReturn(mapOf("test_flag" to true))
        whenever(mockRemoteProvider.getBoolean("test_flag", false)).thenThrow(RuntimeException("Error"))
        whenever(mockFallbackProvider.getBoolean("test_flag", false)).thenReturn(true)
        
        compositeProvider = CompositeFeatureFlagProvider(mockRemoteProvider, mockFallbackProvider)
        
        // When: Getting boolean flag
        val value = compositeProvider.getBoolean("test_flag", false)
        
        // Then: Should use fallback
        assertEquals(true, value)
        verify(mockFallbackProvider).getBoolean("test_flag", false)
    }
    
    @Test
    fun `getBoolean should use primary when non-remote provider`() {
        // Given: Primary is non-remote provider
        whenever(mockPrimaryProvider.getBoolean("test_flag", false)).thenReturn(true)
        
        compositeProvider = CompositeFeatureFlagProvider(mockPrimaryProvider, mockFallbackProvider)
        
        // When: Getting boolean flag
        val value = compositeProvider.getBoolean("test_flag", false)
        
        // Then: Should use primary
        assertEquals(true, value)
        verify(mockPrimaryProvider).getBoolean("test_flag", false)
        verify(mockFallbackProvider, never()).getBoolean(any(), any())
    }
    
    @Test
    fun `getString should use fallback when remote provider not ready`() {
        // Given: Primary is remote provider but flags not initialised
        mockRemoteProvider = mock()
        whenever(mockRemoteProvider.isReady()).thenReturn(false)
        whenever(mockFallbackProvider.getString("test_flag", "default")).thenReturn("fallback_value")
        
        compositeProvider = CompositeFeatureFlagProvider(mockRemoteProvider, mockFallbackProvider)
        
        // When: Getting string flag
        val value = compositeProvider.getString("test_flag", "default")
        
        // Then: Should use fallback
        assertEquals("fallback_value", value)
        verify(mockFallbackProvider).getString("test_flag", "default")
    }
    
    @Test
    fun `getInt should use fallback when remote provider not ready`() {
        // Given: Primary is remote provider but flags not initialised
        mockRemoteProvider = mock()
        whenever(mockRemoteProvider.isReady()).thenReturn(false)
        whenever(mockFallbackProvider.getInt("test_flag", 0)).thenReturn(42)
        
        compositeProvider = CompositeFeatureFlagProvider(mockRemoteProvider, mockFallbackProvider)
        
        // When: Getting int flag
        val value = compositeProvider.getInt("test_flag", 0)
        
        // Then: Should use fallback
        assertEquals(42, value)
        verify(mockFallbackProvider).getInt("test_flag", 0)
    }
}

