# Supabase Development Workflow

This document outlines the workflow for managing Supabase configuration and deployments.

## Development Workflow

### 1. Local Development

```bash
# Start local Supabase instance
supabase start

# Create a new migration
supabase migration new add_user_profile_table

# Edit the migration file in supabase/migrations/
# Test locally
supabase db reset

# Stop when done
supabase stop
```

### 2. Making Changes

**Database Schema Changes**:
1. Create migration: `supabase migration new description`
2. Write SQL in the migration file
3. Test locally: `supabase db reset`
4. Commit migration file to git
5. Push to branch → CI/CD validates and deploys

**Configuration Changes**:
1. Edit `supabase/config.toml`
2. Test locally: `supabase start`
3. Commit changes to git
4. Push to branch → CI/CD validates

### 3. Deployment Flow

```
┌─────────────────┐
│  Local Changes  │
│  (migrations)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Commit to Git  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Push to Branch │
└────────┬────────┘
         │
         ├─────────────────┬─────────────────┐
         ▼                 ▼                 ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Validate   │  │   Preview    │  │   Deploy    │
│  Migrations  │  │  (PR only)   │  │ (main/dev)  │
└──────────────┘  └──────────────┘  └──────────────┘
```

## Branch Strategy

- **`main`**: Production deployments
  - Merging to `main` → Deploys to production Supabase project
  - Requires review and passing CI

- **`develop`**: Staging deployments
  - Merging to `develop` → Deploys to staging Supabase project
  - For testing before production

- **Feature branches**: Validation only
  - PRs validate migrations
  - Preview migrations in local Supabase instance
  - No automatic deployment

## CI/CD Pipeline Behavior

### On Push to `main` or `develop`
1. ✅ Validate migration syntax
2. ✅ Check migration naming convention
3. ✅ Deploy to respective environment (staging/production)
4. ✅ Verify deployment success

### On Pull Request
1. ✅ Validate migration syntax
2. ✅ Check migration naming convention
3. ✅ Start local Supabase
4. ✅ Apply migrations
5. ✅ Generate diff preview
6. ❌ No deployment (preview only)

### On App Code Changes (ignores Supabase)
- App CI pipeline runs normally
- Supabase pipeline does not run

## Migration Guidelines

### Creating Migrations

1. **Use descriptive names**:
   ```bash
   supabase migration new create_moods_table
   # Creates: 20241117120000_create_moods_table.sql
   ```

2. **Write idempotent SQL**:
   ```sql
   CREATE TABLE IF NOT EXISTS moods (...);
   CREATE INDEX IF NOT EXISTS idx_moods_user_id ON moods(user_id);
   ```

3. **Include RLS policies**:
   ```sql
   ALTER TABLE moods ENABLE ROW LEVEL SECURITY;
   CREATE POLICY "Users can view own moods" ON moods FOR SELECT USING (auth.uid() = user_id);
   ```

4. **Add indexes for performance**:
   ```sql
   CREATE INDEX idx_moods_user_id ON moods(user_id);
   ```

### Migration Ordering

Migrations are applied in timestamp order. The timestamp in the filename ensures:
- Correct application order
- No conflicts between developers
- Clear history of changes

### Rollback Strategy

Supabase migrations are forward-only by default. To rollback:
1. Create a new migration that reverses the change
2. Or manually fix via Supabase dashboard (not recommended for production)

## Environment Setup

### Local Development
```bash
# Start local Supabase
supabase start

# Get local credentials
supabase status

# Use in app:
# SUPABASE_URL=http://localhost:54321
# SUPABASE_ANON_KEY=<from supabase status>
```

### Staging
- Separate Supabase project
- Deployed from `develop` branch
- Used for testing before production

### Production
- Production Supabase project
- Deployed from `main` branch
- Requires approval/review

## Security Considerations

1. **RLS Policies**: Always enforce Row-Level Security
   - Users can only access their own data
   - Policies are defined in migrations

2. **Service Role Key**: Never commit service role keys
   - Use GitHub Secrets for CI/CD
   - Use `.env.local` for local development (not tracked)

3. **Anon Key**: Safe to commit (public key)
   - Limited by RLS policies
   - Can be in BuildConfig or environment variables

4. **Database Password**: Never commit
   - Use GitHub Secrets
   - Use environment variables

## Troubleshooting

### Migration Fails in CI/CD
1. Check migration syntax
2. Verify migration naming convention
3. Check Supabase access token
4. Review workflow logs

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

### Migration Conflicts
```bash
# Check migration status
supabase migration list

# Reset local database
supabase db reset

# Check remote status
supabase db remote commit
```

## Best Practices

1. ✅ **Always test migrations locally** before committing
2. ✅ **Use descriptive migration names**
3. ✅ **Include RLS policies** in every table migration
4. ✅ **Add indexes** for query performance
5. ✅ **Write idempotent SQL** (IF NOT EXISTS)
6. ✅ **Review migrations** in PRs before merging
7. ❌ **Never modify existing migrations** (create new ones)
8. ❌ **Never commit secrets** (use environment variables)

