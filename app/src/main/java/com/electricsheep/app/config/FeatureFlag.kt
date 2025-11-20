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
    
    /**
     * Shows a feature flag indicator icon on the landing page.
     * Used for testing feature flag functionality.
     */
    const val SHOW_FEATURE_FLAG_INDICATOR = "show_feature_flag_indicator"
    
    /**
     * Enables the Trivia Quiz app feature.
     * When enabled, shows the Trivia Quiz option on the landing page.
     */
    const val ENABLE_TRIVIA_APP = "enable_trivia_app"
    
    // Add more feature flags here as needed
    // const val ENABLE_MOOD_ANALYTICS = "enable_mood_analytics"
    // const val ENABLE_EXPORT = "enable_export"
}

