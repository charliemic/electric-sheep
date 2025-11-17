package com.electricsheep.app.config

import com.electricsheep.app.util.Logger

/**
 * Abstract base for remote feature flag providers.
 * 
 * Future implementations should:
 * - Fetch flags from remote service (e.g., Firebase Remote Config, Supabase, custom API)
 * - Cache flags locally for offline access
 * - Support real-time updates
 * - Handle network failures gracefully
 * 
 * Example implementation structure:
 * ```kotlin
 * class SupabaseFeatureFlagProvider(
 *     private val supabaseClient: SupabaseClient,
 *     private val localCache: FeatureFlagCache
 * ) : RemoteFeatureFlagProvider() {
 *     override suspend fun fetchFlags(): Map<String, Any> {
 *         // Fetch from Supabase
 *     }
 * }
 * ```
 */
abstract class RemoteFeatureFlagProvider : FeatureFlagProvider {
    
    /**
     * Cache of feature flags fetched from remote.
     * Should be updated periodically or via real-time subscriptions.
     */
    protected var cachedFlags: Map<String, Any> = emptyMap()
    
    /**
     * Whether flags have been fetched at least once.
     */
    protected var flagsInitialised: Boolean = false
    
    /**
     * Fetch flags from remote service.
     * Should be called on app start and periodically.
     * 
     * @return Map of flag keys to values
     */
    abstract suspend fun fetchFlags(): Map<String, Any>
    
    /**
     * Initialise flags by fetching from remote.
     * Falls back to cached values if fetch fails.
     */
    suspend fun initialise() {
        try {
            Logger.info("RemoteFeatureFlagProvider", "Fetching feature flags from remote")
            cachedFlags = fetchFlags()
            flagsInitialised = true
            Logger.info("RemoteFeatureFlagProvider", "Feature flags fetched: ${cachedFlags.size} flags")
        } catch (e: Exception) {
            Logger.warn("RemoteFeatureFlagProvider", "Failed to fetch feature flags, using cached values", e)
            // Use existing cached flags or empty map
        }
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (!flagsInitialised) {
            Logger.debug("RemoteFeatureFlagProvider", "Flags not initialised, using default for: $key")
            return defaultValue
        }
        
        return when (val value = cachedFlags[key]) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: defaultValue
            else -> {
                Logger.debug("RemoteFeatureFlagProvider", "Flag $key not found or wrong type, using default: $defaultValue")
                defaultValue
            }
        }
    }
    
    override fun getString(key: String, defaultValue: String): String {
        if (!flagsInitialised) {
            return defaultValue
        }
        
        return when (val value = cachedFlags[key]) {
            is String -> value
            else -> {
                Logger.debug("RemoteFeatureFlagProvider", "Flag $key not found or wrong type, using default: $defaultValue")
                defaultValue
            }
        }
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        if (!flagsInitialised) {
            return defaultValue
        }
        
        return when (val value = cachedFlags[key]) {
            is Int -> value
            is String -> value.toIntOrNull() ?: defaultValue
            else -> {
                Logger.debug("RemoteFeatureFlagProvider", "Flag $key not found or wrong type, using default: $defaultValue")
                defaultValue
            }
        }
    }
}

