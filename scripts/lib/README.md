# Supabase Script Libraries

Shared libraries for Supabase operations, providing reusable abstractions for common patterns.

## Libraries

### supabase-postgrest.sh

Provides functions for PostgREST API operations (same pattern as feature flags).

**Functions**:
- `postgrest_validate_env()` - Validate SUPABASE_URL and SUPABASE_SECRET_KEY
- `postgrest_upsert(table, json_payload, filter_query, verbose)` - Upsert via PATCH-then-POST pattern
- `postgrest_insert(table, json_payload, verbose)` - Insert a record
- `postgrest_query(table, filter_query, select_columns)` - Query records

**Usage**:
```bash
source scripts/lib/supabase-postgrest.sh

# Upsert a mood entry
JSON='{"user_id":"xxx","score":8,"timestamp":1234567890}'
postgrest_upsert "moods" "$JSON" "user_id=eq.xxx&timestamp=eq.1234567890"
echo "Result: $POSTGREST_RESULT"  # "updated", "inserted", or "failed"
```

### supabase-auth-admin.sh

Provides functions for Supabase Auth Admin API operations.

**Functions**:
- `auth_admin_validate_env()` - Validate SUPABASE_URL and SUPABASE_SECRET_KEY
- `auth_admin_user_exists(user_id, email)` - Check if user exists
- `auth_admin_create_user(user_id, email, password, display_name)` - Create a user

**Usage**:
```bash
source scripts/lib/supabase-auth-admin.sh

# Check if user exists
if auth_admin_user_exists "user-id" "user@example.com"; then
    echo "User exists"
fi

# Create user
auth_admin_create_user "user-id" "user@example.com" "password123" "Display Name"
echo "Created user ID: $AUTH_ADMIN_USER_ID"
```

## Environment Variables

Both libraries require:
- `SUPABASE_URL` - Supabase project URL (e.g., `https://xxx.supabase.co`)
- `SUPABASE_SECRET_KEY` - Secret key (starts with `sb_secret_...`)

**Note**: Use `SUPABASE_SECRET_KEY` (not `SUPABASE_SERVICE_ROLE_KEY`) for consistency with feature flags workflow.

## Pattern Consistency

These libraries follow the same patterns as `scripts/sync-feature-flags.sh`:
- ✅ Use PostgREST API for data operations
- ✅ Use PATCH-then-POST pattern for upserts
- ✅ Use `SUPABASE_SECRET_KEY` environment variable
- ✅ Idempotent operations (safe to run multiple times)

## Related Documentation

- `scripts/sync-feature-flags.sh` - Feature flags sync (reference implementation)
- `.github/workflows/supabase-feature-flags-deploy.yml` - Feature flags workflow
- `.github/workflows/test-data-nightly-update.yml` - Test data workflow



