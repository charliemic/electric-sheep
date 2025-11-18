package com.electricsheep.app.config

import android.content.Context
import android.content.SharedPreferences
import com.electricsheep.app.util.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Cache for feature flags with TTL (Time To Live) support.
 * 
 * Stores flags in SharedPreferences with:
 * - Flag values
 * - Last fetch timestamp
 * - Flag versions (for cache invalidation)
 * 
 * TTL: 5 minutes (300 seconds) - flags are refreshed if cache is older than TTL
 */
@Serializable
private data class CachedFlags(
    val flags: Map<String, String>, // Key -> JSON-encoded value
    val versions: Map<String, Int>, // Key -> version number
    val fetchedAt: Long // Timestamp in milliseconds
)

class FeatureFlagCache(
    private val context: Context,
    private val ttlSeconds: Long = 300L // 5 minutes default TTL
) {
    companion object {
        private const val PREFS_NAME = "feature_flags_cache"
        private const val KEY_CACHED_FLAGS = "cached_flags"
        private const val KEY_LAST_FETCH = "last_fetch"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Check if cache is valid (not expired).
     */
    fun isCacheValid(): Boolean {
        val lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L)
        if (lastFetch == 0L) return false
        
        val now = System.currentTimeMillis()
        val ageSeconds = (now - lastFetch) / 1000
        val isValid = ageSeconds < ttlSeconds
        
        Logger.debug("FeatureFlagCache", "Cache age: ${ageSeconds}s, TTL: ${ttlSeconds}s, Valid: $isValid")
        return isValid
    }
    
    /**
     * Get cached flag value.
     * Returns null if flag not in cache or cache expired.
     */
    fun getCachedValue(key: String, valueType: String): Any? {
        if (!isCacheValid()) {
            Logger.debug("FeatureFlagCache", "Cache expired, not returning cached value for $key")
            return null
        }
        
        val cachedJson = prefs.getString("flag_$key", null) ?: return null
        val cachedVersion = prefs.getInt("flag_${key}_version", -1)
        
        return try {
            when (valueType) {
                "boolean" -> json.decodeFromString<Boolean>(cachedJson)
                "string" -> json.decodeFromString<String>(cachedJson)
                "int" -> json.decodeFromString<Int>(cachedJson)
                else -> null
            }
        } catch (e: Exception) {
            Logger.warn("FeatureFlagCache", "Failed to deserialize cached value for $key", e)
            null
        }
    }
    
    /**
     * Get cached version for a flag.
     */
    fun getCachedVersion(key: String): Int? {
        if (!isCacheValid()) return null
        val version = prefs.getInt("flag_${key}_version", -1)
        return if (version >= 0) version else null
    }
    
    /**
     * Store flags in cache.
     */
    fun cacheFlags(flags: Map<String, Any>, versions: Map<String, Int>) {
        val editor = prefs.edit()
        val now = System.currentTimeMillis()
        
        // Store each flag
        flags.forEach { (key, value) ->
            try {
                val jsonValue = when (value) {
                    is Boolean -> json.encodeToString(value)
                    is String -> json.encodeToString(value)
                    is Int -> json.encodeToString(value)
                    else -> null
                }
                
                if (jsonValue != null) {
                    editor.putString("flag_$key", jsonValue)
                    editor.putInt("flag_${key}_version", versions[key] ?: 1)
                }
            } catch (e: Exception) {
                Logger.warn("FeatureFlagCache", "Failed to cache flag $key", e)
            }
        }
        
        // Store last fetch timestamp
        editor.putLong(KEY_LAST_FETCH, now)
        editor.apply()
        
        Logger.info("FeatureFlagCache", "Cached ${flags.size} flags at $now")
    }
    
    /**
     * Check if a flag needs refresh (version changed or not in cache).
     */
    fun needsRefresh(key: String, currentVersion: Int): Boolean {
        val cachedVersion = getCachedVersion(key)
        return when {
            cachedVersion == null -> {
                Logger.debug("FeatureFlagCache", "Flag $key not in cache, needs refresh")
                true
            }
            cachedVersion < currentVersion -> {
                Logger.debug("FeatureFlagCache", "Flag $key version changed ($cachedVersion -> $currentVersion), needs refresh")
                true
            }
            !isCacheValid() -> {
                Logger.debug("FeatureFlagCache", "Cache expired, flag $key needs refresh")
                true
            }
            else -> false
        }
    }
    
    /**
     * Clear all cached flags.
     */
    fun clear() {
        prefs.edit().clear().apply()
        Logger.info("FeatureFlagCache", "Cache cleared")
    }
    
    /**
     * Get cache age in seconds.
     */
    fun getCacheAgeSeconds(): Long {
        val lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L)
        if (lastFetch == 0L) return Long.MAX_VALUE
        return (System.currentTimeMillis() - lastFetch) / 1000
    }
}

