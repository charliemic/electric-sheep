#!/bin/bash

# Check for Rule and Workflow Updates
# Detects if rules or workflow docs have been updated since last check
# This enables real-time sharing of agent improvements while maintaining isolation

set -e

RULES_DIR=".cursor/rules"
WORKFLOW_DIR="docs/development/workflow"
COORDINATION_DOC="docs/development/workflow/AGENT_COORDINATION.md"
PRE_WORK_SCRIPT="scripts/pre-work-check.sh"
COORDINATION_SCRIPT="scripts/check-agent-coordination.sh"
UPDATE_CHECK_SCRIPT="scripts/check-rule-updates.sh"

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "❌ ERROR: Not in a git repository"
    exit 1
fi

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         RULE AND WORKFLOW UPDATE CHECK                     ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if we're on main branch (or can access main)
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
MAIN_AVAILABLE=false

if [ "$CURRENT_BRANCH" = "main" ]; then
    MAIN_AVAILABLE=true
elif git rev-parse --verify main > /dev/null 2>&1; then
    MAIN_AVAILABLE=true
fi

if [ "$MAIN_AVAILABLE" = false ]; then
    echo "⚠️  WARNING: Cannot access main branch"
    echo "   → Switch to main: git checkout main"
    echo "   → Or fetch latest: git fetch origin main"
    echo ""
    echo "💡 Tip: Run this check after pulling latest main"
    exit 0
fi

# Fetch latest from origin (non-blocking)
echo "📡 Fetching latest from origin..."
if git fetch origin main --quiet 2>/dev/null; then
    echo "   ✅ Fetched latest from origin"
else
    echo "   ⚠️  Could not fetch from origin (may be offline)"
fi
echo ""

# Check if local main is behind remote main
LOCAL_MAIN=$(git rev-parse main 2>/dev/null || echo "")
REMOTE_MAIN=$(git rev-parse origin/main 2>/dev/null || echo "")

if [ -n "$LOCAL_MAIN" ] && [ -n "$REMOTE_MAIN" ] && [ "$LOCAL_MAIN" != "$REMOTE_MAIN" ]; then
    echo "🔄 UPDATES AVAILABLE: Local main is behind remote main"
    echo "   → Pull latest: git checkout main && git pull origin main"
    echo "   → Then check for rule/workflow updates"
    echo ""
    
    # Show what changed
    echo "📋 Changes in remote main:"
    git log --oneline "$LOCAL_MAIN".."$REMOTE_MAIN" --max-count=10 2>/dev/null || echo "   (Could not show changes)"
    echo ""
    
    # Check specifically for rule/workflow changes
    RULE_CHANGES=$(git diff --name-only "$LOCAL_MAIN".."$REMOTE_MAIN" -- "$RULES_DIR" "$WORKFLOW_DIR" "$COORDINATION_DOC" "$PRE_WORK_SCRIPT" "$COORDINATION_SCRIPT" "$UPDATE_CHECK_SCRIPT" 2>/dev/null || echo "")
    
    if [ -n "$RULE_CHANGES" ]; then
        echo "📝 Rule/Workflow files changed:"
        echo "$RULE_CHANGES" | while read -r file; do
            if [ -n "$file" ]; then
                echo "   • $file"
            fi
        done
        echo ""
        echo "💡 IMPORTANT: Rule/workflow updates detected!"
        echo "   → Pull latest main to get updates: git pull origin main"
        echo "   → Review updated files before starting work"
    fi
else
    echo "✅ Local main is up to date with remote"
    echo ""
fi

# Check for uncommitted changes to rule/workflow files
echo "🔍 Checking for local uncommitted changes to rules/workflow..."
UNCOMMITTED_RULES=$(git status --porcelain "$RULES_DIR" "$WORKFLOW_DIR" "$COORDINATION_DOC" "$PRE_WORK_SCRIPT" "$COORDINATION_SCRIPT" "$UPDATE_CHECK_SCRIPT" 2>/dev/null || echo "")

if [ -n "$UNCOMMITTED_RULES" ]; then
    echo "   ⚠️  WARNING: You have uncommitted changes to rules/workflow files:"
    echo "$UNCOMMITTED_RULES" | while read -r line; do
        if [ -n "$line" ]; then
            echo "   • $line"
        fi
    done
    echo ""
    echo "   💡 Tip: Commit these changes or stash them before starting work"
    echo "   → If improving rules/workflow, create PR with improvements"
else
    echo "   ✅ No uncommitted changes to rules/workflow files"
fi
echo ""

# Check last pull time (if we can determine it)
if [ -n "$LOCAL_MAIN" ] && [ -n "$REMOTE_MAIN" ]; then
    LAST_PULL_TIME=$(git log -1 --format=%ct "$LOCAL_MAIN" 2>/dev/null || echo "0")
    CURRENT_TIME=$(date +%s)
    TIME_SINCE_PULL=$((CURRENT_TIME - LAST_PULL_TIME))
    
    if [ "$TIME_SINCE_PULL" -gt 3600 ]; then  # 1 hour
        HOURS_SINCE=$((TIME_SINCE_PULL / 3600))
        echo "⏰ Last pull was $HOURS_SINCE hour(s) ago"
        echo "   💡 Consider pulling latest main to get rule/workflow updates"
        echo ""
    fi
fi

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    SUMMARY                                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

if [ -n "$LOCAL_MAIN" ] && [ -n "$REMOTE_MAIN" ] && [ "$LOCAL_MAIN" != "$REMOTE_MAIN" ]; then
    echo "🔄 ACTION REQUIRED: Pull latest main to get rule/workflow updates"
    echo ""
    echo "   Steps:"
    echo "   1. git checkout main"
    echo "   2. git pull origin main"
    echo "   3. Review updated rules/workflow files"
    echo "   4. Create worktree from updated main"
    echo ""
    echo "   See: docs/development/workflow/REAL_TIME_COLLABORATION.md"
    exit 1
elif [ -n "$UNCOMMITTED_RULES" ]; then
    echo "⚠️  WARNING: Uncommitted changes to rules/workflow files"
    echo "   → Commit or stash before starting work"
    exit 0
else
    echo "✅ Rules and workflow are up to date"
    echo ""
    echo "💡 Next steps:"
    echo "   1. Continue with pre-work check: ./scripts/pre-work-check.sh"
    echo "   2. Create worktree: ./scripts/create-worktree.sh <task-name>"
    echo "   3. Start work in isolated worktree"
    exit 0
fi

