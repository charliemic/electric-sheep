#!/usr/bin/env bash
# Create handover document automatically
# Usage: ./scripts/create-handover.sh [task-name]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

TASK_NAME="${1:-$(basename $(git rev-parse --show-toplevel))}"
HANDOVER_DIR="${PROJECT_ROOT}/docs/development/handovers"
HANDOVER_FILE="${HANDOVER_DIR}/${TASK_NAME}-$(date +%Y%m%d).md"
QUEUE_FILE="${PROJECT_ROOT}/docs/development/workflow/HANDOVER_QUEUE.md"

# Create handovers directory if needed
mkdir -p "$HANDOVER_DIR"

echo -e "${BLUE}📝 Creating handover document...${NC}"
echo ""

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" = "main" ]; then
    echo -e "${RED}Error: Cannot create handover on main branch${NC}"
    exit 1
fi

# Get recent commits
RECENT_COMMITS=$(git log --oneline -10)

# Get modified files
MODIFIED_FILES=$(git diff --name-only main...HEAD 2>/dev/null || git diff --name-only HEAD)

# Get current status
CURRENT_STATUS=$(git status --short)

# Create handover document
cat > "$HANDOVER_FILE" << EOF
# Handover: ${TASK_NAME}

**Date**: $(date +%Y-%m-%d)  
**Status**: Ready for continuation  
**Branch**: \`${CURRENT_BRANCH}\`

## Context

[Describe what work was being done]

## Current State

### What's Done
$(echo "$RECENT_COMMITS" | head -5 | sed 's/^/- ✅ /')

### What's In Progress
- [Current work items]

### What's Next
- [Next steps clearly defined]

## Key Files Modified

$(echo "$MODIFIED_FILES" | head -10 | sed 's/^/- `/' | sed 's/$/`/')

## Current Status

\`\`\`
${CURRENT_STATUS}
\`\`\`

## Next Steps

1. [Step 1]
2. [Step 2]
3. [Step 3]

## Questions/Decisions

- [Open questions]
- [Decisions needed]

## Related Work

- [Related PRs]
- [Related issues]
- [Related documentation]

## Notes

[Any additional context]
EOF

echo -e "${GREEN}✅ Handover document created: ${HANDOVER_FILE}${NC}"
echo ""
echo "Next steps:"
echo "1. Review and complete the handover document"
echo "2. Add to queue: ${QUEUE_FILE}"
echo "3. Commit the handover"
echo "4. Handover to new agent"

