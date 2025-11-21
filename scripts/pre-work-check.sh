#!/bin/bash

# Pre-Work Checklist Automation
# Enforces mandatory pre-work steps before starting any work
# This script should be run before making ANY changes

set -e

COORDINATION_DOC="docs/development/AGENT_COORDINATION.md"
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
ERRORS=0
WARNINGS=0

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         PRE-WORK CHECKLIST (MANDATORY)                     ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# 1. Check if on main branch
echo "1️⃣  Checking branch..."
if [ "$CURRENT_BRANCH" = "main" ] || [ -z "$CURRENT_BRANCH" ]; then
    echo "   ❌ ERROR: You are on 'main' branch or no branch detected!"
    echo "   → Create a feature branch: git checkout -b feature/<task-name>"
    echo "   → Or use worktree: ./scripts/create-worktree.sh <task-name>"
    ERRORS=$((ERRORS + 1))
else
    echo "   ✅ On feature branch: $CURRENT_BRANCH"
    
    # Check branch naming convention
    if [[ ! "$CURRENT_BRANCH" =~ ^(feature|fix|refactor|docs|test)/ ]]; then
        echo "   ⚠️  WARNING: Branch name doesn't follow convention"
        echo "   → Expected: <type>/<task-description>"
        WARNINGS=$((WARNINGS + 1))
    fi
fi
echo ""

# 2. Check for remote updates
echo "2️⃣  Checking for remote updates..."
if git fetch origin main --quiet 2>/dev/null; then
    LOCAL=$(git rev-parse main 2>/dev/null || echo "")
    REMOTE=$(git rev-parse origin/main 2>/dev/null || echo "")
    
    if [ -n "$LOCAL" ] && [ -n "$REMOTE" ] && [ "$LOCAL" != "$REMOTE" ]; then
        echo "   ⚠️  WARNING: Remote main has updates"
        echo "   → Run: git pull origin main"
        echo "   → Or merge: git merge origin/main"
        WARNINGS=$((WARNINGS + 1))
    else
        echo "   ✅ Local main is up to date"
    fi
else
    echo "   ⚠️  Could not fetch from origin (may be offline)"
fi
echo ""

# 3. Check coordination
echo "3️⃣  Checking agent coordination..."
if [ -f "$COORDINATION_DOC" ]; then
    echo "   ✅ Coordination doc found"
    
    # Run coordination check script if it exists
    if [ -f "scripts/check-agent-coordination.sh" ]; then
        echo "   → Running coordination check..."
        if ! ./scripts/check-agent-coordination.sh; then
            ERRORS=$((ERRORS + 1))
        fi
    fi
else
    echo "   ⚠️  WARNING: Coordination doc not found: $COORDINATION_DOC"
    WARNINGS=$((WARNINGS + 1))
fi
echo ""

# 4. Check for existing fixes in main
echo "4️⃣  Checking for existing fixes in main..."
if [ -n "$CURRENT_BRANCH" ] && [ "$CURRENT_BRANCH" != "main" ]; then
    # Check if this looks like a fix branch
    if [[ "$CURRENT_BRANCH" =~ ^fix/ ]]; then
        echo "   💡 Tip: Before fixing, check if fix already exists in main:"
        echo "   → git log origin/main --grep=\"<issue-keyword>\" --oneline"
    fi
fi
echo ""

# 5. Check for available tools
echo "5️⃣  Checking available automation tools..."
TOOL_COUNT=$(find scripts -name "*.sh" -o -name "*.py" 2>/dev/null | wc -l | tr -d ' ')
echo "   ✅ Found $TOOL_COUNT scripts available"
echo "   💡 Tip: Check scripts/ directory for automation before manual steps"
echo ""

# 6. Check for relevant cursor rules
echo "6️⃣  Checking cursor rules..."
RULE_COUNT=$(find .cursor/rules -name "*.mdc" 2>/dev/null | wc -l | tr -d ' ')
if [ "$RULE_COUNT" -gt 0 ]; then
    echo "   ✅ Found $RULE_COUNT cursor rules"
    echo "   💡 Tip: Check .cursor/rules/ for relevant rules before starting"
    
    # Suggest rule discovery
    if [ -f "scripts/discover-rules.sh" ]; then
        echo "   → Run: ./scripts/discover-rules.sh <task-keyword>"
    fi
else
    echo "   ⚠️  No cursor rules found"
fi
echo ""

# 7. Check for uncommitted changes
echo "7️⃣  Checking working directory..."
if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    echo "   ⚠️  WARNING: You have uncommitted changes"
    echo "   → Commit or stash before starting new work"
    WARNINGS=$((WARNINGS + 1))
else
    echo "   ✅ Working directory is clean"
fi
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    SUMMARY                                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo "✅ All checks passed! You're ready to start work."
    echo ""
    echo "💡 Next steps:"
    echo "   1. Update coordination doc if needed: $COORDINATION_DOC"
    echo "   2. Use worktree if modifying shared files: ./scripts/create-worktree.sh"
    echo "   3. Reference relevant rules: .cursor/rules/"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo "⚠️  $WARNINGS warning(s) found. Review above and proceed with caution."
    echo ""
    echo "💡 You can proceed, but consider addressing warnings first."
    exit 0
else
    echo "❌ $ERRORS error(s) found. Fix errors before proceeding."
    echo ""
    echo "💡 Fix the errors above, then run this script again."
    exit 1
fi

