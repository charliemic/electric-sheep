# Supabase Setup Guide

Complete guide for setting up and managing Supabase in the Electric Sheep project.

## Quick Start

1. **Choose Setup Type**: [Local Development](#local-development) or [Cloud Setup](#cloud-setup)
2. **Install Prerequisites**: [Supabase CLI](#supabase-cli) and [Docker](#docker-optional-for-local)
3. **Configure App**: Add credentials to `local.properties` (see [App Configuration](#app-configuration))

## Supabase CLI

### Installation

```bash
# macOS
brew install supabase/tap/supabase

# Verify
supabase --version
```

### Authentication

```bash
# Login to Supabase (opens browser)
supabase login

# Link to your project
supabase link --project-ref YOUR_PROJECT_REF
```

## Local Development

### Prerequisites
- Docker Desktop installed and running
- Supabase CLI installed

### Setup Steps

1. **Start local Supabase**:
   ```bash
   supabase start
   ```
   This starts:
   - PostgreSQL database (port 54322)
   - Supabase API (port 54321)
   - Auth service
   - Storage service
   - Studio UI (port 54323)

2. **Get local credentials**:
   ```bash
   supabase status
   ```
   Copy the `API URL` and `anon key` to `local.properties`

3. **Create migrations**:
   ```bash
   supabase migration new create_moods_table
   ```
   Edit the migration file in `supabase/migrations/`

4. **Test locally**:
   ```bash
   supabase db reset  # Applies all migrations
   ```

5. **Stop when done**:
   ```bash
   supabase stop
   ```

See [supabase/README.md](../../supabase/README.md) for detailed local setup.

## Cloud Setup

### Step-by-Step Guide

Follow the detailed guide: [CLOUD_SETUP_STEPS.md](CLOUD_SETUP_STEPS.md)

**Quick Summary**:
1. Get project reference ID from Supabase dashboard
2. Login: `supabase login`
3. Link: `supabase link --project-ref YOUR_PROJECT_REF`
4. Push migrations: `supabase db push`
5. Get credentials from dashboard → Settings → API
6. Add to `local.properties`

### App Configuration

Add Supabase credentials to `local.properties` (not committed to git):

```properties
supabase.url=https://your-project.supabase.co
supabase.anon.key=your-anon-key
```

See `local.properties.example` for template.

## Migration Management

### Creating Migrations

```bash
# Create new migration
supabase migration new description_of_change

# Edit the migration file
# File location: supabase/migrations/YYYYMMDDHHMMSS_description_of_change.sql
```

### Migration Best Practices

1. **Naming**: Use descriptive names: `create_moods_table`, `add_user_profile_column`
2. **Idempotency**: Use `IF NOT EXISTS` and `IF EXISTS`:
   ```sql
   CREATE TABLE IF NOT EXISTS moods (...);
   CREATE INDEX IF NOT EXISTS idx_moods_user_id ON moods(user_id);
   ```
3. **RLS Policies**: Always include Row-Level Security:
   ```sql
   ALTER TABLE moods ENABLE ROW LEVEL SECURITY;
   CREATE POLICY "Users can view own moods" ON moods FOR SELECT USING (auth.uid() = user_id);
   ```
4. **Indexes**: Add indexes for performance-critical queries
5. **Forward-Only**: Never modify existing migrations (create new ones)

### Testing Migrations

```bash
# Test locally
supabase db reset

# Check migration status
supabase migration list

# Verify in Studio UI
# Open: http://localhost:54323
```

## CI/CD Deployment

Migrations are automatically deployed via GitHub Actions:

- **Staging**: Deploys when pushing to `develop` branch
- **Production**: Deploys when pushing to `main` branch
- **PRs**: Validates migrations (no deployment)

See [CI_CD_MIGRATION_SETUP.md](CI_CD_MIGRATION_SETUP.md) for setup.

## Authentication Setup

### Google OAuth

1. **Google Cloud Console**: [GOOGLE_OAUTH_SETUP.md](GOOGLE_OAUTH_SETUP.md)
2. **Supabase Dashboard**: [SUPABASE_GOOGLE_CONFIG.md](SUPABASE_GOOGLE_CONFIG.md)
3. **Email Confirmation**: [SUPABASE_EMAIL_CONFIRMATION.md](SUPABASE_EMAIL_CONFIRMATION.md)

### Service Role Key

For server-side operations: [SERVICE_ROLE_SETUP.md](SERVICE_ROLE_SETUP.md)

## Directory Structure

```
supabase/
├── migrations/          # Database migrations (SQL files)
│   └── YYYYMMDDHHMMSS_description.sql
├── config.toml          # Supabase project configuration
└── README.md            # Additional setup instructions
```

## Troubleshooting

### Local Supabase Won't Start
```bash
# Check Docker is running
docker ps

# Reset Supabase
supabase stop
supabase start

# Check logs
supabase logs
```

### Migration Fails
- Check migration syntax
- Verify migration naming convention
- Test locally first: `supabase db reset`
- Check CI/CD logs for specific errors

### App Can't Connect
- Verify `local.properties` has correct URL and key
- Check BuildConfig values are populated
- Verify network connectivity
- Check logs for connection errors

## Resources

- [Supabase CLI Documentation](https://supabase.com/docs/guides/cli)
- [Database Migrations Guide](https://supabase.com/docs/guides/cli/local-development#database-migrations)
- [Row-Level Security](https://supabase.com/docs/guides/auth/row-level-security)
- [Supabase Auth](https://supabase.com/docs/guides/auth)

