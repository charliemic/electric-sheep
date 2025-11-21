#!/bin/bash

# Environment setup helper for Electric Sheep development
# Usage: source scripts/setup-env.sh
# This script sets up JAVA_HOME and ANDROID_HOME for Mac development

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Set Java 17 if available (Mac-native tool)
if JAVA_HOME_17=$(/usr/libexec/java_home -v 17 2>/dev/null); then
    export JAVA_HOME="$JAVA_HOME_17"
    echo -e "${GREEN}✓${NC} JAVA_HOME set to Java 17: $JAVA_HOME"
elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
    echo -e "${GREEN}✓${NC} JAVA_HOME set to Java 17: $JAVA_HOME"
else
    echo -e "${YELLOW}⚠${NC} Java 17 not found. Install with: brew install openjdk@17"
    echo -e "${YELLOW}⚠${NC} Or download from: https://adoptium.net/temurin/releases/?version=17"
    if [ -n "$JAVA_HOME" ]; then
        echo -e "${YELLOW}⚠${NC} Current JAVA_HOME: $JAVA_HOME (may cause build issues with Java 24+)"
    fi
fi

# Set Android SDK if not already set
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
        echo -e "${GREEN}✓${NC} ANDROID_HOME set to: $ANDROID_HOME"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
        echo -e "${GREEN}✓${NC} ANDROID_HOME set to: $ANDROID_HOME"
    else
        echo -e "${YELLOW}⚠${NC} Android SDK not found. Install with: brew install --cask android-commandlinetools"
    fi
else
    echo -e "${GREEN}✓${NC} ANDROID_HOME already set: $ANDROID_HOME"
fi

# Add Android SDK tools to PATH if not already there
if [ -n "$ANDROID_HOME" ]; then
    if [[ ":$PATH:" != *":$ANDROID_HOME/platform-tools:"* ]]; then
        export PATH="$PATH:$ANDROID_HOME/platform-tools"
        echo -e "${GREEN}✓${NC} Added Android platform-tools to PATH"
    fi
    if [[ ":$PATH:" != *":$ANDROID_HOME/cmdline-tools/latest/bin:"* ]]; then
        export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
        echo -e "${GREEN}✓${NC} Added Android cmdline-tools to PATH"
    fi
fi

# Verify Java version
if [ -n "$JAVA_HOME" ]; then
    JAVA_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 18 ]; then
        echo -e "${RED}✗${NC} Warning: Java $JAVA_VERSION detected. Android builds require Java 17 or earlier."
        echo -e "${RED}✗${NC} Please install Java 17: brew install openjdk@17"
    fi
fi



