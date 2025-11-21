# Metrics Environment Strategy

**Date**: 2025-11-21  
**Status**: Final Strategy  
**Decision**: Staging for schema, production for data

## Strategy Overview

### Schema Deployments (Migrations)
- ✅ **Always deploy to staging first** - Test all schema changes in staging
- ✅ **Then deploy to production** - After validation, deploy to production
- ✅ **Safety first** - Catch migration issues before production

### Metrics Collection (Data)
- ✅ **Always write to production** - All metrics data goes to production Supabase
- ✅ **Real data** - Track actual development metrics, not test data
- ✅ **Single source of truth** - All metrics in one place

## Rationale

### Why Staging for Schema?

1. **Safety**: Test migrations before production
2. **Validation**: Verify RLS policies, functions, constraints work
3. **Rollback**: Easy to fix issues in staging before production
4. **Consistency**: Matches existing deployment pattern (feature flags, moods)

### Why Production for Data?

1. **Accuracy**: Metrics should reflect real development activity
2. **Completeness**: All PRs, deployments, tests in one place
3. **Simplicity**: No need to aggregate across environments
4. **Value**: Real metrics are more useful than test metrics

## Implementation

### Workflow Configuration

**Schema Deployment** (`.github/workflows/supabase-schema-deploy.yml`):
- ✅ Deploys to staging on `develop` branch
- ✅ Deploys to production on `main` branch
- ✅ Always goes through staging first

**Metrics Collection** (`.github/workflows/record-metrics.yml`):
- ✅ Always uses `SUPABASE_PROJECT_REF` (production)
- ✅ Always uses `SUPABASE_SECRET_KEY` (production)
- ✅ No branch-based environment switching

### Code Changes

**Before** (branch-based):
```yaml
# Used staging for PRs, production for main
if [ "$BRANCH" = "main" ]; then
  PROJECT_REF="${{ secrets.SUPABASE_PROJECT_REF }}"
else
  PROJECT_REF="${{ secrets.SUPABASE_PROJECT_REF_STAGING }}"
fi
```

**After** (always production):
```yaml
# Always use production for metrics data
PROJECT_REF="${{ secrets.SUPABASE_PROJECT_REF }}"
SUPABASE_SECRET_KEY="${{ secrets.SUPABASE_SECRET_KEY }}"
```

## Deployment Flow

### Initial Setup

1. **Deploy schema to staging**:
   ```bash
   git push origin feature/ai-optimization-research:develop
   # Triggers: supabase-schema-deploy.yml → staging
   ```

2. **Verify staging deployment**:
   - Check GitHub Actions workflow
   - Verify tables in Supabase staging dashboard
   - Test metrics collection (optional, but schema is ready)

3. **Deploy schema to production**:
   ```bash
   git push origin develop:main
   # Triggers: supabase-schema-deploy.yml → production
   ```

4. **Metrics collection starts**:
   - Next PR/workflow_run triggers `record-metrics.yml`
   - Metrics written to production Supabase
   - Data appears in production metrics tables

### Future Changes

**Schema changes** (new tables, columns, functions):
1. Deploy to staging first
2. Validate in staging
3. Deploy to production

**Metrics collection** (workflow changes, new metrics):
- No deployment needed (just workflow changes)
- Metrics continue writing to production

## Benefits

### Safety
- ✅ Schema changes tested in staging first
- ✅ Catch migration issues before production
- ✅ Easy rollback if needed

### Accuracy
- ✅ Real metrics data (not test data)
- ✅ Complete picture of development activity
- ✅ Single source of truth

### Simplicity
- ✅ Clear separation: schema vs data
- ✅ No environment confusion
- ✅ Consistent workflow

## Verification

### Schema Deployment
- ✅ Check staging Supabase dashboard for tables
- ✅ Verify RLS policies are active
- ✅ Test PostgREST API access (optional)

### Metrics Collection
- ✅ Check production Supabase dashboard for data
- ✅ Verify metrics appear after PR/workflow_run
- ✅ Query metrics tables to verify data

## Related Documentation

- `docs/architecture/METRICS_DEPLOYMENT_STRATEGY.md` - Deployment strategy details
- `docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md` - Architecture overview
- `.github/workflows/supabase-schema-deploy.yml` - Schema deployment workflow
- `.github/workflows/record-metrics.yml` - Metrics collection workflow

