package com.electricsheep.app.config

/**
 * Feature flag keys.
 * Centralised location for all feature flag identifiers.
 */
object FeatureFlag {
    /**
     * When enabled, disables all remote sync operations.
     * Useful for development and testing offline functionality.
     */
    const val OFFLINE_ONLY = "offline_only"
    
    // Add more feature flags here as needed
    // const val ENABLE_MOOD_ANALYTICS = "enable_mood_analytics"
    // const val ENABLE_EXPORT = "enable_export"
}

