#!/bin/bash

# Post-Merge Cleanup Automation
# Automatically cleans up after PR merge

set -e

PR_NUMBER="${1:-}"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         POST-MERGE CLEANUP (AUTOMATED)                     ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Step 0: Check other agents and worktrees (REQUIRED)
echo "0️⃣  Checking other active work..."
echo ""

WORKTREES=$(git worktree list --porcelain 2>/dev/null | grep -E "^worktree" | sed 's/worktree //' || echo "")
FEATURE_BRANCHES=$(git branch | grep -E "^  (feature|fix|refactor|docs|test)/" | sed 's/^  //' || echo "")

if [ -n "$WORKTREES" ] || [ -n "$FEATURE_BRANCHES" ]; then
    echo "   📋 Active work detected:"
    echo ""
    
    if [ -n "$WORKTREES" ]; then
        echo "   Active Worktrees:"
        while IFS= read -r worktree; do
            BRANCH=$(git -C "$worktree" branch --show-current 2>/dev/null || echo "unknown")
            echo "     - $worktree [branch: $BRANCH]"
        done <<< "$WORKTREES"
        echo ""
    fi
    
    if [ -n "$FEATURE_BRANCHES" ]; then
        echo "   Active Feature Branches:"
        while IFS= read -r branch; do
            # Check if merged
            if git branch --merged main | grep -q "^  $branch$"; then
                echo "     - $branch [MERGED - safe to delete]"
            else
                echo "     - $branch [ACTIVE - do not delete]"
            fi
        done <<< "$FEATURE_BRANCHES"
        echo ""
    fi
    
    echo "   ⚠️  Please verify these are correct before cleanup proceeds."
    echo ""
    read -p "   Continue with cleanup? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "   ❌ Cleanup cancelled"
        exit 0
    fi
    echo ""
fi

# Step 1: Verify PR is merged (if PR number provided)
if [ -n "$PR_NUMBER" ]; then
    echo "1️⃣  Verifying PR #$PR_NUMBER merge status..."
    if command -v gh >/dev/null 2>&1; then
        MERGED=$(gh pr view "$PR_NUMBER" --json merged -q .merged 2>/dev/null || echo "false")
        if [ "$MERGED" = "true" ]; then
            echo "   ✅ PR #$PR_NUMBER is merged"
        else
            echo "   ⚠️  PR #$PR_NUMBER is not merged yet"
            echo "   → Skipping cleanup until PR is merged"
            exit 0
        fi
    else
        echo "   ⚠️  GitHub CLI not found, skipping PR verification"
    fi
    echo ""
fi

# Step 2: Switch to main and pull latest
echo "2️⃣  Switching to main and pulling latest..."
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")

if [ "$CURRENT_BRANCH" != "main" ]; then
    echo "   → Switching from $CURRENT_BRANCH to main..."
    git checkout main 2>/dev/null || {
        echo "   ❌ Could not switch to main (may have uncommitted changes)"
        echo "   → Commit or stash changes first"
        exit 1
    }
fi

echo "   → Pulling latest from origin..."
git pull origin main 2>/dev/null || {
    echo "   ⚠️  Could not pull from origin (may be offline)"
}
echo "   ✅ On main branch with latest changes"
echo ""

# Step 3: Delete merged branches
echo "3️⃣  Cleaning up merged branches..."
MERGED_BRANCHES=$(git branch --merged main | grep -E "  (feature|fix|refactor|docs|test)/" | sed 's/^  //' || echo "")

if [ -n "$MERGED_BRANCHES" ]; then
    echo "   Found merged branches:"
    while IFS= read -r branch; do
        echo "     - $branch"
        if git branch -d "$branch" 2>/dev/null; then
            echo "       ✅ Deleted"
        else
            echo "       ⚠️  Could not delete (may have unmerged commits)"
        fi
    done <<< "$MERGED_BRANCHES"
else
    echo "   ✅ No merged branches to clean up"
fi
echo ""

# Step 4: Remove worktrees (if PR number provided, try to find associated worktree)
if [ -n "$PR_NUMBER" ]; then
    echo "4️⃣  Checking for associated worktree..."
    # Try to find worktree based on PR number or branch name
    # This is a heuristic - may need adjustment
    WORKTREE_PATTERN="electric-sheep-*"
    for worktree in $WORKTREE_PATTERN; do
        if [ -d "../$worktree" ]; then
            BRANCH=$(git -C "../$worktree" branch --show-current 2>/dev/null || echo "")
            if [ -n "$BRANCH" ] && git branch --merged main | grep -q "^  $BRANCH$"; then
                echo "   → Found merged worktree: $worktree"
                read -p "   Remove worktree $worktree? (y/N): " -n 1 -r
                echo ""
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    git worktree remove "../$worktree" 2>/dev/null && echo "   ✅ Removed" || echo "   ⚠️  Could not remove"
                fi
            fi
        fi
    done
    echo ""
fi

# Step 5: Delete temp branches
echo "5️⃣  Cleaning up temp branches..."
TEMP_BRANCHES=$(git branch | grep -E "^(  )?(temp|tmp|test-)" | sed 's/^  //' || echo "")

if [ -n "$TEMP_BRANCHES" ]; then
    echo "   Found temp branches:"
    while IFS= read -r branch; do
        echo "     - $branch"
        if git branch -D "$branch" 2>/dev/null; then
            echo "       ✅ Deleted"
        else
            echo "       ⚠️  Could not delete"
        fi
    done <<< "$TEMP_BRANCHES"
else
    echo "   ✅ No temp branches to clean up"
fi
echo ""

# Step 6: Prune worktrees
echo "6️⃣  Pruning stale worktree references..."
git worktree prune 2>/dev/null || true
echo "   ✅ Worktree references cleaned"
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    CLEANUP COMPLETE                      ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "✅ Cleanup completed!"
echo ""
echo "💡 Next steps:"
echo "   1. Start new work from fresh main: git checkout -b feature/<new-task>"
echo "   2. Run pre-work check: ./scripts/pre-work-check.sh"
echo ""

