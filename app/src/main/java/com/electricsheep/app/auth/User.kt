package com.electricsheep.app.auth

import kotlinx.serialization.Serializable

/**
 * Represents a user role in the system.
 */
enum class UserRole {
    /**
     * Regular user - default role for all users.
     * Can use Android app features and view public dashboard pages.
     */
    USER,
    
    /**
     * Admin user - elevated permissions.
     * Can access dashboard authoring, admin features, and manage users.
     */
    ADMIN
}

/**
 * Represents a user in the system.
 * 
 * @param id Unique user identifier (from authentication provider)
 * @param email User's email address
 * @param displayName User's display name (optional)
 * @param createdAt When the user account was created
 * @param role User's role (defaults to USER)
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val createdAt: Long? = null,
    val role: UserRole = UserRole.USER
) {
    /**
     * Validate that the user has required fields
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && email.isNotBlank()
    }
    
    /**
     * Check if user is an admin.
     */
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN
}

