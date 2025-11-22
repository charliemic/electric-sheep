# OAuth Scope Requirements

**Date**: 2025-01-22  
**Purpose**: Document OAuth scope requirements for different PR types and operations

---

## Overview

Different GitHub operations require different OAuth scopes. This document outlines which scopes are needed for various operations and how to handle cases where the required scope is not available.

---

## OAuth Scope Reference

### Standard PRs (Code Changes)

**Required Scope**: `repo` (read/write)

**Operations**:
- ✅ Create PRs
- ✅ Merge PRs (squash, merge, rebase)
- ✅ Read repository contents
- ✅ Write to repository
- ✅ Update PRs

**Can Merge**: ✅ Yes (with standard OAuth token)

**Example**:
```bash
gh pr merge 75 --squash --delete-branch
# Works with standard repo scope
```

---

### Workflow File PRs

**Required Scope**: `repo` + `workflow` (read/write + workflow)

**Operations**:
- ✅ Create PRs
- ❌ Merge PRs that modify `.github/workflows/` files
- ✅ Read workflow files
- ❌ Write to workflow files (via PR merge)

**Can Merge**: ❌ No (requires `workflow` scope)

**Workaround**: Manual merge via GitHub UI (has workflow scope)

**Example**:
```bash
gh pr merge 72 --squash --delete-branch
# Error: "refusing to allow an OAuth App to create or update workflow 
# .github/workflows/xxx.yml without workflow scope"
```

**Affected PRs**:
- PRs that modify `.github/workflows/*.yml` files
- Dependabot PRs updating GitHub Actions versions
- Any PR that touches workflow files

---

### Security-Sensitive Operations

**Required Scope**: `repo` + `security_events` (read/write + security)

**Operations**:
- ✅ Read security alerts
- ✅ Dismiss security alerts
- ✅ Update security settings

**Can Merge**: ✅ Yes (if security scope available)

---

### Repository Settings Changes

**Required Scope**: `admin:repo` (admin access)

**Operations**:
- ✅ Modify repository settings
- ✅ Change branch protection rules
- ✅ Update repository secrets

**Can Merge**: ❌ No (requires admin scope)

**Workaround**: Manual changes via GitHub UI

---

## Common Scenarios

### Scenario 1: Standard Feature PR

**PR Type**: Code changes (not workflow files)  
**Required Scope**: `repo`  
**Can Merge**: ✅ Yes  
**Action**: Use standard `gh pr merge` command

```bash
gh pr merge <pr-number> --squash --delete-branch
```

---

### Scenario 2: Dependabot PR Updating Workflow Files

**PR Type**: Workflow file changes  
**Required Scope**: `repo` + `workflow`  
**Can Merge**: ❌ No (workflow scope required)  
**Action**: Manual merge via GitHub UI

**Steps**:
1. Open PR in GitHub UI
2. Click "Merge pull request"
3. Select merge method (squash, merge, or rebase)
4. Confirm merge

**Why Manual?**: GitHub UI has workflow scope, CLI token may not

---

### Scenario 3: Multiple PRs with Mixed Types

**Situation**: Some PRs are standard, some modify workflow files

**Strategy**:
1. Merge standard PRs via CLI (have `repo` scope)
2. Merge workflow PRs via GitHub UI (need `workflow` scope)

**Example**:
```bash
# Standard PRs - merge via CLI
gh pr merge 75 --squash --delete-branch  # ✅ Works
gh pr merge 84 --squash --delete-branch  # ✅ Works

# Workflow PRs - merge via GitHub UI
# PR #72, #71, #70 - manual merge required
```

---

## Checking OAuth Scope

### Current Token Scopes

**Check via GitHub CLI**:
```bash
gh auth status
# Shows current token scopes
```

**Check via GitHub API**:
```bash
gh api user
# Returns user info (indirect scope check)
```

### Required Scopes for Operations

**Standard Operations**:
- `repo` - Read/write repository access

**Workflow Operations**:
- `repo` + `workflow` - Read/write + workflow file access

**Admin Operations**:
- `admin:repo` - Full repository admin access

---

## Workarounds

### Workaround 1: Manual Merge via GitHub UI

**When**: Workflow scope not available  
**How**: Use GitHub web interface  
**Why**: GitHub UI has workflow scope by default

**Steps**:
1. Navigate to PR in GitHub
2. Click "Merge pull request"
3. Select merge method
4. Confirm merge

---

### Workaround 2: Update OAuth Token

**When**: Need automated workflow PR merging  
**How**: Update OAuth token to include `workflow` scope  
**Why**: Enables automated merging of workflow PRs

**Steps**:
1. Generate new OAuth token with `workflow` scope
2. Update `gh` CLI authentication
3. Retry PR merge

**Note**: Consider security implications of broader scope

---

### Workaround 3: Accept Manual Merge

**When**: Workflow scope not available, automation not critical  
**How**: Accept manual merge for workflow PRs  
**Why**: Workflow PRs are infrequent, manual merge is acceptable

**Strategy**: 
- Automate standard PRs (have `repo` scope)
- Manual merge workflow PRs (need `workflow` scope)

---

## Best Practices

### 1. Check PR Type Before Merging

**Before merging, check if PR modifies workflow files**:
```bash
gh pr view <pr-number> --json files --jq '.files[] | select(.path | contains(".github/workflows")) | .path'
```

**If workflow files modified**:
- Expect workflow scope requirement
- Plan for manual merge if scope not available

---

### 2. Batch Process by Type

**Strategy**: 
- Merge all standard PRs first (via CLI)
- Then handle workflow PRs (via UI or with workflow scope)

**Benefits**:
- Maximize automation
- Minimize manual intervention
- Clear separation of concerns

---

### 3. Document Scope Requirements

**For each PR type, document**:
- Required OAuth scopes
- Merge method (CLI vs UI)
- Workarounds if scope not available

**Update this document** when new scope requirements are discovered

---

## Troubleshooting

### Error: "refusing to allow an OAuth App to create or update workflow"

**Cause**: Missing `workflow` OAuth scope

**Solution**:
1. Manual merge via GitHub UI (recommended)
2. Update OAuth token to include `workflow` scope
3. Accept manual merge for workflow PRs

---

### Error: "the base branch policy prohibits the merge"

**Cause**: Branch protection rules (CI checks must pass)

**Solution**:
1. Ensure all CI checks pass
2. Wait for CI to complete
3. Retry merge after checks pass

---

### Error: "Pull request Auto merge is not allowed"

**Cause**: Repository doesn't allow auto-merge

**Solution**:
1. Use manual merge
2. Or enable auto-merge in repository settings

---

## Related Documentation

- `docs/development/reports/PR_MERGE_INCIDENT_ANALYSIS.md` - Incident analysis
- `docs/development/reports/ACTIONABLE_INSIGHTS_PRIORITIZED.md` - Actionable insights
- `.cursor/rules/cicd.mdc` - CI/CD principles
- GitHub OAuth Scopes: https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/scopes-for-oauth-apps

---

## Summary

| PR Type | Required Scope | Can Merge via CLI | Workaround |
|---------|---------------|-------------------|------------|
| Standard (code) | `repo` | ✅ Yes | N/A |
| Workflow files | `repo` + `workflow` | ❌ No | Manual merge via UI |
| Security | `repo` + `security_events` | ✅ Yes | N/A |
| Admin | `admin:repo` | ❌ No | Manual via UI |

**Recommendation**: Accept manual merge for workflow PRs (infrequent, acceptable overhead)

