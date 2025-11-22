# Label Workflow Integration - Proposal

**Date**: 2025-01-20  
**Status**: Proposal

## Overview

Integrate automatic label updates into the workflow to ensure issue labels (especially status labels) are always up to date as work progresses.

## Integration Points

### 1. **Pre-Work Check** (When Starting Work)
**Trigger**: `./scripts/pre-work-check.sh`  
**Action**: Update issue status from `status:not-started` → `status:in-progress`

**When**:
- User starts work on an issue
- Branch name matches issue number or title
- Issue exists and is in `status:not-started`

**Implementation**:
- Detect issue from branch name (e.g., `feature/52-configure-release-signing`)
- Update issue label: Remove `status:not-started`, add `status:in-progress`
- Optional: Add comment to issue "Work started on this issue"

### 2. **PR Creation** (When Creating Pull Request)
**Trigger**: When PR is created  
**Action**: Update issue status from `status:in-progress` → `status:review`

**When**:
- PR is created with issue reference (e.g., "Fixes #52")
- Issue is in `status:in-progress`

**Implementation**:
- GitHub Actions workflow or PR template hook
- Detect issue from PR description/body
- Update issue label: Remove `status:in-progress`, add `status:review`
- Link PR to issue (automatic if PR references issue)

### 3. **PR Merged** (When PR is Merged)
**Trigger**: When PR is merged  
**Action**: Update issue status from `status:review` → `status:completed` (or close issue)

**When**:
- PR is merged
- Issue is in `status:review`

**Implementation**:
- GitHub Actions workflow (on PR merge)
- Detect issue from PR description/body
- Update issue label: Remove `status:review`, add `status:completed`
- Optional: Close issue automatically

### 4. **Manual Updates** (When Blocked)
**Trigger**: Manual or when dependencies detected  
**Action**: Update issue status to `status:blocked`

**When**:
- User manually marks as blocked
- Dependency detected (e.g., another issue must be completed first)

**Implementation**:
- Helper script: `./scripts/update-issue-status.sh <issue-number> blocked`
- Or manual update via GitHub UI

## Proposed Implementation

### Helper Script: `update-issue-status.sh`

**Purpose**: Update issue status labels automatically

**Usage**:
```bash
# Update issue status
./scripts/update-issue-status.sh <issue-number> <status>

# Examples:
./scripts/update-issue-status.sh 52 in-progress
./scripts/update-issue-status.sh 52 blocked
./scripts/update-issue-status.sh 52 review
./scripts/update-issue-status.sh 52 completed
```

**Features**:
- Remove old status label
- Add new status label
- Preserve other labels
- Add comment to issue (optional)
- Validate status transition (e.g., can't go from `completed` → `in-progress`)

### Integration with Pre-Work Check

**Add to `pre-work-check.sh`**:
```bash
# 8.5. Update issue status (if working on issue)
echo "8️⃣.5️⃣  Updating issue status..."
if [ -f "scripts/update-issue-status.sh" ]; then
    # Detect issue from branch name or environment
    ISSUE_NUMBER=$(detect_issue_from_branch "$CURRENT_BRANCH")
    if [ -n "$ISSUE_NUMBER" ]; then
        echo "   → Detected issue #$ISSUE_NUMBER from branch name"
        echo "   → Updating status to 'in-progress'..."
        ./scripts/update-issue-status.sh "$ISSUE_NUMBER" in-progress
    else
        echo "   💡 Tip: Branch name format 'feature/52-<description>' auto-updates issue status"
    fi
fi
```

### Integration with PR Creation

**GitHub Actions Workflow** (`.github/workflows/update-issue-labels.yml`):
```yaml
name: Update Issue Labels

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  update-labels:
    runs-on: ubuntu-latest
    steps:
      - name: Extract issue numbers
        id: extract-issues
        run: |
          # Extract issue numbers from PR body/description
          ISSUES=$(echo "${{ github.event.pull_request.body }}" | grep -oE '#[0-9]+' | sed 's/#//' | tr '\n' ' ')
          echo "issues=$ISSUES" >> $GITHUB_OUTPUT
      
      - name: Update issue status to review
        if: steps.extract-issues.outputs.issues != ''
        run: |
          for issue in ${{ steps.extract-issues.outputs.issues }}; do
            gh issue edit "$issue" --remove-label "status:in-progress" --add-label "status:review"
          done
```

### Integration with PR Merge

**GitHub Actions Workflow** (update existing or add to release workflow):
```yaml
name: Complete Issues on Merge

on:
  pull_request:
    types: [closed]

jobs:
  complete-issues:
    if: github.event.pull_request.merged == true
    runs-on: ubuntu-latest
    steps:
      - name: Extract issue numbers
        id: extract-issues
        run: |
          ISSUES=$(echo "${{ github.event.pull_request.body }}" | grep -oE '#[0-9]+' | sed 's/#//' | tr '\n' ' ')
          echo "issues=$ISSUES" >> $GITHUB_OUTPUT
      
      - name: Mark issues as completed
        if: steps.extract-issues.outputs.issues != ''
        run: |
          for issue in ${{ steps.extract-issues.outputs.issues }}; do
            gh issue edit "$issue" --remove-label "status:review" --add-label "status:completed"
            # Optionally close issue
            # gh issue close "$issue"
          done
```

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

## Benefits

1. ✅ **Always Up to Date**: Labels reflect current work state
2. ✅ **Better Visibility**: Easy to see what's in progress, blocked, or in review
3. ✅ **Automated**: Reduces manual work
4. ✅ **Consistent**: Enforces proper status transitions
5. ✅ **Filtering**: Better filtering and reporting

## Implementation Plan

### Phase 1: Helper Script
1. Create `scripts/update-issue-status.sh`
2. Test with manual updates
3. Document usage

### Phase 2: Pre-Work Integration
1. Add issue detection to `pre-work-check.sh`
2. Auto-update status when starting work
3. Test with branch naming convention

### Phase 3: PR Integration
1. Create GitHub Actions workflow for PR creation
2. Create GitHub Actions workflow for PR merge
3. Test with sample PRs

### Phase 4: Documentation
1. Update workflow documentation
2. Update cursor rules
3. Add examples

## Branch Naming Convention for Auto-Detection

**Proposed Format**: `<type>/<issue-number>-<description>`

**Examples**:
- `feature/52-configure-release-signing`
- `fix/55-crash-reporting-integration`
- `refactor/60-ssl-certificate-pinning`

**Benefits**:
- Easy to extract issue number from branch name
- Clear connection between branch and issue
- Automatic status updates

## Manual Override

**Always allow manual updates**:
- Users can manually update labels via GitHub UI
- Helper script can be run manually
- No enforcement, just automation assistance

## Related Documentation

- `docs/development/analysis/LABEL_SYSTEM_FINAL.md` - Label structure
- `docs/development/BACKLOG_GITHUB_ISSUES_APPROACH.md` - Issue workflow
- `.cursor/rules/improvements-backlog.mdc` - TODO/Issue integration

