# Secret Scan Git History Issue

**Date**: 2025-11-22  
**Status**: ⚠️ Additional Issue Identified  
**Related**: Pipeline Issues Resolution Initiative - Agent 1

## Issue Summary

After fixing the `.gitleaks.toml` structure, secret scanning is now encountering a different issue related to Git history.

## Error Details

```
[git] fatal: ambiguous argument 'cbdcaf44ab5c217fbe9399593bdbc579e8ead5f3^..0c3c33ee67aa4f560e3d47fb708c4f9cba797a52': unknown revision or path not in the working tree.
failed to scan Git repository error="stderr is not empty"
```

## Root Cause

**Primary Issue (FIXED)**: `.gitleaks.toml` structure incompatibility with Gitleaks v8
- ✅ **Status**: Fixed - Config structure corrected

**Secondary Issue (NEW)**: Git history not available for commit range scanning
- The workflow uses `fetch-depth: 1` for PRs (shallow clone)
- Gitleaks tries to scan commit range `cbdcaf4^..0c3c33e`
- The parent commit `cbdcaf4^` is not available in shallow clone
- Gitleaks fails because it can't access the full commit history

## Impact

- **Config parsing**: ✅ Working (no more structure errors)
- **Secret detection**: ⚠️ Partial (scan completed but with warnings)
- **Workflow status**: ❌ Fails due to git history error

## Solution Options

### Option 1: Increase Fetch Depth (Recommended)

Update `.github/workflows/secret-scan.yml`:

```yaml
- name: Checkout code
  uses: actions/checkout@v6
  with:
    # Fetch enough history for Gitleaks to scan commit ranges
    fetch-depth: 50  # Or 0 for full history (slower)
```

**Pros**: Simple, allows Gitleaks to scan commit ranges  
**Cons**: Slightly slower checkout

### Option 2: Use `--no-git` Mode

Update workflow to scan files directly instead of git history:

```yaml
- name: Run Gitleaks
  uses: gitleaks/gitleaks-action@v2
  with:
    no-git: true  # Scan files directly, not git history
    # ... other options
```

**Pros**: Faster, works with shallow clones  
**Cons**: Doesn't scan commit history (only current files)

### Option 3: Conditional Fetch Depth

Fetch more history only when needed:

```yaml
- name: Checkout code
  uses: actions/checkout@v6
  with:
    fetch-depth: ${{ github.event_name == 'schedule' && 0 || 50 }}
```

**Pros**: Balance between speed and functionality  
**Cons**: Still may not have enough history for all cases

## Recommendation

**Use Option 1** with `fetch-depth: 50`:
- Provides enough history for most commit range scans
- Still reasonably fast
- Maintains full scanning capability
- Better than full history (`fetch-depth: 0`) for performance

## Status

- ✅ Config structure fixed
- ⚠️ Git history issue identified
- ⏳ Fix pending (workflow update needed)

## Related

- `docs/development/ci-cd/PIPELINE_ISSUES_ROOT_CAUSE_ANALYSIS.md` - Original root cause analysis
- `.github/workflows/secret-scan.yml` - Secret scanning workflow
