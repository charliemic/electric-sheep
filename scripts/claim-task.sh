#!/bin/bash

# Claim a task and set up work environment
# Usage: ./scripts/claim-task.sh <issue-number> [branch-name]

set -e

ISSUE_NUMBER=$1
BRANCH_NAME=$2

if [ -z "$ISSUE_NUMBER" ]; then
    echo "❌ Error: Issue number required"
    echo "Usage: ./scripts/claim-task.sh <issue-number> [branch-name]"
    echo ""
    echo "Example:"
    echo "  ./scripts/claim-task.sh 100"
    echo "  ./scripts/claim-task.sh 100 feature/onboarding-screens"
    exit 1
fi

# Default branch name if not provided
if [ -z "$BRANCH_NAME" ]; then
    BRANCH_NAME="feature/task-$ISSUE_NUMBER"
fi

echo "📋 Claiming Task #$ISSUE_NUMBER"
echo ""

# Check if GitHub CLI is available
if ! command -v gh &> /dev/null; then
    echo "❌ Error: GitHub CLI not found"
    echo "Install: https://cli.github.com/"
    exit 1
fi

# Get issue details
echo "→ Fetching issue details..."
ISSUE=$(gh issue view "$ISSUE_NUMBER" --json number,title,labels,state,body 2>/dev/null || echo "{}")

if [ "$ISSUE" = "{}" ]; then
    echo "❌ Error: Issue #$ISSUE_NUMBER not found"
    exit 1
fi

ISSUE_TITLE=$(echo "$ISSUE" | jq -r '.title')
ISSUE_STATE=$(echo "$ISSUE" | jq -r '.state')
CURRENT_STATUS=$(echo "$ISSUE" | jq -r '.labels[] | select(.name | startswith("status:")) | .name' | head -1 || echo "")

# Check if issue is already claimed
if [ "$CURRENT_STATUS" = "status:in-progress" ] || [ "$CURRENT_STATUS" = "status:claimed" ]; then
    ASSIGNEE=$(gh issue view "$ISSUE_NUMBER" --json assignees --jq '.assignees[].login' | head -1 || echo "")
    if [ -n "$ASSIGNEE" ]; then
        echo "⚠️  Warning: Issue #$ISSUE_NUMBER is already assigned to @$ASSIGNEE"
        echo "   Status: $CURRENT_STATUS"
        echo ""
        read -p "Do you want to claim it anyway? (y/N) " -n 1 -r
        echo ""
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "Cancelled."
            exit 0
        fi
    fi
fi

# Claim the issue
echo "→ Claiming issue..."
gh issue edit "$ISSUE_NUMBER" \
    --add-label "status:in-progress" \
    --remove-label "status:available" \
    --assign "@me" 2>/dev/null || {
    echo "❌ Error: Failed to claim issue #$ISSUE_NUMBER"
    exit 1
}

echo "✅ Claimed issue #$ISSUE_NUMBER: $ISSUE_TITLE"
echo ""

# Check current branch
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")

# Create branch if needed
if [ "$CURRENT_BRANCH" = "main" ] || [ -z "$CURRENT_BRANCH" ]; then
    echo "→ Creating branch: $BRANCH_NAME"
    git checkout -b "$BRANCH_NAME" 2>/dev/null || {
        echo "⚠️  Warning: Could not create branch (may already exist)"
        echo "→ Switching to existing branch..."
        git checkout "$BRANCH_NAME" 2>/dev/null || {
            echo "❌ Error: Could not create or switch to branch"
            exit 1
        }
    }
    echo "✅ On branch: $BRANCH_NAME"
else
    echo "ℹ️  Already on branch: $CURRENT_BRANCH"
    echo "   (Not creating new branch - you're already on a feature branch)"
fi

echo ""

# Update coordination doc
COORDINATION_DOC="docs/development/workflow/AGENT_COORDINATION.md"
if [ -f "$COORDINATION_DOC" ]; then
    echo "→ Updating coordination doc..."
    
    # Check if entry already exists
    if grep -q "Issue #$ISSUE_NUMBER" "$COORDINATION_DOC"; then
        echo "   ℹ️  Entry already exists in coordination doc"
    else
        echo "   💡 Add entry to coordination doc:"
        echo "      Task: $ISSUE_TITLE"
        echo "      Issue: #$ISSUE_NUMBER"
        echo "      Branch: $BRANCH_NAME"
        echo "      Status: In Progress"
    fi
else
    echo "   ⚠️  Coordination doc not found: $COORDINATION_DOC"
fi

echo ""

# Show next steps
echo "📖 Next Steps:"
echo ""
echo "1. Read task details:"
echo "   → Issue: gh issue view $ISSUE_NUMBER"
echo "   → Task board: docs/development/workflow/TASK_BOARD.md"
echo ""
echo "2. Review task requirements:"
echo "   → Acceptance criteria"
echo "   → Files to modify"
echo "   → Dependencies"
echo ""
echo "3. Start work:"
echo "   → Update coordination doc: $COORDINATION_DOC"
echo "   → Begin implementation"
echo "   → Update status as work progresses"
echo ""
echo "4. When complete:"
echo "   → Update issue: gh issue edit $ISSUE_NUMBER --add-label 'status:complete' --remove-label 'status:in-progress'"
echo "   → Create PR"
echo "   → Update coordination doc"
echo ""

# Show issue details
echo "📋 Task Details:"
echo "$ISSUE" | jq -r '
    "   Title: \(.title)
    Status: \(.labels[] | select(.name | startswith("status:")) | .name | sub("status:"; ""))
    Skills: \(.labels[] | select(.name | startswith("skill:")) | .name | sub("skill:"; ""))
    Priority: \(.labels[] | select(.name | startswith("priority:")) | .name | sub("priority:"; "") // "Not set")
    "'

echo ""
echo "✅ Ready to start work on task #$ISSUE_NUMBER!"

