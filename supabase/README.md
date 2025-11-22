# Supabase Configuration

This directory contains all Supabase configuration, migrations, and infrastructure as code.

## Structure

```
supabase/
├── migrations/          # Database migrations (SQL files)
├── config.toml          # Supabase project configuration
├── seed.sql             # Seed data for development
└── README.md            # This file
```

## Prerequisites

Install Supabase CLI:
```bash
# macOS
brew install supabase/tap/supabase

# Or using npm
npm install -g supabase

# Verify installation
supabase --version
```

## Setup

### 1. Link to Supabase Project

```bash
# Login to Supabase
supabase login

# Link to your project (get project ref from Supabase dashboard)
supabase link --project-ref your-project-ref
```

### 2. Initialize Local Development

```bash
# Start local Supabase (Docker required)
supabase start

# This creates:
# - Local PostgreSQL database
# - Local Supabase API
# - Local Auth service
# - Local Storage service
```

### 3. Create Migrations

```bash
# Create a new migration
supabase migration new create_moods_table

# This creates: supabase/migrations/YYYYMMDDHHMMSS_create_moods_table.sql
```

## Development Workflow

### Local Development
1. Make changes to migrations in `supabase/migrations/`
2. Test locally: `supabase start` (runs all migrations)
3. Reset if needed: `supabase db reset`

### Deploy to Remote
```bash
# Push migrations to remote Supabase project
supabase db push

# Or deploy specific migration
supabase migration up
```

## Version Control

All Supabase configuration is version controlled:
- ✅ **Migrations**: SQL files in `supabase/migrations/`
- ✅ **Config**: `config.toml` (project settings)
- ✅ **Seed Data**: `seed.sql` (optional)
- ❌ **Not tracked**: `.env` files (use GitHub Secrets for CI/CD)

## CI/CD Integration

See `.github/workflows/supabase-schema-deploy.yml` for automated deployment.

The pipeline:
1. Runs on changes to `supabase/` directory
2. Validates migrations
3. Deploys to staging/production based on branch
4. Runs tests against deployed schema

## Environment Variables

For local development, create `.env.local` (not tracked):
```bash
SUPABASE_URL=http://localhost:54321
SUPABASE_ANON_KEY=your-local-anon-key
SUPABASE_SERVICE_ROLE_KEY=your-local-service-role-key
```

For CI/CD, use GitHub Secrets:
- `SUPABASE_PROJECT_REF`
- `SUPABASE_ACCESS_TOKEN`
- `SUPABASE_DB_PASSWORD` (if needed)

## Database Schema

Current schema:
- `moods` table (see migrations for structure)
- Row-Level Security (RLS) policies
- Indexes for performance

## Authentication

Supabase Auth is configured via:
- Dashboard: https://supabase.com/dashboard
- CLI: `supabase auth` commands
- Config: `config.toml` auth settings

## Resources

- [Supabase CLI Docs](https://supabase.com/docs/guides/cli)
- [Database Migrations](https://supabase.com/docs/guides/cli/local-development#database-migrations)
- [Row-Level Security](https://supabase.com/docs/guides/auth/row-level-security)

