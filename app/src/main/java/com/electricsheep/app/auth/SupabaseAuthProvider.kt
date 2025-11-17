package com.electricsheep.app.auth

import android.content.Context
import android.net.Uri
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase authentication provider implementation.
 * 
 * Uses Supabase Auth for authentication, supporting:
 * - Email/password authentication
 * - Google OAuth (SSO)
 * - Session management
 * 
 * @param supabaseClient Supabase client instance (must have Auth module installed)
 * @param context Android context for OAuth redirect handling
 */
class SupabaseAuthProvider(
    private val supabaseClient: SupabaseClient,
    private val context: Context
) : AuthProvider {
    
    private val supabaseUrl: String
        get() = supabaseClient.supabaseUrl
    
    /**
     * Extract display name from user metadata or email.
     */
    private fun extractDisplayName(metadata: JsonObject?, email: String?): String? {
        if (metadata != null) {
            // Try to get display_name as string
            try {
                val displayName = metadata["display_name"]
                if (displayName is kotlinx.serialization.json.JsonPrimitive && displayName.isString) {
                    return displayName.content
                }
            } catch (e: Exception) {
                // Ignore
            }
            // Try to get full_name as string
            try {
                val fullName = metadata["full_name"]
                if (fullName is kotlinx.serialization.json.JsonPrimitive && fullName.isString) {
                    return fullName.content
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        return email?.substringBefore("@")
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            val userInfo = session?.user
            
            if (userInfo != null) {
                val displayName = extractDisplayName(userInfo.userMetadata, userInfo.email)
                User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = displayName,
                    createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Failed to get current user", e)
            null
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            Logger.info("SupabaseAuthProvider", "Sign in attempt for: $email")
            
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = getCurrentUser()
            if (user != null) {
                Logger.info("SupabaseAuthProvider", "User signed in: ${user.id}")
                Result.success(user)
            } else {
                Logger.error("SupabaseAuthProvider", "Sign in succeeded but user is null")
                Result.failure(AuthError.Generic("Sign in succeeded but user is not available. Please try again."))
            }
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Sign in failed", e)
            Result.failure(AuthError.fromException(e))
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String?): Result<User> {
        return try {
            Logger.info("SupabaseAuthProvider", "Sign up attempt for: $email")
            
            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return Result.failure(AuthError.InvalidEmail())
            }
            
            // Validate password length
            if (password.length < 6) {
                return Result.failure(AuthError.WeakPassword())
            }
            
            val metadata = if (displayName != null) {
                buildJsonObject {
                    put("display_name", displayName)
                }
            } else {
                null
            }
            
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = metadata
            }
            
            // After signUp, try to get user from current session
            // Note: If email confirmation is required, user won't be available until confirmed
            val userInfo = supabaseClient.auth.currentSessionOrNull()?.user
            
            if (userInfo != null) {
                val extractedDisplayName = extractDisplayName(userInfo.userMetadata, userInfo.email)
                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = extractedDisplayName,
                    createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis()
                )
                Logger.info("SupabaseAuthProvider", "User signed up: ${user.id}")
                Result.success(user)
            } else {
                // If email confirmation is required, the user won't be available until confirmed
                Logger.warn("SupabaseAuthProvider", "Sign up succeeded but user is null - email confirmation may be required")
                Result.failure(AuthError.EmailNotConfirmed())
            }
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Sign up failed", e)
            Result.failure(AuthError.fromException(e))
        }
    }

    /**
     * Get the OAuth sign-in URL for Google.
     * This URL should be opened in a browser/WebView, and the callback will be handled via deep link.
     * 
     * @return The OAuth URL to open in browser, or error
     */
    suspend fun getGoogleOAuthUrl(): Result<String> {
        return try {
            Logger.info("SupabaseAuthProvider", "Getting Google OAuth URL")
            
            val redirectUrl = "com.electricsheep.app://auth-callback"
            
            // Build OAuth URL manually based on Supabase pattern
            val url = "$supabaseUrl/auth/v1/authorize?provider=google&redirect_to=$redirectUrl"
            
            Logger.info("SupabaseAuthProvider", "OAuth URL generated: $url")
            Result.success(url)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Failed to get OAuth URL", e)
            Result.failure(e)
        }
    }

    /**
     * Handle OAuth callback from deep link.
     * Called when user returns from OAuth provider via deep link.
     * 
     * @param callbackUri The callback URI from the OAuth provider
     * @return Result containing the authenticated user or error
     */
    suspend fun handleOAuthCallback(callbackUri: Uri): Result<User> {
        return try {
            Logger.info("SupabaseAuthProvider", "Handling OAuth callback: $callbackUri")
            
            // Check for error in callback
            val error = callbackUri.getQueryParameter("error")
            if (error != null) {
                val errorDescription = callbackUri.getQueryParameter("error_description") ?: error
                Logger.error("SupabaseAuthProvider", "OAuth callback error: $error - $errorDescription")
                return Result.failure(AuthError.OAuthCallbackError(Exception(errorDescription)))
            }
            
            // Exchange the OAuth code for a session
            // Parse the callback URI to extract the code
            val code = callbackUri.getQueryParameter("code")
            if (code != null) {
                supabaseClient.auth.exchangeCodeForSession(code)
            } else {
                return Result.failure(AuthError.OAuthCallbackError(Exception("No authorization code found in callback URI")))
            }
            
            // Get the current session after callback
            val session = supabaseClient.auth.currentSessionOrNull()
            val userInfo = session?.user
            
            if (userInfo != null) {
                val extractedDisplayName = extractDisplayName(userInfo.userMetadata, userInfo.email)
                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = extractedDisplayName,
                    createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis()
                )
                Logger.info("SupabaseAuthProvider", "OAuth callback handled successfully: ${user.id}")
                Result.success(user)
            } else {
                Logger.error("SupabaseAuthProvider", "OAuth callback succeeded but user is null")
                Result.failure(AuthError.OAuthCallbackError(Exception("OAuth callback succeeded but user is not available")))
            }
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "OAuth callback handling failed", e)
            Result.failure(AuthError.fromException(e))
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            Logger.info("SupabaseAuthProvider", "Sign out for user")
            supabaseClient.auth.signOut()
            Logger.info("SupabaseAuthProvider", "User signed out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Sign out failed", e)
            Result.failure(e)
        }
    }

    override suspend fun refreshSession(): Result<Unit> {
        return try {
            Logger.debug("SupabaseAuthProvider", "Refreshing session")
            supabaseClient.auth.refreshCurrentSession()
            Logger.debug("SupabaseAuthProvider", "Session refreshed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Session refresh failed", e)
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            Logger.info("SupabaseAuthProvider", "Password reset requested for: $email")
            supabaseClient.auth.resetPasswordForEmail(email)
            Logger.info("SupabaseAuthProvider", "Password reset email sent")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Password reset failed", e)
            Result.failure(e)
        }
    }
}

