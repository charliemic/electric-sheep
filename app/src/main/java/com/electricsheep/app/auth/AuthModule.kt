package com.electricsheep.app.auth

import com.electricsheep.app.util.Logger

/**
 * Authentication module for dependency injection and setup.
 * Provides instances of authentication providers and user manager.
 */
object AuthModule {
    
    /**
     * Create authentication provider.
     * Currently returns PlaceholderAuthProvider for development.
     * 
     * TODO: Replace with real authentication provider when implementing auth:
     * - Supabase Auth
     * - Auth0
     * - Firebase Auth
     * - Custom OAuth provider
     */
    fun createAuthProvider(): AuthProvider {
        Logger.info("AuthModule", "Creating authentication provider (placeholder)")
        return PlaceholderAuthProvider()
    }
    
    /**
     * Create user manager
     */
    fun createUserManager(authProvider: AuthProvider): UserManager {
        Logger.info("AuthModule", "Creating user manager")
        return UserManager(authProvider)
    }
}

