#!/bin/bash

# Pre-PR Check Script
# Validates branch is ready for PR creation
# Run before creating a Pull Request

set -e

CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
ERRORS=0
WARNINGS=0

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         PRE-PR CHECKLIST                                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if on main branch
if [ "$CURRENT_BRANCH" = "main" ] || [ -z "$CURRENT_BRANCH" ]; then
    echo "❌ ERROR: Cannot create PR from main branch"
    echo "   → Create a feature branch first"
    exit 1
fi

echo "✅ Branch: $CURRENT_BRANCH"
echo ""

# 1. Check branch sync status
echo "1️⃣  Checking branch sync status..."
git fetch origin main --quiet 2>/dev/null || true

LOCAL_MAIN=$(git rev-parse main 2>/dev/null || echo "")
REMOTE_MAIN=$(git rev-parse origin/main 2>/dev/null || echo "")
BRANCH_COMMITS=$(git rev-list --count origin/main.."$CURRENT_BRANCH" 2>/dev/null || echo "0")
MAIN_AHEAD=$(git rev-list --count "$CURRENT_BRANCH"..origin/main 2>/dev/null || echo "0")

if [ "$MAIN_AHEAD" -gt 0 ]; then
    echo "   ❌ ERROR: Branch is $MAIN_AHEAD commit(s) behind main"
    echo "   → Sync before creating PR: git fetch origin && git rebase origin/main"
    echo "   → See .cursor/rules/branch-synchronization.mdc"
    ERRORS=$((ERRORS + 1))
else
    echo "   ✅ Branch is up to date with main"
fi

if [ "$BRANCH_COMMITS" -eq 0 ]; then
    echo "   ⚠️  WARNING: Branch has no commits ahead of main"
    echo "   → Make sure you've committed your changes"
    WARNINGS=$((WARNINGS + 1))
else
    echo "   ✅ Branch has $BRANCH_COMMITS commit(s) to merge"
fi
echo ""

# 2. Check for uncommitted changes
echo "2️⃣  Checking for uncommitted changes..."
if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    echo "   ⚠️  WARNING: You have uncommitted changes"
    echo "   → Commit or stash before creating PR"
    echo "   → Quick commit: git add -A && git commit -m \"WIP: <description>\""
    WARNINGS=$((WARNINGS + 1))
else
    echo "   ✅ Working directory is clean"
fi
echo ""

# 3. Check for conflicts (if query tool available)
echo "3️⃣  Checking for file conflicts..."
if [ -f "scripts/query-agent-coordination.sh" ]; then
    # Get modified files compared to main
    MODIFIED_FILES=$(git diff --name-only origin/main..."$CURRENT_BRANCH" 2>/dev/null || echo "")
    
    if [ -n "$MODIFIED_FILES" ]; then
        CONFLICT_DETECTED=0
        for file in $MODIFIED_FILES; do
            if [ -f "$file" ]; then
                if ! ./scripts/query-agent-coordination.sh check-file "$file" > /dev/null 2>&1; then
                    CONFLICT_DETECTED=1
                    echo "   ⚠️  WARNING: Potential conflict with file: $file"
                    echo "   → Run: ./scripts/query-agent-coordination.sh who-owns $file"
                fi
            fi
        done
        
        if [ $CONFLICT_DETECTED -eq 0 ]; then
            echo "   ✅ No file conflicts detected"
        else
            WARNINGS=$((WARNINGS + 1))
        fi
    else
        echo "   ✅ No modified files to check"
    fi
else
    echo "   ⚠️  Query tool not found (optional check)"
fi
echo ""

# 4. Check if tests pass (if gradle available)
echo "4️⃣  Checking if tests pass..."
if [ -f "gradlew" ]; then
    echo "   💡 Tip: Run tests before creating PR:"
    echo "   → ./gradlew test"
    echo "   → Ensure all tests pass"
else
    echo "   ⚠️  Gradle wrapper not found (skip test check)"
fi
echo ""

# 5. Check coordination doc
echo "5️⃣  Checking coordination documentation..."
COORDINATION_DOC="docs/development/workflow/AGENT_COORDINATION.md"
if [ -f "$COORDINATION_DOC" ]; then
    if grep -q "$CURRENT_BRANCH" "$COORDINATION_DOC" 2>/dev/null; then
        echo "   ✅ Branch is documented in coordination doc"
    else
        echo "   ⚠️  WARNING: Branch not found in coordination doc"
        echo "   → Update: $COORDINATION_DOC"
        echo "   → Mark work as Complete when PR is ready"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo "   ⚠️  Coordination doc not found"
fi
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    SUMMARY                                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "✅ All checks passed! Ready to create PR."
    echo ""
    echo "💡 Next steps:"
    echo "   1. Push branch: git push -u origin $CURRENT_BRANCH"
    echo "   2. Create PR via GitHub UI or: gh pr create"
    echo "   3. Update coordination doc status to Complete"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "⚠️  $WARNINGS warning(s) found. Review above and proceed with caution."
    echo ""
    echo "💡 You can create PR, but consider addressing warnings first."
    exit 0
else
    echo "❌ $ERRORS error(s) found. Fix errors before creating PR."
    echo ""
    echo "💡 Fix the errors above, then run this script again."
    exit 1
fi

