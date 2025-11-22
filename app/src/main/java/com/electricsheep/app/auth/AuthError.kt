package com.electricsheep.app.auth

/**
 * Represents different types of authentication errors with user-friendly messages.
 */
sealed class AuthError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /**
     * Invalid email or password
     */
    data class InvalidCredentials(override val cause: Throwable? = null) 
        : AuthError("Invalid email or password. Please check your credentials and try again.", cause)
    
    /**
     * User already exists (sign up)
     */
    data class UserAlreadyExists(override val cause: Throwable? = null) 
        : AuthError("An account with this email already exists. Please sign in instead.", cause)
    
    /**
     * Email not confirmed
     */
    data class EmailNotConfirmed(override val cause: Throwable? = null) 
        : AuthError("Please check your email and confirm your account before signing in.", cause)
    
    /**
     * Weak password (doesn't meet requirements)
     */
    data class WeakPassword(override val cause: Throwable? = null) 
        : AuthError("Password is too weak. Please use at least 6 characters.", cause)
    
    /**
     * Invalid email format
     */
    data class InvalidEmail(override val cause: Throwable? = null) 
        : AuthError("Please enter a valid email address.", cause)
    
    /**
     * Network error (no internet connection)
     */
    data class NetworkError(override val cause: Throwable? = null) 
        : AuthError("Unable to connect. Please check your internet connection and try again.", cause)
    
    /**
     * OAuth error (Google sign-in failed)
     */
    data class OAuthError(override val cause: Throwable? = null) 
        : AuthError("Google sign-in failed. Please try again.", cause)
    
    /**
     * OAuth callback error (missing code or invalid redirect)
     */
    data class OAuthCallbackError(override val cause: Throwable? = null) 
        : AuthError("Authentication was cancelled or failed. Please try again.", cause)
    
    /**
     * User not found (sign in)
     */
    data class UserNotFound(override val cause: Throwable? = null) 
        : AuthError("No account found with this email. Please sign up first.", cause)
    
    /**
     * Generic authentication error
     */
    data class Generic(override val message: String, override val cause: Throwable? = null) 
        : AuthError(message, cause)
    
    companion object {
        /**
         * Parse a Supabase exception into a user-friendly AuthError.
         */
        fun fromException(exception: Throwable): AuthError {
            val message = exception.message?.lowercase() ?: ""
            val cause = exception.cause ?: exception
            
            return when {
                // Invalid credentials
                message.contains("invalid login") || 
                message.contains("invalid credentials") ||
                message.contains("email or password") ||
                message.contains("wrong password") -> InvalidCredentials(cause)
                
                // User already exists
                message.contains("user already registered") ||
                message.contains("already registered") ||
                message.contains("email already exists") ||
                message.contains("already exists") -> UserAlreadyExists(cause)
                
                // Email not confirmed
                message.contains("email not confirmed") ||
                message.contains("email_not_confirmed") ||
                message.contains("confirm your email") -> EmailNotConfirmed(cause)
                
                // Weak password
                message.contains("password") && (
                    message.contains("too short") ||
                    message.contains("too weak") ||
                    message.contains("minimum") ||
                    message.contains("at least")
                ) -> WeakPassword(cause)
                
                // Invalid email
                message.contains("invalid email") ||
                message.contains("email format") ||
                message.contains("malformed email") -> InvalidEmail(cause)
                
                // Network errors
                message.contains("network") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("unable to connect") ||
                message.contains("no internet") ||
                exception is java.net.UnknownHostException ||
                exception is java.net.ConnectException -> NetworkError(cause)
                
                // OAuth errors
                message.contains("oauth") ||
                message.contains("google") && message.contains("failed") ||
                message.contains("authorization") && message.contains("failed") -> OAuthError(cause)
                
                // User not found
                message.contains("user not found") ||
                message.contains("no user found") -> UserNotFound(cause)
                
                // Generic error - use original message if it's user-friendly, otherwise generic
                else -> {
                    val userMessage = if (message.isNotBlank() && message.length < 100) {
                        message.replaceFirstChar { it.uppercaseChar() }
                    } else {
                        "An error occurred. Please try again."
                    }
                    Generic(userMessage, cause)
                }
            }
        }
    }
}

