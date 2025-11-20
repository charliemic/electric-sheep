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
    echo "   Expected format: <type>/<agent-id>-<description>"
    echo "   Example: feature/agent-1-test-helpers"
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

# Check modified files against ownership rules
if [ -n "$MODIFIED_FILES" ]; then
    echo "=== File Ownership Check ==="
    
    # Define ownership patterns
    AGENT_1_PATTERNS=("scripts/test-" "test-automation/" "docs/testing/")
    AGENT_2_PATTERNS=("app/.*/config/" "docs/development/")
    AGENT_3_PATTERNS=("app/.*/ui/components/" "app/.*/ui/theme/" "docs/architecture/")
    AGENT_4_PATTERNS=("app/.*/data/repositories/" "app/.*/data/models/")
    
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
        is_owned=false
        
        # Check if shared file
        for pattern in "${SHARED_FILES[@]}"; do
            if [[ "$file" =~ $pattern ]]; then
                echo "⚠️  SHARED FILE: $file"
                echo "   This file requires coordination - check $COORDINATION_DOC"
                is_shared=true
                break
            fi
        done
        
        # Check ownership if not shared
        if [ "$is_shared" = false ]; then
            # Check Agent 1 patterns
            for pattern in "${AGENT_1_PATTERNS[@]}"; do
                if [[ "$file" =~ $pattern ]]; then
                    is_owned=true
                    break
                fi
            done
            
            # Check Agent 2 patterns
            if [ "$is_owned" = false ]; then
                for pattern in "${AGENT_2_PATTERNS[@]}"; do
                    if [[ "$file" =~ $pattern ]]; then
                        is_owned=true
                        break
                    fi
                done
            fi
            
            # Check Agent 3 patterns
            if [ "$is_owned" = false ]; then
                for pattern in "${AGENT_3_PATTERNS[@]}"; do
                    if [[ "$file" =~ $pattern ]]; then
                        is_owned=true
                        break
                    fi
                done
            fi
            
            # Check Agent 4 patterns
            if [ "$is_owned" = false ]; then
                for pattern in "${AGENT_4_PATTERNS[@]}"; do
                    if [[ "$file" =~ $pattern ]]; then
                        is_owned=true
                        break
                    fi
                done
            fi
            
            if [ "$is_owned" = true ]; then
                echo "✅ $file (owned by agent based on pattern)"
            else
                echo "⚠️  UNKNOWN OWNERSHIP: $file"
                echo "   Verify this file is in your ownership area"
            fi
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
echo "For complete guidelines, see: docs/development/MULTI_AGENT_WORKFLOW.md"

