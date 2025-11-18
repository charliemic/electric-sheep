package com.electricsheep.app.auth

import android.content.Context
import android.net.Uri
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.user.UserSession
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
        } catch (e: com.electricsheep.app.error.NetworkError) {
            // Network errors are recoverable - convert to AuthError for consistency
            e.log("SupabaseAuthProvider", "Sign in failed - network error")
            val authError = AuthError.NetworkError(e)
            Result.failure(authError)
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
                Logger.warn("SupabaseAuthProvider", "Invalid email format: $email")
                return Result.failure(AuthError.InvalidEmail())
            }
            
            // Validate password length
            if (password.length < 6) {
                Logger.warn("SupabaseAuthProvider", "Password too short: ${password.length} characters (minimum: 6)")
                return Result.failure(AuthError.WeakPassword())
            }
            
            Logger.debug("SupabaseAuthProvider", "Email and password validation passed")
            
            val metadata = if (displayName != null) {
                Logger.debug("SupabaseAuthProvider", "Including display name in metadata: $displayName")
                buildJsonObject {
                    put("display_name", displayName)
                }
            } else {
                Logger.debug("SupabaseAuthProvider", "No display name provided")
                null
            }
            
            Logger.debug("SupabaseAuthProvider", "Calling Supabase auth.signUpWith(Email)")
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = metadata
            }
            Logger.debug("SupabaseAuthProvider", "Supabase signUpWith completed, checking for user session")
            
            // After signUp, try to get user from current session
            // Note: If email confirmation is required, user won't be available until confirmed
            val session = supabaseClient.auth.currentSessionOrNull()
            Logger.debug("SupabaseAuthProvider", "Current session after sign up: ${if (session != null) "available" else "null"}")
            
            val userInfo = session?.user
            
            if (userInfo != null) {
                val extractedDisplayName = extractDisplayName(userInfo.userMetadata, userInfo.email)
                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = extractedDisplayName,
                    createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis()
                )
                Logger.info("SupabaseAuthProvider", "User signed up successfully - ID: ${user.id}, Email: ${user.email}")
                Logger.debug("SupabaseAuthProvider", "User display name: ${user.displayName}, Created: ${user.createdAt}")
                Result.success(user)
            } else {
                // If email confirmation is required, the user won't be available until confirmed
                Logger.warn("SupabaseAuthProvider", "Sign up succeeded but user is null - email confirmation may be required")
                Logger.debug("SupabaseAuthProvider", "This is expected if email confirmation is enabled in Supabase")
                Result.failure(AuthError.EmailNotConfirmed())
            }
        } catch (e: com.electricsheep.app.error.NetworkError) {
            // Network errors are recoverable - convert to AuthError for consistency
            e.log("SupabaseAuthProvider", "Sign up failed - network error")
            Logger.debug("SupabaseAuthProvider", "Network error details - recoverable: ${e.isRecoverable}, shouldRetry: ${e.shouldRetry}")
            val authError = AuthError.NetworkError(e)
            Result.failure(authError)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Sign up failed", e)
            Logger.debug("SupabaseAuthProvider", "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
            val authError = AuthError.fromException(e)
            Logger.debug("SupabaseAuthProvider", "Converted to AuthError: ${authError.javaClass.simpleName} - ${authError.message}")
            Result.failure(authError)
        }
    }

    /**
     * Initiate Google OAuth sign-in using Supabase SDK's native method.
     * The SDK handles PKCE, state, and deep link callbacks automatically.
     * 
     * Based on Supabase Kotlin SDK documentation and examples:
     * - Uses signInWith(Google) which returns an OAuthUrlResult with a URL property
     * - The SDK automatically handles PKCE when FlowType.PKCE is configured
     * - Deep link configuration is handled by Auth module settings
     * 
     * @return Result containing the OAuth URL to open in browser, or error
     */
    suspend fun getGoogleOAuthUrl(): Result<String> {
        return try {
            Logger.info("SupabaseAuthProvider", "Initiating Google OAuth sign-in using native SDK method")
            Logger.debug("SupabaseAuthProvider", "SDK will handle PKCE, state, and deep link callbacks automatically")
            
            // Use Supabase SDK's native getOAuthUrl method
            // This method returns the OAuth URL that needs to be opened in a browser
            // The SDK automatically handles:
            // - PKCE code challenge generation (if FlowType.PKCE is configured)
            // - State parameter generation
            // - Deep link configuration (from Auth module config: scheme + host)
            // getOAuthUrl signature from JAR inspection:
            // getOAuthUrl(OAuthProvider, String, String, Function1<ExternalAuthConfigDefaults, Unit>)
            // Parameters: provider, redirectTo (String), scopes (String?), config lambda
            val redirectUrl = "com.electricsheep.app://auth-callback"
            var oauthUrl = supabaseClient.auth.getOAuthUrl(
                Google,
                redirectUrl,
                "", // scopes as String (empty for default)
                { } // config lambda for ExternalAuthConfigDefaults (empty for defaults)
            )
            
            // The SDK's getOAuthUrl generates auth/v1/ but needs auth/v1/authorize
            // Also need to add the API key as a query parameter
            val anonKey = com.electricsheep.app.BuildConfig.SUPABASE_ANON_KEY
            val uri = android.net.Uri.parse(oauthUrl)
            
            // Fix the path: SDK generates auth/v1/ but should be auth/v1/authorize
            val fixedPath = if (uri.path == "/auth/v1/" || uri.path == "/auth/v1") {
                "/auth/v1/authorize"
            } else {
                uri.path
            }
            
            val builder = uri.buildUpon()
                .path(fixedPath)
            
            // Add API key if not already present
            if (uri.getQueryParameter("apikey") == null) {
                builder.appendQueryParameter("apikey", anonKey)
            }
            
            oauthUrl = builder.build().toString()
            Logger.debug("SupabaseAuthProvider", "Fixed OAuth URL path to /auth/v1/authorize and added API key")
            
            Logger.info("SupabaseAuthProvider", "Google OAuth URL obtained from SDK")
            Logger.debug("SupabaseAuthProvider", "OAuth URL: ${oauthUrl.take(200)}...")
            
            Result.success(oauthUrl)
        } catch (e: com.electricsheep.app.error.NetworkError) {
            e.log("SupabaseAuthProvider", "Google OAuth sign-in failed - network error")
            val authError = AuthError.NetworkError(e)
            Result.failure(authError)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Failed to initiate Google OAuth sign-in", e)
            Logger.debug("SupabaseAuthProvider", "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
            Result.failure(AuthError.fromException(e))
        }
    }

    /**
     * Handle OAuth callback from deep link using Supabase SDK's native method.
     * The SDK handles PKCE verification, state validation, and session creation automatically.
     * 
     * Note: This method should be called from MainActivity after handleDeeplinks() processes the intent.
     * The SDK's handleDeeplinks() method processes the deep link and completes the OAuth flow.
     * 
     * @param callbackUri The callback URI from the OAuth provider (for logging/debugging)
     * @return Result containing the authenticated user or error
     */
    suspend fun handleOAuthCallback(callbackUri: Uri): Result<User> {
        return try {
            Logger.info("SupabaseAuthProvider", "OAuth callback received: $callbackUri")
            Logger.debug("SupabaseAuthProvider", "Callback URI scheme: ${callbackUri.scheme}, host: ${callbackUri.host}")
            Logger.debug("SupabaseAuthProvider", "Note: SDK's handleDeeplinks() should have already processed this callback")
            
            // The SDK's handleDeeplinks() method (called in MainActivity) has already:
            // - Validated the state parameter
            // - Exchanged the code for tokens (with PKCE if configured)
            // - Created and stored the session
            // 
            // We just need to retrieve the current session and get the user
            Logger.debug("SupabaseAuthProvider", "Retrieving current session from SDK")
            val session = supabaseClient.auth.currentSessionOrNull()
            
            if (session == null) {
                Logger.error("SupabaseAuthProvider", "Session is null after OAuth callback - SDK may not have processed the callback yet")
                Logger.debug("SupabaseAuthProvider", "Ensure MainActivity.handleDeeplinks() is called before this method")
                return Result.failure(AuthError.OAuthCallbackError(Exception("Session is not available. Please ensure handleDeeplinks() was called in MainActivity.")))
            }
            
            val userInfo = session.user
            Logger.debug("SupabaseAuthProvider", "Retrieved session - User ID: ${userInfo?.id}, Email: ${userInfo?.email}, Session expires at: ${session.expiresAt}")
            
            if (userInfo != null) {
                val extractedDisplayName = extractDisplayName(userInfo.userMetadata, userInfo.email)
                val user = User(
                    id = userInfo.id,
                    email = userInfo.email ?: "",
                    displayName = extractedDisplayName,
                    createdAt = userInfo.createdAt?.toEpochMilliseconds() ?: System.currentTimeMillis()
                )
                Logger.info("SupabaseAuthProvider", "OAuth callback handled successfully - User: ${user.id} (${user.email}), Display name: ${user.displayName}")
                Result.success(user)
            } else {
                Logger.error("SupabaseAuthProvider", "OAuth callback succeeded but user is null in session")
                Result.failure(AuthError.OAuthCallbackError(Exception("OAuth callback succeeded but user is not available")))
            }
        } catch (e: com.electricsheep.app.error.NetworkError) {
            e.log("SupabaseAuthProvider", "OAuth callback failed - network error")
            val authError = AuthError.NetworkError(e)
            Result.failure(authError)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "OAuth callback handling failed", e)
            Logger.debug("SupabaseAuthProvider", "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
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
            Logger.debug("SupabaseAuthProvider", "Validating email format before password reset")
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Logger.warn("SupabaseAuthProvider", "Invalid email format for password reset: $email")
                return Result.failure(AuthError.InvalidEmail())
            }
            
            Logger.debug("SupabaseAuthProvider", "Calling Supabase auth.resetPasswordForEmail")
            supabaseClient.auth.resetPasswordForEmail(email)
            Logger.info("SupabaseAuthProvider", "Password reset email sent successfully")
            Result.success(Unit)
        } catch (e: com.electricsheep.app.error.NetworkError) {
            e.log("SupabaseAuthProvider", "Password reset failed - network error")
            val authError = AuthError.NetworkError(e)
            Result.failure(authError)
        } catch (e: Exception) {
            Logger.error("SupabaseAuthProvider", "Password reset failed", e)
            Logger.debug("SupabaseAuthProvider", "Exception type: ${e.javaClass.simpleName}, message: ${e.message}")
            Result.failure(AuthError.fromException(e))
        }
    }
}

