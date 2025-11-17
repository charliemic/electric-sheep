# Supabase Setup and Configuration

This document describes how Supabase is configured and managed in this project.

## Overview

Supabase configuration is managed in the `supabase/` directory and version controlled alongside the app code. This allows:
- **Infrastructure as Code**: All database schema changes are tracked in git
- **Reproducible Deployments**: Same migrations run in dev, staging, and production
- **Parallel CI/CD**: Separate pipelines for app and Supabase deployments
- **Team Collaboration**: Everyone can see and review database changes

## Directory Structure

```
supabase/
├── migrations/          # Database migrations (SQL files)
│   └── YYYYMMDDHHMMSS_description.sql
├── config.toml          # Supabase project configuration
├── seed.sql             # Optional seed data
└── README.md            # Setup instructions
```

## Supabase CLI

The Supabase CLI is used for:
- **Local Development**: Run Supabase locally with Docker
- **Migration Management**: Create and apply database migrations
- **Remote Deployment**: Push migrations to cloud projects
- **Project Linking**: Connect local config to remote projects

### Installation

```bash
# macOS
brew install supabase/tap/supabase

# Verify
supabase --version
```

## Workflow

### Local Development

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

2. **Create a migration**:
   ```bash
   supabase migration new create_moods_table
   ```
   Creates: `supabase/migrations/20241117120000_create_moods_table.sql`

3. **Edit the migration file** with your SQL

4. **Test locally**:
   ```bash
   supabase db reset  # Applies all migrations
   ```

5. **Link to remote project** (when ready):
   ```bash
   supabase login
   supabase link --project-ref your-project-ref
   ```

### Deployment

#### Manual Deployment
```bash
# Push migrations to linked project
supabase db push
```

#### Automated Deployment (CI/CD)

The `.github/workflows/supabase-deploy.yml` workflow:
- **Validates** migrations on every push
- **Deploys to staging** when pushing to `develop` branch
- **Deploys to production** when pushing to `main` branch
- **Previews** migrations on pull requests

## Migration Best Practices

1. **Naming Convention**: `YYYYMMDDHHMMSS_description.sql`
   - Example: `20241117120000_create_moods_table.sql`
   - Timestamp ensures correct ordering

2. **Idempotency**: Use `IF NOT EXISTS` and `IF EXISTS` where possible
   ```sql
   CREATE TABLE IF NOT EXISTS moods (...);
   CREATE INDEX IF NOT EXISTS idx_moods_user_id ON moods(user_id);
   ```

3. **Rollback Consideration**: Document how to rollback if needed
   ```sql
   -- Rollback: DROP TABLE IF EXISTS moods;
   ```

4. **RLS Policies**: Always include Row-Level Security policies
   ```sql
   ALTER TABLE moods ENABLE ROW LEVEL SECURITY;
   CREATE POLICY "Users can view own moods" ON moods FOR SELECT USING (auth.uid() = user_id);
   ```

5. **Indexes**: Add indexes for performance-critical queries
   ```sql
   CREATE INDEX idx_moods_user_id ON moods(user_id);
   CREATE INDEX idx_moods_timestamp ON moods(timestamp DESC);
   ```

## Environment Management

### Local Development
Create `.env.local` (not tracked in git):
```bash
SUPABASE_URL=http://localhost:54321
SUPABASE_ANON_KEY=your-local-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-local-service-role-key
```

### CI/CD Secrets
Configure in GitHub Settings → Secrets:
- `SUPABASE_ACCESS_TOKEN`: For CLI authentication
- `SUPABASE_PROJECT_REF_STAGING`: Staging project reference
- `SUPABASE_PROJECT_REF_PROD`: Production project reference
- `SUPABASE_DB_PASSWORD`: Database password (if needed)

## Database Schema

### Current Schema

**moods table**:
- `id` (UUID, primary key)
- `user_id` (UUID, foreign key to auth.users)
- `score` (INTEGER, 1-10)
- `timestamp` (BIGINT)
- `created_at` (BIGINT, nullable)
- `updated_at` (BIGINT, nullable)

**Indexes**:
- `idx_moods_user_id` on `user_id`
- `idx_moods_timestamp` on `timestamp DESC`

**RLS Policies**:
- Users can only SELECT their own moods
- Users can only INSERT moods with their own user_id
- Users can only UPDATE their own moods
- Users can only DELETE their own moods

## Authentication Setup

Supabase Auth is configured via:
1. **Dashboard**: https://supabase.com/dashboard → Authentication
2. **CLI**: `supabase auth` commands
3. **Config**: `config.toml` auth settings

### Auth Configuration
- Email/password enabled
- Signup enabled
- Email confirmations: Disabled (for development)
- JWT expiry: 3600 seconds (1 hour)

## CI/CD Pipeline

### App Pipeline (`.github/workflows/ci.yml`)
- Runs on app code changes
- Ignores `supabase/` directory changes
- Builds, tests, lints Android app

### Supabase Pipeline (`.github/workflows/supabase-deploy.yml`)
- Runs on `supabase/` directory changes
- Validates migrations
- Deploys to staging (develop branch)
- Deploys to production (main branch)
- Previews migrations (pull requests)

### Parallel Execution
Both pipelines can run simultaneously:
- App changes → CI pipeline runs
- Supabase changes → Supabase pipeline runs
- Both change → Both pipelines run in parallel

## Troubleshooting

### Migration Conflicts
If migrations conflict:
```bash
# Check migration status
supabase migration list

# Reset local database
supabase db reset

# Check remote status
supabase db remote commit
```

### Local Supabase Issues
```bash
# Stop and restart
supabase stop
supabase start

# Check logs
supabase logs
```

### CI/CD Failures
- Check GitHub Secrets are configured
- Verify project refs are correct
- Check Supabase access token is valid
- Review workflow logs for specific errors

## Resources

- [Supabase CLI Documentation](https://supabase.com/docs/guides/cli)
- [Database Migrations Guide](https://supabase.com/docs/guides/cli/local-development#database-migrations)
- [Row-Level Security](https://supabase.com/docs/guides/auth/row-level-security)
- [Supabase Auth](https://supabase.com/docs/guides/auth)

