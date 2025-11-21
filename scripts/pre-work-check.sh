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

# 2. Check for remote updates (CRITICAL for multi-agent workflow)
echo "2️⃣  Checking for remote updates..."
if git fetch origin main --quiet 2>/dev/null; then
    LOCAL=$(git rev-parse main 2>/dev/null || echo "")
    REMOTE=$(git rev-parse origin/main 2>/dev/null || echo "")
    
    if [ -n "$LOCAL" ] && [ -n "$REMOTE" ] && [ "$LOCAL" != "$REMOTE" ]; then
        echo "   ❌ ERROR: Remote main has updates (CRITICAL for multi-agent workflow)"
        echo "   → Sync your branch: git fetch origin && git rebase origin/main"
        echo "   → Or update main first: git checkout main && git pull origin main"
        echo "   → See .cursor/rules/branch-synchronization.mdc for details"
        ERRORS=$((ERRORS + 1))
    else
        echo "   ✅ Local main is up to date"
    fi
    
    # Also check if current branch is behind main
    if [ -n "$CURRENT_BRANCH" ] && [ "$CURRENT_BRANCH" != "main" ]; then
        BRANCH_BEHIND=$(git rev-list --count origin/main.."$CURRENT_BRANCH" 2>/dev/null || echo "0")
        MAIN_AHEAD=$(git rev-list --count "$CURRENT_BRANCH"..origin/main 2>/dev/null || echo "0")
        
        if [ "$MAIN_AHEAD" -gt 0 ]; then
            echo "   ⚠️  WARNING: Your branch is $MAIN_AHEAD commit(s) behind main"
            echo "   → Sync: git fetch origin && git rebase origin/main"
            echo "   → See .cursor/rules/branch-synchronization.mdc"
            WARNINGS=$((WARNINGS + 1))
        fi
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

# 5.5. Reminder: Check for existing artifacts before creating new ones
echo "5️⃣.5️⃣  Artifact duplication prevention..."
if [ -f "scripts/check-existing-artifacts.sh" ]; then
    echo "   ✅ Helper script available: ./scripts/check-existing-artifacts.sh <keyword>"
    echo "   💡 CRITICAL: Before creating new files/scripts/docs, search for existing ones"
    echo "   → Run: ./scripts/check-existing-artifacts.sh <keyword>"
    echo "   → Check .cursor/rules/artifact-duplication.mdc for full guidelines"
else
    echo "   💡 CRITICAL: Before creating new files/scripts/docs, search for existing ones:"
    echo "   → find . -name \"*<keyword>*\" -not -path \"*/\.*\" -not -path \"*/build/*\""
    echo "   → grep -r \"<keyword>\" scripts/ docs/ --include=\"*.sh\" --include=\"*.py\" --include=\"*.md\""
    echo "   → Check .cursor/rules/artifact-duplication.mdc for full guidelines"
fi
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
    echo "   💡 Tip: Use WIP commits frequently to prevent work loss"
    echo "   → Quick commit: git add -A && git commit -m \"WIP: <description>\""
    WARNINGS=$((WARNINGS + 1))
else
    echo "   ✅ Working directory is clean"
fi
echo ""

# 7.5. Reminder about frequent commits
echo "7️⃣.5️⃣  Frequent commits safety net..."
if [ -n "$CURRENT_BRANCH" ] && [ "$CURRENT_BRANCH" != "main" ]; then
    # Check last commit time
    LAST_COMMIT_TIME=$(git log -1 --format=%ct 2>/dev/null || echo "0")
    CURRENT_TIME=$(date +%s)
    TIME_SINCE_COMMIT=$((CURRENT_TIME - LAST_COMMIT_TIME))
    
    if [ "$TIME_SINCE_COMMIT" -gt 1800 ]; then  # 30 minutes
        echo "   💡 REMINDER: Commit frequently (every 15-30 min) to prevent work loss"
        echo "   → Last commit: $((TIME_SINCE_COMMIT / 60)) minutes ago"
        echo "   → Use WIP commits for incomplete work: git commit -m \"WIP: <description>\""
        echo "   → Check .cursor/rules/frequent-commits.mdc for guidelines"
    else
        echo "   ✅ Good commit frequency (last commit < 30 min ago)"
    fi
else
    echo "   💡 REMINDER: Commit frequently (every 15-30 min) to prevent work loss"
    echo "   → Use WIP commits for incomplete work"
    echo "   → Check .cursor/rules/frequent-commits.mdc for guidelines"
fi
echo ""

# 8. Check for scope creep (existing session)
echo "8️⃣  Checking for scope creep..."
if [ -f "scripts/track-session-scope.sh" ]; then
    SESSIONS_DIR="development-metrics/sessions"
    CURRENT_SESSION_FILE="$SESSIONS_DIR/.current-session-id"
    
    if [ -f "$CURRENT_SESSION_FILE" ]; then
        SESSION_ID=$(cat "$CURRENT_SESSION_FILE")
        SESSION_FILE="$SESSIONS_DIR/${SESSION_ID}.json"
        
        if [ -f "$SESSION_FILE" ]; then
            echo "   → Active session detected: $SESSION_ID"
            echo "   → Checking for scope creep..."
            echo ""
            
            # Run scope creep check (suppress errors if script has issues)
            if ./scripts/track-session-scope.sh check 2>/dev/null; then
                echo ""
                echo "   💡 To start a new chat session:"
                echo "   → Commit current work: git commit -m \"WIP: [description]\""
                echo "   → Click 'New Chat' in Cursor or press Cmd+L (Mac) / Ctrl+L (Windows/Linux)"
                echo "   → Reference: \"Continuing from [previous task]\""
            else
                echo "   ⚠️  Could not check scope creep (script may need updates)"
            fi
        else
            echo "   ✅ No active session detected"
            echo "   💡 To track session scope: ./scripts/track-session-scope.sh start \"<task>\""
        fi
    else
        echo "   ✅ No active session detected"
        echo "   💡 To track session scope: ./scripts/track-session-scope.sh start \"<task>\""
    fi
else
    echo "   💡 Scope creep detection available: ./scripts/track-session-scope.sh"
    echo "   → Check .cursor/rules/scope-creep-detection.mdc for guidelines"
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
    echo "   1. Search for existing artifacts before creating new ones"
    echo "   2. Update coordination doc if needed: $COORDINATION_DOC"
    echo "   3. Use worktree if modifying shared files: ./scripts/create-worktree.sh"
    echo "   4. Reference relevant rules: .cursor/rules/"
    echo "   5. Track session scope: ./scripts/track-session-scope.sh start \"<task>\""
    echo "   6. 💡 REMINDER: Commit frequently (every 15-30 min) to prevent work loss"
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

