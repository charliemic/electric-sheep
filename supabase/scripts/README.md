# Supabase Scripts

Executable scripts for managing Supabase test data and operations.

## Scripts

### create-test-users.sh

Creates test users in Supabase Auth via Admin API.

**Prerequisites**:
- `SUPABASE_URL` environment variable
- `SUPABASE_SERVICE_ROLE_KEY` environment variable (service role key, not anon key)

**Usage**:
```bash
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_SERVICE_ROLE_KEY=sb_secret_xxx
./create-test-users.sh
```

**Features**:
- Idempotent (checks if user exists before creating)
- Creates all 8 test users (2 tech levels × 4 mood patterns)
- Sets default password: `test-password-123` (override with `TEST_PASSWORD` env var)

### seed-test-data.sh

Runs all seed scripts in order: creates users and loads baseline data.

**Prerequisites**:
- Supabase CLI installed and configured
- `supabase link` must be run first
- `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` for user creation

**Usage**:
```bash
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_SERVICE_ROLE_KEY=sb_secret_xxx
supabase link --project-ref xxx
./seed-test-data.sh
```

**What it does**:
1. Creates test users (via Admin API)
2. Loads baseline mood data (30 days, via SQL)

### add-daily-data.sh

Adds one day's worth of mood data for all test users (yesterday's data).

**Prerequisites**:
- Supabase CLI installed and configured
- `supabase link` must be run first
- `generate_mood_score()` function must exist

**Usage**:
```bash
supabase link --project-ref xxx
./add-daily-data.sh
```

**Features**:
- Idempotent (checks if yesterday's data exists before adding)
- Adds data for all 8 test users
- Generates realistic mood scores based on each user's pattern

**When to run**:
- Manually: For testing or recovery
- Automatically: Via nightly CI/CD job (`.github/workflows/test-data-nightly-update.yml`)

## Environment Variables

### Required for User Creation

- `SUPABASE_URL`: Your Supabase project URL (e.g., `https://xxx.supabase.co`)
- `SUPABASE_SERVICE_ROLE_KEY`: Service role key (starts with `sb_secret_...`)
  - Get from: Supabase Dashboard → Settings → API → Project API keys → `secret` key
  - ⚠️ **Never commit this key** - use GitHub Secrets in CI/CD

### Optional

- `TEST_PASSWORD`: Password for test users (default: `test-password-123`)

## CI/CD Integration

The nightly data update job (`.github/workflows/test-data-nightly-update.yml`) runs `add-daily-data.sh` automatically:

- **Schedule**: Daily at 2 AM UTC
- **Manual trigger**: Available via `workflow_dispatch`
- **Environment**: Uses staging environment secrets

## Security Notes

- ✅ Service role keys are stored in GitHub Secrets
- ✅ Scripts check for existing data/users (idempotent)
- ✅ Test users use dedicated email domain (`@electric-sheep.test`)
- ❌ Never commit service role keys to repository
- ❌ Never use test users in production

## Troubleshooting

### "User does not exist" warnings

Run `create-test-users.sh` first to create test users.

### "Supabase is not linked" error

Run `supabase link --project-ref <your-project-ref>` first.

### "generate_mood_score function not found"

Run the function creation script:
```bash
supabase db execute < supabase/seed/functions/generate_mood_score.sql
```

### Permission errors

Ensure you're using the **service role key** (not anon key) for Admin API operations.

## Related Documentation

- `supabase/seed/` - Seed SQL scripts
- `.github/workflows/test-data-nightly-update.yml` - Nightly update workflow
- `docs/architecture/TEST_FIXTURES_ARCHITECTURE.md` - Architecture documentation



