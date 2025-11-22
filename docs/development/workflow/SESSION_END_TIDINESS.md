# Session End Tidiness - Repository Cleanup

**Date**: 2025-01-20  
**Purpose**: Ensure repository is tidy before ending session

## CRITICAL: Repository Must Be Tidy Before Session End

**Before ending a session, the repository should be in a clean, tidy state:**

### Required Checks

1. ✅ **PR is merged** (if PR was created)
2. ✅ **Merged branches deleted** (no stale branches)
3. ✅ **Worktrees removed** (if used for this session)
4. ✅ **Untracked files handled** (committed, stashed, or removed)
5. ✅ **Uncommitted changes resolved** (committed or stashed)
6. ✅ **On main branch** (after cleanup)
7. ✅ **Repository is tidy** (no clutter, ready for next session)

## Why Tidiness Matters

### Benefits

- ✅ **Clear State**: Easy to see what's active vs completed
- ✅ **No Confusion**: Don't accidentally work on merged branches
- ✅ **Clean Start**: Next session starts from clean state
- ✅ **Better Organization**: Easier to track active work
- ✅ **Prevents Errors**: Avoid working on wrong branches

### Problems from Untidy Repository

- ❌ **Confusion**: Which branches are active vs merged?
- ❌ **Accidental Work**: Working on merged/abandoned branches
- ❌ **Clutter**: Hard to see what needs attention
- ❌ **Wasted Time**: Cleaning up later instead of during session
- ❌ **Merge Conflicts**: Stale branches can cause conflicts

## Session End Checklist

### 1. Verify PR Status
```bash
# Check if PR is merged
gh pr view <pr-number> --json state,mergedAt
```

### 2. Run Cleanup
```bash
# Automated cleanup (if PR merged)
./scripts/post-merge-cleanup.sh <pr-number>

# Or manual cleanup
git checkout main
git pull origin main
git branch -d <merged-branch>
git worktree remove <worktree-path>
```

### 3. Check Repository State
```bash
# Check for merged branches
git branch --merged main | grep -v "main"

# Check for worktrees
git worktree list

# Check for untracked files
git status --porcelain | grep "^??"

# Check for uncommitted changes
git status --porcelain | grep -v "^??"
```

### 4. Handle Untracked Files
```bash
# Option 1: Commit if important
git add <file>
git commit -m "docs: session summary"

# Option 2: Stash if temporary
git stash push -m "Session end: temporary files"

# Option 3: Remove if not needed
rm <file>
```

### 5. Handle Uncommitted Changes
```bash
# Option 1: Commit if complete
git add <files>
git commit -m "feat: complete work"

# Option 2: Stash if incomplete
git stash push -m "WIP: incomplete work"

# Option 3: Discard if not needed (careful!)
git checkout -- <file>
```

### 6. Verify Final State
```bash
# Should be clean
git status

# Should be on main
git branch --show-current

# Should have no merged branches
git branch --merged main | grep -v "main"
```

## Automated Session End Check

**Use the session end check script:**

```bash
./scripts/session-end-check.sh <pr-number>
```

**This script verifies:**
- ✅ PR is merged
- ✅ Cleanup is complete
- ✅ On main branch
- ✅ Merged branches deleted
- ✅ Worktrees removed
- ✅ Untracked files handled
- ✅ Uncommitted changes resolved
- ✅ Repository is tidy

## Integration with Workflow

### Pre-Session End

**Before ending session, run:**

```bash
# 1. Check PR status
gh pr view <pr-number> --json state,mergedAt

# 2. If merged, run cleanup
./scripts/post-merge-cleanup.sh <pr-number>

# 3. Verify tidiness
./scripts/session-end-check.sh <pr-number>
```

### During Session

**Keep repository tidy throughout:**
- Commit frequently (prevents accumulation)
- Handle untracked files as you go
- Delete merged branches immediately
- Remove worktrees after merge

### After Session End

**Repository should be:**
- ✅ On main branch
- ✅ Up to date with remote
- ✅ No merged branches
- ✅ No worktrees for merged work
- ✅ No untracked files
- ✅ No uncommitted changes
- ✅ Ready for next session

## Examples

### Good: Tidy Repository
```bash
$ git status
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean

$ git branch --merged main
* main

$ git worktree list
/path/to/electric-sheep  abc1234 [main]
```

### Bad: Untidy Repository
```bash
$ git status
On branch experimental/onboarding-validation-issue-52
Changes not staged for commit:
  modified:   SESSION_END_SUMMARY.md
Untracked files:
  SENIOR_DEV_REVIEW_FEEDBACK.md
  docs/development/analysis/SESSION_SUMMARY_APP_SPECIFICITY.md

$ git branch --merged main
  docs/ai-driven-coding-lessons-learned
  experimental/onboarding-validation-issue-52
  feature/branch-based-publishing
* main

$ git worktree list
/path/to/electric-sheep  abc1234 [experimental/onboarding-validation-issue-52]
/path/to/electric-sheep-other  def5678 [feature/other]
```

## Related Documentation

- `.cursor/rules/repository-maintenance.mdc` - Repository maintenance rules
- `.cursor/rules/scope-creep-detection.mdc` - Session end requirements
- `scripts/session-end-check.sh` - Session end verification script
- `scripts/post-merge-cleanup.sh` - Post-merge cleanup automation

