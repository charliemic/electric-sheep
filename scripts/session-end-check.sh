#!/bin/bash

# Session End Check - Verify merge and cleanup before ending session
# Usage: ./scripts/session-end-check.sh [pr-number]

set -e

PR_NUMBER="${1:-}"
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         SESSION END CHECK                                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Check PR status
if [ -n "$PR_NUMBER" ]; then
    echo "1️⃣  Checking PR #$PR_NUMBER status..."
    
    if ! command -v gh >/dev/null 2>&1; then
        echo "   ⚠️  GitHub CLI not found, cannot check PR status"
        echo "   → Please check PR status manually: https://github.com/charliemic/electric-sheep/pull/$PR_NUMBER"
    else
        PR_STATE=$(gh pr view "$PR_NUMBER" --json state,mergedAt,isDraft,mergeable --jq -r '.state' 2>/dev/null || echo "UNKNOWN")
        PR_MERGED=$(gh pr view "$PR_NUMBER" --json mergedAt --jq -r '.mergedAt' 2>/dev/null || echo "null")
        PR_DRAFT=$(gh pr view "$PR_NUMBER" --json isDraft --jq -r '.isDraft' 2>/dev/null || echo "false")
        
        if [ "$PR_MERGED" != "null" ] && [ -n "$PR_MERGED" ]; then
            echo "   ✅ PR #$PR_NUMBER is MERGED"
            echo "   → Merged at: $PR_MERGED"
            PR_IS_MERGED=true
        elif [ "$PR_DRAFT" = "true" ]; then
            echo "   ⚠️  PR #$PR_NUMBER is still DRAFT"
            echo "   → PR must be marked ready and merged before cleanup"
            PR_IS_MERGED=false
        elif [ "$PR_STATE" = "CLOSED" ]; then
            echo "   ⚠️  PR #$PR_NUMBER is CLOSED (not merged)"
            echo "   → PR was closed without merging"
            PR_IS_MERGED=false
        else
            echo "   ⚠️  PR #$PR_NUMBER is still OPEN"
            echo "   → PR must be merged before cleanup"
            PR_IS_MERGED=false
        fi
    fi
    echo ""
else
    echo "1️⃣  No PR number provided"
    echo "   → Skipping PR status check"
    echo "   → To check PR: ./scripts/session-end-check.sh <pr-number>"
    PR_IS_MERGED=false
    echo ""
fi

# Step 2: Check if cleanup is needed
if [ "$PR_IS_MERGED" = "true" ]; then
    echo "2️⃣  PR is merged - Running cleanup..."
    echo ""
    
    if [ -f "scripts/post-merge-cleanup.sh" ]; then
        echo "   → Running post-merge cleanup..."
        ./scripts/post-merge-cleanup.sh "$PR_NUMBER"
    else
        echo "   ⚠️  post-merge-cleanup.sh not found"
        echo "   → Manual cleanup required"
    fi
else
    echo "2️⃣  PR not merged yet - Cleanup will run after merge"
    echo ""
    echo "   📋 To complete session end after PR merge:"
    echo "   1. Merge PR #$PR_NUMBER"
    echo "   2. Run: ./scripts/post-merge-cleanup.sh $PR_NUMBER"
    echo "   3. Verify cleanup completed"
    echo ""
fi

# Step 3: Final status
echo "3️⃣  Final Status Check"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ "$PR_IS_MERGED" = "true" ]; then
    # Check if we're on main
    CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
    if [ "$CURRENT_BRANCH" = "main" ]; then
        echo "   ✅ On main branch"
    else
        echo "   ⚠️  Not on main branch (current: $CURRENT_BRANCH)"
        echo "   → Should switch to main after cleanup"
    fi
    
    # Check for merged branches
    MERGED_BRANCHES=$(git branch --merged main | grep -E "  (feature|fix|refactor|docs|test)/" | sed 's/^  //' || echo "")
    if [ -z "$MERGED_BRANCHES" ]; then
        echo "   ✅ No merged branches to clean up"
    else
        echo "   ⚠️  Merged branches still exist:"
        echo "$MERGED_BRANCHES" | sed 's/^/     - /'
        echo "   → Should be deleted by cleanup script"
    fi
    
    # Check for worktrees
    WORKTREES=$(git worktree list --porcelain 2>/dev/null | grep -E "^worktree" | sed 's/worktree //' || echo "")
    if [ -z "$WORKTREES" ]; then
        echo "   ✅ No worktrees to clean up"
    else
        echo "   ⚠️  Worktrees still exist:"
        echo "$WORKTREES" | sed 's/^/     - /'
        echo "   → Should be removed by cleanup script"
    fi
    
    echo ""
    echo "   ✅ Session end complete - PR merged and cleanup done"
else
    echo "   ⚠️  Session end incomplete - PR not merged yet"
    echo ""
    echo "   📋 Next steps:"
    echo "   1. Review PR #$PR_NUMBER: https://github.com/charliemic/electric-sheep/pull/$PR_NUMBER"
    echo "   2. Mark PR as ready (if draft)"
    echo "   3. Merge PR after CI passes"
    echo "   4. Run: ./scripts/post-merge-cleanup.sh $PR_NUMBER"
    echo "   5. Verify cleanup completed"
    echo ""
    echo "   ❌ Session cannot end until PR is merged and cleanup is done"
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║         SESSION END CHECK COMPLETE                         ║"
echo "╚════════════════════════════════════════════════════════════╝"

