package com.electricsheep.app.auth

import com.electricsheep.app.util.Logger
import java.util.UUID

/**
 * Placeholder authentication provider for development.
 * 
 * This implementation:
 * - Stores user in memory (lost on app restart)
 * - No actual authentication (accepts any credentials)
 * - Generates fake user IDs
 * - Suitable for development/testing only
 * 
 * TODO: Replace with real authentication provider (e.g., Supabase Auth, Auth0, etc.)
 */
class PlaceholderAuthProvider : AuthProvider {
    
    private var currentUser: User? = null
    
    override suspend fun getCurrentUser(): User? {
        Logger.debug("PlaceholderAuthProvider", "Getting current user: ${currentUser?.id}")
        return currentUser
    }
    
    override suspend fun signIn(email: String, password: String): Result<User> {
        Logger.info("PlaceholderAuthProvider", "Sign in attempt for: $email")
        
        // Placeholder: Accept any credentials and create/find user
        // In real implementation, this would validate credentials with auth service
        val user = currentUser ?: User(
            id = UUID.randomUUID().toString(),
            email = email,
            displayName = email.substringBefore("@"),
            createdAt = System.currentTimeMillis()
        )
        
        currentUser = user
        Logger.info("PlaceholderAuthProvider", "User signed in: ${user.id}")
        return Result.success(user)
    }
    
    override suspend fun signUp(email: String, password: String, displayName: String?): Result<User> {
        Logger.info("PlaceholderAuthProvider", "Sign up attempt for: $email")
        
        // Placeholder: Create new user without validation
        // In real implementation, this would create user in auth service
        val user = User(
            id = UUID.randomUUID().toString(),
            email = email,
            displayName = displayName ?: email.substringBefore("@"),
            createdAt = System.currentTimeMillis()
        )
        
        currentUser = user
        Logger.info("PlaceholderAuthProvider", "User signed up: ${user.id}")
        return Result.success(user)
    }
    
    override suspend fun signOut(): Result<Unit> {
        Logger.info("PlaceholderAuthProvider", "Sign out for user: ${currentUser?.id}")
        currentUser = null
        return Result.success(Unit)
    }
    
    override suspend fun refreshSession(): Result<Unit> {
        Logger.debug("PlaceholderAuthProvider", "Refreshing session (no-op in placeholder)")
        // Placeholder: No-op
        return Result.success(Unit)
    }
    
    override suspend fun resetPassword(email: String): Result<Unit> {
        Logger.info("PlaceholderAuthProvider", "Password reset requested for: $email")
        // Placeholder: No-op
        return Result.success(Unit)
    }
}

