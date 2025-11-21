# Triggering Test Data Seeding Workflows

**Purpose**: Guide for triggering test data seeding workflows ad-hoc from any branch, including from Cursor.

---

## Available Workflows

### 1. Seed Test Data (Initial Setup)
**Workflow**: `test-data-initial-seed.yml`  
**Purpose**: Creates test users and loads 30 days of baseline mood data

### 2. Update Test Data (Nightly)
**Workflow**: `test-data-nightly-update.yml`  
**Purpose**: Adds yesterday's mood data for all test users

---

## Triggering Methods

### Method 1: GitHub Web UI

1. **Navigate to Actions**:
   - Go to: https://github.com/charliemic/electric-sheep/actions
   - Or: https://github.com/charliemic/electric-sheep/actions/workflows/test-data-initial-seed.yml

2. **Select Workflow**:
   - Click on "Seed Test Data (Initial Setup)" or "Update Test Data (Nightly)"

3. **Click "Run workflow"**:
   - Select branch (any branch, including feature branches)
   - Choose environment: `staging` (default) or `production`
   - Optionally provide `project_ref` (if different from secrets)
   - Click "Run workflow"

### Method 2: GitHub CLI (From Cursor)

**From any branch, including feature branches:**

#### Initial Seed
```bash
# From current branch (defaults to staging)
gh workflow run test-data-initial-seed.yml

# Specify environment
gh workflow run test-data-initial-seed.yml \
  --field environment=staging

# Specify branch explicitly
gh workflow run test-data-initial-seed.yml \
  --ref feature/test-data-seeding \
  --field environment=staging

# With custom project ref
gh workflow run test-data-initial-seed.yml \
  --field environment=staging \
  --field project_ref=rmcnvcqnowgsvvbmfssi
```

#### Daily Update
```bash
# From current branch (defaults to staging)
gh workflow run test-data-nightly-update.yml

# Specify environment
gh workflow run test-data-nightly-update.yml \
  --field environment=staging

# From specific branch
gh workflow run test-data-nightly-update.yml \
  --ref feature/test-data-seeding \
  --field environment=staging
```

#### Watch Workflow Run
```bash
# After triggering, watch the run
gh run watch

# Or list recent runs
gh run list --workflow=test-data-initial-seed.yml

# View logs
gh run view --log
```

### Method 3: From Cursor Terminal

**Quick trigger from Cursor:**

```bash
# Make sure you're in the right directory
cd /Users/CharlieCalver/git/electric-sheep-test-data-seeding

# Trigger initial seed (uses current branch)
gh workflow run test-data-initial-seed.yml --field environment=staging

# Watch it run
gh run watch
```

---

## Workflow Inputs

### Common Inputs (Both Workflows)

| Input | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `environment` | choice | No | `staging` | Environment to seed (`staging` or `production`) |
| `project_ref` | string | No | From secrets | Supabase project reference ID (override secrets) |

### Environment Behavior

**Staging (default)**:
- Uses `SUPABASE_PROJECT_REF_STAGING` secret
- Uses `SUPABASE_SECRET_KEY_STAGING` secret
- Uses `SUPABASE_DB_PASSWORD_STAGING` secret
- Safe for testing

**Production**:
- Uses `SUPABASE_PROJECT_REF` secret
- Uses `SUPABASE_SECRET_KEY` secret
- Uses `SUPABASE_DB_PASSWORD` secret
- ⚠️ **Use with caution** - affects production data

---

## Branch Support

### ✅ Works From Any Branch

Both workflows can be triggered from:
- ✅ `main` branch
- ✅ `develop` branch
- ✅ Feature branches (e.g., `feature/test-data-seeding`)
- ✅ Any branch in the repository

**Why this works:**
- Workflows use `workflow_dispatch` which works from any branch
- GitHub Actions checks out the code from the branch you specify
- Secrets are repository-level, so available from any branch

### Branch Selection

**Via GitHub CLI:**
```bash
# Use current branch (default)
gh workflow run test-data-initial-seed.yml

# Specify branch explicitly
gh workflow run test-data-initial-seed.yml --ref feature/your-branch

# Use main branch
gh workflow run test-data-initial-seed.yml --ref main
```

**Via GitHub Web UI:**
- Branch dropdown shows all branches
- Select the branch you want to use

---

## Workflow Behavior

### Initial Seed Workflow

**What it does:**
1. Creates 8 test users via Supabase Admin API
2. Creates `generate_mood_score()` SQL function
3. Loads 30 days of baseline mood data
4. Verifies data was seeded

**Idempotent**: Safe to run multiple times (skips existing users/data)

### Daily Update Workflow

**What it does:**
1. Ensures `generate_mood_score()` function exists
2. Adds yesterday's mood data for all test users
3. Verifies data was updated

**Idempotent**: Safe to run multiple times (skips if data exists)

---

## Examples

### Example 1: Test Seeding from Feature Branch

```bash
# You're working on feature/test-data-seeding branch
cd /Users/CharlieCalver/git/electric-sheep-test-data-seeding

# Trigger seeding workflow
gh workflow run test-data-initial-seed.yml \
  --ref feature/test-data-seeding \
  --field environment=staging

# Watch it run
gh run watch
```

### Example 2: Update Test Data Manually

```bash
# Add yesterday's data manually (instead of waiting for nightly)
gh workflow run test-data-nightly-update.yml \
  --field environment=staging

# Check status
gh run list --workflow=test-data-nightly-update.yml --limit 1
```

### Example 3: Test Production Seeding (Careful!)

```bash
# ⚠️ Only if you need to seed production
gh workflow run test-data-initial-seed.yml \
  --field environment=production
```

---

## Verification

### Check Workflow Status

```bash
# List recent runs
gh run list --workflow=test-data-initial-seed.yml

# View specific run
gh run view <run-id>

# Watch live logs
gh run watch <run-id>
```

### Verify Data in Supabase

After workflow completes, verify in Supabase Dashboard:
- 8 test users exist in `auth.users`
- 240 mood entries exist (8 users × 30 days)
- `generate_mood_score()` function exists

---

## Troubleshooting

### "Workflow not found"

**Problem**: Workflow doesn't appear in GitHub Actions

**Solution**: 
- Workflows must exist on the branch you're triggering from
- Or workflows must exist on `main` branch (GitHub recognizes workflows from default branch)

**Fix**: Merge workflow changes to `main` first, then trigger from any branch

### "Secret not found"

**Problem**: Workflow fails with "secret not set" error

**Solution**:
- Check that secrets exist in GitHub repository settings
- Verify environment name matches secret names (staging vs production)
- Use `project_ref` input to override if needed

### "Permission denied"

**Problem**: Can't trigger workflow

**Solution**:
- Verify GitHub CLI is authenticated: `gh auth status`
- Check repository permissions
- Ensure you have `workflow_dispatch` permission

---

## Best Practices

1. ✅ **Use staging for testing** - Default to `staging` environment
2. ✅ **Test from feature branches** - Verify workflows work before merging
3. ✅ **Watch workflow runs** - Use `gh run watch` to monitor progress
4. ✅ **Verify after seeding** - Check Supabase Dashboard to confirm data
5. ⚠️ **Be careful with production** - Only use production when necessary

---

## Quick Reference

```bash
# Initial seed (staging, current branch)
gh workflow run test-data-initial-seed.yml

# Initial seed (staging, specific branch)
gh workflow run test-data-initial-seed.yml --ref feature/your-branch

# Daily update (staging)
gh workflow run test-data-nightly-update.yml

# Watch latest run
gh run watch

# List runs
gh run list --workflow=test-data-initial-seed.yml
```

---

**Status**: ✅ Workflows support ad-hoc triggering from any branch via GitHub CLI or Web UI

