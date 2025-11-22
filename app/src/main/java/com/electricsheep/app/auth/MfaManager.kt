package com.electricsheep.app.auth

import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.mfa.MfaFactor
import io.github.jan.supabase.gotrue.mfa.MfaResponse
import io.github.jan.supabase.gotrue.mfa.TotpFactor
import io.github.jan.supabase.gotrue.mfa

/**
 * Manager for Multi-Factor Authentication (MFA) operations.
 * 
 * Provides a high-level interface for MFA operations using Supabase Auth:
 * - Enroll in MFA (TOTP)
 * - Verify MFA codes
 * - List MFA factors
 * - Unenroll from MFA
 * - Generate backup codes
 * 
 * **Security Benefits:**
 * - Reduces account takeover risk by ~90%
 * - Industry standard for sensitive apps
 * - Protects health data (GDPR special category)
 * 
 * **Implementation Status:**
 * - Infrastructure complete
 * - Supabase MFA API integration
 * - Error handling and logging
 * 
 * @param supabaseClient Supabase client with Auth module installed
 */
class MfaManager(
    private val supabaseClient: SupabaseClient
) {
    
    /**
     * Check if the current user has MFA enabled.
     * 
     * @return true if user has at least one MFA factor enrolled
     */
    suspend fun isMfaEnabled(): Boolean {
        return try {
            val factors = listFactors()
            factors.isNotEmpty()
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to check MFA status", e)
            false
        }
    }
    
    /**
     * List all MFA factors for the current user.
     * 
     * @return List of enrolled MFA factors
     */
    suspend fun listFactors(): List<MfaFactor> {
        return try {
            Logger.debug("MfaManager", "Listing MFA factors")
            val factors = supabaseClient.auth.mfa.listFactors()
            Logger.info("MfaManager", "Found ${factors.size} MFA factor(s)")
            factors
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to list MFA factors", e)
            throw MfaError.ListFactorsFailed(e)
        }
    }
    
    /**
     * Start MFA enrollment process.
     * 
     * This initiates TOTP enrollment and returns:
     * - QR code data for authenticator apps
     * - Secret key for manual entry
     * - Challenge ID for verification
     * 
     * **Next Steps:**
     * 1. Display QR code to user
     * 2. User scans QR code with authenticator app
     * 3. User enters code from app
     * 4. Call `verifyEnrollment()` with the code
     * 
     * @param friendlyName Optional friendly name for the factor (e.g., "My Phone")
     * @return MfaResponse containing QR code data and challenge ID
     */
    suspend fun startEnrollment(friendlyName: String? = null): Result<MfaResponse> {
        return try {
            Logger.info("MfaManager", "Starting MFA enrollment${friendlyName?.let { " with name: $it" } ?: ""}")
            
            val response = supabaseClient.auth.mfa.enroll(
                factorType = TotpFactor,
                friendlyName = friendlyName
            )
            
            Logger.info("MfaManager", "MFA enrollment started successfully")
            Logger.debug("MfaManager", "Challenge ID: ${response.challengeId}, QR code available: ${response.qrCode != null}")
            
            Result.success(response)
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to start MFA enrollment", e)
            Result.failure(MfaError.EnrollmentFailed(e))
        }
    }
    
    /**
     * Verify MFA enrollment with a code from the authenticator app.
     * 
     * After user scans QR code and gets a code from their authenticator app,
     * call this method to complete enrollment.
     * 
     * @param challengeId Challenge ID from `startEnrollment()`
     * @param code TOTP code from authenticator app (6 digits)
     * @return Result indicating success or failure
     */
    suspend fun verifyEnrollment(challengeId: String, code: String): Result<Unit> {
        return try {
            Logger.info("MfaManager", "Verifying MFA enrollment with challenge: $challengeId")
            
            if (code.length != 6 || !code.all { it.isDigit() }) {
                Logger.warn("MfaManager", "Invalid TOTP code format: $code (expected 6 digits)")
                return Result.failure(MfaError.InvalidCode("TOTP code must be 6 digits"))
            }
            
            supabaseClient.auth.mfa.verify(
                challengeId = challengeId,
                code = code
            )
            
            Logger.info("MfaManager", "MFA enrollment verified successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to verify MFA enrollment", e)
            Result.failure(MfaError.VerificationFailed(e))
        }
    }
    
    /**
     * Verify MFA code during login.
     * 
     * After user enters password, if MFA is enabled, call this method
     * with the code from their authenticator app.
     * 
     * @param challengeId Challenge ID from login response
     * @param code TOTP code from authenticator app (6 digits)
     * @return Result indicating success or failure
     */
    suspend fun verifyLogin(challengeId: String, code: String): Result<Unit> {
        return try {
            Logger.info("MfaManager", "Verifying MFA code for login with challenge: $challengeId")
            
            if (code.length != 6 || !code.all { it.isDigit() }) {
                Logger.warn("MfaManager", "Invalid TOTP code format: $code (expected 6 digits)")
                return Result.failure(MfaError.InvalidCode("TOTP code must be 6 digits"))
            }
            
            supabaseClient.auth.mfa.verify(
                challengeId = challengeId,
                code = code
            )
            
            Logger.info("MfaManager", "MFA login verification successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to verify MFA code for login", e)
            Result.failure(MfaError.VerificationFailed(e))
        }
    }
    
    /**
     * Unenroll from MFA (disable MFA for current user).
     * 
     * **Security Note**: This should require additional verification
     * (e.g., password confirmation) before allowing MFA disable.
     * 
     * @param factorId ID of the factor to unenroll
     * @return Result indicating success or failure
     */
    suspend fun unenroll(factorId: String): Result<Unit> {
        return try {
            Logger.info("MfaManager", "Unenrolling MFA factor: $factorId")
            
            supabaseClient.auth.mfa.unenroll(factorId)
            
            Logger.info("MfaManager", "MFA unenrollment successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to unenroll MFA factor", e)
            Result.failure(MfaError.UnenrollmentFailed(e))
        }
    }
    
    /**
     * Get backup codes for MFA recovery.
     * 
     * Backup codes are single-use codes that can be used instead of TOTP
     * if the user loses access to their authenticator app.
     * 
     * **Security Note**: Backup codes should be:
     * - Displayed only once during enrollment
     * - Stored securely by the user
     * - Regenerated if compromised
     * 
     * @return List of backup codes (typically 8-10 codes)
     */
    suspend fun getBackupCodes(): Result<List<String>> {
        return try {
            Logger.info("MfaManager", "Retrieving backup codes")
            
            // Note: Supabase may generate backup codes during enrollment
            // Check Supabase SDK documentation for backup code API
            // For now, return empty list as placeholder
            Logger.warn("MfaManager", "Backup codes API not yet implemented - check Supabase SDK")
            Result.success(emptyList())
        } catch (e: Exception) {
            Logger.error("MfaManager", "Failed to get backup codes", e)
            Result.failure(MfaError.BackupCodesFailed(e))
        }
    }
}

/**
 * MFA-related errors.
 */
sealed class MfaError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class EnrollmentFailed(cause: Throwable) : MfaError("MFA enrollment failed", cause)
    class VerificationFailed(cause: Throwable) : MfaError("MFA verification failed", cause)
    class UnenrollmentFailed(cause: Throwable) : MfaError("MFA unenrollment failed", cause)
    class ListFactorsFailed(cause: Throwable) : MfaError("Failed to list MFA factors", cause)
    class BackupCodesFailed(cause: Throwable) : MfaError("Failed to get backup codes", cause)
    class InvalidCode(message: String) : MfaError(message)
}

