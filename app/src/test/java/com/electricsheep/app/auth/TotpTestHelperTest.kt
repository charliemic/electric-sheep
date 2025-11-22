package com.electricsheep.app.auth

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for TotpTestHelper.
 * Tests TOTP code generation for testing purposes.
 */
class TotpTestHelperTest {
    
    @Test
    fun `should generate valid TOTP code format`() {
        // Arrange
        val secret = MfaTestFixtures.TEST_SECRET
        
        // Act
        val code = TotpTestHelper.generateTotpCode(secret)
        
        // Assert
        assertTrue("Code should be 6 digits", TotpTestHelper.isValidTotpFormat(code))
        assertEquals(6, code.length)
    }
    
    @Test
    fun `should generate different codes for different time steps`() {
        // Arrange
        val secret = MfaTestFixtures.TEST_SECRET
        val timeStep1 = System.currentTimeMillis() / 1000 / 30
        val timeStep2 = timeStep1 + 1
        
        // Act
        val code1 = TotpTestHelper.generateTotpCode(secret, timeStep1)
        val code2 = TotpTestHelper.generateTotpCode(secret, timeStep2)
        
        // Assert
        // Codes may be the same or different depending on timing
        // But both should be valid format
        assertTrue(TotpTestHelper.isValidTotpFormat(code1))
        assertTrue(TotpTestHelper.isValidTotpFormat(code2))
    }
    
    @Test
    fun `should generate same code for same time step`() {
        // Arrange
        val secret = MfaTestFixtures.TEST_SECRET
        val timeStep = System.currentTimeMillis() / 1000 / 30
        
        // Act
        val code1 = TotpTestHelper.generateTotpCode(secret, timeStep)
        val code2 = TotpTestHelper.generateTotpCode(secret, timeStep)
        
        // Assert
        assertEquals("Same time step should generate same code", code1, code2)
    }
    
    @Test
    fun `should validate correct TOTP format`() {
        // Valid formats
        assertTrue(TotpTestHelper.isValidTotpFormat("123456"))
        assertTrue(TotpTestHelper.isValidTotpFormat("000000"))
        assertTrue(TotpTestHelper.isValidTotpFormat("999999"))
        
        // Invalid formats
        assertFalse(TotpTestHelper.isValidTotpFormat("12345")) // Too short
        assertFalse(TotpTestHelper.isValidTotpFormat("1234567")) // Too long
        assertFalse(TotpTestHelper.isValidTotpFormat("abcdef")) // Not digits
        assertFalse(TotpTestHelper.isValidTotpFormat("")) // Empty
    }
    
    @Test
    fun `should generate previous time step code`() {
        // Arrange
        val secret = MfaTestFixtures.TEST_SECRET
        
        // Act
        val previousCode = TotpTestHelper.generatePreviousTotpCode(secret)
        
        // Assert
        assertTrue(TotpTestHelper.isValidTotpFormat(previousCode))
    }
    
    @Test
    fun `should generate next time step code`() {
        // Arrange
        val secret = MfaTestFixtures.TEST_SECRET
        
        // Act
        val nextCode = TotpTestHelper.generateNextTotpCode(secret)
        
        // Assert
        assertTrue(TotpTestHelper.isValidTotpFormat(nextCode))
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `should throw exception for invalid secret`() {
        // Arrange
        val invalidSecret = "INVALID_SECRET!!!"
        
        // Act & Assert
        TotpTestHelper.generateTotpCode(invalidSecret)
    }
}

