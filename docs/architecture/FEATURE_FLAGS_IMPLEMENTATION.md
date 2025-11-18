# Feature Flags Implementation

**Date**: 2024-11-18  
**Status**: Implemented

## Overview

Feature flags are now implemented using Supabase PostgREST with version-controlled deployment via GitHub Actions.

## Architecture

### Database Schema

**Table**: `feature_flags`

- **id**: UUID primary key
- **key**: Unique flag identifier (TEXT, UNIQUE)
- **value_type**: One of `boolean`, `string`, or `int`
- **boolean_value** / **string_value** / **int_value**: Value matching the type
- **enabled**: Whether the flag is active
- **segment_id**: Future: Link to user segments (NULL = global flag)
- **user_id**: User-specific flag (NULL = applies to all users)
- **description**: Human-readable description
- **created_at** / **updated_at**: Timestamps

**Indexes**:
- `idx_feature_flags_key` - Fast lookups by key
- `idx_feature_flags_segment_id` - Future segmentation support
- `idx_feature_flags_user_id` - User-specific flags
- `idx_feature_flags_enabled` - Filter enabled flags
- `idx_feature_flags_key_user_enabled` - Composite index for common queries

**RLS Policies**:
- Users can read flags that apply to them (global, user-specific, or segment-based)
- Service role can manage all flags (for CI/CD deployments)

### Application Layer

**Components**:

1. **`FeatureFlag`** (data model)
   - Kotlinx serialization for PostgREST
   - Helper methods for value extraction and user matching

2. **`SupabaseFeatureFlagProvider`**
   - Extends `RemoteFeatureFlagProvider`
   - Fetches flags from Supabase via PostgREST
   - Supports user-specific and global flags
   - Caches flags locally
   - Falls back to defaults on error

3. **`FeatureFlagManager`**
   - Wraps provider implementation
   - Provides convenient access methods
   - Exposes provider for type checking

4. **`DataModule`**
   - Creates `FeatureFlagManager` with appropriate provider
   - Uses Supabase provider if available, otherwise config-based

5. **`ElectricSheepApplication`**
   - Initializes feature flags on app start
   - Fetches flags from Supabase asynchronously
   - Falls back gracefully on errors

## Version Control & Deployment

### Feature Flag Definition

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

### GitHub Actions Workflow

**File**: `.github/workflows/deploy-feature-flags.yml`

**Triggers**:
- Push to `main` or `develop` branches (when `feature-flags/` changes)
- Pull requests (validation only)
- Manual workflow dispatch

**Jobs**:

1. **`validate-flags`**: Validates YAML syntax and flag structure
2. **`deploy-staging`**: Deploys to staging environment (on `develop`)
3. **`deploy-production`**: Deploys to production (on `main`)
4. **`preview-flags`**: Shows flag changes in PR comments

### Sync Script

**File**: `scripts/sync-feature-flags.sh`

**Features**:
- Reads flags from YAML
- Validates structure
- Upserts flags to Supabase database
- Supports both direct PostgreSQL connection and PostgREST API
- Provides clear success/error feedback

**Usage**:
```bash
export SUPABASE_DB_URL="postgresql://..."
./scripts/sync-feature-flags.sh feature-flags/flags.yaml
```

## Usage in Code

### Reading Feature Flags

```kotlin
// Get feature flag manager
val featureFlagManager = (application as ElectricSheepApplication).featureFlagManager

// Check boolean flag
val isEnabled = featureFlagManager.isEnabled("my_feature", defaultValue = false)

// Get string flag
val value = featureFlagManager.getString("my_string_flag", defaultValue = "default")

// Get int flag
val count = featureFlagManager.getInt("my_int_flag", defaultValue = 0)
```

### Adding New Flags

1. Edit `feature-flags/flags.yaml`
2. Add flag definition
3. Commit and push
4. GitHub Actions automatically deploys

## Future Enhancements

### Segmentation Support

The schema already supports segmentation via `segment_id`. To implement:

1. Create `segments` table
2. Create `user_segments` junction table
3. Update RLS policies to check segment membership
4. Update `SupabaseFeatureFlagProvider` to filter by segment

### Real-time Updates

Can add Supabase Realtime subscription to `SupabaseFeatureFlagProvider`:

```kotlin
supabaseClient.from("feature_flags")
    .subscribe {
        // Refresh flags when changed
        refresh()
    }
```

### A/B Testing

For percentage rollouts or A/B testing, add Edge Function:

```typescript
// supabase/functions/feature-flags/index.ts
// Evaluate flags with percentage rollouts, user targeting, etc.
```

## Migration

**File**: `supabase/migrations/20251118130744_create_feature_flags_table.sql`

Run via existing Supabase deployment workflow.

## Testing

### Unit Tests

- `FeatureFlag` model tests
- `SupabaseFeatureFlagProvider` tests (with mocked Supabase client)
- `FeatureFlagManager` tests

### Integration Tests

- Test flag fetching from Supabase
- Test user-specific flags
- Test fallback behavior

## Security

- **RLS Policies**: Users can only read flags that apply to them
- **Service Role**: CI/CD uses service role for deployments
- **Validation**: YAML validation prevents invalid flags
- **Type Safety**: Kotlin type system ensures correct usage

## Monitoring

- Log flag fetches and errors
- Monitor flag usage in analytics
- Track deployment success/failure in GitHub Actions

