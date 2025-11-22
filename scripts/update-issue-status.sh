#!/bin/bash

# Update GitHub Issue Status Label
# Automatically updates issue status labels as work progresses
#
# Usage:
#   ./scripts/update-issue-status.sh <issue-number> <status>
#
# Status values:
#   - not-started
#   - in-progress
#   - blocked
#   - review
#   - completed
#
# Examples:
#   ./scripts/update-issue-status.sh 52 in-progress
#   ./scripts/update-issue-status.sh 52 blocked
#   ./scripts/update-issue-status.sh 52 review
#   ./scripts/update-issue-status.sh 52 completed

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if GitHub CLI is installed
if ! command -v gh >/dev/null 2>&1; then
    echo -e "${RED}❌ GitHub CLI (gh) not found${NC}"
    echo "   → Install: brew install gh (macOS) or see https://cli.github.com/"
    exit 1
fi

# Check if authenticated
if ! gh auth status >/dev/null 2>&1; then
    echo -e "${RED}❌ Not authenticated with GitHub${NC}"
    echo "   → Run: gh auth login"
    exit 1
fi

# Validate arguments
if [ $# -lt 2 ]; then
    echo -e "${RED}❌ Usage: $0 <issue-number> <status>${NC}"
    echo ""
    echo "Status values:"
    echo "  - not-started"
    echo "  - in-progress"
    echo "  - blocked"
    echo "  - review"
    echo "  - completed"
    echo ""
    echo "Examples:"
    echo "  $0 52 in-progress"
    echo "  $0 52 blocked"
    exit 1
fi

ISSUE_NUMBER="$1"
NEW_STATUS="$2"

# Validate status
VALID_STATUSES=("not-started" "in-progress" "blocked" "review" "completed")
VALID=false
for status in "${VALID_STATUSES[@]}"; do
    if [ "$NEW_STATUS" = "$status" ]; then
        VALID=true
        break
    fi
done

if [ "$VALID" = false ]; then
    echo -e "${RED}❌ Invalid status: $NEW_STATUS${NC}"
    echo "   Valid statuses: ${VALID_STATUSES[*]}"
    exit 1
fi

# Get current issue labels
echo -e "${BLUE}📋 Updating issue #$ISSUE_NUMBER status to '$NEW_STATUS'...${NC}"

# Get current labels
CURRENT_LABELS=$(gh issue view "$ISSUE_NUMBER" --json labels --jq '.labels[].name' 2>/dev/null || echo "")

if [ -z "$CURRENT_LABELS" ]; then
    echo -e "${RED}❌ Issue #$ISSUE_NUMBER not found or not accessible${NC}"
    exit 1
fi

# Find current status label
CURRENT_STATUS_LABEL=""
for label in $CURRENT_LABELS; do
    if [[ "$label" =~ ^status: ]]; then
        CURRENT_STATUS_LABEL="$label"
        break
    fi
done

# Prepare label update command
LABEL_UPDATE_CMD="gh issue edit $ISSUE_NUMBER"

# Remove old status label if exists
if [ -n "$CURRENT_STATUS_LABEL" ]; then
    if [ "$CURRENT_STATUS_LABEL" = "status:$NEW_STATUS" ]; then
        echo -e "${YELLOW}⚠️  Issue #$ISSUE_NUMBER already has status '$NEW_STATUS'${NC}"
        exit 0
    fi
    LABEL_UPDATE_CMD="$LABEL_UPDATE_CMD --remove-label \"$CURRENT_STATUS_LABEL\""
    echo -e "${BLUE}   → Removing: $CURRENT_STATUS_LABEL${NC}"
fi

# Add new status label
NEW_STATUS_LABEL="status:$NEW_STATUS"
LABEL_UPDATE_CMD="$LABEL_UPDATE_CMD --add-label \"$NEW_STATUS_LABEL\""
echo -e "${BLUE}   → Adding: $NEW_STATUS_LABEL${NC}"

# Execute label update
if eval "$LABEL_UPDATE_CMD" >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Issue #$ISSUE_NUMBER status updated to '$NEW_STATUS'${NC}"
    
    # Optional: Add comment
    if [ "$NEW_STATUS" = "in-progress" ]; then
        COMMENT="Work started on this issue"
    elif [ "$NEW_STATUS" = "blocked" ]; then
        COMMENT="Issue is blocked"
    elif [ "$NEW_STATUS" = "review" ]; then
        COMMENT="PR created, issue in review"
    elif [ "$NEW_STATUS" = "completed" ]; then
        COMMENT="PR merged, issue completed"
    else
        COMMENT=""
    fi
    
    if [ -n "$COMMENT" ]; then
        gh issue comment "$ISSUE_NUMBER" --body "$COMMENT" >/dev/null 2>&1 || true
    fi
    
    exit 0
else
    echo -e "${RED}❌ Failed to update issue #$ISSUE_NUMBER${NC}"
    echo "   → Check issue number and permissions"
    exit 1
fi

