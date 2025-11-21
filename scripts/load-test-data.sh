#!/bin/bash

# Load test data for mood chart testing
# This script loads test fixture data into both local database and Supabase (if configured)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Default values
TECH_LEVEL="${1:-NOVICE}"
MOOD_PATTERN="${2:-HIGH_STABLE}"
DAYS="${3:-30}"
DEVICE_ID="${4:-}"

echo "═══════════════════════════════════════════════════════════════"
echo "📊 LOADING TEST DATA FOR MOOD CHART"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "Tech Level: $TECH_LEVEL"
echo "Mood Pattern: $MOOD_PATTERN"
echo "Days: $DAYS"
echo ""

# Get device ID if not provided
if [ -z "$DEVICE_ID" ]; then
    if [ -f "$SCRIPT_DIR/get-device-id.sh" ]; then
        DEVICE_ID=$("$SCRIPT_DIR/get-device-id.sh" 2>/dev/null || echo "")
    fi
fi

if [ -z "$DEVICE_ID" ]; then
    echo "⚠️  No device found. Starting emulator..."
    "$SCRIPT_DIR/emulator-manager.sh" ensure
    DEVICE_ID=$("$SCRIPT_DIR/get-device-id.sh")
fi

echo "📱 Device: $DEVICE_ID"
echo ""

# Build the app first
echo "🔨 Building app..."
cd "$PROJECT_ROOT"
./gradlew :app:assembleDebug --no-daemon > /dev/null 2>&1 || {
    echo "❌ Build failed"
    exit 1
}

# Install app
echo "📦 Installing app..."
adb -s "$DEVICE_ID" install -r "$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk" > /dev/null 2>&1 || {
    echo "❌ Installation failed"
    exit 1
}

# Launch app
echo "🚀 Launching app..."
adb -s "$DEVICE_ID" shell am start -n com.electricsheep.app/.MainActivity > /dev/null 2>&1

echo ""
echo "✅ App installed and launched"
echo ""
echo "📝 To load test data programmatically, use:"
echo "   TestDataLoader.loadTestData("
echo "       techLevel = TechLevel.$TECH_LEVEL,"
echo "       moodPattern = MoodPattern.$MOOD_PATTERN,"
echo "       days = $DAYS"
echo "   )"
echo ""
echo "💡 Test data will be loaded when you sign in with a test user:"
echo "   Email: test-${TECH_LEVEL,,}-${MOOD_PATTERN,,}@electric-sheep.test"
echo ""



