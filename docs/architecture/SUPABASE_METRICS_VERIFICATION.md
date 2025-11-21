# Supabase Metrics Integration Verification

**Date**: 2025-11-21  
**Status**: Pre-Deployment Verification  
**Purpose**: Ensure metrics integration follows working patterns

## Critical Issues to Verify

### 1. PostgREST API Table Names ✅ FIXED

**Issue**: PostgREST doesn't use schema-qualified table names in URLs

**Pattern from working code**:
- ✅ `feature_flags` (not `public.feature_flags`)
- ✅ `moods` (not `public.moods`)

**Our Implementation**:
- ✅ Fixed: Use `pr_events` (not `metrics.pr_events`)
- ✅ Fixed: Use `test_runs` (not `metrics.test_runs`)
- ✅ Fixed: Use `deployment_events` (not `metrics.deployment_events`)

**How it works**: PostgREST searches all exposed schemas (defined in `config.toml`). Since `metrics` is in the schemas list, it will find our tables.

### 2. Environment Variable Names ✅ FIXED

**Issue**: Must match existing working pattern

**Working Pattern** (from feature flags):
- `SUPABASE_SECRET_KEY_STAGING` (not `SUPABASE_STAGING_SECRET_KEY`)
- `SUPABASE_SECRET_KEY` (for production)
- Get URL from `supabase status` or construct from project ID

**Our Implementation**:
- ✅ Fixed: Use `SUPABASE_SECRET_KEY_STAGING` (matches working pattern)
- ✅ Fixed: Get URL from Supabase CLI or construct from project ID
- ✅ Fixed: Use same fallback pattern as feature flags

### 3. Schema Configuration ✅ VERIFIED

**Requirement**: `metrics` schema must be in `config.toml` schemas list

**Status**: ✅ Already done
```toml
schemas = ["public", "graphql_public", "metrics"]
```

### 4. Migration Deployment ✅ VERIFIED

**Pattern**: Use Supabase CLI (`supabase db push`), not direct database connections

**Status**: ✅ Our migrations will deploy via existing `supabase-schema-deploy.yml` workflow

**Verification**:
- Migrations follow naming convention: `YYYYMMDDHHMMSS_description.sql` ✅
- Schema creation migration runs first ✅
- Tables migration runs second ✅
- Uses same workflow as existing migrations ✅

### 5. RLS Policies ✅ VERIFIED

**Pattern**: Service role can manage all, authenticated users can read

**Status**: ✅ All tables have correct RLS policies matching existing pattern

### 6. Workflow Dependencies ✅ VERIFIED

**Issue**: Metrics collection needs schema to exist first

**Solution**: 
- Schema deployment happens via `supabase-schema-deploy.yml` (on push to main/develop)
- Metrics collection happens via `record-metrics.yml` (on PR/workflow_run)
- No direct dependency needed - schema will be deployed before metrics are collected

**Note**: For first deployment, schema must be deployed before metrics collection can work.

## Verification Checklist

### Pre-Deployment
- [x] PostgREST API uses table names (not schema-qualified) ✅
- [x] Environment variable names match working pattern ✅
- [x] Schema is in `config.toml` schemas list ✅
- [x] Migrations follow naming convention ✅
- [x] RLS policies match existing pattern ✅
- [x] Workflow uses Supabase CLI pattern ✅

### Post-Deployment
- [ ] Verify migrations applied successfully
- [ ] Verify tables exist in Supabase dashboard
- [ ] Verify RLS policies are active
- [ ] Test PostgREST API access (manual test)
- [ ] Verify workflow can access tables
- [ ] Test end-to-end metrics collection

## Testing Plan

### 1. Local Testing (Optional)
```bash
# Start local Supabase
supabase start

# Apply migrations
supabase db reset

# Test API access
export SUPABASE_URL="http://127.0.0.1:54321"
export SUPABASE_SECRET_KEY="<local-service-role-key>"
./scripts/record-pr-event.sh created 999 "Test" "test-branch" "test-user"
```

### 2. Staging Testing (Recommended)
1. Deploy migrations to staging
2. Verify tables exist
3. Test API access manually
4. Create test PR and verify workflow records metrics

### 3. Production Deployment
1. Deploy migrations to production
2. Verify tables exist
3. Monitor first few metrics collections
4. Verify data appears correctly

## Known Limitations

1. **First-time setup**: Schema must be deployed before metrics collection works
2. **PostgREST schema search**: If table names conflict between schemas, `public` wins (we don't have conflicts)
3. **Workflow dependencies**: No explicit dependency - relies on schema being deployed first

## Related Documentation

- `docs/archive/development/SUPABASE_MIGRATION_FAILURE_ANALYSIS.md` - Past issues
- `.github/workflows/supabase-feature-flags-deploy.yml` - Working pattern reference
- `scripts/lib/supabase-postgrest.sh` - PostgREST library
- `supabase/config.toml` - Schema configuration

