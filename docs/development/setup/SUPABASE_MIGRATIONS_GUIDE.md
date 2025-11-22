# Supabase Migrations - Correct Approach

**CRITICAL**: Migrations are applied via **CI/CD (GitHub Actions)**, NOT locally.

## The Problem We Keep Hitting

**❌ WRONG**: Trying to run migrations locally with `supabase db push`
- Migration history mismatches
- Authentication issues
- Local Docker requirements
- Dead end that wastes time

**✅ CORRECT**: Migrations are automatically applied via GitHub Actions when pushed to `main` or `develop`

## How Migrations Actually Work

### 1. Create Migration File

```bash
# Create migration file in supabase/migrations/
# Format: YYYYMMDDHHMMSS_description.sql
# Example: 20251122000000_create_prompts_table.sql
```

### 2. Commit and Push

```bash
git add supabase/migrations/20251122000000_create_prompts_table.sql
git commit -m "feat: Add prompts table migration"
git push origin main  # or develop
```

### 3. CI/CD Automatically Applies

**GitHub Actions workflow** (`.github/workflows/supabase-schema-deploy.yml`):
- ✅ Detects changes to `supabase/migrations/`
- ✅ Validates migration syntax
- ✅ Applies migration via `supabase db push` (in CI/CD environment)
- ✅ Verifies deployment

**No local execution needed!**

## Why Not Local?

### Migration History Mismatches
- Remote database has migrations not in local repo
- Local repo has migrations not in remote
- `supabase db push` fails with history conflicts
- Requires complex repair operations

### Authentication Complexity
- Requires `SUPABASE_ACCESS_TOKEN` (Personal Access Token)
- Requires `SUPABASE_DB_PASSWORD`
- Local linking can be problematic
- CI/CD has these configured automatically

### Docker Requirements
- Local Supabase requires Docker
- Docker may not be running
- Additional setup complexity
- CI/CD environment is ready

## The Correct Workflow

### For Agents: Migration Workflow

1. **Create Migration File**
   ```bash
   # Just create the SQL file
   # supabase/migrations/20251122000000_create_prompts_table.sql
   ```

2. **Commit and Push**
   ```bash
   git add supabase/migrations/20251122000000_create_prompts_table.sql
   git commit -m "feat: Add prompts table migration"
   git push origin main
   ```

3. **CI/CD Applies Automatically**
   - GitHub Actions detects the migration
   - Validates and applies it
   - No local execution needed

4. **Verify in Supabase Dashboard**
   - Check table exists
   - Verify schema is correct

### Never Do This Locally

**❌ DON'T:**
```bash
# Don't try to run locally
supabase db push  # Will fail with history mismatches
supabase migration up  # Requires local Docker
psql $SUPABASE_DB_URL -f migration.sql  # Requires DB URL, complex
```

**✅ DO:**
```bash
# Just commit and push - CI/CD handles it
git add supabase/migrations/
git commit -m "Add migration"
git push origin main
```

## Migration Repair (If Needed)

**Only if migration history is truly broken:**

```bash
# Repair migration history (mark as applied without running)
supabase migration repair --status applied <migration-timestamp>

# But this doesn't actually apply the migration!
# The migration still needs to be applied via CI/CD
```

**Better approach**: Let CI/CD handle it. The workflow will apply all pending migrations.

## Verification

### Check Migration Status

**In Supabase Dashboard:**
1. Go to: https://supabase.com/dashboard/project/[PROJECT_REF]
2. Navigate to: Database → Migrations
3. See applied migrations

**Via GitHub Actions:**
1. Go to: https://github.com/[REPO]/actions
2. Find "Deploy Supabase Database Schema" workflow
3. Check deployment logs

### Verify Table Exists

**In Supabase Dashboard:**
1. Go to: Table Editor
2. Check if table exists

**Via SQL Editor:**
```sql
SELECT * FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name = 'prompts';
```

## Troubleshooting

### "Migration history mismatch"

**Solution**: Don't try to fix locally. Let CI/CD handle it:
1. Commit migration file
2. Push to `main` or `develop`
3. CI/CD will apply it

### "Migration already applied"

**This is fine!** Supabase tracks applied migrations. If it's already applied, the workflow will skip it.

### "Table doesn't exist after push"

**Check**:
1. Did CI/CD workflow run? (Check GitHub Actions)
2. Did workflow succeed? (Check logs)
3. Is migration file in correct location? (`supabase/migrations/`)
4. Does migration file follow naming convention? (`YYYYMMDDHHMMSS_description.sql`)

## Key Takeaways

1. **✅ Migrations go through CI/CD** - Never run locally
2. **✅ Just commit and push** - CI/CD handles application
3. **✅ Verify in dashboard** - Check Supabase after deployment
4. **❌ Don't try local execution** - It will fail with history mismatches
5. **❌ Don't try to repair locally** - Let CI/CD handle it

## Related Documentation

- `.github/workflows/supabase-schema-deploy.yml` - CI/CD workflow
- `docs/development/ci-cd/CI_CD_MIGRATION_SETUP.md` - Setup guide
- `supabase/README.md` - Supabase configuration

