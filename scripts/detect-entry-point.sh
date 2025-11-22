#!/bin/bash

# Entry Point Detection Script
# Detects how user entered the workflow and gathers appropriate context

set -e

# Entry point types
ENTRY_POINT_UNKNOWN="unknown"
ENTRY_POINT_PROMPT_CREATE="prompt_create"
ENTRY_POINT_PROMPT_EVALUATE="prompt_evaluate"
ENTRY_POINT_PROMPT_FIX="prompt_fix"
ENTRY_POINT_MANUAL_EDIT="manual_edit"
ENTRY_POINT_MANUAL_COMMIT="manual_commit"
ENTRY_POINT_SCRIPT="script"
ENTRY_POINT_DOCS="docs"

# Detect entry point
detect_entry_point() {
    # Check if called from script with explicit entry point
    if [ -n "$SCRIPT_ENTRY_POINT" ]; then
        echo "$SCRIPT_ENTRY_POINT"
        return
    fi
    
    # Check if called from AI agent (prompt-based)
    if [ -n "$CURSOR_PROMPT" ] || [ -n "$USER_PROMPT" ]; then
        PROMPT_TEXT="${CURSOR_PROMPT:-$USER_PROMPT}"
        
        # Analyze prompt to determine type
        PROMPT_LOWER=$(echo "$PROMPT_TEXT" | tr '[:upper:]' '[:lower:]')
        
        if echo "$PROMPT_LOWER" | grep -qE "(create|add|implement|build|make).*(feature|component|screen)"; then
            echo "$ENTRY_POINT_PROMPT_CREATE"
            return
        elif echo "$PROMPT_LOWER" | grep -qE "(evaluate|analyze|review|assess|check).*(state|system|code|implementation)"; then
            echo "$ENTRY_POINT_PROMPT_EVALUATE"
            return
        elif echo "$PROMPT_LOWER" | grep -qE "(fix|debug|repair|resolve).*(bug|error|issue|problem)"; then
            echo "$ENTRY_POINT_PROMPT_FIX"
            return
        fi
    fi
    
    # Check git state for manual entry
    if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
        # Check if files are staged
        if [ -n "$(git diff --cached --name-only 2>/dev/null)" ]; then
            echo "$ENTRY_POINT_MANUAL_COMMIT"
            return
        else
            echo "$ENTRY_POINT_MANUAL_EDIT"
            return
        fi
    fi
    
    # Check if called from script
    if [ -n "$0" ] && [ "$0" != "-bash" ] && [ "$0" != "bash" ]; then
        SCRIPT_NAME=$(basename "$0")
        if [ "$SCRIPT_NAME" != "detect-entry-point.sh" ]; then
            echo "$ENTRY_POINT_SCRIPT"
            return
        fi
    fi
    
    # Check if in docs directory or reading docs
    if [ -f "docs/development/ONBOARDING_RETURNING_CONTRIBUTORS.md" ] && pwd | grep -q "docs"; then
        echo "$ENTRY_POINT_DOCS"
        return
    fi
    
    # Default
    echo "$ENTRY_POINT_UNKNOWN"
}

# Gather context based on entry point
gather_context() {
    local entry_point="$1"
    local task_description="${2:-}"
    
    echo "🔍 Entry Point: $entry_point"
    echo ""
    
    case "$entry_point" in
        "$ENTRY_POINT_PROMPT_CREATE")
            gather_create_context "$task_description"
            ;;
        "$ENTRY_POINT_PROMPT_EVALUATE")
            gather_evaluate_context "$task_description"
            ;;
        "$ENTRY_POINT_PROMPT_FIX")
            gather_fix_context "$task_description"
            ;;
        "$ENTRY_POINT_MANUAL_EDIT"|"$ENTRY_POINT_MANUAL_COMMIT")
            gather_manual_context
            ;;
        "$ENTRY_POINT_SCRIPT")
            gather_script_context
            ;;
        "$ENTRY_POINT_DOCS")
            gather_docs_context
            ;;
        *)
            gather_general_context
            ;;
    esac
}

# Gather context for create tasks
gather_create_context() {
    local task="$1"
    
    echo "📋 Gathering context for CREATE task..."
    echo ""
    
    # Run pre-work check
    if [ -f "scripts/pre-work-check.sh" ]; then
        echo "1️⃣  Running pre-work check..."
        ./scripts/pre-work-check.sh 2>&1 | head -20
        echo ""
    fi
    
    # Find similar patterns
    if [ -n "$task" ]; then
        echo "2️⃣  Searching for similar patterns..."
        # Extract keywords from task
        KEYWORDS=$(echo "$task" | grep -oE "\w+" | head -3 | tr '\n' ' ')
        echo "   Keywords: $KEYWORDS"
        echo "   💡 Tip: Use codebase search to find similar implementations"
        echo ""
    fi
    
    # Check coordination
    if [ -f "docs/development/AGENT_COORDINATION.md" ]; then
        echo "3️⃣  Checking coordination..."
        if grep -q "In Progress" "docs/development/AGENT_COORDINATION.md" 2>/dev/null; then
            echo "   ⚠️  Other work in progress - check coordination doc"
        else
            echo "   ✅ No active coordination conflicts"
        fi
        echo ""
    fi
}

# Gather context for evaluate tasks
gather_evaluate_context() {
    local task="$1"
    
    echo "📋 Gathering context for EVALUATE task..."
    echo ""
    
    # Get current state
    echo "1️⃣  Current state:"
    git status --short 2>/dev/null || echo "   (no git repo)"
    echo ""
    
    # Recent changes
    echo "2️⃣  Recent changes:"
    git log --oneline -5 2>/dev/null || echo "   (no recent commits)"
    echo ""
    
    # Related files
    if [ -n "$task" ]; then
        echo "3️⃣  Related files:"
        echo "   💡 Tip: Use codebase search to find related code"
        echo ""
    fi
}

# Gather context for fix tasks
gather_fix_context() {
    local task="$1"
    
    echo "📋 Gathering context for FIX task..."
    echo ""
    
    # Run pre-work check
    if [ -f "scripts/pre-work-check.sh" ]; then
        echo "1️⃣  Running pre-work check..."
        ./scripts/pre-work-check.sh 2>&1 | head -20
        echo ""
    fi
    
    # Check test status
    echo "2️⃣  Test status:"
    if [ -f "gradlew" ]; then
        echo "   💡 Run: ./gradlew test"
    else
        echo "   (no gradle wrapper found)"
    fi
    echo ""
    
    # Error logs
    echo "3️⃣  Error information:"
    echo "   💡 Check error logs, test failures, or CI failures"
    echo ""
}

# Gather context for manual work
gather_manual_context() {
    echo "📋 Gathering context for MANUAL work..."
    echo ""
    
    # Run pre-work check
    if [ -f "scripts/pre-work-check.sh" ]; then
        echo "1️⃣  Running pre-work check..."
        ./scripts/pre-work-check.sh 2>&1 | head -20
        echo ""
    fi
    
    # Show current changes
    echo "2️⃣  Current changes:"
    git status --short 2>/dev/null || echo "   (no changes)"
    echo ""
    
    # Show branch
    CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
    if [ -n "$CURRENT_BRANCH" ]; then
        echo "3️⃣  Current branch: $CURRENT_BRANCH"
    else
        echo "3️⃣  ⚠️  Not on a branch (create feature branch)"
    fi
    echo ""
}

# Gather context for script execution
gather_script_context() {
    echo "📋 Gathering context for SCRIPT execution..."
    echo ""
    
    # Show script name
    SCRIPT_NAME=$(basename "$0" 2>/dev/null || echo "unknown")
    echo "1️⃣  Script: $SCRIPT_NAME"
    echo ""
    
    # Run pre-work check
    if [ -f "scripts/pre-work-check.sh" ]; then
        echo "2️⃣  Running pre-work check..."
        ./scripts/pre-work-check.sh 2>&1 | head -20
        echo ""
    fi
}

# Gather context for docs
gather_docs_context() {
    echo "📋 Gathering context for DOCUMENTATION..."
    echo ""
    
    echo "1️⃣  Available guides:"
    echo "   → Returning contributors: docs/development/ONBOARDING_RETURNING_CONTRIBUTORS.md"
    echo "   → New starters: docs/development/ONBOARDING_NEW_STARTERS.md"
    echo "   → Quick reference: docs/development/ONBOARDING_QUICK_REFERENCE.md"
    echo ""
    
    # Run pre-work check
    if [ -f "scripts/pre-work-check.sh" ]; then
        echo "2️⃣  Current state:"
        ./scripts/pre-work-check.sh 2>&1 | head -10
        echo ""
    fi
}

# Gather general context
gather_general_context() {
    echo "📋 Gathering general context..."
    echo ""
    
    # Run pre-work check
    if [ -f "scripts/pre-work-check.sh" ]; then
        echo "1️⃣  Running pre-work check..."
        ./scripts/pre-work-check.sh 2>&1 | head -20
        echo ""
    fi
    
    # Show current state
    echo "2️⃣  Current state:"
    git status --short 2>/dev/null || echo "   (no git repo)"
    echo ""
}

# Main
main() {
    ENTRY_POINT=$(detect_entry_point)
    TASK_DESCRIPTION="${1:-}"
    
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║         ENTRY POINT DETECTION & CONTEXT GATHERING         ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    
    gather_context "$ENTRY_POINT" "$TASK_DESCRIPTION"
    
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║                    CONTEXT READY                           ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo ""
    echo "💡 Next steps depend on your entry point:"
    echo ""
    case "$ENTRY_POINT" in
        "$ENTRY_POINT_PROMPT_CREATE")
            echo "   → Continue with feature creation"
            echo "   → Check similar patterns before implementing"
            ;;
        "$ENTRY_POINT_PROMPT_EVALUATE")
            echo "   → Review gathered context"
            echo "   → Analyze current state"
            ;;
        "$ENTRY_POINT_PROMPT_FIX")
            echo "   → Review error information"
            echo "   → Run tests to reproduce issue"
            ;;
        "$ENTRY_POINT_MANUAL_EDIT"|"$ENTRY_POINT_MANUAL_COMMIT")
            echo "   → Review pre-work check results"
            echo "   → Ensure on feature branch"
            echo "   → Run tests before committing"
            ;;
        "$ENTRY_POINT_SCRIPT")
            echo "   → Follow script output"
            echo "   → Complete script workflow"
            ;;
        "$ENTRY_POINT_DOCS")
            echo "   → Follow documentation guide"
            echo "   → Use quick reference as needed"
            ;;
        *)
            echo "   → Run pre-work check: ./scripts/pre-work-check.sh"
            echo "   → See: docs/development/ONBOARDING_RETURNING_CONTRIBUTORS.md"
            ;;
    esac
    echo ""
}

# Run if executed directly
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    main "$@"
fi

