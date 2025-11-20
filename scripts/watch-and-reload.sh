#!/bin/bash

# Watch for file changes and automatically rebuild/reload the app
# Usage: ./scripts/watch-and-reload.sh
# Requires: fswatch (install via: brew install fswatch)

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}👀 Electric Sheep - Watch and Reload${NC}"
echo ""

# Check if fswatch is installed
if ! command -v fswatch &> /dev/null; then
    echo -e "${YELLOW}⚠ fswatch not found. Installing via Homebrew...${NC}"
    brew install fswatch
fi

# Set Java 17 if available (Mac-native tool)
if JAVA_HOME_17=$(/usr/libexec/java_home -v 17 2>/dev/null); then
    export JAVA_HOME="$JAVA_HOME_17"
elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
fi

# Check if emulator is running
if ! adb devices | grep -q "device$"; then
    echo -e "${YELLOW}⚠ No device connected. Please start an emulator first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} Watching for changes in app/src/..."
echo -e "${BLUE}Press Ctrl+C to stop${NC}"
echo ""

# Watch for changes in source files
fswatch -o app/src/ | while read f; do
    echo -e "${BLUE}🔄 Change detected, rebuilding...${NC}"
    
    # Build and install
    ./gradlew installDebug 2>&1 | grep -E "(BUILD|Task|error|Error|FAILED)" | tail -10
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Build successful, restarting app..."
        adb shell am force-stop com.electricsheep.app
        sleep 0.5
        adb shell am start -n com.electricsheep.app/.MainActivity
        echo -e "${GREEN}✓${NC} App restarted"
    else
        echo -e "${YELLOW}⚠ Build failed, check errors above${NC}"
    fi
    
    echo ""
done

