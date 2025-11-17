package com.electricsheep.app.config

import com.electricsheep.app.BuildConfig
import com.electricsheep.app.util.Logger

/**
 * Feature flag provider that reads from BuildConfig.
 * Suitable for development and build-time configuration.
 * 
 * For production, consider implementing RemoteFeatureFlagProvider
 * that fetches flags from a remote service.
 */
class ConfigBasedFeatureFlagProvider : FeatureFlagProvider {
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return when (key) {
            FeatureFlag.OFFLINE_ONLY -> {
                // Read from BuildConfig.OFFLINE_ONLY_MODE
                // This is generated at build time by Gradle, so it always exists
                val value = BuildConfig.OFFLINE_ONLY_MODE
                Logger.debug("ConfigBasedFeatureFlagProvider", "OFFLINE_ONLY_MODE = $value")
                value
            }
            else -> {
                Logger.debug("ConfigBasedFeatureFlagProvider", "Unknown feature flag: $key, using default: $defaultValue")
                defaultValue
            }
        }
    }
    
    override fun getString(key: String, defaultValue: String): String {
        Logger.debug("ConfigBasedFeatureFlagProvider", "String feature flags not yet implemented, using default for: $key")
        return defaultValue
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        Logger.debug("ConfigBasedFeatureFlagProvider", "Int feature flags not yet implemented, using default for: $key")
        return defaultValue
    }
}

