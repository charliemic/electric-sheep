#!/bin/bash

# Agent Coordination Check Script
# Validates branch naming, checks file ownership, and warns about conflicts

set -e

COORDINATION_DOC="docs/development/AGENT_COORDINATION.md"
CURRENT_BRANCH=$(git branch --show-current)
MODIFIED_FILES=$(git diff --name-only main 2>/dev/null || git diff --cached --name-only 2>/dev/null || echo "")

echo "=== Agent Coordination Check ==="
echo ""

# Check if on main branch
if [ "$CURRENT_BRANCH" = "main" ]; then
    echo "❌ ERROR: You are on 'main' branch!"
    echo "   Create a feature branch before making changes:"
    echo "   git checkout -b feature/<agent-id>-<feature-name>"
    exit 1
fi

echo "✅ Current branch: $CURRENT_BRANCH"
echo ""

# Check branch naming convention
if [[ ! "$CURRENT_BRANCH" =~ ^(feature|fix|refactor|docs|test)/ ]]; then
    echo "⚠️  WARNING: Branch name doesn't follow convention"
    echo "   Expected format: <type>/<task-description>"
    echo "   Example: feature/test-helpers"
    echo ""
fi

# Check if coordination doc exists
if [ ! -f "$COORDINATION_DOC" ]; then
    echo "⚠️  WARNING: Coordination document not found: $COORDINATION_DOC"
    echo ""
else
    echo "✅ Coordination document found"
    echo ""
    
    # Check if current branch is documented
    if grep -q "$CURRENT_BRANCH" "$COORDINATION_DOC"; then
        echo "✅ Branch is documented in coordination doc"
    else
        echo "⚠️  WARNING: Branch not found in coordination doc"
        echo "   Consider adding your work to: $COORDINATION_DOC"
    fi
    echo ""
fi

# Check modified files for shared files
if [ -n "$MODIFIED_FILES" ]; then
    echo "=== Shared File Check ==="
    
    SHARED_FILES=(
        "app/.*/ui/screens/LandingScreen.kt"
        "app/.*/ElectricSheepApplication.kt"
        "app/.*/data/DataModule.kt"
        "app/build.gradle.kts"
        "build.gradle.kts"
    )
    
    # Check each modified file
    for file in $MODIFIED_FILES; do
        is_shared=false
        
        # Check if shared file
        for pattern in "${SHARED_FILES[@]}"; do
            if [[ "$file" =~ $pattern ]]; then
                echo "⚠️  SHARED FILE: $file"
                echo "   This file requires coordination - check $COORDINATION_DOC"
                echo "   Consider using git worktree for file system isolation"
                is_shared=true
                break
            fi
        done
        
        if [ "$is_shared" = false ]; then
            echo "✅ $file"
        fi
    done
    echo ""
fi

echo "=== Summary ==="
echo "✅ Branch check: Passed"
if [ -f "$COORDINATION_DOC" ]; then
    echo "✅ Coordination doc: Found"
    echo ""
    echo "💡 Tip: Update $COORDINATION_DOC with your work status"
else
    echo "⚠️  Coordination doc: Not found (create it if needed)"
fi
echo ""
echo "💡 For file system isolation, use:"
echo "   ./scripts/create-worktree.sh <task-name>"
echo ""
echo "For complete guidelines, see: docs/development/MULTI_AGENT_WORKFLOW.md"

