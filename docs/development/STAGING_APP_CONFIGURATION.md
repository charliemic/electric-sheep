# Staging App Configuration

**Date**: 2025-11-18  
**Status**: Configuration Guide

## Overview

The app already has full support for using staging Supabase in debug builds. No code changes are needed - just configuration.

## How It Works

The app uses BuildConfig flags to switch between staging and production Supabase:

1. **`USE_STAGING_SUPABASE`** - Boolean flag (default: `false`)
2. **`SUPABASE_STAGING_URL`** - Staging project URL
3. **`SUPABASE_STAGING_ANON_KEY`** - Staging project anon key

When `USE_STAGING_SUPABASE` is `true` in debug builds, the app automatically:
- Uses staging URL and anon key instead of production
- Shows a "STAGING" indicator badge on the landing screen
- Fetches feature flags from staging environment
- Connects to staging database for all operations

## Configuration Steps

### 1. Add Staging Credentials to `local.properties`

Add your staging project credentials:

```properties
# Production (required)
supabase.url=https://your-production-project.supabase.co
supabase.anon.key=your-production-anon-key

# Staging (optional, for testing)
supabase.staging.url=https://your-staging-project.supabase.co
supabase.staging.anon.key=your-staging-anon-key
```

**Where to find these values:**
- **Staging URL**: `https://<staging-project-ref>.supabase.co` (e.g., `https://rmcnvcqnowgsvvbmfssi.supabase.co`)
- **Staging Anon Key**: Supabase Dashboard → Staging Project → Settings → API → `anon` `public` key
- **Staging Project Ref**: Found in your Supabase dashboard URL: `https://supabase.com/dashboard/project/<project-ref>`

### 2. Enable Staging in Debug Builds

Edit `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        // Enable staging Supabase for testing
        buildConfigField("boolean", "USE_STAGING_SUPABASE", "true")
    }
    release {
        // Staging is always disabled in release builds
        buildConfigField("boolean", "USE_STAGING_SUPABASE", "false")
    }
}
```

### 3. Rebuild the App

```bash
./gradlew :app:assembleDebug
```

Or rebuild in Android Studio.

## Visual Indicator

When using staging, you'll see a **"STAGING"** badge (red) in the top-left corner of the landing screen. This makes it clear which environment you're connected to.

- **"STAGING"** (red) = Using staging Supabase
- **"PROD"** (blue) = Using production Supabase (or staging disabled)

## Code Implementation

The staging logic is already implemented in `DataModule.kt`:

```kotlin
// In debug builds, allow switching to staging Supabase
val useStaging = BuildConfig.USE_STAGING_SUPABASE && BuildConfig.DEBUG

val finalUrl = if (useStaging) {
    val stagingUrl = BuildConfig.SUPABASE_STAGING_URL
    if (stagingUrl.isNotEmpty() && stagingUrl != "\"\"") {
        Logger.info("DataModule", "Using STAGING Supabase: $stagingUrl")
        stagingUrl
    } else {
        Logger.warn("DataModule", "USE_STAGING_SUPABASE is true but SUPABASE_STAGING_URL is not set, using production")
        supabaseUrl
    }
} else {
    supabaseUrl
}
```

**No code changes needed** - the app already handles this automatically!

## Testing Workflow

1. **Set up staging project** (see `STAGING_ENVIRONMENT_SETUP.md`)
2. **Add staging credentials** to `local.properties`
3. **Enable staging** in `build.gradle.kts` (debug build type)
4. **Rebuild and run** the app
5. **Verify** you see the "STAGING" badge
6. **Test** feature flags and data operations against staging

## Important Notes

- **Staging is only available in debug builds** - release builds always use production
- **Staging credentials are optional** - if not set, the app falls back to production
- **The staging indicator** only appears in debug builds
- **All Supabase operations** (auth, data, feature flags) use staging when enabled

## Troubleshooting

### Staging Not Working

1. **Check `local.properties`**:
   - Ensure `supabase.staging.url` and `supabase.staging.anon.key` are set
   - No quotes around values
   - Correct project reference in URL

2. **Check `build.gradle.kts`**:
   - `USE_STAGING_SUPABASE` is set to `true` in debug build type
   - Rebuild after changing this value

3. **Check logs**:
   - Look for "Using STAGING Supabase" in logcat
   - If you see "USE_STAGING_SUPABASE is true but SUPABASE_STAGING_URL is not set", check `local.properties`

### Still Using Production

- Ensure you're running a **debug build** (not release)
- Verify `BuildConfig.DEBUG` is `true` (it should be automatically)
- Check that `USE_STAGING_SUPABASE` is `true` in debug build type
- Rebuild the app after configuration changes

