package com.electricsheep.app.config

import com.electricsheep.app.util.Logger

/**
 * Composite feature flag provider that uses multiple sources with fallback.
 * 
 * Priority order:
 * 1. Primary provider (e.g., Supabase) - remote source
 * 2. Fallback provider (e.g., Config) - local defaults
 * 
 * This ensures features are agnostic to the source - they always get a value,
 * either from the primary source or the fallback.
 */
class CompositeFeatureFlagProvider(
    val primaryProvider: FeatureFlagProvider,
    private val fallbackProvider: FeatureFlagProvider
) : FeatureFlagProvider {
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        // Check if primary provider is a remote provider and has the flag
        if (primaryProvider is RemoteFeatureFlagProvider) {
            val remoteProvider = primaryProvider as RemoteFeatureFlagProvider
            // If flags are initialised and flag exists in cache, use primary value
            if (remoteProvider.isReady() && remoteProvider.getCachedFlags().containsKey(key)) {
                try {
                    val primaryValue = primaryProvider.getBoolean(key, defaultValue)
                    Logger.debug("CompositeFeatureFlagProvider", "Using primary provider value for $key: $primaryValue")
                    return primaryValue
                } catch (e: Exception) {
                    Logger.warn("CompositeFeatureFlagProvider", "Primary provider failed for $key, using fallback", e)
                    return fallbackProvider.getBoolean(key, defaultValue)
                }
            } else {
                // Flags not initialised or flag not in cache, use fallback
                Logger.debug("CompositeFeatureFlagProvider", "Primary provider not ready for $key, using fallback")
                return fallbackProvider.getBoolean(key, defaultValue)
            }
        } else {
            // Non-remote provider, try primary first, fallback on error
            return try {
                primaryProvider.getBoolean(key, defaultValue)
            } catch (e: Exception) {
                Logger.warn("CompositeFeatureFlagProvider", "Primary provider failed for $key, using fallback", e)
                fallbackProvider.getBoolean(key, defaultValue)
            }
        }
    }
    
    override fun getString(key: String, defaultValue: String): String {
        // Check if primary provider is a remote provider and has the flag
        if (primaryProvider is RemoteFeatureFlagProvider) {
            val remoteProvider = primaryProvider as RemoteFeatureFlagProvider
            if (remoteProvider.isReady() && remoteProvider.getCachedFlags().containsKey(key)) {
                try {
                    val primaryValue = primaryProvider.getString(key, defaultValue)
                    Logger.debug("CompositeFeatureFlagProvider", "Using primary provider value for $key: $primaryValue")
                    return primaryValue
                } catch (e: Exception) {
                    Logger.warn("CompositeFeatureFlagProvider", "Primary provider failed for $key, using fallback", e)
                    return fallbackProvider.getString(key, defaultValue)
                }
            } else {
                Logger.debug("CompositeFeatureFlagProvider", "Primary provider not ready for $key, using fallback")
                return fallbackProvider.getString(key, defaultValue)
            }
        } else {
            return try {
                primaryProvider.getString(key, defaultValue)
            } catch (e: Exception) {
                Logger.warn("CompositeFeatureFlagProvider", "Primary provider failed for $key, using fallback", e)
                fallbackProvider.getString(key, defaultValue)
            }
        }
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        // Check if primary provider is a remote provider and has the flag
        if (primaryProvider is RemoteFeatureFlagProvider) {
            val remoteProvider = primaryProvider as RemoteFeatureFlagProvider
            if (remoteProvider.isReady() && remoteProvider.getCachedFlags().containsKey(key)) {
                try {
                    val primaryValue = primaryProvider.getInt(key, defaultValue)
                    Logger.debug("CompositeFeatureFlagProvider", "Using primary provider value for $key: $primaryValue")
                    return primaryValue
                } catch (e: Exception) {
                    Logger.warn("CompositeFeatureFlagProvider", "Primary provider failed for $key, using fallback", e)
                    return fallbackProvider.getInt(key, defaultValue)
                }
            } else {
                Logger.debug("CompositeFeatureFlagProvider", "Primary provider not ready for $key, using fallback")
                return fallbackProvider.getInt(key, defaultValue)
            }
        } else {
            return try {
                primaryProvider.getInt(key, defaultValue)
            } catch (e: Exception) {
                Logger.warn("CompositeFeatureFlagProvider", "Primary provider failed for $key, using fallback", e)
                fallbackProvider.getInt(key, defaultValue)
            }
        }
    }
}

