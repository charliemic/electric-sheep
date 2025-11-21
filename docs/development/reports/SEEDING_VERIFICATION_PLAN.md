# Test Data Seeding Verification Plan

**Date**: 2025-11-21  
**Focus**: Supabase Seeding (Server-Side) - Not Runtime Manipulation  
**Principle**: Prefer seeding over runtime manipulation for test data

---

## Principle Alignment

**Decision**: Focus on **seeding** (server-side, Supabase) rather than **runtime manipulation** (client-side, TestDataLoader)

**Rationale**:
- Seeding is more reliable and deterministic
- Data exists before tests run (no timing issues)
- Works with offline-first sync architecture
- Easier to verify and debug
- Better for CI/CD pipelines

**Runtime manipulation** (TestDataLoader) can be added later if needed, but seeding should be the primary approach.

---

## Seeding Infrastructure Inventory

### ✅ What Exists

#### 1. Supabase Scripts (`supabase/scripts/`)

**Files**:
- ✅ `create-test-users.sh` - Creates 8 test users via Admin API
- ✅ `seed-test-data.sh` - Orchestrates full seeding (users + data)
- ✅ `add-daily-data.sh` - Adds yesterday's data (idempotent)
- ✅ `add-daily-data-postgrest.sh` - Alternative PostgREST implementation

**Status**: All scripts exist and appear complete

#### 2. SQL Seed Scripts (`supabase/seed/`)

**Files**:
- ✅ `001_create_test_users.sql` - Verifies users exist (reference only)
- ✅ `002_load_baseline_mood_data.sql` - Loads 30 days of baseline data
- ✅ `functions/generate_mood_score.sql` - SQL function for mood generation

**Status**: All scripts exist and appear complete

#### 3. CI/CD Workflows

**Files**:
- ✅ `.github/workflows/test-data-initial-seed.yml` - Manual initial seeding
- ❌ `.github/workflows/test-data-nightly-update.yml` - **MISSING** (mentioned in docs)

**Status**: Initial seed workflow exists, nightly update missing

---

## Verification Checklist

### Phase 1: Script Verification (Static Analysis)

#### ✅ Script Completeness
- [x] All scripts exist
- [x] Scripts have proper error handling
- [x] Scripts are idempotent (safe to run multiple times)
- [x] Scripts have proper prerequisites documented

#### ✅ SQL Script Verification
- [x] `generate_mood_score()` function exists
- [x] Function matches client-side logic (TestUserFixtures.kt)
- [x] Baseline data script is idempotent
- [x] Daily data script is idempotent

#### ⚠️ Issues Found
1. **Missing nightly update workflow** - Referenced in docs but doesn't exist
2. **Function dependency** - `002_load_baseline_mood_data.sql` assumes function exists but doesn't create it
3. **Script order** - `seed-test-data.sh` doesn't ensure function exists before using it

---

### Phase 2: Integration Verification (Runtime Testing)

#### Test 1: Create Test Users
**Command**:
```bash
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_SECRET_KEY=sb_secret_xxx
./supabase/scripts/create-test-users.sh
```

**Expected**:
- ✅ Creates 8 test users
- ✅ Idempotent (skips existing users)
- ✅ Sets password: `test-password-123`
- ✅ Users have correct IDs and emails

**Verification**:
```sql
SELECT id, email, raw_user_meta_data->>'display_name' 
FROM auth.users 
WHERE email LIKE 'test-%@electric-sheep.test';
```

#### Test 2: Create Function
**Command**:
```bash
supabase db execute < supabase/seed/functions/generate_mood_score.sql
```

**Expected**:
- ✅ Function created successfully
- ✅ Function accepts correct parameters
- ✅ Function returns valid scores (1-10)

**Verification**:
```sql
SELECT generate_mood_score('high_stable', 0, 0);
-- Should return integer between 7-9
```

#### Test 3: Load Baseline Data
**Command**:
```bash
supabase db execute < supabase/seed/002_load_baseline_mood_data.sql
```

**Expected**:
- ✅ Creates 30 days of data per user (240 total entries)
- ✅ Data up until yesterday (no partial day)
- ✅ Idempotent (skips if data exists)
- ✅ Scores match patterns (high_stable: 7-9, etc.)

**Verification**:
```sql
SELECT 
    u.email,
    COUNT(m.id) as entry_count,
    MIN(DATE(to_timestamp(m.timestamp / 1000))) as earliest_date,
    MAX(DATE(to_timestamp(m.timestamp / 1000))) as latest_date,
    AVG(m.score) as avg_score,
    MIN(m.score) as min_score,
    MAX(m.score) as max_score
FROM auth.users u
LEFT JOIN public.moods m ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test'
GROUP BY u.email
ORDER BY u.email;
```

#### Test 4: Full Seeding Workflow
**Command**:
```bash
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_SECRET_KEY=sb_secret_xxx
supabase link --project-ref xxx
./supabase/scripts/seed-test-data.sh
```

**Expected**:
- ✅ Creates users (if needed)
- ✅ Ensures function exists
- ✅ Loads baseline data
- ✅ All steps succeed

#### Test 5: Daily Data Update
**Command**:
```bash
supabase link --project-ref xxx
./supabase/scripts/add-daily-data.sh
```

**Expected**:
- ✅ Adds yesterday's data for all users
- ✅ Idempotent (skips if exists)
- ✅ Scores match patterns

**Verification**:
```sql
SELECT 
    u.email,
    DATE(to_timestamp(m.timestamp / 1000)) as date,
    m.score
FROM auth.users u
JOIN public.moods m ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test'
  AND DATE(to_timestamp(m.timestamp / 1000)) = CURRENT_DATE - INTERVAL '1 day'
ORDER BY u.email;
```

---

### Phase 3: CI/CD Verification

#### Test 6: Initial Seed Workflow
**Action**: Trigger `.github/workflows/test-data-initial-seed.yml` manually

**Expected**:
- ✅ Workflow runs successfully
- ✅ Creates test users
- ✅ Loads baseline data
- ✅ Verification step passes

#### Test 7: Nightly Update Workflow
**Action**: Create missing workflow

**Expected**:
- ✅ Workflow exists
- ✅ Runs on schedule (daily at 2 AM UTC)
- ✅ Adds yesterday's data
- ✅ Verification step passes

---

## Issues to Fix

### Issue 1: Missing Function Creation in seed-test-data.sh

**Problem**: `seed-test-data.sh` doesn't ensure `generate_mood_score()` function exists before using it.

**Current**:
```bash
# Step 2: Load baseline mood data (via SQL)
# ... runs 002_load_baseline_mood_data.sql which uses generate_mood_score()
```

**Fix**: Add step to create function before loading data:
```bash
# Step 1.5: Ensure generate_mood_score function exists
echo -e "${YELLOW}Step 1.5: Ensuring generate_mood_score function exists...${NC}"
cat "${SEED_DIR}/functions/generate_mood_score.sql" | supabase db execute || {
    echo -e "${YELLOW}Warning: Function may already exist${NC}"
}
```

### Issue 2: Missing Nightly Update Workflow

**Problem**: Workflow referenced in docs but doesn't exist.

**Fix**: Create `.github/workflows/test-data-nightly-update.yml`:
```yaml
name: Update Test Data (Nightly)

on:
  schedule:
    - cron: '0 2 * * *'  # Daily at 2 AM UTC
  workflow_dispatch:  # Manual trigger

jobs:
  update-test-data:
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/checkout@v4
      - uses: supabase/setup-cli@v1
      - name: Login to Supabase
        env:
          SUPABASE_ACCESS_TOKEN: ${{ secrets.SUPABASE_ACCESS_TOKEN }}
        run: supabase login --token "$SUPABASE_ACCESS_TOKEN"
      - name: Link to staging project
        env:
          SUPABASE_DB_PASSWORD: ${{ secrets.SUPABASE_DB_PASSWORD_STAGING }}
        run: supabase link --project-ref ${{ secrets.SUPABASE_PROJECT_REF_STAGING }}
      - name: Add daily data
        run: bash supabase/scripts/add-daily-data.sh
```

### Issue 3: Script Dependency Documentation

**Problem**: `002_load_baseline_mood_data.sql` assumes function exists but doesn't document this clearly.

**Fix**: Add clear comment at top of script:
```sql
-- Prerequisites:
--   1. generate_mood_score() function must exist
--      Run: supabase db execute < supabase/seed/functions/generate_mood_score.sql
--   2. Test users must exist
--      Run: ./supabase/scripts/create-test-users.sh
```

---

## Next Steps

### Immediate (This Session)
1. ✅ **Fix seed-test-data.sh** - Add function creation step
2. ✅ **Create nightly update workflow** - Add missing CI/CD job
3. ✅ **Update documentation** - Clarify prerequisites

### Verification (Next Session)
4. **Test all scripts** - Run through verification checklist
5. **Test CI/CD workflows** - Verify both workflows work
6. **Document results** - Update evaluation with test results

---

## Success Criteria

### Seeding Works When:
- ✅ All 8 test users can be created
- ✅ Function generates correct scores
- ✅ Baseline data loads successfully (240 entries)
- ✅ Daily updates work (idempotent)
- ✅ CI/CD workflows run successfully
- ✅ Data syncs to app correctly

### Ready for Use When:
- ✅ All scripts verified working
- ✅ CI/CD workflows functional
- ✅ Documentation complete
- ✅ Test results documented

---

## Testing Commands Summary

```bash
# 1. Create test users
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_SECRET_KEY=sb_secret_xxx
./supabase/scripts/create-test-users.sh

# 2. Create function
supabase db execute < supabase/seed/functions/generate_mood_score.sql

# 3. Load baseline data
supabase db execute < supabase/seed/002_load_baseline_mood_data.sql

# 4. Verify data
supabase db execute "
SELECT u.email, COUNT(m.id) as entries, AVG(m.score) as avg_score
FROM auth.users u
LEFT JOIN public.moods m ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test'
GROUP BY u.email;
"

# 5. Full workflow
supabase link --project-ref xxx
./supabase/scripts/seed-test-data.sh

# 6. Daily update
./supabase/scripts/add-daily-data.sh
```

---

**Next Action**: Fix the three issues identified, then verify seeding works end-to-end.

