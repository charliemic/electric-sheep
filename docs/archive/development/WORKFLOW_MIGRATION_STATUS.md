# Supabase Workflow Migration Status

## Overview

The Supabase workflows were renamed for clarity:
- `supabase-deploy.yml` → `supabase-schema-deploy.yml` (Deploy Supabase Database Schema)
- `deploy-feature-flags.yml` → `supabase-feature-flags-deploy.yml` (Deploy Feature Flag Values)

## Current Status

### Workflow Recognition

⚠️ **Important**: GitHub Actions only recognizes workflows that exist on the **default branch** (main). 

The renamed workflows are currently only on the `feature/remote-feature-flags` branch. They will not be recognized by GitHub Actions until merged to `main`.

### Migration Status

**Last Successful Schema Deployment**: 2025-11-18T13:49:36Z (feature/remote-feature-flags branch)

**Migrations Available**:
1. `20241117000000_create_moods_table.sql` - Creates moods table
2. `20251118130744_create_feature_flags_table.sql` - Creates feature_flags table
3. `20251118140000_add_feature_flags_versioning.sql` - Adds versioning to feature_flags

### Next Steps

1. **Merge feature branch to main** to activate new workflow names
2. **Verify migrations are applied** to staging environment
3. **Test feature flags deployment** to staging

### Manual Triggering

Until the workflows are on main, you can manually trigger the old workflow names:
- `Supabase Deploy` (old name, still active on main)
- `deploy-feature-flags.yml` (old name, still active on main)

Or use `workflow_dispatch` from the feature branch after merging.

## Workflow Fixes Applied

1. ✅ Fixed feature flags workflow path reference (updated to new filename)
2. ✅ Fixed deploy-staging condition to allow workflow_dispatch from any branch
3. ✅ Added `USE_STAGING_SUPABASE` to release build type (fixes compilation errors)

