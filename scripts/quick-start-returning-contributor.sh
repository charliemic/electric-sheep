#!/bin/bash

# Quick Start Script for Returning Contributors
# Gets you back up to speed in 2 minutes

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║    QUICK START FOR RETURNING CONTRIBUTORS                 ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if we're in the right directory
if [ ! -f "README.md" ] || [ ! -d ".git" ]; then
    echo "❌ Error: Must run from project root directory"
    exit 1
fi

echo "📋 Step 1: Syncing with remote..."
echo ""

# Check current branch
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")

if [ "$CURRENT_BRANCH" != "main" ]; then
    echo "⚠️  You're on branch: $CURRENT_BRANCH"
    echo "   Switching to main..."
    git checkout main 2>/dev/null || echo "   (Could not switch - may have uncommitted changes)"
fi

# Fetch latest
echo "   Fetching latest from remote..."
git fetch origin main --quiet 2>/dev/null || {
    echo "   ⚠️  Could not fetch from remote (may be offline)"
}

# Check if behind
LOCAL=$(git rev-parse main 2>/dev/null || echo "")
REMOTE=$(git rev-parse origin/main 2>/dev/null || echo "")

if [ -n "$LOCAL" ] && [ -n "$REMOTE" ] && [ "$LOCAL" != "$REMOTE" ]; then
    echo "   ⚠️  Local main is behind remote"
    echo "   → Run: git pull origin main"
else
    echo "   ✅ Up to date with remote"
fi

echo ""
echo "📋 Step 2: Verifying environment..."
echo ""

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "   ✅ Java: $JAVA_VERSION"
else
    echo "   ⚠️  Java not found - check README.md for setup"
fi

# Check Android SDK
if [ -n "$ANDROID_HOME" ]; then
    echo "   ✅ Android SDK: $ANDROID_HOME"
else
    echo "   ⚠️  ANDROID_HOME not set - check README.md for setup"
fi

# Check Gradle
if [ -f "gradlew" ]; then
    echo "   ✅ Gradle wrapper found"
else
    echo "   ❌ Gradle wrapper not found"
    exit 1
fi

echo ""
echo "📋 Step 3: Running pre-work check..."
echo ""

if [ -f "scripts/pre-work-check.sh" ]; then
    ./scripts/pre-work-check.sh
else
    echo "   ⚠️  Pre-work check script not found"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    READY TO CONTRIBUTE!                    ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "📚 Next Steps:"
echo ""
echo "1. Create a feature branch:"
echo "   git checkout -b feature/your-minor-update"
echo ""
echo "2. Make your changes"
echo ""
echo "3. Test your changes:"
echo "   ./gradlew test"
echo ""
echo "4. Commit and push:"
echo "   git add ."
echo "   git commit -m \"fix: your change description\""
echo "   git push origin feature/your-minor-update"
echo ""
echo "5. Create PR via GitHub UI or:"
echo "   gh pr create"
echo ""
echo "📖 Full guide: docs/development/ONBOARDING_RETURNING_CONTRIBUTORS.md"
echo ""
echo "Happy contributing! 🚀"

