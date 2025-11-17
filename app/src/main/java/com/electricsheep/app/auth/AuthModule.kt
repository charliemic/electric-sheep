package com.electricsheep.app.auth

import android.content.Context
import com.electricsheep.app.data.DataModule
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient

/**
 * Authentication module for dependency injection and setup.
 * Provides instances of authentication providers and user manager.
 */
object AuthModule {
    
    /**
     * Create authentication provider.
     * Uses Supabase Auth if available, otherwise falls back to placeholder.
     * 
     * @param context Android context (required for OAuth redirect handling)
     * @param supabaseClient Supabase client (can be null if offline-only)
     * @return AuthProvider instance
     */
    fun createAuthProvider(context: Context, supabaseClient: SupabaseClient?): AuthProvider {
        return if (supabaseClient != null) {
            Logger.info("AuthModule", "Creating Supabase authentication provider")
            SupabaseAuthProvider(supabaseClient, context)
        } else {
            Logger.info("AuthModule", "Creating placeholder authentication provider (offline-only mode)")
            PlaceholderAuthProvider()
        }
    }
    
    /**
     * Create user manager
     */
    fun createUserManager(authProvider: AuthProvider): UserManager {
        Logger.info("AuthModule", "Creating user manager")
        return UserManager(authProvider)
    }
}

