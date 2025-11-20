# Feature Flag Deployment Notes

**Date**: 2025-11-20  
**Status**: Active

## Connection Method

### Why Not Direct Database Connection?

**Answer**: We **do** use direct database connection when available. The workflow now constructs `SUPABASE_DB_URL` and the sync script prefers it over PostgREST API.

**Not IPv6 Related**: The reason for using PostgREST API as fallback is **not** due to IPv6 limitations. GitHub Actions runners support both IPv4 and IPv6. The previous limitation was:
1. **Configuration**: `SUPABASE_DB_URL` wasn't being set in the workflow
2. **Connection String Format**: Previous attempts had incorrect connection string format
3. **Fallback Strategy**: Script falls back to PostgREST API when DB connection unavailable

### Current Approach

**Primary**: Direct PostgreSQL Connection
- Uses Supabase connection pooler (port 6543)
- Format: `postgresql://postgres.[PROJECT_REF]:[PASSWORD]@aws-0-[REGION].pooler.supabase.com:6543/postgres`
- More reliable, atomic operations, better performance

**Fallback**: PostgREST API
- Used when `SUPABASE_DB_URL` is not available or connection fails
- Uses PATCH-then-POST pattern for upserts
- Slightly slower but works reliably

### IPv6 Support

GitHub Actions runners support both IPv4 and IPv6. Supabase connection pooler uses IPv4 addresses, so IPv6 is not a factor in this decision.

## Workflow Configuration

The workflow now:
1. Constructs `SUPABASE_DB_URL` from GitHub secrets
2. Passes it to the sync script
3. Script uses direct connection if available, falls back to API if not

## Verification

To verify flags are set in Supabase staging:

1. **Check workflow logs**: Look for "✓ Synced" or "✓ Updated" messages
2. **Query Supabase directly**: Use Supabase CLI or dashboard
3. **Check app**: Feature flag should be available in app when enabled

## Related Documentation

- `docs/architecture/FEATURE_FLAG_MANAGEMENT_EVALUATION.md` - Detailed evaluation
- `scripts/sync-feature-flags.sh` - Sync script implementation
- `.github/workflows/supabase-feature-flags-deploy.yml` - CI/CD workflow

