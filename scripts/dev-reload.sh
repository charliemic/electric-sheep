#!/bin/bash

# Development reload script for Electric Sheep
# Rebuilds, installs, and launches the app with log monitoring
# Usage: ./scripts/dev-reload.sh

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Electric Sheep - Development Reload${NC}"
echo ""

# Set Java 17 if available (Mac-native tool)
if JAVA_HOME_17=$(/usr/libexec/java_home -v 17 2>/dev/null); then
    export JAVA_HOME="$JAVA_HOME_17"
    echo -e "${GREEN}✓${NC} Using Java 17: $JAVA_HOME"
elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
    echo -e "${GREEN}✓${NC} Using Java 17: $JAVA_HOME"
else
    echo -e "${YELLOW}⚠${NC} Java 17 not found. Build may fail with Java 24+"
fi

# Find Android SDK and ADB
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    fi
fi

ADB="${ANDROID_HOME}/platform-tools/adb"
if [ ! -f "$ADB" ]; then
    ADB="adb"  # Fallback to system adb
fi

# Check if emulator is running (ensure one is available)
echo -e "${BLUE}Checking for connected device...${NC}"
DEVICE=$(./scripts/get-device-id.sh 2>/dev/null || ./scripts/emulator-manager.sh ensure 2>/dev/null || echo "")

if [ -z "$DEVICE" ]; then
    echo -e "${YELLOW}⚠ No device connected. Starting emulator...${NC}"
    DEVICE=$(./scripts/emulator-manager.sh ensure)
fi

echo -e "${GREEN}✓${NC} Device found: $DEVICE"

# Clean build (optional, can be skipped for faster iteration)
if [ "$1" == "--clean" ]; then
    echo -e "${BLUE}Cleaning build...${NC}"
    ./gradlew clean
fi

# Build and install
echo -e "${BLUE}Building and installing app...${NC}"
./gradlew installDebug

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Build successful"
    
    # Clear app data to trigger fresh migration (optional)
    if [ "$1" == "--fresh" ]; then
        echo -e "${BLUE}Clearing app data for fresh start...${NC}"
        $ADB -s "$DEVICE" shell pm clear com.electricsheep.app
        echo -e "${GREEN}✓${NC} App data cleared"
    fi
    
    # Launch app
    echo -e "${BLUE}Launching app...${NC}"
    $ADB -s "$DEVICE" shell am start -n com.electricsheep.app/.MainActivity
    
    # Wait a moment for app to start
    sleep 2
    
    # Show recent logs
    echo -e "${BLUE}Recent app logs:${NC}"
    echo ""
    $ADB -s "$DEVICE" logcat -d | grep -i "electric\|mood\|database\|migration" | tail -20
    
    echo ""
    echo -e "${GREEN}✓${NC} App launched successfully"
    echo ""
    echo -e "${BLUE}To watch logs in real-time, run:${NC}"
    echo "  adb -s $DEVICE logcat | grep -i electric"
    echo ""
    echo -e "${BLUE}To access the database:${NC}"
    echo "  adb -s $DEVICE shell"
    echo "  run-as com.electricsheep.app"
    echo "  cd /data/data/com.electricsheep.app/databases"
    echo "  sqlite3 electric_sheep_database"
    echo ""
    echo -e "${BLUE}Emulator management:${NC}"
    echo "  ./scripts/emulator-manager.sh status  # Check status"
    echo "  ./scripts/emulator-manager.sh stop    # Stop emulator"
else
    echo -e "${YELLOW}⚠ Build failed${NC}"
    exit 1
fi

