package com.electricsheep.app.auth

import com.electricsheep.app.util.Logger

/**
 * Interface for authentication providers.
 * 
 * Supports different authentication backends (e.g., Auth0, Supabase Auth, custom OAuth).
 * This abstraction allows switching authentication providers without changing app code.
 */
interface AuthProvider {
    /**
     * Get the current authenticated user, if any.
     * Returns null if no user is authenticated.
     * 
     * @return Current user or null
     */
    suspend fun getCurrentUser(): User?
    
    /**
     * Sign in a user with email and password.
     * 
     * @param email User's email
     * @param password User's password
     * @return Result containing the authenticated user or error
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
    /**
     * Sign up a new user with email and password.
     * 
     * @param email User's email
     * @param password User's password
     * @param displayName Optional display name
     * @return Result containing the newly created user or error
     */
    suspend fun signUp(email: String, password: String, displayName: String? = null): Result<User>
    
    /**
     * Sign out the current user.
     * 
     * @return Result indicating success or failure
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Check if a user is currently authenticated.
     * 
     * @return True if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * Refresh the current user's authentication token/session.
     * Useful for keeping sessions alive.
     * 
     * @return Result indicating success or failure
     */
    suspend fun refreshSession(): Result<Unit>
    
    /**
     * Reset password for a user.
     * 
     * @param email User's email
     * @return Result indicating success or failure
     */
    suspend fun resetPassword(email: String): Result<Unit>
}

