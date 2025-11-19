# Staging Environment Setup Guide

**Date**: 2025-11-18  
**Status**: Setup Guide

## Overview

This guide explains how to set up a separate staging Supabase project and configure the CI/CD workflows to deploy migrations and feature flags to both staging and production environments.

## Migration Sequence

All migrations must be applied in chronological order to a new staging project:

### 1. `20241117000000_create_moods_table.sql`
**Purpose**: Creates the initial `moods` table for mood tracking  
**Dependencies**: None (base schema)  
**Creates**:
- `public.moods` table with user scoping
- Indexes on `user_id` and `timestamp`
- Row-Level Security (RLS) policies for user data isolation

### 2. `20251118130744_create_feature_flags_table.sql`
**Purpose**: Creates the `feature_flags` table for remote feature flag management  
**Dependencies**: None (independent of moods table)  
**Creates**:
- `public.feature_flags` table with segmentation support
- Indexes for fast lookups
- RLS policies (users can read, service role can manage)

### 3. `20251118140000_add_feature_flags_versioning.sql`
**Purpose**: Adds versioning support to feature flags for cache invalidation  
**Dependencies**: Requires `feature_flags` table (from migration #2)  
**Creates**:
- `version` column on `feature_flags` table
- Version increment trigger function
- Auto-increment trigger

## Migration Order

The migrations are already correctly sequenced by timestamp:
```
20241117000000 → 20251118130744 → 20251118140000
```

**Important**: Migrations must run in this exact order. The Supabase CLI automatically applies migrations in chronological order based on the timestamp prefix.

## Setting Up Staging Project

### Step 1: Create New Supabase Project

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Click "New Project"
3. Create a project specifically for staging (e.g., "electric-sheep-staging")
4. **Important**: This must be a **new, empty project**. Do not use an existing project that has been modified, as the CLI will try to reapply all migrations.

### Step 2: Get Project Reference

1. In the Supabase Dashboard, navigate to your new staging project
2. Go to Settings → General
3. Copy the **Project Reference ID** (e.g., `abcdefghijklmnop`)

### Step 3: Get Database Password

1. In the Supabase Dashboard, go to Settings → Database
2. Under "Connection string", find the database password
3. If you haven't set one, you can reset it here

### Step 4: Add GitHub Secrets

Add the following secrets to your GitHub repository:

**Required Secrets**:
- `SUPABASE_ACCESS_TOKEN` - Your Supabase Personal Access Token (already set)
- `SUPABASE_DB_PASSWORD` - Database password (same for both environments, or separate if different)
- `SUPABASE_PROJECT_REF` - **Production** project reference ID
- `SUPABASE_PROJECT_REF_STAGING` - **Staging** project reference ID (new)

**Optional** (if you want separate passwords):
- `SUPABASE_DB_PASSWORD_STAGING` - Staging database password
- `SUPABASE_DB_PASSWORD_PROD` - Production database password

### Step 5: Initial Migration to Staging

Once the secrets are set, the workflow will automatically deploy migrations to staging when:
- Changes are pushed to the `develop` branch
- Or manually triggered via `workflow_dispatch`

**Manual Deployment** (if needed):
```bash
# Link to staging project
supabase link --project-ref <staging-project-ref>

# Deploy all migrations
supabase db push

# Verify migrations
supabase migration list
```

## Workflow Behavior

### Schema Deployments

- **Staging**: Deploys when changes are pushed to `develop` branch
- **Production**: Deploys when changes are pushed to `main` branch
- **Manual**: Can be triggered via `workflow_dispatch` with optional `project_ref` input

### Feature Flag Deployments

- **Staging**: Uses `SUPABASE_PROJECT_REF_STAGING` (or falls back to `SUPABASE_PROJECT_REF`)
- **Production**: Uses `SUPABASE_PROJECT_REF` (production project)
- Both use the same `SUPABASE_DB_PASSWORD` and `SUPABASE_ACCESS_TOKEN`

## Verification

After setting up staging, verify:

1. **Migrations Applied**:
   ```bash
   supabase migration list --linked
   ```
   Should show all 3 migrations applied.

2. **Tables Created**:
   - `public.moods` table exists
   - `public.feature_flags` table exists
   - `public.feature_flags.version` column exists

3. **RLS Policies**:
   - Check that RLS is enabled on both tables
   - Verify policies are created correctly

4. **Feature Flags**:
   - Run the feature flags deployment workflow
   - Verify flags are synced to staging project

## Troubleshooting

### "Tenant or user not found" Error

This usually means:
- Project reference is incorrect
- Database password is incorrect
- Connection string format is wrong

Check that:
- `SUPABASE_PROJECT_REF_STAGING` matches your staging project reference
- `SUPABASE_DB_PASSWORD` is correct
- The connection string format is: `postgresql://postgres.[PROJECT_REF]:[PASSWORD]@aws-0-[REGION].pooler.supabase.com:6543/postgres`

### Migration Conflicts

If you see migration conflicts:
- Ensure staging project is **completely new and empty**
- Do not manually create tables in the dashboard before running migrations
- The CLI tracks migration state - if a project has been modified, it may try to reapply changes

### Feature Flags Not Syncing

- Verify `SUPABASE_PROJECT_REF_STAGING` is set correctly
- Check that `feature_flags` table exists in staging
- Ensure RLS policies allow service role to manage flags

