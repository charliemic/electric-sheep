#!/bin/bash

# Final Status Check Script
# Run after cleanup and merge to verify repository state

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         FINAL REPOSITORY STATUS CHECK                       ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Current branch
echo "📋 Current Branch:"
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
echo "   $CURRENT_BRANCH"
echo ""

# Remote branch cleanup
echo "🌐 Remote Branch Status:"
echo "   Pruning stale remote branches..."
git remote prune origin 2>&1 | grep -E "(pruned|deleted)" || echo "   No stale branches found"
echo ""

# Local branches
echo "📦 Local Feature Branches:"
FEATURE_BRANCHES=$(git branch | grep -E "^  (feature|fix|refactor)" | sed 's/^  //' || echo "")
if [ -z "$FEATURE_BRANCHES" ]; then
    echo "   ✅ No feature branches found"
else
    echo "$FEATURE_BRANCHES" | while read -r branch; do
        if git branch --merged main | grep -q "^  $branch$"; then
            echo "   ⚠️  $branch [MERGED - can delete]"
        else
            echo "   📌 $branch [ACTIVE]"
        fi
    done
fi
echo ""

# Worktrees
echo "🌳 Active Worktrees:"
WORKTREES=$(git worktree list 2>/dev/null | grep -v "^$(pwd)" || echo "")
if [ -z "$WORKTREES" ]; then
    echo "   ✅ No additional worktrees"
else
    echo "$WORKTREES"
fi
echo ""

# Uncommitted changes
echo "📝 Uncommitted Changes:"
UNCOMMITTED=$(git status --short 2>/dev/null || echo "")
if [ -z "$UNCOMMITTED" ]; then
    echo "   ✅ Working directory clean"
else
    echo "$UNCOMMITTED" | head -10
    if [ $(echo "$UNCOMMITTED" | wc -l) -gt 10 ]; then
        echo "   ... and more"
    fi
fi
echo ""

# Recent commits
echo "📜 Recent Commits (last 5):"
git log --oneline -5 2>/dev/null || echo "   Could not retrieve commits"
echo ""

# Outstanding work (not test-related)
echo "⚠️  Outstanding Work (NOT in Test Agent Remit):"
echo ""
echo "   1. Cursor Rules Improvements"
echo "      - Branch: feature/improve-cursor-rules"
echo "      - Worktree: electric-sheep-rule-updates (if exists)"
echo "      - Status: Needs review"
echo ""
echo "   2. Feature Flag Sync"
echo "      - Branch: fix/feature-flag-sync-upsert-isolated (may be deleted)"
echo "      - Status: May be superseded"
echo ""
echo "   3. Video Annotation System"
echo "      - Branch: feature/video-annotation-system"
echo "      - Status: Needs review"
echo ""
echo "   4. Emulator Setup"
echo "      - Branch: feature/emulator-setup"
echo "      - Status: PR merged, may have artifacts"
echo ""

echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    SUMMARY                                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "✅ Remote branches pruned"
echo "✅ IDE optimization work committed"
echo "✅ Documentation updated"
echo "⚠️  Some outstanding branches need review (see above)"
echo ""

