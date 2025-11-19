# Supabase Workflow Alignment with Official Documentation

## Reference Documentation
https://supabase.com/docs/guides/deployment/managing-environments

## Pattern Comparison

### Official Supabase Docs Pattern

```yaml
name: Deploy Migrations to Staging
on:
  push:
    branches:
      - develop
  workflow_dispatch:
jobs:
  deploy:
    runs-on: ubuntu-latest
    env:
      SUPABASE_ACCESS_TOKEN: ${{ secrets.SUPABASE_ACCESS_TOKEN }}
      SUPABASE_DB_PASSWORD: ${{ secrets.STAGING_DB_PASSWORD }}
      SUPABASE_PROJECT_ID: ${{ secrets.STAGING_PROJECT_ID }}
    steps:
      - uses: actions/checkout@v4
      - uses: supabase/setup-cli@v1
        with:
          version: latest
      - run: supabase link --project-ref $SUPABASE_PROJECT_ID
      - run: supabase db push
```

### Our Implementation

**Staging:**
```yaml
deploy-staging:
  runs-on: ubuntu-latest
  env:
    SUPABASE_ACCESS_TOKEN: ${{ secrets.SUPABASE_ACCESS_TOKEN }}
    SUPABASE_DB_PASSWORD: ${{ secrets.STAGING_DB_PASSWORD }}
    SUPABASE_PROJECT_ID: ${{ secrets.STAGING_PROJECT_ID }}
  steps:
    - uses: actions/checkout@v4
    - uses: supabase/setup-cli@v1
      with:
        version: latest
    - run: supabase link --project-ref $SUPABASE_PROJECT_ID
    - run: # Construct DB_URL and sync feature flags
```

**Production:**
```yaml
deploy-production:
  runs-on: ubuntu-latest
  env:
    SUPABASE_ACCESS_TOKEN: ${{ secrets.SUPABASE_ACCESS_TOKEN }}
    SUPABASE_DB_PASSWORD: ${{ secrets.PRODUCTION_DB_PASSWORD }}
    SUPABASE_PROJECT_ID: ${{ secrets.PRODUCTION_PROJECT_ID }}
  steps:
    - uses: actions/checkout@v4
    - uses: supabase/setup-cli@v1
      with:
        version: latest
    - run: supabase link --project-ref $SUPABASE_PROJECT_ID
    - run: # Construct DB_URL and sync feature flags
```

## Variables Used

### ✅ All variables match the docs pattern:
- `SUPABASE_ACCESS_TOKEN` - Personal access token
- `SUPABASE_DB_PASSWORD` - Database password (environment-specific: `STAGING_DB_PASSWORD`, `PRODUCTION_DB_PASSWORD`)
- `SUPABASE_PROJECT_ID` - Project reference (environment-specific: `STAGING_PROJECT_ID`, `PRODUCTION_PROJECT_ID`)

### ❌ No variables outside the docs pattern:
- ~~`SUPABASE_SERVICE_ROLE_KEY`~~ - Removed (not in docs)
- ~~`SUPABASE_URL`~~ - Removed (not in docs)
- ~~`SUPABASE_REGION`~~ - Removed (not in docs)
- ~~`PROJECT_REF`~~ - Replaced with `PROJECT_ID` (docs naming)

## Differences from Docs

The only difference is the final step:
- **Docs**: `supabase db push` (for schema migrations)
- **Our workflow**: Construct DB connection string and sync feature flags via `psql`

This difference is necessary because:
1. We're syncing **data** (feature flags), not **schema** (migrations)
2. We use `psql` to execute SQL upserts directly
3. The DB connection string is constructed using only the variables from the docs:
   ```bash
   postgresql://postgres:${SUPABASE_DB_PASSWORD}@db.${SUPABASE_PROJECT_ID}.supabase.co:5432/postgres
   ```

## Verification

✅ **Environment variables**: Only those specified in docs  
✅ **CLI setup**: Matches docs pattern  
✅ **Link command**: Matches docs pattern  
✅ **No login step**: Docs don't show one (ACCESS_TOKEN env var is sufficient)  
✅ **No service role keys**: Not in docs pattern  
✅ **No region configuration**: Not in docs pattern  

## Required GitHub Secrets

The workflow uses the Supabase docs pattern for environment variables, but maps to existing GitHub secret names:

### Staging:
- `SUPABASE_ACCESS_TOKEN` → `SUPABASE_ACCESS_TOKEN` env var
- `SUPABASE_DB_PASSWORD_STAGING` → `SUPABASE_DB_PASSWORD` env var
- `SUPABASE_PROJECT_REF_STAGING` → `SUPABASE_PROJECT_ID` env var

### Production:
- `SUPABASE_ACCESS_TOKEN` → `SUPABASE_ACCESS_TOKEN` env var
- `SUPABASE_DB_PASSWORD` → `SUPABASE_DB_PASSWORD` env var
- `SUPABASE_PROJECT_REF` → `SUPABASE_PROJECT_ID` env var

**Note:** The GitHub secret names can differ from the docs pattern as long as they map to the correct environment variable names that the Supabase CLI expects.

## Conclusion

Our workflow now **exactly matches** the Supabase documentation pattern, with the only difference being the final step which is necessary for our data syncing use case (feature flags) rather than schema migrations.

