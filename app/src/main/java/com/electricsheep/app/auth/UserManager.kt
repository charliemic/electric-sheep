package com.electricsheep.app.auth

import com.electricsheep.app.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the current user session and authentication state.
 * Provides reactive updates when user signs in/out.
 * 
 * This is the primary interface for accessing current user information
 * throughout the app.
 */
class UserManager(
    private val authProvider: AuthProvider
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    
    /**
     * StateFlow of the current user.
     * Emits null when no user is authenticated.
     */
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    /**
     * Check if a user is currently authenticated.
     */
    val isAuthenticated: Boolean
        get() = _currentUser.value != null
    
    /**
     * Initialise user manager by loading current user from auth provider.
     * Should be called on app start.
     */
    suspend fun initialise() {
        Logger.info("UserManager", "Initialising user manager")
        val user = authProvider.getCurrentUser()
        _currentUser.value = user
        if (user != null) {
            Logger.info("UserManager", "Current user loaded: ${user.id}")
        } else {
            Logger.debug("UserManager", "No authenticated user")
        }
    }
    
    /**
     * Sign in a user.
     * 
     * @param email User's email
     * @param password User's password
     * @return Result containing the authenticated user or error
     */
    suspend fun signIn(email: String, password: String): Result<User> {
        Logger.info("UserManager", "Signing in user: $email")
        return authProvider.signIn(email, password).also { result ->
            result.onSuccess { user ->
                _currentUser.value = user
                Logger.info("UserManager", "User signed in: ${user.id}")
            }.onFailure { error ->
                Logger.error("UserManager", "Sign in failed", error)
            }
        }
    }
    
    /**
     * Sign up a new user.
     * 
     * @param email User's email
     * @param password User's password
     * @param displayName Optional display name
     * @return Result containing the newly created user or error
     */
    suspend fun signUp(email: String, password: String, displayName: String? = null): Result<User> {
        Logger.info("UserManager", "Signing up user: $email")
        return authProvider.signUp(email, password, displayName).also { result ->
            result.onSuccess { user ->
                _currentUser.value = user
                Logger.info("UserManager", "User signed up: ${user.id}")
            }.onFailure { error ->
                Logger.error("UserManager", "Sign up failed", error)
            }
        }
    }
    
    /**
     * Sign out the current user.
     * 
     * @return Result indicating success or failure
     */
    suspend fun signOut(): Result<Unit> {
        Logger.info("UserManager", "Signing out user: ${_currentUser.value?.id}")
        return authProvider.signOut().also { result ->
            result.onSuccess {
                _currentUser.value = null
                Logger.info("UserManager", "User signed out")
            }.onFailure { error ->
                Logger.error("UserManager", "Sign out failed", error)
            }
        }
    }
    
    /**
     * Get the current user ID.
     * Throws exception if no user is authenticated.
     * 
     * @return Current user ID
     * @throws IllegalStateException if no user is authenticated
     */
    fun requireUserId(): String {
        val userId = _currentUser.value?.id
        require(userId != null) { "User must be authenticated" }
        return userId
    }
    
    /**
     * Get the current user ID, or null if not authenticated.
     * 
     * @return Current user ID or null
     */
    fun getUserIdOrNull(): String? {
        return _currentUser.value?.id
    }
}

