package com.electricsheep.app.config

import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.data.model.FeatureFlag
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.first

/**
 * Feature flag provider that fetches flags from Supabase.
 * 
 * Uses PostgREST to query the feature_flags table.
 * Supports TTL caching and version-based cache invalidation.
 */
class SupabaseFeatureFlagProvider(
    private val supabaseClient: SupabaseClient,
    private val userManager: UserManager,
    private val cache: FeatureFlagCache
) : RemoteFeatureFlagProvider() {
    
    override suspend fun fetchFlags(): Map<String, Any> {
        try {
            // Check cache first (if valid and versions match)
            if (cache.isCacheValid()) {
                Logger.debug("SupabaseFeatureFlagProvider", "Cache is valid, checking for updates")
                // Still fetch to check for version changes, but can use cache for immediate response
            }
            
            val userId = userManager.getUserIdOrNull()
            
            // Fetch flags that apply to the current user
            // This includes:
            // 1. Global flags (user_id IS NULL AND segment_id IS NULL)
            // 2. User-specific flags (user_id = current user)
            // 3. Segment-based flags (when segments are implemented)
            val flags = if (userId != null) {
                supabaseClient.from("feature_flags")
                    .select(columns = Columns.ALL) {
                        filter {
                            // Fetch global flags (user_id IS NULL) OR user-specific flags
                            // Note: For null checks, we use a different approach - fetch all and filter in app
                            // OR use RLS policies to handle this server-side
                            eq("enabled", true)
                        }
                    }
                    .decodeList<FeatureFlag>()
                    .filter { flag ->
                        // Filter in app: global flags (user_id IS NULL) OR user-specific flags
                        flag.userId == null || flag.userId == userId
                    }
            } else {
                // No user logged in, only fetch global flags
                supabaseClient.from("feature_flags")
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("enabled", true)
                        }
                    }
                    .decodeList<FeatureFlag>()
                    .filter { flag ->
                        // Filter in app: only global flags (user_id IS NULL AND segment_id IS NULL)
                        flag.userId == null && flag.segmentId == null
                    }
            }
            
            // Convert flags to map and cache versions
            val flagMap = flags.associate { flag ->
                val value: Any = when (flag.valueType) {
                    "boolean" -> flag.booleanValue ?: false
                    "string" -> flag.stringValue ?: ""
                    "int" -> flag.intValue ?: 0
                    else -> false
                }
                flag.key to value
            }
            
            // Cache flags with versions
            val versions = flags.associate { it.key to it.version }
            cache.cacheFlags(flagMap, versions)
            
            Logger.info("SupabaseFeatureFlagProvider", "Fetched ${flagMap.size} feature flags from Supabase")
            return flagMap
            
        } catch (e: Exception) {
            Logger.error("SupabaseFeatureFlagProvider", "Failed to fetch feature flags from Supabase", e)
            // Return empty map on error, will use defaults
            return emptyMap()
        }
    }
    
    /**
     * Initialise flags by fetching from Supabase.
     * Should be called on app start.
     */
    override suspend fun initialise() {
        super.initialise()
    }
    
    /**
     * Refresh flags from Supabase.
     * Can be called periodically or when user changes.
     */
    suspend fun refresh() {
        Logger.debug("SupabaseFeatureFlagProvider", "Refreshing feature flags")
        cachedFlags = fetchFlags()
        flagsInitialised = true
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        // Try cache first if valid
        if (cache.isCacheValid()) {
            val cachedValue = cache.getCachedValue(key, "boolean")
            if (cachedValue != null) {
                Logger.debug("SupabaseFeatureFlagProvider", "Using cached value for $key: $cachedValue")
                return cachedValue as Boolean
            }
        }
        
        // Fall back to parent implementation
        return super.getBoolean(key, defaultValue)
    }
    
    override fun getString(key: String, defaultValue: String): String {
        if (cache.isCacheValid()) {
            val cachedValue = cache.getCachedValue(key, "string")
            if (cachedValue != null) {
                Logger.debug("SupabaseFeatureFlagProvider", "Using cached value for $key: $cachedValue")
                return cachedValue as String
            }
        }
        return super.getString(key, defaultValue)
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        if (cache.isCacheValid()) {
            val cachedValue = cache.getCachedValue(key, "int")
            if (cachedValue != null) {
                Logger.debug("SupabaseFeatureFlagProvider", "Using cached value for $key: $cachedValue")
                return cachedValue as Int
            }
        }
        return super.getInt(key, defaultValue)
    }
}

