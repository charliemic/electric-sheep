package com.electricsheep.app.auth

import kotlinx.serialization.Serializable

/**
 * Represents a user in the system.
 * 
 * @param id Unique user identifier (from authentication provider)
 * @param email User's email address
 * @param displayName User's display name (optional)
 * @param createdAt When the user account was created
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val createdAt: Long? = null
) {
    /**
     * Validate that the user has required fields
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && email.isNotBlank()
    }
}

