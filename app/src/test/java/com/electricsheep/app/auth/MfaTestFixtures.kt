package com.electricsheep.app.auth

import io.github.jan.supabase.gotrue.mfa.MfaFactor
import io.github.jan.supabase.gotrue.mfa.MfaFactorType
import io.github.jan.supabase.gotrue.mfa.MfaResponse

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
     * Note: Actual MfaResponse structure may differ - update based on Supabase SDK
     */
    fun createTestEnrollmentResponse(): MfaResponse {
        // Note: MfaResponse structure from Supabase SDK may differ
        // This is a placeholder - update based on actual SDK response
        return MfaResponse(
            qrCode = "otpauth://totp/ElectricSheep:test@example.com?secret=$TEST_SECRET&issuer=ElectricSheep",
            secret = TEST_SECRET,
            challengeId = TEST_CHALLENGE_ID
        )
    }
    
    /**
     * Create a test MFA factor.
     */
    fun createTestFactor(): MfaFactor {
        return MfaFactor(
            id = TEST_FACTOR_ID,
            type = MfaFactorType.TOTP,
            status = "verified",
            createdAt = "2025-01-22T00:00:00Z"
        )
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

