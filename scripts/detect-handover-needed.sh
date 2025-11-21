#!/usr/bin/env bash
# Detect if handover is needed based on conversation metrics
# Usage: ./scripts/detect-handover-needed.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Thresholds (can be overridden via environment variables)
MAX_TURNS="${MAX_TURNS:-100}"
MAX_HOURS="${MAX_HOURS:-4}"
MAX_ERRORS="${MAX_ERRORS:-3}"
MAX_REPETITIONS="${MAX_REPETITIONS:-3}"

echo -e "${BLUE}🔍 Checking if handover is needed...${NC}"
echo ""

# Initialize counters
TRIGGERS=0
WARNINGS=0

# Check 1: Time elapsed (if we can detect it)
# Note: This is a placeholder - actual implementation would track conversation start time
if [ -f "${PROJECT_ROOT}/.conversation-start" ]; then
    START_TIME=$(cat "${PROJECT_ROOT}/.conversation-start")
    CURRENT_TIME=$(date +%s)
    ELAPSED_HOURS=$(( (CURRENT_TIME - START_TIME) / 3600 ))
    
    if [ $ELAPSED_HOURS -gt $MAX_HOURS ]; then
        echo -e "${YELLOW}⚠️  Time threshold exceeded: ${ELAPSED_HOURS} hours (max: ${MAX_HOURS})${NC}"
        TRIGGERS=$((TRIGGERS + 1))
    else
        echo -e "${GREEN}✅ Time elapsed: ${ELAPSED_HOURS} hours (under threshold)${NC}"
    fi
else
    echo -e "${BLUE}ℹ️  No conversation start time tracked${NC}"
fi

# Check 2: Git commits (proxy for work done)
RECENT_COMMITS=$(git log --oneline --since="4 hours ago" | wc -l | tr -d ' ')
if [ $RECENT_COMMITS -gt 10 ]; then
    echo -e "${YELLOW}⚠️  High commit count: ${RECENT_COMMITS} commits in last 4 hours${NC}"
    WARNINGS=$((WARNINGS + 1))
else
    echo -e "${GREEN}✅ Commit count: ${RECENT_COMMITS} (reasonable)${NC}"
fi

# Check 3: Files modified (proxy for context size)
MODIFIED_FILES=$(git diff --name-only HEAD | wc -l | tr -d ' ')
if [ $MODIFIED_FILES -gt 20 ]; then
    echo -e "${YELLOW}⚠️  Many files modified: ${MODIFIED_FILES} files${NC}"
    WARNINGS=$((WARNINGS + 1))
else
    echo -e "${GREEN}✅ Files modified: ${MODIFIED_FILES} (reasonable)${NC}"
fi

# Check 4: Error indicators (test failures, build failures)
# This would need integration with CI/CD or local test results
if [ -f "${PROJECT_ROOT}/.recent-failures" ]; then
    FAILURE_COUNT=$(cat "${PROJECT_ROOT}/.recent-failures")
    if [ $FAILURE_COUNT -gt $MAX_ERRORS ]; then
        echo -e "${YELLOW}⚠️  High error rate: ${FAILURE_COUNT} recent failures${NC}"
        TRIGGERS=$((TRIGGERS + 1))
    fi
fi

# Summary
echo ""
if [ $TRIGGERS -gt 0 ]; then
    echo -e "${RED}🚨 HANDOVER RECOMMENDED${NC}"
    echo -e "${RED}   ${TRIGGERS} threshold(s) exceeded${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Create handover document: ./scripts/create-handover.sh"
    echo "2. Add to queue: docs/development/workflow/HANDOVER_QUEUE.md"
    echo "3. Commit and handover to new agent"
    exit 1
elif [ $WARNINGS -gt 0 ]; then
    echo -e "${YELLOW}⚠️  HANDOVER MAY BE BENEFICIAL${NC}"
    echo -e "${YELLOW}   ${WARNINGS} warning(s) detected${NC}"
    echo ""
    echo "Consider creating handover if:"
    echo "- Work is at a natural breakpoint"
    echo "- Context is getting complex"
    echo "- Quality is declining"
    exit 0
else
    echo -e "${GREEN}✅ No handover needed${NC}"
    echo "All metrics within acceptable ranges"
    exit 0
fi

