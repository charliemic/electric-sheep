package com.electricsheep.app.config

import com.electricsheep.app.BuildConfig
import com.electricsheep.app.util.Logger

/**
 * Feature flag provider that reads from BuildConfig.
 * 
 * This serves as a fallback provider when Supabase is unavailable or flags haven't been fetched yet.
 * All flags defined in feature-flags/flags.yaml should have corresponding BuildConfig fields
 * to ensure fallback values are available.
 * 
 * BuildConfig fields are generated at build time by Gradle from build.gradle.kts.
 * The naming convention is: FLAG_KEY_UPPERCASE (e.g., "offline_only" -> "OFFLINE_ONLY_MODE")
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
                // For other boolean flags, try to read from BuildConfig
                // BuildConfig field name convention: FLAG_KEY_UPPERCASE
                val configKey = key.uppercase().replace("-", "_")
                try {
                    // Use reflection to access BuildConfig field (if it exists)
                    val field = BuildConfig::class.java.getField("${configKey}_MODE")
                    val value = field.get(null) as Boolean
                    Logger.debug("ConfigBasedFeatureFlagProvider", "$configKey = $value")
                    value
                } catch (e: Exception) {
                    Logger.debug("ConfigBasedFeatureFlagProvider", "BuildConfig field not found for $key, using default: $defaultValue")
                    defaultValue
                }
            }
        }
    }
    
    override fun getString(key: String, defaultValue: String): String {
        // Try to read from BuildConfig
        val configKey = key.uppercase().replace("-", "_")
        try {
            val field = BuildConfig::class.java.getField("${configKey}_VALUE")
            val value = field.get(null) as String
            Logger.debug("ConfigBasedFeatureFlagProvider", "$configKey = $value")
            return value
        } catch (e: Exception) {
            Logger.debug("ConfigBasedFeatureFlagProvider", "BuildConfig field not found for $key, using default: $defaultValue")
            return defaultValue
        }
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        // Try to read from BuildConfig
        val configKey = key.uppercase().replace("-", "_")
        try {
            val field = BuildConfig::class.java.getField("${configKey}_VALUE")
            val value = field.get(null) as Int
            Logger.debug("ConfigBasedFeatureFlagProvider", "$configKey = $value")
            return value
        } catch (e: Exception) {
            Logger.debug("ConfigBasedFeatureFlagProvider", "BuildConfig field not found for $key, using default: $defaultValue")
            return defaultValue
        }
    }
}

