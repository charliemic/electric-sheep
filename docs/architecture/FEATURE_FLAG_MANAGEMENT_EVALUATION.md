# Feature Flag Management Evaluation

**Date**: 2025-11-20  
**Status**: Evaluation & Recommendations

## Problem Statement

The feature flag sync script was failing when attempting to sync flags that already exist in Supabase. The script was using POST with `Prefer: resolution=merge-duplicates`, but this approach doesn't work correctly with PostgREST when targeting specific records via URL filters.

## Current Implementation Analysis

### Issues Identified

1. **PostgREST Upsert Limitations**:
   - POST with `Prefer: resolution=merge-duplicates` doesn't work when using URL filters (`?key=eq.$KEY`)
   - This header only works when posting to the table root without filters
   - Results in HTTP 409 (Conflict) errors for existing flags

2. **No Retry Logic**:
   - Script fails immediately on first error
   - Doesn't attempt alternative approaches

3. **Limited Error Handling**:
   - Doesn't distinguish between "flag exists" vs "other errors"
   - Doesn't provide actionable error messages

### Current Fix Applied

**Solution**: Use PATCH-then-POST pattern:
1. Try PATCH to update existing flag
2. If 404 (not found), use POST to insert new flag
3. Handle other errors appropriately

**Benefits**:
- ✅ Works reliably with PostgREST
- ✅ Handles both insert and update cases
- ✅ Clear error messages

## Alternative Approaches Evaluated

### Option 1: Direct PostgreSQL Connection (Current - Recommended)

**How it works**:
- Use `psql` with direct database connection
- Execute SQL with `ON CONFLICT DO UPDATE` (native upsert)

**Pros**:
- ✅ Native PostgreSQL upsert support
- ✅ Atomic operations
- ✅ Better performance
- ✅ More reliable
- ✅ Already implemented in script

**Cons**:
- ❌ Requires database connection string
- ❌ More complex setup (needs password/credentials)

**Recommendation**: **Use this as primary method** when `SUPABASE_DB_URL` is available.

### Option 2: PostgREST PATCH-then-POST (Current Fix)

**How it works**:
- Try PATCH to update existing record
- If 404, use POST to insert new record

**Pros**:
- ✅ Works with PostgREST API
- ✅ No direct database access needed
- ✅ Uses service role key (simpler auth)

**Cons**:
- ❌ Two API calls in worst case
- ❌ Not atomic (race condition possible)
- ❌ Slightly slower

**Recommendation**: **Use as fallback** when `SUPABASE_DB_URL` is not available.

### Option 3: Supabase Management API

**How it works**:
- Use Supabase Management API for project management
- Automate flag creation/updates via API

**Pros**:
- ✅ Official Supabase approach
- ✅ Better integration with Supabase ecosystem
- ✅ Supports project-level operations

**Cons**:
- ❌ Requires Management API access token
- ❌ More complex setup
- ❌ May not support all operations we need

**Recommendation**: **Consider for future** if we need more advanced project management features.

### Option 4: Database Functions/Stored Procedures

**How it works**:
- Create PostgreSQL function for upsert logic
- Call function via PostgREST or direct connection

**Pros**:
- ✅ Atomic operations
- ✅ Reusable logic
- ✅ Can handle complex validation

**Cons**:
- ❌ Requires database migration
- ❌ More complex to maintain
- ❌ Harder to debug

**Recommendation**: **Consider for future** if we need complex validation or business logic.

### Option 5: Supabase Edge Functions

**How it works**:
- Create Edge Function for flag management
- Handle upsert logic in TypeScript/Deno

**Pros**:
- ✅ Server-side logic
- ✅ Can add validation/authorization
- ✅ Better error handling

**Cons**:
- ❌ More complex architecture
- ❌ Additional deployment step
- ❌ Overkill for simple upserts

**Recommendation**: **Not needed** for current use case. Consider if we need:
- Complex validation
- A/B testing logic
- Percentage rollouts
- Advanced targeting

## Recommended Approach

### Primary: Direct PostgreSQL Connection

**When to use**:
- CI/CD pipelines (have database credentials)
- Local development (can use connection string)
- Production deployments

**Implementation**:
```bash
export SUPABASE_DB_URL="postgresql://user:password@host:port/database"
./scripts/sync-feature-flags.sh feature-flags/flags.yaml
```

### Fallback: PostgREST PATCH-then-POST

**When to use**:
- When `SUPABASE_DB_URL` is not available
- Using only API keys (no database access)
- Quick manual updates

**Implementation**:
```bash
export SUPABASE_URL="https://project.supabase.co"
export SUPABASE_SERVICE_ROLE_KEY="service_role_key"
./scripts/sync-feature-flags.sh feature-flags/flags.yaml
```

## Improvements Made

### 1. Fixed PostgREST Upsert Logic

**Before**:
```bash
# POST with merge-duplicates (doesn't work with filters)
curl -X POST \
  -H "Prefer: resolution=merge-duplicates" \
  "$URL/rest/v1/feature_flags?key=eq.$KEY"
```

**After**:
```bash
# Try PATCH (update), then POST (insert) if 404
curl -X PATCH "$URL/rest/v1/feature_flags?key=eq.$KEY"  # Update
# If 404, then:
curl -X POST "$URL/rest/v1/feature_flags"  # Insert
```

### 2. Better Error Handling

- Distinguishes between "not found" (404) and other errors
- Provides clear error messages
- Continues processing other flags even if one fails

### 3. Verbose Mode

- Added `--verbose` flag for debugging
- Shows detailed output for troubleshooting

## Future Improvements

### Short Term

1. **Add Retry Logic**:
   - Retry failed operations with exponential backoff
   - Handle transient network errors

2. **Batch Operations**:
   - Process multiple flags in single transaction
   - Reduce API calls

3. **Validation**:
   - Validate flag structure before syncing
   - Check for required fields
   - Validate value types

### Medium Term

1. **Dry Run Mode**:
   - `--dry-run` flag to preview changes
   - Show what would be updated without making changes

2. **Diff View**:
   - Show differences between YAML and database
   - Highlight what will change

3. **Rollback Support**:
   - Store previous flag values
   - Ability to rollback to previous state

### Long Term

1. **Feature Flag Management UI**:
   - Web interface for managing flags
   - Visual diff and preview
   - Approval workflow

2. **A/B Testing Support**:
   - Percentage rollouts
   - User segmentation
   - Experiment tracking

3. **Audit Logging**:
   - Track all flag changes
   - Who changed what and when
   - Change history

## Best Practices

### 1. Always Use Direct Database Connection in CI/CD

**Why**: More reliable, atomic operations, better performance

**How**: Set `SUPABASE_DB_URL` in GitHub Secrets

### 2. Validate Before Deploying

**Why**: Catch errors early, prevent bad deployments

**How**: Run validation step before sync

### 3. Use Feature Branches

**Why**: Test flag changes before merging to main

**How**: Create feature branch, test, then merge

### 4. Document Flag Changes

**Why**: Track why flags were added/changed

**How**: Include description in commit messages

### 5. Monitor Flag Usage

**Why**: Identify unused flags, track adoption

**How**: Add logging/metrics for flag checks

## Testing Recommendations

### Unit Tests

- Test upsert logic for each value type
- Test error handling
- Test edge cases (null values, special characters)

### Integration Tests

- Test against real Supabase instance
- Test both PostgreSQL and PostgREST paths
- Test concurrent updates

### Manual Testing

- Test sync with existing flags
- Test sync with new flags
- Test error scenarios

## Conclusion

The current fix (PATCH-then-POST) resolves the immediate issue and provides a reliable fallback when direct database access isn't available. However, **direct PostgreSQL connection should be preferred** for CI/CD pipelines due to its reliability and performance.

The script now handles both insert and update cases correctly, with clear error messages and better debugging support.

## Related Documentation

- `scripts/sync-feature-flags.sh` - Sync script implementation
- `.github/workflows/supabase-feature-flags-deploy.yml` - CI/CD workflow
- `docs/architecture/FEATURE_FLAGS_IMPLEMENTATION.md` - Implementation details
- `feature-flags/README.md` - Feature flag usage guide

