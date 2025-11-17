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

# Set Java 17 if available
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
    echo -e "${GREEN}✓${NC} Using Java 17"
fi

# Check if emulator is running
echo -e "${BLUE}Checking for connected device...${NC}"
if ! adb devices | grep -q "device$"; then
    echo -e "${YELLOW}⚠ No device connected. Please start an emulator first.${NC}"
    exit 1
fi

DEVICE=$(adb devices | grep "device$" | head -1 | cut -f1)
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
        adb shell pm clear com.electricsheep.app
        echo -e "${GREEN}✓${NC} App data cleared"
    fi
    
    # Launch app
    echo -e "${BLUE}Launching app...${NC}"
    adb shell am start -n com.electricsheep.app/.MainActivity
    
    # Wait a moment for app to start
    sleep 2
    
    # Show recent logs
    echo -e "${BLUE}Recent app logs:${NC}"
    echo ""
    adb logcat -d | grep -i "electric\|mood\|database\|migration" | tail -20
    
    echo ""
    echo -e "${GREEN}✓${NC} App launched successfully"
    echo ""
    echo -e "${BLUE}To watch logs in real-time, run:${NC}"
    echo "  adb logcat | grep -i electric"
    echo ""
    echo -e "${BLUE}To access the database:${NC}"
    echo "  adb shell"
    echo "  run-as com.electricsheep.app"
    echo "  cd /data/data/com.electricsheep.app/databases"
    echo "  sqlite3 electric_sheep_database"
else
    echo -e "${YELLOW}⚠ Build failed${NC}"
    exit 1
fi

