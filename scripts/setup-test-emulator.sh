#!/bin/bash

# Setup script for creating a new emulator and testing environment switching
# Usage: ./scripts/setup-test-emulator.sh

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}📱 Electric Sheep - Test Emulator Setup${NC}"
echo ""

# Find Android SDK
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo -e "${RED}✗${NC} Android SDK not found. Please set ANDROID_HOME or install Android SDK."
        exit 1
    fi
fi

EMULATOR="$ANDROID_HOME/emulator/emulator"
ADB="$ANDROID_HOME/platform-tools/adb"

# Check if emulator and adb exist
if [ ! -f "$EMULATOR" ]; then
    echo -e "${RED}✗${NC} Emulator not found at $EMULATOR"
    exit 1
fi

if [ ! -f "$ADB" ]; then
    echo -e "${RED}✗${NC} ADB not found at $ADB"
    exit 1
fi

echo -e "${GREEN}✓${NC} Android SDK found at: $ANDROID_HOME"
echo ""

# List existing emulators
echo -e "${BLUE}Existing emulators:${NC}"
$EMULATOR -list-avds
echo ""

# Check if we need to create a new emulator
EMULATOR_NAME="electric_sheep_env_test"
EMULATOR_EXISTS=false

if $EMULATOR -list-avds | grep -q "^${EMULATOR_NAME}$"; then
    echo -e "${YELLOW}⚠${NC} Emulator '$EMULATOR_NAME' already exists"
    EMULATOR_EXISTS=true
fi

# Create new emulator if it doesn't exist
if [ "$EMULATOR_EXISTS" = false ]; then
    echo -e "${BLUE}Creating new emulator: $EMULATOR_NAME${NC}"
    
    # Try to find avdmanager in various locations
    AVDMANAGER=""
    POSSIBLE_PATHS=(
        "$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager"
        "$ANDROID_HOME/cmdline-tools/bin/avdmanager"
        "$ANDROID_HOME/tools/bin/avdmanager"
        "$(which avdmanager 2>/dev/null)"
    )
    
    for path in "${POSSIBLE_PATHS[@]}"; do
        if [ -f "$path" ]; then
            AVDMANAGER="$path"
            break
        fi
    done
    
    if [ -n "$AVDMANAGER" ]; then
        echo -e "${GREEN}✓${NC} Found avdmanager at: $AVDMANAGER"
        
        # Try to create AVD (this might require system image to be installed)
        echo -e "${BLUE}Attempting to create AVD...${NC}"
        if $AVDMANAGER create avd \
            -n "$EMULATOR_NAME" \
            -k "system-images;android-34;google_apis;x86_64" \
            -d "pixel_5" \
            --force > /dev/null 2>&1; then
            echo -e "${GREEN}✓${NC} Emulator created successfully"
        else
            echo -e "${YELLOW}⚠${NC} Could not create AVD automatically (system image may not be installed)"
            # Use first available emulator as fallback
            FALLBACK_EMULATOR=$($EMULATOR -list-avds | head -1)
            if [ -n "$FALLBACK_EMULATOR" ]; then
                echo -e "${BLUE}Using existing emulator instead: $FALLBACK_EMULATOR${NC}"
                EMULATOR_NAME="$FALLBACK_EMULATOR"
            else
                echo -e "${RED}✗${NC} No emulators available. Please create one manually using Android Studio."
                exit 1
            fi
        fi
    else
        echo -e "${YELLOW}⚠${NC} avdmanager not found. Using existing emulator instead."
        # Use first available emulator
        EMULATOR_NAME=$($EMULATOR -list-avds | head -1)
        echo -e "${BLUE}Using existing emulator: $EMULATOR_NAME${NC}"
    fi
fi

# Check if emulator is already running
if $ADB devices | grep -q "emulator"; then
    echo -e "${YELLOW}⚠${NC} An emulator is already running"
    RUNNING_DEVICE=$($ADB devices | grep "emulator" | head -1 | cut -f1)
    echo -e "${BLUE}Using existing emulator: $RUNNING_DEVICE${NC}"
else
    # Start the emulator
    echo -e "${BLUE}Starting emulator: $EMULATOR_NAME${NC}"
    echo -e "${YELLOW}This may take a minute...${NC}"
    
    # Start emulator in background
    $EMULATOR -avd "$EMULATOR_NAME" -no-snapshot-load > /dev/null 2>&1 &
    EMULATOR_PID=$!
    
    # Wait for emulator to boot
    echo -e "${BLUE}Waiting for emulator to boot...${NC}"
    $ADB wait-for-device
    
    # Wait for boot to complete
    echo -e "${BLUE}Waiting for boot to complete...${NC}"
    $ADB shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'
    
    echo -e "${GREEN}✓${NC} Emulator is ready"
fi

# Get device ID
DEVICE=$($ADB devices | grep "emulator" | head -1 | cut -f1)
if [ -z "$DEVICE" ]; then
    echo -e "${RED}✗${NC} No emulator device found"
    exit 1
fi

echo -e "${GREEN}✓${NC} Device: $DEVICE"
echo ""

# Set Java 17 if available
if [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
    export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
    echo -e "${GREEN}✓${NC} Using Java 17"
fi

# Build the app
echo -e "${BLUE}Building app...${NC}"
./gradlew :app:assembleDebug

if [ $? -ne 0 ]; then
    echo -e "${RED}✗${NC} Build failed"
    exit 1
fi

echo -e "${GREEN}✓${NC} Build successful"
echo ""

# Install the app
echo -e "${BLUE}Installing app on $DEVICE...${NC}"
$ADB -s "$DEVICE" install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} App installed successfully"
    echo ""
    
    # Launch the app
    echo -e "${BLUE}Launching app...${NC}"
    $ADB -s "$DEVICE" shell am start -n com.electricsheep.app/.MainActivity
    
    echo ""
    echo -e "${GREEN}✓${NC} Setup complete!"
    echo ""
    echo -e "${BLUE}To test environment switching:${NC}"
    echo "  1. Look for the environment indicator (STAGING/PROD) in the top-right"
    echo "  2. Tap it to open the dropdown menu"
    echo "  3. Select a different environment"
    echo "  4. If logged in, you'll see a warning dialog"
    echo ""
    echo -e "${BLUE}To view logs:${NC}"
    echo "  adb -s $DEVICE logcat | grep -i electric"
    echo ""
    echo -e "${BLUE}To stop the emulator:${NC}"
    echo "  adb -s $DEVICE emu kill"
else
    echo -e "${RED}✗${NC} Installation failed"
    exit 1
fi

