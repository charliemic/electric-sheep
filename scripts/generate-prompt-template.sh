#!/bin/bash

# Prompt Template Generator
# Generates structured prompt templates with mandatory checklists

set -e

TASK_TYPE="${1:-feature}"
TASK_DESCRIPTION="${2:-}"

if [ -z "$TASK_DESCRIPTION" ]; then
    echo "Usage: $0 <task-type> <task-description>"
    echo ""
    echo "Task types: feature, fix, refactor, docs, test"
    echo ""
    echo "Examples:"
    echo "  $0 feature user-authentication"
    echo "  $0 fix login-bug"
    echo "  $0 refactor error-handling"
    echo ""
    exit 1
fi

echo "╔════════════════════════════════════════════════════════════╗"
echo "║         PROMPT TEMPLATE GENERATOR                         ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Task: $TASK_TYPE/$TASK_DESCRIPTION"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Pre-Work Section
echo "## MANDATORY Pre-Work Checklist"
echo ""
echo "Before starting, run:"
echo "\`\`\`bash"
echo "./scripts/pre-work-check.sh"
echo "\`\`\`"
echo ""
echo "This will verify:"
echo "- [ ] Not on main branch"
echo "- [ ] Latest main pulled"
echo "- [ ] Coordination checked"
echo "- [ ] Relevant rules discovered"
echo "- [ ] Available tools identified"
echo ""

# Rule Discovery
echo "## Discover Relevant Rules"
echo ""
echo "Run rule discovery:"
echo "\`\`\`bash"
echo "./scripts/discover-rules.sh $TASK_DESCRIPTION"
echo "\`\`\`"
echo ""
echo "Read relevant rules from \`.cursor/rules/\`:"
RULES=$(./scripts/discover-rules.sh "$TASK_DESCRIPTION" 2>/dev/null | grep "^📋" | sed 's/^📋 /- /' || echo "- Check .cursor/rules/ directory")
echo "$RULES"
echo ""

# Coordination
echo "## Check Coordination"
echo ""
echo "Before modifying files:"
echo "\`\`\`bash"
echo "./scripts/check-agent-coordination.sh"
echo "\`\`\`"
echo ""
echo "If modifying shared files, use worktree:"
echo "\`\`\`bash"
echo "./scripts/create-worktree.sh $TASK_DESCRIPTION"
echo "\`\`\`"
echo ""

# Implementation
echo "## Implementation"
echo ""
echo "Following the rules discovered above, implement:"
echo "- [ ] $TASK_DESCRIPTION"
echo ""
echo "**Explicitly reference rules in your implementation:**"
echo "- \"Following .cursor/rules/<rule-name>.mdc, I will...\""
echo "- \"Per .cursor/rules/<rule-name>.mdc, I will use...\""
echo ""

# Testing
echo "## Testing"
echo ""
echo "Before committing:"
echo "\`\`\`bash"
echo "./gradlew test"
echo "\`\`\`"
echo ""
echo "- [ ] All tests pass locally"
echo "- [ ] No failing tests"
echo "- [ ] Tests added for new functionality"
echo ""

# Documentation
echo "## Documentation"
echo ""
echo "Update coordination doc:"
echo "- [ ] Add entry to \`docs/development/AGENT_COORDINATION.md\`"
echo "- [ ] Document files modified"
echo "- [ ] Update status (In Progress → Complete)"
echo ""

# Post-Merge (if applicable)
echo "## Post-Merge Cleanup"
echo ""
echo "After PR merge, run:"
echo "\`\`\`bash"
echo "./scripts/post-merge-cleanup.sh <pr-number>"
echo "\`\`\`"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 This template ensures all mandatory steps are included."
echo ""

