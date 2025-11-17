# Feature Flags Architecture

This document describes the feature flag system for the Electric Sheep Android app.

## Overview

The feature flag system provides a flexible way to enable/disable features at runtime or build time. It's designed with extensibility in mind, supporting both local (config-based) and remote flag providers.

## Architecture

```
┌─────────────────────────────────────┐
│     FeatureFlagManager              │
│  (Single point of access)           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    FeatureFlagProvider               │
│    (Interface)                       │
└──────┬──────────────────┬───────────┘
       │                  │
┌──────▼──────┐   ┌──────▼───────────┐
│   Config    │   │     Remote        │
│   Based     │   │   (Future)        │
│  Provider   │   │   Provider        │
└─────────────┘   └───────────────────┘
```

## Components

### 1. FeatureFlag (`config/FeatureFlag.kt`)

Centralised constants for all feature flag keys:
- `OFFLINE_ONLY`: Disables all remote sync operations

### 2. FeatureFlagProvider (`config/FeatureFlagProvider.kt`)

Interface for feature flag providers:
- `getBoolean(key, defaultValue)`: Get boolean flag
- `getString(key, defaultValue)`: Get string flag
- `getInt(key, defaultValue)`: Get integer flag
- `isEnabled(key, defaultValue)`: Convenience method for boolean flags

### 3. ConfigBasedFeatureFlagProvider (`config/ConfigBasedFeatureFlagProvider.kt`)

Current implementation that reads from BuildConfig:
- Reads `BuildConfig.OFFLINE_ONLY_MODE` for offline-only flag
- Suitable for development and build-time configuration
- Falls back to defaults if BuildConfig field is missing

### 4. RemoteFeatureFlagProvider (`config/RemoteFeatureFlagProvider.kt`)

Abstract base class for future remote flag providers:
- Supports caching flags locally
- Handles network failures gracefully
- Can be extended to support Supabase, Firebase Remote Config, etc.

### 5. FeatureFlagManager (`config/FeatureFlagManager.kt`)

Central manager that provides a single point of access:
- Wraps the underlying provider
- Provides convenience methods like `isOfflineOnly()`
- Can be extended to support multiple providers with priority/fallback

## Usage

### Enabling Offline-Only Mode

To enable offline-only mode for development:

**Option 1: BuildConfig (Recommended for development)**

Edit `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("boolean", "OFFLINE_ONLY_MODE", "true")
    }
}
```

**Option 2: Per-build variant**

You can create a custom build variant:

```kotlin
productFlavors {
    create("offline") {
        buildConfigField("boolean", "OFFLINE_ONLY_MODE", "true")
    }
    create("online") {
        buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
    }
}
```

Then build with: `./gradlew assembleOfflineDebug`

### Using Feature Flags in Code

```kotlin
// In repository or other components
class MyRepository(
    private val featureFlagManager: FeatureFlagManager
) {
    fun doSomething() {
        if (featureFlagManager.isOfflineOnly()) {
            // Skip remote operations
            return
        }
        // Normal flow with remote operations
    }
}
```

### Adding New Feature Flags

1. Add constant to `FeatureFlag.kt`:
```kotlin
const val ENABLE_NEW_FEATURE = "enable_new_feature"
```

2. Add handling in `ConfigBasedFeatureFlagProvider`:
```kotlin
FeatureFlag.ENABLE_NEW_FEATURE -> {
    BuildConfig.ENABLE_NEW_FEATURE
}
```

3. Add BuildConfig field in `build.gradle.kts`:
```kotlin
buildConfigField("boolean", "ENABLE_NEW_FEATURE", "false")
```

4. Use in code:
```kotlin
if (featureFlagManager.isEnabled(FeatureFlag.ENABLE_NEW_FEATURE)) {
    // New feature code
}
```

## Integration Points

### MoodRepository

The repository checks `isOfflineOnly()` before:
- Saving moods to remote
- Updating moods in remote
- Deleting moods from remote
- Syncing with remote

### SyncManager

The sync manager checks `isOfflineOnly()` before:
- Starting periodic sync
- Triggering immediate sync

## Future: Remote Feature Flags

When implementing remote feature flags:

1. **Create a concrete RemoteFeatureFlagProvider**:
```kotlin
class SupabaseFeatureFlagProvider(
    private val supabaseClient: SupabaseClient
) : RemoteFeatureFlagProvider() {
    override suspend fun fetchFlags(): Map<String, Any> {
        // Fetch from Supabase feature_flags table
        return supabaseClient.from("feature_flags")
            .select()
            .decodeList<FeatureFlag>()
            .associate { it.key to it.value }
    }
}
```

2. **Update DataModule**:
```kotlin
fun createFeatureFlagManager(): FeatureFlagManager {
    val provider = if (useRemoteFlags) {
        SupabaseFeatureFlagProvider(supabaseClient)
    } else {
        ConfigBasedFeatureFlagProvider()
    }
    return FeatureFlagManager(provider)
}
```

3. **Initialise in Application**:
```kotlin
val featureFlagManager = DataModule.createFeatureFlagManager()
if (featureFlagManager is RemoteFeatureFlagProvider) {
    featureFlagManager.initialise()
}
```

## Testing

### Unit Tests

- `FeatureFlagManagerTest`: Tests manager delegation to provider
- `MoodRepositoryOfflineOnlyTest`: Tests repository behaviour when offline-only is enabled

### Manual Testing

1. Enable offline-only mode in BuildConfig
2. Build and run app
3. Verify:
   - No remote sync operations occur
   - Local operations work normally
   - Sync manager doesn't start periodic sync
   - Logs show "offline-only mode enabled" messages

## Best Practices

1. **Always provide defaults**: Feature flags should have sensible defaults
2. **Log flag usage**: Use Logger to track when flags affect behaviour
3. **Test both states**: Test code paths with flags enabled and disabled
4. **Document flags**: Document what each flag does and when to use it
5. **Keep flags simple**: Avoid complex flag logic; prefer simple boolean flags
6. **Remove obsolete flags**: Clean up flags that are no longer needed

## Current Flags

| Flag | Key | Default | Description |
|------|-----|---------|-------------|
| Offline Only | `OFFLINE_ONLY` | `false` | Disables all remote sync operations. Useful for development and testing offline functionality. |

