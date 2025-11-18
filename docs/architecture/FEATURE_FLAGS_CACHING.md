# Feature Flags Caching & Versioning

**Date**: 2024-11-18  
**Status**: Implemented

## Overview

Feature flags now support **TTL (Time To Live) caching** and **version-based cache invalidation** for optimal performance and reliability.

## TTL Caching

### Default TTL: 5 minutes (300 seconds)

**Rationale**:
- Feature flags don't change frequently
- 5 minutes balances freshness with performance
- Reduces network requests and improves app responsiveness
- Can be configured per provider instance

### Cache Storage

**Location**: SharedPreferences (`feature_flags_cache`)

**Stored Data**:
- Flag values (JSON-serialized)
- Flag versions (for cache invalidation)
- Last fetch timestamp

### Cache Behavior

1. **Cache Hit** (valid, within TTL):
   - Returns cached value immediately
   - No network request
   - Fast response time

2. **Cache Miss** (expired or missing):
   - Fetches from Supabase
   - Updates cache with new values and versions
   - Returns fresh value

3. **Version Mismatch**:
   - Even if cache is valid, if version changed, fetches fresh data
   - Ensures users get latest flag values when flags are updated

## Versioning

### Database Schema

**Column**: `version INTEGER DEFAULT 1 NOT NULL`

**Auto-increment**: Database trigger increments version when flag values change

**Trigger Logic**:
- Only increments if actual flag value changed (boolean_value, string_value, int_value, or enabled)
- Prevents unnecessary version bumps for metadata-only updates

### Version-Based Cache Invalidation

**How it works**:
1. Flag fetched from Supabase includes version number
2. Version stored in cache alongside flag value
3. On next request, compare cached version with current version
4. If versions differ, cache is invalidated and fresh data fetched

**Benefits**:
- Immediate cache invalidation when flags change
- No need to wait for TTL expiration
- Ensures users always get latest flag values

## Implementation

### FeatureFlagCache

**Location**: `app/src/main/java/com/electricsheep/app/config/FeatureFlagCache.kt`

**Key Methods**:
- `isCacheValid()`: Checks if cache is within TTL
- `getCachedValue(key, valueType)`: Retrieves cached flag value
- `getCachedVersion(key)`: Gets cached version number
- `cacheFlags(flags, versions)`: Stores flags with versions
- `needsRefresh(key, currentVersion)`: Checks if flag needs refresh

### SupabaseFeatureFlagProvider

**Cache Integration**:
- Checks cache before fetching from Supabase
- Uses cached values if valid and versions match
- Falls back to Supabase fetch if cache invalid or version changed
- Updates cache after successful fetch

**Flow**:
```
1. getBoolean(key) called
2. Check cache.isCacheValid()
3. If valid, get cached value
4. If invalid or missing, fetch from Supabase
5. Cache new values with versions
6. Return value
```

## Refresh Mechanisms

### Automatic Refresh

1. **On App Start**: Flags fetched and cached
2. **TTL Expiration**: Cache invalidated, next request triggers fetch
3. **Version Change**: Cache invalidated immediately, fresh fetch

### Manual Refresh

**Method**: `SupabaseFeatureFlagProvider.refresh()`

**Use Cases**:
- User login/logout (may change available flags)
- Periodic background refresh
- After flag updates in Supabase

## Performance

### Cache Hit Performance

- **Latency**: < 1ms (SharedPreferences read)
- **Network**: 0 requests
- **Battery**: Minimal impact

### Cache Miss Performance

- **Latency**: ~100-500ms (Supabase API call)
- **Network**: 1 request
- **Battery**: Normal network usage

### Typical Usage Pattern

- **App Start**: Cache miss (initial fetch)
- **Subsequent Requests**: Cache hits (within 5 minutes)
- **After TTL**: Cache miss (refresh)
- **After Flag Update**: Immediate cache miss (version change)

## Configuration

### TTL Customization

```kotlin
// Default: 5 minutes
val cache = FeatureFlagCache(context)

// Custom: 10 minutes
val cache = FeatureFlagCache(context, ttlSeconds = 600L)

// Custom: 1 minute (for testing)
val cache = FeatureFlagCache(context, ttlSeconds = 60L)
```

### Recommended TTL Values

- **Development**: 1-2 minutes (faster iteration)
- **Production**: 5 minutes (good balance)
- **High-frequency flags**: 1 minute (if flags change often)
- **Stable flags**: 10-15 minutes (if flags rarely change)

## Testing

### Test Scenarios

1. **Cache Hit**: Verify cached value returned
2. **Cache Expiration**: Verify fresh fetch after TTL
3. **Version Change**: Verify cache invalidation on version bump
4. **Offline**: Verify cache used when Supabase unavailable
5. **Flag Update**: Verify version increment triggers refresh

## Future Enhancements

### Potential Improvements

1. **Background Refresh**: Periodically refresh cache before TTL expires
2. **Smart TTL**: Adjust TTL based on flag change frequency
3. **Cache Warming**: Pre-fetch flags on app start
4. **Multi-level Cache**: Memory cache + SharedPreferences
5. **Cache Analytics**: Track cache hit/miss rates

## Monitoring

### Metrics to Track

- Cache hit rate
- Average cache age
- Version change frequency
- Cache refresh latency
- Network request count

### Logging

All cache operations are logged at DEBUG level:
- Cache hits/misses
- TTL expiration
- Version mismatches
- Cache updates

