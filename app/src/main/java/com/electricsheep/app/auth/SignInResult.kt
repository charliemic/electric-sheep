package com.electricsheep.app.auth

/**
 * Result of a sign-in attempt.
 * 
 * Can represent either:
 * - Successful authentication (User)
 * - MFA challenge requiring verification (MfaChallenge)
 * - Error (AuthError)
 */
sealed class SignInResult {
    /**
     * Sign-in succeeded, user is authenticated.
     */
    data class Success(val user: User) : SignInResult()
    
    /**
     * Sign-in requires MFA verification.
     * User must provide TOTP code to complete authentication.
     * 
     * @param challengeId Challenge ID for MFA verification
     * @param userId User ID (available after password verification)
     */
    data class MfaChallenge(
        val challengeId: String,
        val userId: String
    ) : SignInResult()
    
    /**
     * Sign-in failed with an error.
     */
    data class Error(val error: AuthError) : SignInResult()
}

