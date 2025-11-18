# Feature Flags Fallback Strategy

**Date**: 2024-11-18  
**Status**: Implemented

## Overview

Feature flags use a **composite provider pattern** with Supabase as the primary source and BuildConfig as the fallback. This ensures features are **agnostic to the source** - they always get a value.

## Architecture

### Provider Hierarchy

```
FeatureFlagManager
  └── CompositeFeatureFlagProvider
        ├── Primary: SupabaseFeatureFlagProvider (Supabase)
        └── Fallback: ConfigBasedFeatureFlagProvider (BuildConfig)
```

### Fallback Logic

1. **Primary (Supabase)**:
   - Fetches flags from `feature_flags` table via PostgREST
   - Caches flags locally
   - Only used if flags are initialised AND flag exists in cache

2. **Fallback (BuildConfig)**:
   - Reads from `BuildConfig` fields generated at build time
   - Used when:
     - Supabase flags not yet initialised
     - Flag not found in Supabase cache
     - Supabase unavailable (offline mode)
     - Primary provider throws exception

### Dependency Injection

**Features using flags are completely agnostic to the source**:

```kotlin
// Feature code doesn't know or care where the flag comes from
val featureFlagManager = (application as ElectricSheepApplication).featureFlagManager
val isEnabled = featureFlagManager.isEnabled("my_feature", defaultValue = false)
```

The `FeatureFlagManager` abstracts away the provider implementation, ensuring:
- ✅ Features always get a value (primary or fallback)
- ✅ No need to check if Supabase is available
- ✅ Graceful degradation when remote source unavailable
- ✅ Consistent API regardless of source

## BuildConfig Requirements

**All flags in `feature-flags/flags.yaml` must have corresponding BuildConfig fields** for fallback values.

### Naming Convention

- **Boolean flags**: `FLAG_KEY_UPPERCASE_MODE`
  - Example: `offline_only` → `OFFLINE_ONLY_MODE`
  
- **String/Int flags**: `FLAG_KEY_UPPERCASE_VALUE`
  - Example: `export_format` → `EXPORT_FORMAT_VALUE`
  - Example: `max_moods_per_day` → `MAX_MOODS_PER_DAY_VALUE`

### Adding BuildConfig Fields

In `app/build.gradle.kts`:

```kotlin
defaultConfig {
    // Boolean flag
    buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
    
    // String flag
    buildConfigField("String", "EXPORT_FORMAT_VALUE", "\"csv\"")
    
    // Int flag
    buildConfigField("int", "MAX_MOODS_PER_DAY_VALUE", "10")
}
```

### Pipeline Validation

The GitHub Actions workflow automatically validates that:
1. All flags in `flags.yaml` have corresponding BuildConfig fields
2. BuildConfig field names follow the naming convention
3. BuildConfig field types match flag value types

**If validation fails**, the pipeline will:
- List all missing BuildConfig fields
- Show examples of how to add them
- Fail the build/PR

## Example Flow

### Scenario 1: Supabase Available, Flag Exists

1. App starts → `SupabaseFeatureFlagProvider.initialise()` fetches flags
2. Feature requests flag → `CompositeFeatureFlagProvider.getBoolean()`
3. Checks: Flags initialised? ✅ Flag in cache? ✅
4. **Returns Supabase value** (primary source)

### Scenario 2: Supabase Available, Flag Not Found

1. App starts → Flags fetched from Supabase
2. Feature requests flag → Flag not in Supabase cache
3. **Falls back to BuildConfig value** (fallback source)

### Scenario 3: Supabase Unavailable (Offline)

1. App starts → Supabase client is null
2. `DataModule` creates `ConfigBasedFeatureFlagProvider` only
3. Feature requests flag → **Returns BuildConfig value** (only source)

### Scenario 4: Supabase Not Yet Initialised

1. App starts → Flags still fetching from Supabase
2. Feature requests flag → Flags not initialised yet
3. **Falls back to BuildConfig value** (temporary fallback)
4. Once Supabase flags loaded, future requests use Supabase

## Benefits

1. **Source Agnostic**: Features don't need to know about Supabase/BuildConfig
2. **Graceful Degradation**: App works even if Supabase unavailable
3. **Fast Startup**: BuildConfig values available immediately
4. **Type Safety**: BuildConfig fields validated at build time
5. **Version Control**: Both sources (Supabase + BuildConfig) in version control
6. **Pipeline Validation**: Ensures fallback values always exist

## Testing

### Unit Tests

- Test `CompositeFeatureFlagProvider` fallback logic
- Test `ConfigBasedFeatureFlagProvider` BuildConfig reading
- Test fallback when primary provider not ready

### Integration Tests

- Test flag fetching from Supabase
- Test fallback to BuildConfig when Supabase unavailable
- Test fallback when flag not in Supabase

## Migration Guide

When adding a new flag:

1. **Add to `feature-flags/flags.yaml`**:
   ```yaml
   - key: my_new_feature
     value_type: boolean
     boolean_value: false
     enabled: true
   ```

2. **Add BuildConfig field in `app/build.gradle.kts`**:
   ```kotlin
   buildConfigField("boolean", "MY_NEW_FEATURE_MODE", "false")
   ```

3. **Commit and push** → Pipeline validates both exist

4. **Deploy** → Flag synced to Supabase, BuildConfig available as fallback

## Future Enhancements

- **Real-time Updates**: Supabase Realtime subscription to refresh flags
- **Local Caching**: Persist Supabase flags to local storage for offline access
- **Flag Versioning**: Track flag versions and migration paths
- **A/B Testing**: Edge Function for percentage rollouts (still uses fallback)

