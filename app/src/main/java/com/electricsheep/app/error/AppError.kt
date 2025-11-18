package com.electricsheep.app.error

import com.electricsheep.app.util.Logger

/**
 * Base sealed class for all application errors.
 * 
 * Errors are classified as either:
 * - **Recoverable**: User can retry or take action to resolve
 * - **Non-Recoverable**: System-level issues that require app restart or admin intervention
 */
sealed class AppError(
    message: String,
    cause: Throwable? = null,
    val isRecoverable: Boolean = true,
    val userMessage: String = message,
    val shouldRetry: Boolean = false,
    val retryDelayMillis: Long = 0L
) : Exception(message, cause) {
    
    /**
     * Log this error with appropriate level and context.
     * 
     * @param tag Log tag (component name)
     * @param context Additional context for debugging
     */
    fun log(tag: String, context: String = "") {
        val contextMsg = if (context.isNotBlank()) " - $context" else ""
        if (isRecoverable) {
            Logger.warn(tag, "Recoverable error: ${this::class.simpleName}$contextMsg", cause)
        } else {
            Logger.error(tag, "Non-recoverable error: ${this::class.simpleName}$contextMsg", cause)
        }
    }
}

/**
 * Network-related errors.
 */
sealed class NetworkError(
    message: String,
    cause: Throwable? = null,
    userMessage: String = message,
    isRecoverable: Boolean = true,
    shouldRetry: Boolean = true,
    retryDelayMillis: Long = 2000L // 2 seconds default
) : AppError(message, cause, isRecoverable, userMessage, shouldRetry, retryDelayMillis) {
    
    /**
     * No internet connection - user can check connection and retry
     */
    class NoConnection(cause: Throwable? = null)
        : NetworkError(
            message = "No internet connection",
            cause = cause,
            userMessage = "No internet connection. Please check your network and try again.",
            retryDelayMillis = 3000L
        )
    
    /**
     * Connection timeout - network is slow or server is unresponsive
     */
    data class Timeout(val timeoutSeconds: Int = 30, val errorCause: Throwable? = null)
        : NetworkError(
            message = "Request timed out after ${timeoutSeconds}s",
            cause = errorCause,
            userMessage = "Request took too long. Please check your connection and try again.",
            retryDelayMillis = 5000L
        )
    
    /**
     * Server error (5xx) - temporary server issue, should retry
     */
    data class ServerError(
        val statusCode: Int = 500,
        val serverMessage: String? = null,
        val errorCause: Throwable? = null
    ) : NetworkError(
        message = "Server error: $statusCode${if (serverMessage != null) " - $serverMessage" else ""}",
        cause = errorCause,
        userMessage = "Server is temporarily unavailable. Please try again in a moment.",
        retryDelayMillis = 10000L // Wait longer for server errors
    )
    
    /**
     * Client error (4xx) - usually not recoverable without user action
     */
    data class ClientError(
        val statusCode: Int = 400,
        val serverMessage: String? = null,
        val errorCause: Throwable? = null
    ) : NetworkError(
        message = "Client error: $statusCode${if (serverMessage != null) " - $serverMessage" else ""}",
        cause = errorCause,
        userMessage = serverMessage ?: "Invalid request. Please check your input and try again.",
        isRecoverable = true, // User can fix input
        shouldRetry = false // Don't auto-retry client errors
    )
    
    /**
     * Authentication/authorization error - user needs to re-authenticate
     */
    class Unauthorized(cause: Throwable? = null)
        : NetworkError(
            message = "Authentication failed",
            cause = cause,
            userMessage = "Your session has expired. Please sign in again.",
            isRecoverable = true,
            shouldRetry = false // Requires user action
        )
    
    /**
     * Rate limiting - too many requests, should retry after delay
     */
    data class RateLimited(
        val retryAfterSeconds: Int = 60,
        val errorCause: Throwable? = null
    ) : NetworkError(
        message = "Rate limited - retry after ${retryAfterSeconds}s",
        cause = errorCause,
        userMessage = "Too many requests. Please wait a moment and try again.",
        retryDelayMillis = retryAfterSeconds * 1000L
    )
    
    /**
     * Unknown network error
     */
    class Unknown(cause: Throwable? = null)
        : NetworkError(
            message = "Unknown network error",
            cause = cause,
            userMessage = "Network error occurred. Please check your connection and try again.",
            retryDelayMillis = 3000L
        )
    
    companion object {
        /**
         * Parse an exception into a NetworkError.
         * 
         * Priority order:
         * 1. Specific exception types (most reliable)
         * 2. HTTP status codes in message
         * 3. Error message patterns (less reliable)
         * 4. Default to Unknown
         */
        fun fromException(exception: Throwable): NetworkError {
            val message = exception.message?.lowercase() ?: ""
            
            return when {
                // Priority 1: Specific exception types (most reliable)
                exception is java.net.UnknownHostException -> NoConnection(exception)
                exception is java.net.ConnectException -> NoConnection(exception)
                exception is java.net.SocketTimeoutException -> {
                    // Try to extract timeout value from exception
                    val timeoutSeconds = extractTimeoutSeconds(message, 30)
                    Timeout(timeoutSeconds, exception)
                }
                exception is java.util.concurrent.TimeoutException -> {
                    val timeoutSeconds = extractTimeoutSeconds(message, 30)
                    Timeout(timeoutSeconds, exception)
                }
                exception is java.net.SocketException && 
                    (message.contains("network") || message.contains("connection")) -> NoConnection(exception)
                
                // Priority 2: HTTP status codes (if available from exception or message)
                // Check for HTTP status codes in message first (more specific)
                Regex("\\b(401|403)\\b").containsMatchIn(message) -> Unauthorized(exception)
                Regex("\\b429\\b").containsMatchIn(message) -> {
                    val retryAfter = extractRetryAfter(message)
                    RateLimited(retryAfter, exception)
                }
                Regex("\\b5\\d{2}\\b").containsMatchIn(message) -> {
                    val statusCode = Regex("(5\\d{2})").find(message)?.value?.toIntOrNull() ?: 500
                    ServerError(statusCode, null, exception)
                }
                Regex("\\b4\\d{2}\\b").containsMatchIn(message) -> {
                    val statusCode = Regex("(4\\d{2})").find(message)?.value?.toIntOrNull() ?: 400
                    ClientError(statusCode, null, exception)
                }
                
                // Priority 3: Error message patterns (less reliable, but useful fallback)
                message.contains("no internet") ||
                message.contains("network unreachable") ||
                message.contains("no route to host") ||
                message.contains("connection refused") -> NoConnection(exception)
                
                message.contains("timeout") ||
                message.contains("timed out") -> {
                    val timeoutSeconds = extractTimeoutSeconds(message, 30)
                    Timeout(timeoutSeconds, exception)
                }
                
                message.contains("unauthorized") ||
                message.contains("forbidden") -> Unauthorized(exception)
                
                message.contains("rate limit") ||
                message.contains("too many requests") -> {
                    val retryAfter = extractRetryAfter(message)
                    RateLimited(retryAfter, exception)
                }
                
                message.contains("server error") ||
                message.contains("internal server error") ||
                message.contains("service unavailable") -> {
                    ServerError(500, null, exception)
                }
                
                message.contains("client error") ||
                message.contains("bad request") -> {
                    ClientError(400, null, exception)
                }
                
                // Default to unknown
                else -> Unknown(exception)
            }
        }
        
        /**
         * Extract timeout seconds from error message.
         * Looks for patterns like "30s", "30 seconds", "timeout: 30", etc.
         */
        private fun extractTimeoutSeconds(message: String, default: Int): Int {
            // Try various patterns
            val patterns = listOf(
                Regex("(\\d+)\\s*seconds?"),
                Regex("(\\d+)\\s*s\\b"),
                Regex("timeout[=:]?\\s*(\\d+)"),
                Regex("after\\s+(\\d+)")
            )
            
            for (pattern in patterns) {
                val match = pattern.find(message)
                if (match != null) {
                    return match.groupValues[1].toIntOrNull() ?: default
                }
            }
            
            return default
        }
        
        /**
         * Extract retry-after seconds from error message.
         * Looks for patterns like "retry-after: 60", "retry after 60s", etc.
         */
        private fun extractRetryAfter(message: String): Int {
            val patterns = listOf(
                Regex("retry[_-]?after[=:]?\\s*(\\d+)", RegexOption.IGNORE_CASE),
                Regex("retry\\s+in\\s+(\\d+)\\s*seconds?", RegexOption.IGNORE_CASE),
                Regex("wait\\s+(\\d+)\\s*seconds?", RegexOption.IGNORE_CASE)
            )
            
            for (pattern in patterns) {
                val match = pattern.find(message)
                if (match != null) {
                    return match.groupValues[1].toIntOrNull() ?: 60
                }
            }
            
            return 60 // Default retry after 60 seconds
        }
    }
}

/**
 * Data operation errors (database, storage, etc.)
 */
sealed class DataError(
    message: String,
    cause: Throwable? = null,
    userMessage: String = message,
    isRecoverable: Boolean = true,
    shouldRetry: Boolean = false
) : AppError(message, cause, isRecoverable, userMessage, shouldRetry) {
    
    /**
     * Database constraint violation (e.g., duplicate key)
     */
    data class ConstraintViolation(val constraint: String? = null, val errorCause: Throwable? = null)
        : DataError(
            message = "Database constraint violation${if (constraint != null) ": $constraint" else ""}",
            cause = errorCause,
            userMessage = "This data already exists. Please check and try again.",
            isRecoverable = true,
            shouldRetry = false
        )
    
    /**
     * Data not found
     */
    data class NotFound(val resource: String = "resource", val errorCause: Throwable? = null)
        : DataError(
            message = "$resource not found",
            cause = errorCause,
            userMessage = "The requested item could not be found.",
            isRecoverable = false,
            shouldRetry = false
        )
    
    /**
     * Invalid data format or validation failure
     */
    data class InvalidData(val field: String? = null, val errorCause: Throwable? = null)
        : DataError(
            message = "Invalid data${if (field != null) " in field: $field" else ""}",
            cause = errorCause,
            userMessage = if (field != null) {
                "Invalid $field. Please check your input."
            } else {
                "Invalid data. Please check your input and try again."
            },
            isRecoverable = true,
            shouldRetry = false
        )
    
    /**
     * Database corruption or unrecoverable error
     */
    class Corruption(cause: Throwable? = null)
        : DataError(
            message = "Database corruption detected",
            cause = cause,
            userMessage = "Data integrity issue detected. The app may need to be reset.",
            isRecoverable = false,
            shouldRetry = false
        )
    
    /**
     * Unknown database error for cases that don't fit other categories
     */
    class Unknown(cause: Throwable? = null)
        : DataError(
            message = "Unknown database error",
            cause = cause,
            userMessage = "A database error occurred. Please try again.",
            isRecoverable = true,
            shouldRetry = false
        )
}

/**
 * System-level errors that are typically non-recoverable.
 */
sealed class SystemError(
    message: String,
    cause: Throwable? = null,
    userMessage: String = message,
    isRecoverable: Boolean = false,
    shouldRetry: Boolean = false
) : AppError(message, cause, isRecoverable, userMessage, shouldRetry) {
    
    /**
     * Out of memory
     */
    class OutOfMemory(cause: Throwable? = null)
        : SystemError(
            message = "Out of memory",
            cause = cause,
            userMessage = "The app is running low on memory. Please close other apps and try again.",
            isRecoverable = false
        )
    
    /**
     * Configuration error - app misconfigured
     */
    data class ConfigurationError(val config: String? = null, val errorCause: Throwable? = null)
        : SystemError(
            message = "Configuration error${if (config != null) ": $config" else ""}",
            cause = errorCause,
            userMessage = "App configuration error. Please contact support.",
            isRecoverable = false
        )
    
    /**
     * Unknown system error
     */
    class Unknown(cause: Throwable? = null)
        : SystemError(
            message = "Unknown system error",
            cause = cause,
            userMessage = "An unexpected error occurred. Please restart the app.",
            isRecoverable = false
        )
}

