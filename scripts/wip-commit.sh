#!/bin/bash

# Quick WIP (Work In Progress) Commit Helper
# Creates a quick commit with WIP prefix to preserve work
# Usage: ./scripts/wip-commit.sh "description of work"

set -e

# Check if description provided
if [ -z "$1" ]; then
    echo "Usage: ./scripts/wip-commit.sh \"description of work\""
    echo ""
    echo "Examples:"
    echo "  ./scripts/wip-commit.sh \"adding user authentication\""
    echo "  ./scripts/wip-commit.sh \"refactoring error handling\""
    echo "  ./scripts/wip-commit.sh \"implementing new feature\""
    exit 1
fi

DESCRIPTION="$1"
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")

# Check if on a branch
if [ -z "$CURRENT_BRANCH" ]; then
    echo "❌ ERROR: Not on a branch. Cannot commit."
    exit 1
fi

# Check if on main branch
if [ "$CURRENT_BRANCH" = "main" ]; then
    echo "❌ ERROR: You are on 'main' branch!"
    echo "   → Create a feature branch first: git checkout -b feature/<task-name>"
    exit 1
fi

# Check if there are changes to commit
if [ -z "$(git status --porcelain 2>/dev/null)" ]; then
    echo "⚠️  No changes to commit. Working directory is clean."
    exit 0
fi

# Create commit message with timestamp
TIMESTAMP=$(date +%H:%M)
COMMIT_MESSAGE="WIP: $DESCRIPTION"

# Stage all changes
echo "📝 Staging all changes..."
git add -A

# Show what will be committed
echo ""
echo "📋 Changes to be committed:"
git status --short
echo ""

# Commit
echo "💾 Creating WIP commit..."
git commit -m "$COMMIT_MESSAGE" || {
    echo "❌ Commit failed. Check git status for details."
    exit 1
}

# Show success
echo ""
echo "✅ WIP commit created successfully!"
echo "   Branch: $CURRENT_BRANCH"
echo "   Message: $COMMIT_MESSAGE"
echo ""
echo "💡 Next steps:"
echo "   - Continue working (commit frequently!)"
echo "   - When ready, clean up WIP commits: git rebase -i HEAD~N"
echo "   - Push when ready for review: git push origin $CURRENT_BRANCH"
echo ""
echo "💡 Remember: Commit frequently (every 15-30 min) to prevent work loss!"

