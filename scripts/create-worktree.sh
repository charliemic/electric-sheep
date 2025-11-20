#!/bin/bash

# Create a git worktree for isolated development
# Usage: ./scripts/create-worktree.sh <task-name> [branch-name]

set -e

TASK_NAME="$1"
BRANCH_NAME="${2:-feature/$TASK_NAME}"

if [ -z "$TASK_NAME" ]; then
    echo "Usage: $0 <task-name> [branch-name]"
    echo "Example: $0 test-helpers"
    echo "Example: $0 design-system feature/design-system"
    exit 1
fi

# Get the main repository path
MAIN_REPO=$(git rev-parse --show-toplevel)
WORKTREE_DIR="../electric-sheep-$TASK_NAME"

# Check if worktree already exists
if [ -d "$WORKTREE_DIR" ]; then
    echo "❌ Worktree already exists: $WORKTREE_DIR"
    echo "   Remove it first: git worktree remove $WORKTREE_DIR"
    exit 1
fi

# Ensure we're in the main repo
cd "$MAIN_REPO"

# Check if branch already exists
if git show-ref --verify --quiet "refs/heads/$BRANCH_NAME"; then
    echo "⚠️  Branch $BRANCH_NAME already exists"
    echo "   Creating worktree from existing branch"
    git worktree add "$WORKTREE_DIR" "$BRANCH_NAME"
else
    echo "✅ Creating new branch: $BRANCH_NAME"
    git worktree add "$WORKTREE_DIR" -b "$BRANCH_NAME"
fi

echo ""
echo "✅ Worktree created: $WORKTREE_DIR"
echo ""
echo "To work in isolation:"
echo "  cd $WORKTREE_DIR"
echo ""
echo "To remove when done:"
echo "  git worktree remove $WORKTREE_DIR"
echo "  # Or from main repo:"
echo "  cd $MAIN_REPO && git worktree remove $WORKTREE_DIR"
echo ""

