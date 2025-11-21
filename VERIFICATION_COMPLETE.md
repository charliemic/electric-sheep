# Test Data Seeding Verification - Complete

**Date**: 2025-11-21  
**Status**: ✅ Static Verification Complete | ⏳ Runtime Testing Ready

---

## ✅ Static Verification Results

All infrastructure checks **PASSED**:

1. ✅ **Script Syntax** - All bash scripts valid
2. ✅ **File Existence** - All required files present
3. ✅ **Function Integration** - Function creation step added
4. ✅ **Test User Consistency** - 8 users match across all scripts
5. ✅ **SQL Function Logic** - Matches client-side implementation
6. ✅ **Workflow Structure** - Nightly update workflow created
7. ✅ **Dependencies** - All dependencies verified
8. ✅ **Idempotency** - All scripts are idempotent

---

## 📋 Available Credentials

### Found in `local.properties`:
- ✅ **Production URL**: `https://mvuzvoyvijsdqsfqjgpd.supabase.co`
- ✅ **Staging URL**: `https://rmcnvcqnowgsvvbmfssi.supabase.co`

### Found in GitHub Secrets:
- ✅ `SUPABASE_ACCESS_TOKEN` - For Supabase CLI login
- ✅ `SUPABASE_SECRET_KEY_STAGING` - Service role key for staging
- ✅ `SUPABASE_DB_PASSWORD_STAGING` - Database password for staging
- ✅ `SUPABASE_PROJECT_REF_STAGING` - Staging project ref: `rmcnvcqnowgsvvbmfssi`

### Tools Available:
- ✅ **Supabase CLI**: v2.58.5
- ✅ **GitHub CLI**: Authenticated

---

## 🚀 Runtime Testing Options

### Option 1: Manual Testing (Recommended)

**Step 1: Get Service Role Key**
```bash
# Option A: From Supabase Dashboard
# Go to: https://supabase.com/dashboard/project/rmcnvcqnowgsvvbmfssi
# Settings → API → secret key (sb_secret_...)

# Option B: From GitHub Secrets (if you have access)
# Go to: https://github.com/charliemic/electric-sheep/settings/secrets/actions
# Copy: SUPABASE_SECRET_KEY_STAGING
```

**Step 2: Set Environment Variables**
```bash
export SUPABASE_URL="https://rmcnvcqnowgsvvbmfssi.supabase.co"
export SUPABASE_SECRET_KEY="sb_secret_..."  # Your service role key
```

**Step 3: Link Supabase (if not already linked)**
```bash
# Get access token from GitHub Secrets or Supabase Dashboard
export SUPABASE_ACCESS_TOKEN="sbp_..."  # Personal Access Token

supabase login --token "$SUPABASE_ACCESS_TOKEN"
supabase link --project-ref rmcnvcqnowgsvvbmfssi
```

**Step 4: Run Seeding**
```bash
cd /Users/CharlieCalver/git/electric-sheep-test-data-seeding
./supabase/scripts/seed-test-data.sh
```

**Step 5: Verify Data**
```sql
-- Run via Supabase CLI or Dashboard
SELECT 
    u.email,
    COUNT(m.id) as entry_count,
    AVG(m.score) as avg_score,
    MIN(m.score) as min_score,
    MAX(m.score) as max_score
FROM auth.users u
LEFT JOIN public.moods m ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test'
GROUP BY u.email
ORDER BY u.email;
```

---

### Option 2: Use Verification Scripts

**Quick Check:**
```bash
./scripts/verify-seeding.sh
```

**Runtime Test (interactive):**
```bash
./scripts/test-seeding-with-credentials.sh
```

---

### Option 3: Use GitHub Actions Workflow

**Trigger Initial Seed Workflow:**
1. Go to: https://github.com/charliemic/electric-sheep/actions/workflows/test-data-initial-seed.yml
2. Click "Run workflow"
3. Select branch: `feature/test-data-seeding` (or `main`)
4. Optionally provide project_ref
5. Click "Run workflow"

**This will:**
- Use GitHub Secrets automatically
- Create test users
- Load baseline data
- Verify data was seeded

---

## 📊 Expected Results

### After Successful Seeding:

**Test Users Created:**
- 8 users with emails: `test-{tech-level}-{mood-pattern}@electric-sheep.test`
- Password: `test-password-123` (default)

**Mood Data Loaded:**
- 240 total entries (8 users × 30 days)
- Data up until yesterday (no partial day)
- Scores match patterns:
  - `high_stable`: 7-9 range
  - `low_stable`: 2-4 range
  - `high_unstable`: 4-10 range
  - `low_unstable`: 1-6 range

**Function Created:**
- `generate_mood_score()` function in database
- Can be called: `SELECT generate_mood_score('high_stable', 0, 0);`

---

## 🔍 Verification Checklist

After running seeding, verify:

- [ ] 8 test users exist in `auth.users`
- [ ] `generate_mood_score()` function exists
- [ ] 240 mood entries created (30 per user)
- [ ] Data dates are correct (up until yesterday)
- [ ] Scores match patterns (verify averages)
- [ ] Idempotency works (re-run doesn't create duplicates)

---

## 📝 Next Steps

1. **Get Service Role Key** - From Supabase Dashboard or GitHub Secrets
2. **Link Supabase** - Use access token to link staging project
3. **Run Seeding** - Execute `seed-test-data.sh`
4. **Verify Data** - Check users and mood entries
5. **Test Daily Update** - Run `add-daily-data.sh` to test daily updates

---

## 🎯 Success Criteria

**Seeding Works When:**
- ✅ All 8 test users created
- ✅ Function generates correct scores
- ✅ Baseline data loads (240 entries)
- ✅ Daily updates work (idempotent)
- ✅ Data syncs to app correctly

---

**Status**: ✅ Infrastructure ready | ⏳ Awaiting runtime test with credentials

