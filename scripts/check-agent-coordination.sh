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
    
    # HIGH RISK patterns (MUST use worktree)
    HIGH_RISK_PATTERNS=(
        "app/.*/ui/screens/.*Screen\.kt"
        "app/.*/ui/screens/.*ViewModel\.kt"
        "app/.*/ui/navigation/.*\.kt"
        "app/.*/.*Application\.kt"
        "app/.*/.*Module\.kt"
        ".*\.gradle\.kts"
        "gradle\.properties"
        "settings\.gradle\.kts"
    )
    
    # MEDIUM-HIGH RISK patterns (Should use worktree)
    MEDIUM_HIGH_RISK_PATTERNS=(
        "app/.*/ui/components/.*\.kt"
        "app/.*/.*Manager\.kt"
        "app/.*/.*Provider\.kt"
    )
    
    # MEDIUM RISK patterns (Consider worktree)
    MEDIUM_RISK_PATTERNS=(
        "app/.*/data/repository/.*Repository\.kt"
        "app/.*/ui/theme/.*\.kt"
        "app/.*/config/.*\.kt"
        "app/.*/.*Factory\.kt"
        "app/.*/MainActivity\.kt"
    )
    
    # Check each modified file
    for file in $MODIFIED_FILES; do
        risk_level="none"
        
        # Check HIGH RISK patterns (MUST use worktree)
        for pattern in "${HIGH_RISK_PATTERNS[@]}"; do
            if [[ "$file" =~ $pattern ]]; then
                echo "🔴 HIGH RISK: $file"
                echo "   ⚠️  MUST use worktree - check $COORDINATION_DOC"
                echo "   This file pattern requires worktree isolation (no exceptions)"
                risk_level="high"
                break
            fi
        done
        
        # Check MEDIUM-HIGH RISK patterns (Should use worktree)
        if [ "$risk_level" = "none" ]; then
            for pattern in "${MEDIUM_HIGH_RISK_PATTERNS[@]}"; do
                if [[ "$file" =~ $pattern ]]; then
                    echo "🟡 MEDIUM-HIGH RISK: $file"
                    echo "   ⚠️  Should use worktree - check $COORDINATION_DOC"
                    echo "   Consider using git worktree for file system isolation"
                    risk_level="medium-high"
                    break
                fi
            done
        fi
        
        # Check MEDIUM RISK patterns (Consider worktree)
        if [ "$risk_level" = "none" ]; then
            for pattern in "${MEDIUM_RISK_PATTERNS[@]}"; do
                if [[ "$file" =~ $pattern ]]; then
                    echo "🟠 MEDIUM RISK: $file"
                    echo "   💡 Consider worktree - check $COORDINATION_DOC"
                    risk_level="medium"
                    break
                fi
            done
        fi
        
        if [ "$risk_level" = "none" ]; then
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

