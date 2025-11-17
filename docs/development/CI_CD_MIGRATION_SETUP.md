# Running Supabase Migrations via CI/CD

Since local Docker setup can be problematic, you can run Supabase migrations directly via GitHub Actions CI/CD.

## Prerequisites

You need to set up GitHub Secrets in your repository:

1. **Go to GitHub Repository Settings**:
   - Visit: https://github.com/YOUR_USERNAME/electric-sheep/settings/secrets/actions
   - Or: Repository → Settings → Secrets and variables → Actions

2. **Add Required Secrets**:
   - **`SUPABASE_ACCESS_TOKEN`**: Your Supabase access token
     - Get it from: https://supabase.com/dashboard/account/tokens
     - Or run: `supabase login` locally and check `~/.supabase/access-token`
   - **`SUPABASE_PROJECT_REF`**: Your project reference ID
     - Value: `mvuzvoyvijsdqsfqjgpd`
     - This is used if staging/prod refs aren't set
   - **`SUPABASE_PROJECT_REF_STAGING`** (optional): Staging project ref
   - **`SUPABASE_PROJECT_REF_PROD`** (optional): Production project ref

## Getting Your Supabase Access Token

If you don't have an access token:

1. **Via Supabase Dashboard**:
   - Go to: https://supabase.com/dashboard/account/tokens
   - Click "Generate new token"
   - Copy the token (you won't see it again!)

2. **Via CLI** (if you can run it):
   ```bash
   supabase login
   # Check the token location (usually ~/.supabase/access-token)
   ```

## Triggering Migration Deployment

### Option 1: Push to Main/Develop Branch

Simply push your migration files:

```bash
git add supabase/migrations/
git commit -m "Add moods table migration"
git push origin main  # or develop
```

The workflow will automatically:
- Validate migrations
- Deploy to production (if on `main`) or staging (if on `develop`)

### Option 2: Manual Trigger (Workflow Dispatch)

1. **Go to GitHub Actions**:
   - Visit: https://github.com/YOUR_USERNAME/electric-sheep/actions
   - Click on "Supabase Deploy" workflow
   - Click "Run workflow"
   - Optionally provide project reference ID
   - Click "Run workflow"

### Option 3: Push Any Branch (for testing)

The workflow will validate migrations on any branch, but only deploy on `main`/`develop`.

## What the Workflow Does

1. **Validates Migrations**:
   - Checks SQL syntax
   - Verifies naming convention (YYYYMMDDHHMMSS_description.sql)

2. **Deploys Migrations**:
   - Links to your Supabase project
   - Runs `supabase db push` to apply all migrations
   - Verifies deployment with `supabase migration list`

3. **Environment-Specific**:
   - `main` branch → Production
   - `develop` branch → Staging
   - Other branches → Validation only

## Current Migration

Your current migration file:
- `supabase/migrations/20241117000000_create_moods_table.sql`
- Creates `moods` table with RLS policies
- Ready to deploy!

## Quick Start

1. **Set GitHub Secrets** (see above)

2. **Commit and push**:
   ```bash
   git add supabase/
   git commit -m "Add Supabase migration for moods table"
   git push origin main
   ```

3. **Watch the pipeline**:
   - Go to: https://github.com/YOUR_USERNAME/electric-sheep/actions
   - Watch the "Supabase Deploy" workflow run
   - Check for any errors

4. **Verify in Supabase**:
   - Go to: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd
   - Navigate to: Table Editor
   - You should see the `moods` table

## Troubleshooting

**"No project reference found"**:
- Ensure `SUPABASE_PROJECT_REF` secret is set
- Or provide it via workflow_dispatch input

**"Authentication failed"**:
- Verify `SUPABASE_ACCESS_TOKEN` is correct
- Token may have expired - generate a new one

**"Migration already applied"**:
- This is fine! Supabase tracks applied migrations
- The workflow will skip already-applied migrations

**"RLS policy already exists"**:
- If policies already exist, the migration may fail
- You may need to modify the migration to use `CREATE POLICY IF NOT EXISTS` (PostgreSQL 9.5+)

## Next Steps

After migration is deployed:
1. Verify table exists in Supabase dashboard
2. Test Google OAuth sign-in
3. Test creating mood entries
4. Verify RLS policies work (users can only see their own data)

