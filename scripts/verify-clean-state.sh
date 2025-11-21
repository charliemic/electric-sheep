#!/bin/bash

# Verify Clean State Before Archiving Agent
# This script checks that the repository is in a clean state before archiving

set -e

echo "🔍 Verifying clean state before archiving agent..."
echo ""

ERRORS=0
WARNINGS=0

# Check 1: Working directory clean
echo "1️⃣  Checking working directory..."
if git status --porcelain | grep -q .; then
    echo "   ❌ ERROR: Uncommitted changes found"
    echo "   → Run: git status"
    echo "   → Commit or stash changes before archiving"
    ERRORS=$((ERRORS + 1))
else
    echo "   ✅ Working directory clean"
fi
echo ""

# Check 2: Branch is pushed
echo "2️⃣  Checking branch is pushed..."
CURRENT_BRANCH=$(git branch --show-current)
if [ -z "$CURRENT_BRANCH" ]; then
    echo "   ⚠️  WARNING: Not on a branch (detached HEAD?)"
    WARNINGS=$((WARNINGS + 1))
elif [ "$CURRENT_BRANCH" = "main" ]; then
    echo "   ⚠️  WARNING: On main branch (should be on feature branch)"
    WARNINGS=$((WARNINGS + 1))
else
    UNPUSHED=$(git log origin/"$CURRENT_BRANCH"..HEAD 2>/dev/null | wc -l || echo "0")
    if [ "$UNPUSHED" -gt 0 ]; then
        echo "   ❌ ERROR: $UNPUSHED unpushed commit(s)"
        echo "   → Run: git push origin $CURRENT_BRANCH"
        ERRORS=$((ERRORS + 1))
    else
        echo "   ✅ All commits pushed"
    fi
fi
echo ""

# Check 3: No untracked files (except intentionally ignored)
echo "3️⃣  Checking for untracked files..."
UNTRACKED=$(git status --porcelain | grep "^??" | wc -l || echo "0")
if [ "$UNTRACKED" -gt 0 ]; then
    echo "   ⚠️  WARNING: $UNTRACKED untracked file(s) found"
    echo "   → Review: git status"
    echo "   → Add to .gitignore or commit if needed"
    WARNINGS=$((WARNINGS + 1))
else
    echo "   ✅ No untracked files"
fi
echo ""

# Check 4: Coordination doc exists
echo "4️⃣  Checking coordination doc..."
if [ -f "docs/development/workflow/AGENT_COORDINATION.md" ]; then
    echo "   ✅ Coordination doc exists"
    echo "   💡 Remember to update status before archiving"
else
    echo "   ⚠️  WARNING: Coordination doc not found"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

# Summary
echo "📋 Summary:"
if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "   ✅ State is clean - safe to archive"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "   ⚠️  State is mostly clean ($WARNINGS warning(s))"
    echo "   → Review warnings above"
    echo "   → Archive if warnings are acceptable"
    exit 0
else
    echo "   ❌ State is NOT clean ($ERRORS error(s), $WARNINGS warning(s))"
    echo "   → Fix errors before archiving"
    exit 1
fi

