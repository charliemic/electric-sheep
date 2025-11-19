# Supabase Feature Flags Architecture

**Date**: 2024-12-19  
**Status**: Design Document

## Overview

This document defines the architecture for using Supabase as a feature flag management tool for the Electric Sheep Android application. The architecture leverages Supabase's native capabilities through the official Kotlin SDK, ensuring we use supported features before considering custom implementations.

## Core Principles

1. **SDK-First Approach**: Always use the Supabase Kotlin SDK (`io.github.jan-tennert.supabase`) for all interactions
2. **Check SDK Capabilities**: Before implementing custom solutions, verify if functionality exists in the SDK
3. **Environment Separation**: Staging and production use separate Supabase projects
4. **Graceful Degradation**: Fallback to BuildConfig values when Supabase is unavailable
5. **Documentation Reference**: Supabase official documentation is the primary source of truth

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Electric Sheep Android App                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           FeatureFlagManager (Public API)                 │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            │                                     │
│                            ▼                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      CompositeFeatureFlagProvider                         │  │
│  │  ┌────────────────────┐  ┌──────────────────────────┐   │  │
│  │  │ Primary:           │  │ Fallback:                │   │  │
│  │  │ SupabaseProvider   │  │ ConfigBasedProvider      │   │  │
│  │  └────────────────────┘  └──────────────────────────┘   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            │                                     │
│                            ▼                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      SupabaseFeatureFlagProvider                          │  │
│  │  • PostgREST queries (via SDK)                            │  │
│  │  • Realtime subscriptions (via SDK)                       │  │
│  │  • FeatureFlagCache (TTL + version-based)                │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            │                                     │
│                            ▼                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      Supabase Kotlin SDK                                  │  │
│  │  • PostgREST module (io.github.jan-tennert.supabase:    │  │
│  │    postgrest-kt)                                          │  │
│  │  • Realtime module (io.github.jan-tennert.supabase:      │  │
│  │    realtime-kt)                                           │  │
│  │  • Auth module (io.github.jan-tennert.supabase:gotrue-kt)│  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Supabase Cloud                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────┐      ┌──────────────────────┐        │
│  │  Staging Project     │      │  Production Project  │        │
│  │  (rmcnvcqnowgs...)   │      │  (mvuzvoyvijsdq...)  │        │
│  └──────────────────────┘      └──────────────────────┘        │
│           │                              │                       │
│           ▼                              ▼                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              PostgreSQL Database                          │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │         feature_flags table                         │ │  │
│  │  │  • id (UUID)                                        │ │  │
│  │  │  • key (TEXT, UNIQUE)                               │ │  │
│  │  │  • value_type (boolean|string|int)                  │ │  │
│  │  │  • boolean_value, string_value, int_value           │ │  │
│  │  │  • enabled (BOOLEAN)                                │ │  │
│  │  │  • user_id (UUID, nullable)                         │ │  │
│  │  │  • segment_id (UUID, nullable)                       │ │  │
│  │  │  • version (INTEGER)                                 │ │  │
│  │  │  • description, timestamps                           │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │         Row-Level Security (RLS)                    │ │  │
│  │  │  • Users can read applicable flags                  │ │  │
│  │  │  • Service role can manage all flags                │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │         PostgREST API                                │ │  │
│  │  │  • Auto-generated REST API                          │ │  │
│  │  │  • Accessed via SDK postgrest-kt module             │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │         Realtime Subscriptions                      │ │  │
│  │  │  • PostgreSQL change notifications                  │ │  │
│  │  │  • Accessed via SDK realtime-kt module              │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Environment Configuration

### Staging Environment

**Purpose**: Testing and development

**Configuration**:
- **Supabase Project URL**: `https://rmcnvcqnowgsvvbmfssi.supabase.co`
- **Anon Key**: Stored in `local.properties` and GitHub Secrets
- **Usage**: Debug builds with `USE_STAGING_SUPABASE=true`

**Access**:
- Local development: `local.properties` → `supabase.staging.url` and `supabase.staging.anon.key`
- CI/CD: GitHub Secrets → `SUPABASE_STAGING_URL` and `SUPABASE_STAGING_ANON_KEY`

### Production Environment

**Purpose**: Live application

**Configuration**:
- **Supabase Project URL**: `https://mvuzvoyvijsdqsfqjgpd.supabase.co`
- **Anon Key**: Stored in `local.properties` and GitHub Secrets
- **Usage**: Release builds (always uses production)

**Access**:
- Local development: `local.properties` → `supabase.url` and `supabase.anon.key`
- CI/CD: GitHub Secrets → `SUPABASE_URL` and `SUPABASE_ANON_KEY`

### Configuration Flow

```
┌─────────────────┐
│ local.properties│  (Local development)
│ or GitHub Secrets│  (CI/CD)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  build.gradle.kts│  (Reads properties)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   BuildConfig   │  (Exposed to app)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  DataModule      │  (Creates Supabase client)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Supabase Client  │  (Initialized with correct URL/key)
└─────────────────┘
```

## Supabase SDK Usage

### SDK Modules Used

1. **PostgREST** (`postgrest-kt`)
   - **Purpose**: Query `feature_flags` table
   - **Documentation**: [Supabase PostgREST Docs](https://supabase.com/docs/reference/kotlin/postgrest)
   - **Usage**: Direct table queries, filtering, RLS enforcement

2. **Realtime** (`realtime-kt`)
   - **Purpose**: Subscribe to flag changes
   - **Documentation**: [Supabase Realtime Docs](https://supabase.com/docs/reference/kotlin/realtime)
   - **Usage**: Listen for INSERT/UPDATE/DELETE on `feature_flags` table

3. **Auth** (`gotrue-kt`)
   - **Purpose**: User authentication for RLS
   - **Documentation**: [Supabase Auth Docs](https://supabase.com/docs/reference/kotlin/auth)
   - **Usage**: User context for user-specific flags

### SDK Initialization

```kotlin
// From DataModule.kt
createSupabaseClient(
    supabaseUrl = finalUrl,  // From BuildConfig
    supabaseKey = finalKey   // From BuildConfig
) {
    install(Postgrest)      // PostgREST module
    install(Realtime)       // Realtime module
    install(Auth) {         // Auth module
        flowType = FlowType.PKCE
        scheme = "com.electricsheep.app"
        host = "auth-callback"
    }
}
```

### PostgREST Queries (via SDK)

**Current Implementation**:
```kotlin
// Fetch flags from Supabase
val flags = supabaseClient.from("feature_flags")
    .select(columns = Columns.ALL) {
        filter {
            eq("enabled", true)
        }
    }
    .decodeList<FeatureFlag>()
```

**SDK Capabilities** (from [Supabase PostgREST Docs](https://supabase.com/docs/reference/kotlin/postgrest)):
- ✅ Filtering (`eq`, `neq`, `gt`, `gte`, `lt`, `lte`, `like`, `ilike`, `is`, `in`, etc.)
- ✅ Ordering (`order`)
- ✅ Pagination (`range`, `limit`, `offset`)
- ✅ Column selection (`select`)
- ✅ Upsert operations (`upsert`)
- ✅ RLS enforcement (automatic)

**Before Custom Implementation**: Check if the SDK supports the query pattern you need.

### Realtime Subscriptions (via SDK)

**Potential Implementation** (not yet implemented):
```kotlin
// Subscribe to flag changes
supabaseClient.from("feature_flags")
    .subscribe(RealtimeAction.ALL) { 
        // Refresh flags when changed
        refresh()
    }
```

**SDK Capabilities** (from [Supabase Realtime Docs](https://supabase.com/docs/reference/kotlin/realtime)):
- ✅ Table subscriptions (`from("table").subscribe()`)
- ✅ Filter subscriptions (`filter()`)
- ✅ Event types (`INSERT`, `UPDATE`, `DELETE`, `ALL`)
- ✅ Channel management

**Before Custom Implementation**: Check if the SDK supports the subscription pattern you need.

## Database Schema

### Feature Flags Table

```sql
CREATE TABLE public.feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key TEXT UNIQUE NOT NULL,
    value_type TEXT NOT NULL CHECK (value_type IN ('boolean', 'string', 'int')),
    boolean_value BOOLEAN,
    string_value TEXT,
    int_value INTEGER,
    enabled BOOLEAN DEFAULT true NOT NULL,
    segment_id UUID,  -- Future: user segmentation
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    version INTEGER DEFAULT 1 NOT NULL,  -- For cache invalidation
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Constraints ensure type safety
    CONSTRAINT check_boolean_value CHECK (
        value_type != 'boolean' OR boolean_value IS NOT NULL
    ),
    CONSTRAINT check_string_value CHECK (
        value_type != 'string' OR string_value IS NOT NULL
    ),
    CONSTRAINT check_int_value CHECK (
        value_type != 'int' OR int_value IS NOT NULL
    ),
    CONSTRAINT check_single_value CHECK (
        (value_type = 'boolean' AND string_value IS NULL AND int_value IS NULL) OR
        (value_type = 'string' AND boolean_value IS NULL AND int_value IS NULL) OR
        (value_type = 'int' AND boolean_value IS NULL AND string_value IS NULL)
    )
);
```

### Indexes

```sql
-- Fast lookups by key
CREATE INDEX idx_feature_flags_key ON public.feature_flags(key);

-- User-specific flags
CREATE INDEX idx_feature_flags_user_id ON public.feature_flags(user_id);

-- Enabled flags filter
CREATE INDEX idx_feature_flags_enabled ON public.feature_flags(enabled);

-- Composite index for common query: enabled flags by key and user
CREATE INDEX idx_feature_flags_key_user_enabled 
    ON public.feature_flags(key, user_id, enabled) 
    WHERE enabled = true;
```

### Row-Level Security (RLS)

**Policy 1: Users can read applicable flags**
```sql
CREATE POLICY "Users can read applicable feature flags"
    ON public.feature_flags
    FOR SELECT
    USING (
        -- Global flags (no user, no segment)
        (user_id IS NULL AND segment_id IS NULL) OR
        -- User-specific flags
        (user_id = auth.uid())
    );
```

**Policy 2: Service role can manage all flags**
```sql
CREATE POLICY "Service role can manage feature flags"
    ON public.feature_flags
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');
```

**RLS Enforcement**: Automatically enforced by Supabase PostgREST API. No custom code needed.

## Application Layer

### Component Hierarchy

```
FeatureFlagManager
  └── CompositeFeatureFlagProvider
        ├── Primary: SupabaseFeatureFlagProvider
        │     ├── SupabaseClient (PostgREST + Realtime)
        │     ├── FeatureFlagCache (TTL + version-based)
        │     └── UserManager (for user-specific flags)
        └── Fallback: ConfigBasedFeatureFlagProvider
              └── BuildConfig (static values)
```

### Data Flow

1. **App Startup**:
   ```
   ElectricSheepApplication.onCreate()
   → DataModule.createFeatureFlagManager()
   → SupabaseFeatureFlagProvider.initialise()
   → fetchFlags() (async)
   → Cache flags in FeatureFlagCache
   ```

2. **Flag Access**:
   ```
   FeatureFlagManager.isEnabled("flag_key", defaultValue)
   → CompositeFeatureFlagProvider.getBoolean()
   → Try SupabaseFeatureFlagProvider (if available)
   → Fallback to ConfigBasedFeatureFlagProvider
   ```

3. **Cache Strategy**:
   ```
   getBoolean("flag_key")
   → Check FeatureFlagCache.isCacheValid() (TTL check)
   → If valid: return cached value
   → If invalid: fetchFlags() from Supabase
   → Check version for cache invalidation
   → Update cache
   ```

### Caching Strategy

**TTL (Time To Live)**: 5 minutes (300 seconds)

**Version-Based Invalidation**: Flags include a `version` field that increments on value changes. Cache is invalidated immediately when version changes, even if TTL hasn't expired.

**Storage**: SharedPreferences (`feature_flags_cache`)

**Benefits**:
- Reduces network requests
- Improves app responsiveness
- Ensures fresh data when flags change (version check)

## SDK Capability Checklist

Before implementing any custom solution, verify if the Supabase SDK supports it:

### ✅ Currently Using

- [x] PostgREST queries (`from("table").select()`)
- [x] Filtering (`filter { eq("enabled", true) }`)
- [x] Type-safe decoding (`decodeList<FeatureFlag>()`)
- [x] RLS enforcement (automatic)
- [x] User authentication (`auth.uid()`)

### 🔄 Available but Not Yet Implemented

- [ ] Realtime subscriptions (`from("table").subscribe()`)
- [ ] Upsert operations (`upsert()`)
- [ ] Batch operations
- [ ] Advanced filtering (complex queries)

### ❓ Before Custom Implementation

If you need functionality not listed above:

1. **Check SDK Documentation**: [Supabase Kotlin SDK Docs](https://supabase.com/docs/reference/kotlin)
2. **Check PostgREST Docs**: [Supabase PostgREST Docs](https://supabase.com/docs/reference/kotlin/postgrest)
3. **Check Realtime Docs**: [Supabase Realtime Docs](https://supabase.com/docs/reference/kotlin/realtime)
4. **Check GitHub Issues**: [Supabase Kotlin SDK Issues](https://github.com/supabase-community/supabase-kt/issues)
5. **Ask**: If functionality isn't available, discuss before implementing custom solution

## Deployment & CI/CD

### Flag Definition

Flags are defined in `feature-flags/flags.yaml`:

```yaml
flags:
  - key: offline_only
    value_type: boolean
    boolean_value: false
    enabled: true
    description: "When enabled, disables all remote sync operations"
    segment_id: null
    user_id: null
```

### Deployment Process

1. **Local Development**:
   - Edit `feature-flags/flags.yaml`
   - Run sync script: `./scripts/sync-feature-flags.sh feature-flags/flags.yaml`
   - Uses `SUPABASE_DB_URL` or `SUPABASE_URL` + `SUPABASE_SERVICE_ROLE_KEY`

2. **CI/CD** (GitHub Actions):
   - Push to `develop` → Deploy to staging
   - Push to `main` → Deploy to production
   - Uses GitHub Secrets for credentials

### Service Role Key

**Purpose**: Bypass RLS for CI/CD deployments

**Storage**:
- Local: Environment variable `SUPABASE_SERVICE_ROLE_KEY`
- CI/CD: GitHub Secret `SUPABASE_SERVICE_ROLE_KEY` (staging/production)

**Usage**: Only for deployments, never in the Android app (anon key only)

## Future Enhancements

### Real-time Updates

**Implementation**: Use Supabase Realtime SDK

```kotlin
// In SupabaseFeatureFlagProvider
supabaseClient.from("feature_flags")
    .subscribe(RealtimeAction.ALL) { 
        action, flag ->
            when (action) {
                RealtimeAction.INSERT, RealtimeAction.UPDATE, RealtimeAction.DELETE -> {
                    refresh() // Refresh flags cache
                }
            }
        }
    }
```

**SDK Support**: ✅ Available in `realtime-kt` module

### User Segmentation

**Schema**: Already supports `segment_id` column

**Implementation Steps**:
1. Create `segments` table
2. Create `user_segments` junction table
3. Update RLS policy to check segment membership
4. Update `SupabaseFeatureFlagProvider` to filter by segment

**SDK Support**: ✅ PostgREST queries support joins and complex filters

### A/B Testing / Percentage Rollouts

**Consideration**: This may require custom logic or Edge Functions

**SDK Check**: 
- PostgREST: ✅ Supports complex queries
- Edge Functions: ✅ Available via `functions-kt` module

**Before Implementation**: Evaluate if Edge Functions are needed or if PostgREST queries are sufficient

## Security Considerations

1. **RLS Policies**: Enforce access control at database level
2. **Anon Key**: Only used in Android app (read-only for users)
3. **Service Role Key**: Only used in CI/CD (never in app)
4. **User Context**: RLS automatically filters flags based on `auth.uid()`
5. **HTTPS**: All Supabase communication over HTTPS (enforced by SDK)

## Monitoring & Observability

### Logging

- Flag fetches (success/failure)
- Cache hits/misses
- Version changes
- RLS policy violations (if any)

### Metrics to Track

- Flag fetch latency
- Cache hit rate
- Flag usage frequency
- Error rates

## References

### Supabase Documentation

- [Supabase Kotlin SDK](https://supabase.com/docs/reference/kotlin)
- [PostgREST API](https://supabase.com/docs/reference/kotlin/postgrest)
- [Realtime Subscriptions](https://supabase.com/docs/reference/kotlin/realtime)
- [Row-Level Security](https://supabase.com/docs/guides/auth/row-level-security)
- [Edge Functions](https://supabase.com/docs/guides/functions)

### Project Documentation

- [Feature Flags Implementation](./FEATURE_FLAGS_IMPLEMENTATION.md)
- [Feature Flags Caching](./FEATURE_FLAGS_CACHING.md)
- [Feature Flags Fallback](./FEATURE_FLAGS_FALLBACK.md)

## Decision Log

### Why Supabase SDK?

- ✅ Official support and maintenance
- ✅ Type-safe Kotlin API
- ✅ Automatic RLS enforcement
- ✅ Built-in error handling
- ✅ Active community and documentation

### Why Not Custom REST Client?

- ❌ Would need to implement RLS logic manually
- ❌ Would need to handle auth tokens manually
- ❌ Would miss SDK optimizations and features
- ❌ Would require more maintenance

### Why PostgREST Instead of Direct SQL?

- ✅ Auto-generated REST API
- ✅ RLS enforcement built-in
- ✅ Type-safe SDK integration
- ✅ No SQL injection risks
- ✅ Consistent with Supabase best practices

