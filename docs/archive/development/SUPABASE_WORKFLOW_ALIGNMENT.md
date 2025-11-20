# Supabase Workflow Alignment

## Reference Documentation
https://supabase.com/docs/guides/deployment/managing-environments

## Implementation Pattern

Our workflows follow the Supabase official pattern for environment management:

### Schema Deployments (`supabase-schema-deploy.yml`)
- Uses `supabase link --project-ref $SUPABASE_PROJECT_ID`
- Uses `supabase db push` for migrations
- Matches docs pattern exactly

### Feature Flag Deployments (`supabase-feature-flags-deploy.yml`)
- Uses `supabase link --project-ref $SUPABASE_PROJECT_ID`
- Uses PostgREST API with secret keys (not direct DB connection)
- Required because RLS policies require service_role authentication

## Environment Variables

### Required (per Supabase docs)
- `SUPABASE_ACCESS_TOKEN` - Personal access token
- `SUPABASE_DB_PASSWORD` - Database password (env-specific: `STAGING_DB_PASSWORD`, `PRODUCTION_DB_PASSWORD`)
- `SUPABASE_PROJECT_ID` - Project reference (env-specific: `STAGING_PROJECT_ID`, `PRODUCTION_PROJECT_ID`)

### Additional (for feature flags only)
- `SUPABASE_SECRET_KEY_STAGING` - Secret key for staging (bypasses RLS)
- `SUPABASE_SECRET_KEY` - Secret key for production (bypasses RLS)

## GitHub Secrets Mapping

**Staging:**
- `SUPABASE_ACCESS_TOKEN` → `SUPABASE_ACCESS_TOKEN`
- `SUPABASE_DB_PASSWORD_STAGING` → `SUPABASE_DB_PASSWORD`
- `SUPABASE_PROJECT_REF_STAGING` → `SUPABASE_PROJECT_ID`
- `SUPABASE_SECRET_KEY_STAGING` → `SUPABASE_SERVICE_ROLE_KEY` (for feature flags)

**Production:**
- `SUPABASE_ACCESS_TOKEN` → `SUPABASE_ACCESS_TOKEN`
- `SUPABASE_DB_PASSWORD` → `SUPABASE_DB_PASSWORD`
- `SUPABASE_PROJECT_REF` → `SUPABASE_PROJECT_ID`
- `SUPABASE_SECRET_KEY` → `SUPABASE_SERVICE_ROLE_KEY` (for feature flags)

## Why PostgREST API for Feature Flags?

Feature flags use PostgREST API instead of direct DB connection because:
1. **RLS Requirements**: RLS policies require `service_role` JWT authentication
2. **IPv6 Issues**: Direct connections use IPv6 only, which GitHub Actions doesn't support
3. **SDK Pattern**: PostgREST API is the recommended Supabase SDK pattern
