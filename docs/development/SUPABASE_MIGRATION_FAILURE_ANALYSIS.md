# Supabase Migration Failure Analysis

## Summary

Reviewed both Supabase workflows on the `main` branch and identified issues with the feature flags deployment.

## Workflow Status

### Schema Deployment ✅ SUCCESS
- **Workflow**: Deploy Supabase Database Schema (Migrations)
- **Last Run**: Run #2 on main (2025-11-18T14:32:42Z)
- **Status**: ✅ Success
- **Migrations Applied**: All 3 migrations successfully deployed
  1. `20241117000000_create_moods_table.sql`
  2. `20251118130744_create_feature_flags_table.sql`
  3. `20251118140000_add_feature_flags_versioning.sql`

### Feature Flags Deployment ❌ FAILED
- **Workflow**: Deploy Feature Flag Values to Supabase
- **Last Run**: Run #2 on main (2025-11-18T14:32:42Z)
- **Status**: ❌ Failure
- **Failed Job**: Deploy to Production
- **Failed Step**: Sync feature flags

## Root Cause Analysis

### Issue 1: Silent Error Handling
**Problem**: The sync script was redirecting all error output to `/dev/null`, making it impossible to diagnose failures.

**Error Message**: 
```
✗ Failed: offline_only
✗ Failed: show_feature_flag_indicator
Sync complete: 0 succeeded, 2 failed
```

**Fix Applied**: 
- Updated script to capture and display `psql` error output
- Changed from `psql ... > /dev/null 2>&1` to capturing output and exit code
- Now displays actual error messages: `echo -e "${RED}Error output: $PSQL_OUTPUT${NC}"`

### Issue 2: Null Value Handling
**Problem**: The script was not properly handling `null` values from YAML for `segment_id` and `user_id`.

**YAML Format**:
```yaml
segment_id: null
user_id: null
```

**Issue**: When `yq` reads `null` from YAML, it returns the string `"null"`, not SQL `NULL`. The script was using `${SEGMENT_ID:-NULL}` which would use the literal string `"null"` in SQL, causing syntax errors.

**Fix Applied**:
- Added explicit null checking: `if [ "$SEGMENT_ID" = "null" ] || [ -z "$SEGMENT_ID" ] || [ "$SEGMENT_ID" = "\"\"" ]`
- Sets `SEGMENT_ID_SQL="NULL"` (SQL NULL) instead of `'null'` (string)
- Same fix for `USER_ID`

## Potential Additional Issues

### Database Connection
The workflow constructs the database URL as:
```bash
DB_URL="postgresql://postgres.${PROJECT_REF}:${DB_PASSWORD}@aws-0-eu-central-1.pooler.supabase.com:6543/postgres"
```

**Potential Issues**:
1. **Hardcoded region**: Uses `aws-0-eu-central-1` - may not match all Supabase projects
2. **Pooler port**: Uses port `6543` (transaction mode) - should verify this is correct
3. **Table existence**: Need to verify `feature_flags` table exists in production (migration may not have run yet)

### Migration Order
The feature flags deployment runs **after** the schema deployment, but they may run in parallel. If the feature flags workflow starts before the schema migration completes, the table won't exist yet.

**Recommendation**: Add a dependency or check to ensure migrations are complete before syncing flags.

## Fixes Applied

1. ✅ **Error visibility**: Script now shows actual `psql` error messages
2. ✅ **Null handling**: Properly converts YAML `null` to SQL `NULL`
3. ✅ **Better debugging**: Error output is displayed in workflow logs

## Next Steps

1. **Trigger workflow manually** to test the fixes:
   ```bash
   gh workflow run "Deploy Feature Flag Values to Supabase" --ref main
   ```

2. **Verify table exists** in production before syncing:
   - Check if `feature_flags` table exists
   - Verify RLS policies are in place

3. **Add workflow dependency** (optional):
   - Make feature flags deployment depend on schema deployment completion
   - Or add a check to verify table exists before syncing

4. **Improve database URL construction**:
   - Use Supabase API to get actual database host/region
   - Or make region configurable via secrets

## Testing

After fixes are deployed, the next run should:
- Display actual error messages if failures occur
- Properly handle null values in SQL
- Show which specific error caused the failure

