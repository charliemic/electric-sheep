package com.electricsheep.app.data.model

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for FeatureFlag data model.
 * Tests value extraction, user matching, and version handling.
 */
class FeatureFlagTest {
    
    @Test
    fun `getValue should return boolean value for boolean type`() {
        // Given: Boolean flag
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = true,
            version = 1
        )
        
        // When: Getting value
        val value = flag.getValue()
        
        // Then: Should return boolean value
        assertEquals(true, value)
    }
    
    @Test
    fun `getValue should return string value for string type`() {
        // Given: String flag
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "string",
            stringValue = "test_value",
            enabled = true,
            version = 1
        )
        
        // When: Getting value
        val value = flag.getValue()
        
        // Then: Should return string value
        assertEquals("test_value", value)
    }
    
    @Test
    fun `getValue should return int value for int type`() {
        // Given: Int flag
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "int",
            intValue = 42,
            enabled = true,
            version = 1
        )
        
        // When: Getting value
        val value = flag.getValue()
        
        // Then: Should return int value
        assertEquals(42, value)
    }
    
    @Test
    fun `appliesToUser should return false when flag is disabled`() {
        // Given: Disabled flag
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = false,
            version = 1
        )
        
        // When: Checking if applies to user
        val applies = flag.appliesToUser("user-1")
        
        // Then: Should not apply
        assertFalse(applies)
    }
    
    @Test
    fun `appliesToUser should return true for global flag`() {
        // Given: Global flag (no user_id, no segment_id)
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = true,
            version = 1,
            userId = null,
            segmentId = null
        )
        
        // When: Checking if applies to user
        val applies = flag.appliesToUser("user-1")
        
        // Then: Should apply
        assertTrue(applies)
    }
    
    @Test
    fun `appliesToUser should return true for user-specific flag matching user`() {
        // Given: User-specific flag
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = true,
            version = 1,
            userId = "user-1",
            segmentId = null
        )
        
        // When: Checking if applies to matching user
        val applies = flag.appliesToUser("user-1")
        
        // Then: Should apply
        assertTrue(applies)
    }
    
    @Test
    fun `appliesToUser should return false for user-specific flag not matching user`() {
        // Given: User-specific flag for different user
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = true,
            version = 1,
            userId = "user-2",
            segmentId = null
        )
        
        // When: Checking if applies to different user
        val applies = flag.appliesToUser("user-1")
        
        // Then: Should not apply
        assertFalse(applies)
    }
    
    @Test
    fun `appliesToUser should return false for segment flag`() {
        // Given: Segment flag (future implementation)
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = true,
            version = 1,
            userId = null,
            segmentId = "segment-1"
        )
        
        // When: Checking if applies to user
        val applies = flag.appliesToUser("user-1")
        
        // Then: Should not apply (segment check not implemented yet)
        assertFalse(applies)
    }
    
    @Test
    fun `version should default to 1`() {
        // Given: Flag without explicit version
        val flag = FeatureFlag(
            id = "1",
            key = "test_flag",
            valueType = "boolean",
            booleanValue = true,
            enabled = true
        )
        
        // Then: Should have default version
        assertEquals(1, flag.version)
    }
}

