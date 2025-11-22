package com.electricsheep.app.auth

import io.github.jan.supabase.gotrue.mfa.MfaFactor

/**
 * Test fixtures for MFA testing.
 * Provides mock data and helper functions for MFA-related tests.
 */
object MfaTestFixtures {
    
    // Test secret (Base32-encoded, for testing only)
    const val TEST_SECRET = "JBSWY3DPEHPK3PXP"
    
    // Test challenge ID
    const val TEST_CHALLENGE_ID = "challenge-test-123"
    
    // Test user ID
    const val TEST_USER_ID = "user-test-456"
    
    // Test factor ID
    const val TEST_FACTOR_ID = "factor-test-789"
    
    /**
     * Create a test MFA enrollment response.
     * Note: Returns Any to avoid type issues - actual type is MfaFactor<FactorType.TOTP.Response>
     * This is a placeholder until proper mocking is implemented
     */
    fun createTestEnrollmentResponse(): Any {
        // Note: This is a placeholder - actual structure depends on Supabase SDK version
        // The implementation uses MfaFactor<FactorType.TOTP.Response>
        // For testing, we'll need to mock this properly
        // Returning empty object for now to avoid compilation errors
        return object {}
    }
    
    /**
     * Create a test MFA factor.
     * Note: Returns Any to avoid type issues - actual type is UserMfaFactor
     * This is a placeholder until proper mocking is implemented
     */
    fun createTestFactor(): Any {
        // Note: MfaFactor structure from Supabase SDK
        // This is a placeholder - update based on actual SDK structure
        // Returning empty object for now to avoid compilation errors
        return object {}
    }
    
    // Note: Verification and unenrollment return Unit in our implementation
    // No response objects needed for testing
    
    /**
     * Create a test MFA challenge sign-in result.
     */
    fun createTestMfaChallenge(): SignInResult.MfaChallenge {
        return SignInResult.MfaChallenge(
            challengeId = TEST_CHALLENGE_ID,
            userId = TEST_USER_ID
        )
    }
    
    /**
     * Generate a valid test TOTP code using the test secret.
     * Uses TotpTestHelper to generate code without requiring a device.
     */
    fun generateTestTotpCode(): String {
        return TotpTestHelper.generateTotpCode(TEST_SECRET)
    }
    
    /**
     * Generate an invalid test TOTP code (wrong format).
     */
    fun generateInvalidTotpCode(): String {
        return "12345" // Too short
    }
    
    /**
     * Generate an expired test TOTP code (previous time step).
     */
    fun generateExpiredTotpCode(): String {
        return TotpTestHelper.generatePreviousTotpCode(TEST_SECRET)
    }
}

