#!/bin/bash

# OCR Integration Test Script
# Automatically tests OCR functionality on real screenshots

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
TEST_AUTOMATION_DIR="$PROJECT_ROOT/test-automation"

cd "$PROJECT_ROOT"

echo "🔍 OCR Integration Test"
echo "======================"
echo ""

# Check if we have screenshots
SCREENSHOT_DIR="$TEST_AUTOMATION_DIR/test-results/screenshots"
if [ ! -d "$SCREENSHOT_DIR" ]; then
    echo "📸 No screenshots directory found. Running a quick test to generate one..."
    echo ""
    
    # Check if emulator is available
    DEVICE=$(./scripts/get-device-id.sh 2>/dev/null || echo "")
    if [ -z "$DEVICE" ]; then
        echo "⚠️  No device found. Starting emulator..."
        ./scripts/emulator-manager.sh ensure
        sleep 5
        DEVICE=$(./scripts/get-device-id.sh 2>/dev/null || echo "emulator-5554")
    fi
    
    echo "📱 Using device: $DEVICE"
    echo "🚀 Running quick test to generate screenshot..."
    
    # Run a minimal test to generate a screenshot
    timeout 60 ./gradlew -p test-automation run --args="--task 'Capture screenshot' --device $DEVICE" 2>&1 | tail -20 || true
    
    echo ""
    echo "⏳ Waiting for screenshot to be saved..."
    sleep 2
fi

# Find latest screenshot
LATEST_SCREENSHOT=$(find "$SCREENSHOT_DIR" -name "*.png" -type f 2>/dev/null | sort -r | head -1)

if [ -z "$LATEST_SCREENSHOT" ]; then
    echo "❌ No screenshots found in $SCREENSHOT_DIR"
    echo ""
    echo "Please run a test first to generate screenshots, or provide a screenshot path:"
    echo "  ./scripts/test-ocr-integration.sh <path-to-screenshot.png>"
    exit 1
fi

echo "📸 Found screenshot: $(basename "$LATEST_SCREENSHOT")"
echo ""

# Run OCR integration test
echo "🧪 Running OCR integration test..."
echo ""

cd "$TEST_AUTOMATION_DIR"
./gradlew testOcr --args="$LATEST_SCREENSHOT" 2>&1 || {
    echo ""
    echo "⚠️  Gradle task failed, trying direct execution..."
    ./gradlew build
    java -cp "build/classes/kotlin/main:build/libs/*:$(./gradlew -q printClasspath)" \
        com.electricsheep.testautomation.vision.OcrIntegrationTestKt "$LATEST_SCREENSHOT"
}

echo ""
echo "✅ OCR Integration Test Complete"

