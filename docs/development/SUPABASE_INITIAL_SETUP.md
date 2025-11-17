# Supabase Initial Setup Guide

This guide walks you through the initial Supabase setup for Electric Sheep.

## Prerequisites

✅ Supabase CLI installed (`supabase --version` should work)
✅ Docker Desktop installed and running (for local development)

## Setup Options

You have two options:

### Option 1: Supabase Cloud (Recommended for Production)

**Pros:**
- Free tier available
- No local setup required
- Production-ready
- Automatic backups
- Easy to share with team

**Cons:**
- Requires internet connection
- Free tier has limits (but generous)

### Option 2: Local Supabase (Development Only)

**Pros:**
- Works offline
- Fast iteration
- No account needed

**Cons:**
- Requires Docker
- Data lost on restart (unless persisted)
- Not suitable for production

## Step-by-Step Setup

### For Supabase Cloud

1. **Create Supabase Account**
   - Visit: https://supabase.com/dashboard
   - Sign up (GitHub, Google, or email)

2. **Create New Project**
   - Click "New Project"
   - Choose organization
   - Enter project details:
     - **Name**: `electric-sheep` (or your choice)
     - **Database Password**: Save this securely!
     - **Region**: Choose closest to you
   - Wait for project to be created (~2 minutes)

3. **Get Project Reference**
   - In project dashboard, go to Settings → General
   - Copy the **Reference ID** (looks like: `abcdefghijklmnop`)

4. **Link Project Locally**
   ```bash
   # Login to Supabase CLI
   supabase login
   
   # Link to your project
   supabase link --project-ref YOUR_PROJECT_REF
   ```

5. **Get Credentials**
   - In Supabase dashboard: Settings → API
   - Copy:
     - **Project URL**: `https://YOUR_PROJECT_REF.supabase.co`
     - **anon/public key**: (starts with `eyJ...`)

6. **Apply Migrations**
   ```bash
   # Push migrations to cloud
   supabase db push
   ```

### For Local Development

1. **Start Local Supabase**
   ```bash
   supabase start
   ```
   This will:
   - Download Docker images (first time only)
   - Start PostgreSQL, API, Auth, Storage services
   - Take ~1-2 minutes first time

2. **Get Local Credentials**
   ```bash
   supabase status
   ```
   This shows:
   - API URL: `http://localhost:54321`
   - anon key: (for local development)
   - Studio URL: `http://localhost:54323`

3. **Apply Migrations**
   ```bash
   supabase db reset
   ```
   This applies all migrations in `supabase/migrations/`

## Next Steps

After setup, you'll need to:

1. **Update App Configuration**
   - Add Supabase URL and key to `BuildConfig` or `local.properties`
   - See `docs/architecture/SUPABASE_SETUP.md` for details

2. **Test Connection**
   - Run the app
   - Verify it can connect to Supabase

3. **Verify RLS Policies**
   - Test that users can only access their own data
   - Test authentication flow

## Troubleshooting

### Docker Not Running
```bash
# Start Docker Desktop, then:
supabase start
```

### Migration Fails
```bash
# Check migration syntax
supabase migration list

# Reset and try again
supabase db reset
```

### Can't Link to Project
- Verify you're logged in: `supabase login`
- Check project ref is correct
- Ensure project is fully created (wait a few minutes)

## Getting Help

- Supabase Docs: https://supabase.com/docs
- CLI Docs: https://supabase.com/docs/guides/cli
- Discord: https://discord.supabase.com

