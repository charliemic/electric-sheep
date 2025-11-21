# Runtime Environment Switching

**Date**: 2025-11-19
**Status**: Feature Documentation

## Overview

The app supports **runtime environment switching** between staging and production in debug builds. This allows developers to switch environments without rebuilding the app, making it easier to test against different Supabase projects.

## Features

- **Runtime switching**: Change environments at runtime via UI (debug builds only)
- **Visual indicator**: Clear visual feedback showing current environment
- **Persistent selection**: Environment choice persists across app restarts
- **Safe switching**: Automatic logout when switching (auth tokens are environment-specific)
- **Loading states**: Full-screen overlay during environment switch

## How It Works

### Environment Manager

The `EnvironmentManager` class manages runtime environment selection:

- Stores selected environment in `SharedPreferences`
- Defaults to **STAGING** if `USE_STAGING_SUPABASE` is `true` in debug builds
- Only available in debug builds (release builds always use production)

### Runtime Reinitialization

When the environment is switched:

1. **User confirmation**: If logged in, shows warning dialog explaining logout
2. **Sign out**: Automatically signs out current user (auth tokens are environment-specific)
3. **Reinitialize**: Creates new Supabase client with selected environment
4. **Reload flags**: Fetches feature flags from new environment
5. **Update UI**: Updates UI when switch completes

### Visual Indicators

- **Environment badge**: Shows "STAGING" (red) or "PROD" (blue) in top-right corner
- **Interactive dropdown**: Tap badge to switch environments
- **Loading overlay**: Full-screen overlay during environment switch

## Usage

### For Developers

1. **Build debug APK** with staging configured
2. **Launch app** - defaults to staging if `USE_STAGING_SUPABASE=true`
3. **Switch environments**: Tap the environment badge and select desired environment
4. **Verify environment**: Check badge color and text

### Configuration

Environment switching requires:

1. **Staging credentials** in `local.properties`
2. **BuildConfig flag** in `app/build.gradle.kts` set to `true` for debug builds
3. **Debug build** - feature only works in debug builds

## Technical Details

### Key Components

1. **EnvironmentManager** - Manages environment selection and persistence
2. **DataModule** - Creates Supabase client with selected environment
3. **ElectricSheepApplication** - Handles reinitialization on switch
4. **LandingScreen** - UI for environment switching

### Safety Features

- **Automatic logout**: Prevents auth token confusion
- **Loading overlay**: Blocks navigation during switch
- **Warning dialog**: Informs user about logout when switching
- **Debug-only**: Feature disabled in release builds

## Related Documentation

- [Staging App Configuration](./STAGING_APP_CONFIGURATION.md) - Build-time configuration
- [Staging Environment Setup](./STAGING_ENVIRONMENT_SETUP.md) - Setting up staging project
