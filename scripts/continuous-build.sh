#!/bin/bash

# Continuous build using Gradle's built-in watch mode
# Usage: ./scripts/continuous-build.sh
# This uses Gradle's continuous build feature to automatically rebuild on changes

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Electric Sheep - Continuous Build${NC}"
echo ""

# Set Java 17 if available
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
fi

# Check if emulator is running
if ! adb devices | grep -q "device$"; then
    echo -e "${YELLOW}⚠ No device connected. Please start an emulator first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} Starting continuous build..."
echo -e "${BLUE}Gradle will watch for changes and rebuild automatically${NC}"
echo -e "${BLUE}Press Ctrl+C to stop${NC}"
echo ""

# Use Gradle's continuous build
# This will rebuild whenever source files change
./gradlew installDebug --continuous

