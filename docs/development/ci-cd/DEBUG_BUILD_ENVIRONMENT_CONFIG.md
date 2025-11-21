# Debug Build Environment Configuration

**Date**: 2025-11-20  
**Status**: Documented

## Current Configuration

### Debug Builds Default to Staging

**Configuration in `app/build.gradle.kts`:**
```kotlin
debug {
    buildConfigField("boolean", "USE_STAGING_SUPABASE", "true")
}
```

**Behavior in `EnvironmentManager.kt`:**
```kotlin
fun getSelectedEnvironment(): Environment {
    if (!BuildConfig.DEBUG) {
        return Environment.PRODUCTION
    }
    
    // Check if staging is available
    val stagingAvailable = BuildConfig.USE_STAGING_SUPABASE &&
                           BuildConfig.SUPABASE_STAGING_URL.isNotEmpty() &&
                           BuildConfig.SUPABASE_STAGING_URL != "\"\""
    
    if (!stagingAvailable) {
        return Environment.PRODUCTION
    }
    
    // Read from preferences, default to staging if USE_STAGING_SUPABASE is true
    val storedValue = prefs.getString(KEY_SELECTED_ENVIRONMENT, null)
    return when {
        storedValue == Environment.STAGING.name -> Environment.STAGING
        storedValue == Environment.PRODUCTION.name -> Environment.PRODUCTION
        BuildConfig.USE_STAGING_SUPABASE -> Environment.STAGING // ✅ Defaults to staging
        else -> Environment.PRODUCTION
    }
}
```

## How It Works

1. **Debug builds**: `USE_STAGING_SUPABASE = true` in `build.gradle.kts`
2. **Default behavior**: If no preference is stored, defaults to `STAGING` when `USE_STAGING_SUPABASE` is true
3. **Requirement**: Staging credentials must be configured in `local.properties`:
   - `supabase.staging.url`
   - `supabase.staging.anon.key`

## Current Issue: App Not Connecting to Supabase

**Problem**: App logs show:
```
W DataModule: Supabase credentials not configured. Using placeholder values.
```

**Root Cause**: `local.properties` file is missing or doesn't have Supabase credentials configured.

**Impact**: 
- App cannot connect to Supabase (neither staging nor production)
- Feature flags fall back to BuildConfig values
- App runs in offline-only mode

## Solution

### To Enable Staging Connection

1. **Create/Update `local.properties`**:
   ```properties
   supabase.url=https://your-prod-project.supabase.co
   supabase.anon.key=your-prod-anon-key
   
   supabase.staging.url=https://your-staging-project.supabase.co
   supabase.staging.anon.key=your-staging-anon-key
   ```

2. **Rebuild the app**:
   ```bash
   ./gradlew clean assembleDebug
   ```

3. **Verify connection**:
   - Check logs for "Using STAGING Supabase: ..."
   - App should connect to staging by default in debug builds

### To Verify Current Environment

**Check app logs**:
```bash
adb logcat | grep -E "(Using STAGING|Using PRODUCTION|DataModule.*Supabase)"
```

**Check SharedPreferences** (if app has run):
```bash
adb shell "run-as com.electricsheep.app cat /data/data/com.electricsheep.app/shared_prefs/environment_prefs.xml"
```

## Feature Flag Status

✅ **Flag Updated**: `enable_trivia_app` is now set to `true` in Supabase staging
- Workflow run: 19534320437
- Status: ✓ Updated: enable_trivia_app
- Sync complete: 3 succeeded, 0 failed

**To see the flag in the app**:
1. Configure Supabase credentials in `local.properties`
2. Rebuild the app
3. App will connect to staging by default (debug builds)
4. Flag will be fetched from Supabase staging
5. Trivia card will appear on landing screen

## Summary

- ✅ **Debug builds default to staging**: `USE_STAGING_SUPABASE = true` in debug build type
- ✅ **EnvironmentManager defaults to staging**: When `USE_STAGING_SUPABASE` is true and no preference stored
- ⚠️ **Credentials required**: App needs `local.properties` with staging credentials to connect
- ✅ **Flag enabled**: `enable_trivia_app` is set to `true` in Supabase staging

## Related Documentation

- `docs/development/STAGING_APP_CONFIGURATION.md` - Staging setup guide
- `docs/development/RUNTIME_ENVIRONMENT_SWITCHING.md` - Runtime environment switching
- `app/src/main/java/com/electricsheep/app/config/EnvironmentManager.kt` - Environment manager implementation

