package com.electricsheep.app.config

import com.electricsheep.app.util.Logger

/**
 * Central manager for feature flags.
 * Provides a single point of access for feature flags,
 * abstracting the underlying provider implementation.
 * 
 * Supports both local (config-based) and remote providers.
 * Can be extended to support multiple providers with priority/fallback.
 */
class FeatureFlagManager(
    private val provider: FeatureFlagProvider
) {
    /**
     * Check if offline-only mode is enabled.
     * When enabled, all remote sync operations should be disabled.
     */
    fun isOfflineOnly(): Boolean {
        val isOffline = provider.isEnabled(FeatureFlag.OFFLINE_ONLY, defaultValue = false)
        if (isOffline) {
            Logger.debug("FeatureFlagManager", "Offline-only mode is enabled")
        }
        return isOffline
    }
    
    /**
     * Get boolean feature flag value.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return provider.getBoolean(key, defaultValue)
    }
    
    /**
     * Get string feature flag value.
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return provider.getString(key, defaultValue)
    }
    
    /**
     * Get integer feature flag value.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return provider.getInt(key, defaultValue)
    }
    
    /**
     * Check if a feature is enabled (convenience method).
     */
    fun isEnabled(key: String, defaultValue: Boolean = false): Boolean {
        return provider.isEnabled(key, defaultValue)
    }
}

