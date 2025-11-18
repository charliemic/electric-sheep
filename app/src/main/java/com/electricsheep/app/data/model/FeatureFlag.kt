package com.electricsheep.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model for feature flags stored in Supabase.
 */
@Serializable
data class FeatureFlag(
    val id: String,
    val key: String,
    @SerialName("value_type")
    val valueType: String, // 'boolean', 'string', 'int'
    @SerialName("boolean_value")
    val booleanValue: Boolean? = null,
    @SerialName("string_value")
    val stringValue: String? = null,
    @SerialName("int_value")
    val intValue: Int? = null,
    val enabled: Boolean,
    val version: Int = 1, // Version number for cache invalidation
    @SerialName("segment_id")
    val segmentId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    val description: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    /**
     * Get the value based on value type.
     */
    fun getValue(): Any? {
        return when (valueType) {
            "boolean" -> booleanValue
            "string" -> stringValue
            "int" -> intValue
            else -> null
        }
    }
    
    /**
     * Check if this flag applies to the given user.
     * A flag applies if:
     * - It's enabled AND
     * - (user_id is null OR matches the user) AND
     * - (segment_id is null OR user is in segment - future implementation)
     */
    fun appliesToUser(userId: String?): Boolean {
        if (!enabled) return false
        
        // If flag has a specific user_id, it only applies to that user
        if (this.userId != null) {
            return this.userId == userId
        }
        
        // If flag has a segment_id, check segment membership (future)
        // For now, segment_id flags are treated as not applicable unless user matches
        if (segmentId != null) {
            // TODO: Check if user is in segment
            return false
        }
        
        // Global flag (no user_id, no segment_id) applies to everyone
        return true
    }
}

