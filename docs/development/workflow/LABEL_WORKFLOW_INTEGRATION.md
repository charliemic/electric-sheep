# Label Workflow Integration - Complete Guide

**Date**: 2025-01-20  
**Status**: Implementation Guide

## Overview

This document describes how issue labels (especially status labels) are automatically updated as work progresses, ensuring labels are always up to date.

## Integration Points

### 1. **Pre-Work Check** (When Starting Work)

**Location**: `scripts/pre-work-check.sh`  
**Trigger**: When running pre-work check before starting work  
**Action**: Auto-detect issue from branch name and update `status:not-started` → `status:in-progress`

**How It Works**:
- Detects issue number from branch name (format: `feature/52-<description>`)
- If issue found, automatically updates status to `in-progress`
- Uses helper script: `./scripts/update-issue-status.sh <issue-number> in-progress`

**Branch Naming Convention**:
```
feature/52-configure-release-signing  → Detects issue #52
fix/55-crash-reporting-integration    → Detects issue #55
refactor/60-ssl-certificate-pinning   → Detects issue #60
```

**Manual Override**:
- Can manually run: `./scripts/update-issue-status.sh <issue-number> in-progress`
- Or update via GitHub UI

### 2. **PR Creation** (When Creating Pull Request)

**Location**: `.github/workflows/update-issue-labels.yml`  
**Trigger**: When PR is opened or updated  
**Action**: Auto-detect issues from PR description and update `status:in-progress` → `status:review`

**How It Works**:
- Extracts issue numbers from PR body/title (e.g., "Fixes #52", "Closes #55", "#60")
- Updates each referenced issue: Remove `status:in-progress`, add `status:review`
- Runs automatically via GitHub Actions

**PR Description Format**:
```markdown
Fixes #52
Closes #55
Related to #60
```

### 3. **PR Merge** (When PR is Merged)

**Location**: `.github/workflows/update-issue-labels.yml`  
**Trigger**: When PR is merged  
**Action**: Auto-detect issues from PR description and update `status:review` → `status:completed`

**How It Works**:
- Extracts issue numbers from PR body/title
- Updates each referenced issue: Remove `status:review`, add `status:completed`
- Runs automatically via GitHub Actions

### 4. **Manual Updates** (When Blocked or Special Cases)

**Location**: Manual or script  
**Trigger**: When work is blocked or special status needed  
**Action**: Update to `status:blocked` or other status

**How It Works**:
- Run helper script: `./scripts/update-issue-status.sh <issue-number> blocked`
- Or update via GitHub UI

## Helper Script: `update-issue-status.sh`

### Usage

```bash
./scripts/update-issue-status.sh <issue-number> <status>
```

### Status Values

- `not-started` - Not yet started
- `in-progress` - Currently being worked on
- `blocked` - Blocked by dependency
- `review` - In code review
- `completed` - Completed and merged

### Examples

```bash
# Start work on issue
./scripts/update-issue-status.sh 52 in-progress

# Mark as blocked
./scripts/update-issue-status.sh 52 blocked

# Mark as in review
./scripts/update-issue-status.sh 52 review

# Mark as completed
./scripts/update-issue-status.sh 52 completed
```

### Features

- ✅ Validates status value
- ✅ Removes old status label
- ✅ Adds new status label
- ✅ Preserves other labels
- ✅ Adds comment to issue (optional)
- ✅ Error handling and validation

## Status Transition Rules

### Valid Transitions

```
status:not-started → status:in-progress ✅
status:in-progress → status:blocked ✅
status:in-progress → status:review ✅
status:blocked → status:in-progress ✅
status:review → status:completed ✅
status:review → status:blocked ✅ (if PR blocked)
```

### Invalid Transitions

```
status:completed → status:in-progress ❌ (reopen issue instead)
status:review → status:not-started ❌ (can't go backwards)
```

## Workflow Integration

### Complete Workflow

1. **Create Issue**: `status:not-started` (default)
2. **Start Work**: 
   - Run `./scripts/pre-work-check.sh`
   - Auto-updates to `status:in-progress` (if branch name includes issue number)
   - Or manually: `./scripts/update-issue-status.sh <issue-number> in-progress`
3. **Create PR**: 
   - Reference issue in PR description (e.g., "Fixes #52")
   - Auto-updates to `status:review` (via GitHub Actions)
4. **PR Merged**: 
   - Auto-updates to `status:completed` (via GitHub Actions)
   - Issue can be closed automatically (optional)

### Branch Naming for Auto-Detection

**Recommended Format**: `<type>/<issue-number>-<description>`

**Examples**:
- `feature/52-configure-release-signing`
- `fix/55-crash-reporting-integration`
- `refactor/60-ssl-certificate-pinning`

**Benefits**:
- Automatic issue detection
- Clear connection between branch and issue
- Automatic status updates

## Benefits

1. ✅ **Always Up to Date**: Labels reflect current work state automatically
2. ✅ **Less Manual Work**: Automation reduces manual label updates
3. ✅ **Better Visibility**: Easy to see what's in progress, blocked, or in review
4. ✅ **Consistent**: Enforces proper status transitions
5. ✅ **Filterable**: Better filtering and reporting in GitHub

## Implementation Status

### ✅ Completed

- Helper script created: `scripts/update-issue-status.sh`
- Pre-work check integration: Auto-detects issue from branch name
- GitHub Actions workflow: Auto-updates on PR creation and merge
- Documentation: Complete guide created

### 📋 Optional Enhancements

- Auto-close issues when marked as `status:completed`
- Add more context in issue comments (branch name, PR link)
- Validate status transitions (prevent invalid transitions)
- Integration with TODO.md updates

## Usage Examples

### Example 1: Starting Work on Issue #52

```bash
# 1. Create branch with issue number
git checkout -b feature/52-configure-release-signing

# 2. Run pre-work check (auto-updates issue status)
./scripts/pre-work-check.sh
# → Detects issue #52 from branch name
# → Updates issue #52: status:not-started → status:in-progress

# 3. Start working...
```

### Example 2: Creating PR

```markdown
# PR Description
Fixes #52

## Changes
- Configured release signing
- Added keystore generation script
- Updated CI/CD for signing
```

**Result**: GitHub Actions automatically updates issue #52 to `status:review`

### Example 3: PR Merged

**Result**: GitHub Actions automatically updates issue #52 to `status:completed`

### Example 4: Manual Update (Blocked)

```bash
# Issue is blocked by dependency
./scripts/update-issue-status.sh 52 blocked
```

## Related Documentation

- `docs/development/analysis/LABEL_SYSTEM_FINAL.md` - Label structure
- `docs/development/BACKLOG_GITHUB_ISSUES_APPROACH.md` - Issue workflow
- `.cursor/rules/improvements-backlog.mdc` - TODO/Issue integration
- `scripts/update-issue-status.sh` - Helper script

