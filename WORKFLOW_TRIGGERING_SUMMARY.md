# Test Data Seeding - Workflow Triggering Summary

**Date**: 2025-11-21  
**Approach**: GitHub Actions workflows (no local scripts, no manual dashboard intervention)

---

## ✅ What's Been Done

### Enhanced Workflows

Both workflows now support:
- ✅ **Ad-hoc triggering** from any branch
- ✅ **Environment selection** (staging/production)
- ✅ **Branch-aware** (works from feature branches)
- ✅ **GitHub CLI compatible** (can trigger from Cursor)

### Updated Workflows

1. **`test-data-initial-seed.yml`**
   - Added `environment` input (staging/production)
   - Dynamic environment selection
   - Branch name in summary

2. **`test-data-nightly-update.yml`**
   - Added `environment` input (staging/production)
   - Dynamic environment selection
   - Branch name in summary

---

## 🚀 How to Trigger

### From Cursor (GitHub CLI)

**Initial Seed:**
```bash
# From current branch (defaults to staging)
gh workflow run test-data-initial-seed.yml

# Specify environment
gh workflow run test-data-initial-seed.yml --field environment=staging

# From specific branch
gh workflow run test-data-initial-seed.yml \
  --ref feature/test-data-seeding \
  --field environment=staging
```

**Daily Update:**
```bash
gh workflow run test-data-nightly-update.yml --field environment=staging
```

**Watch Progress:**
```bash
gh run watch
```

### From GitHub Web UI

1. Go to: https://github.com/charliemic/electric-sheep/actions
2. Select workflow: "Seed Test Data (Initial Setup)" or "Update Test Data (Nightly)"
3. Click "Run workflow"
4. Select branch (any branch)
5. Choose environment: `staging` or `production`
6. Click "Run workflow"

---

## 📋 Workflow Inputs

| Input | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `environment` | choice | No | `staging` | Environment to seed (`staging` or `production`) |
| `project_ref` | string | No | From secrets | Override project reference ID |

---

## ⚠️ Important Notes

### Workflow Recognition

**GitHub Actions only recognizes workflows that exist on the default branch (`main`).**

**Current Status:**
- ✅ Workflows exist on `feature/test-data-seeding` branch
- ⏳ Workflows need to be merged to `main` to be visible/triggerable

**After Merging to Main:**
- Workflows will be visible in GitHub Actions UI
- Can be triggered from any branch
- Can be triggered via GitHub CLI from any branch

### Branch Support

**Once merged to main:**
- ✅ Can trigger from `main` branch
- ✅ Can trigger from `feature/test-data-seeding` branch
- ✅ Can trigger from any branch
- ✅ Workflow uses code from the branch you specify

---

## 🎯 Next Steps

1. **Merge to Main** (when ready):
   ```bash
   # Create PR or merge directly
   git checkout main
   git merge feature/test-data-seeding
   git push origin main
   ```

2. **Trigger from Branch** (after merge):
   ```bash
   # From your feature branch
   gh workflow run test-data-initial-seed.yml \
     --ref feature/test-data-seeding \
     --field environment=staging
   ```

3. **Verify Results**:
   - Check workflow run status
   - Verify data in Supabase Dashboard
   - Confirm 8 users and 240 mood entries

---

## 📚 Documentation

- **Triggering Guide**: `docs/development/guides/TRIGGER_SEEDING_WORKFLOWS.md`
- **Verification Results**: `docs/development/reports/SEEDING_VERIFICATION_RESULTS.md`
- **Evaluation**: `docs/development/reports/TEST_DATA_SEEDING_EVALUATION.md`

---

**Status**: ✅ Workflows enhanced and ready | ⏳ Awaiting merge to main for visibility

