#!/bin/bash

# Get current connected device ID
# Usage: ./scripts/get-device-id.sh
# Returns device ID or exits with error if no device

set -e

# Find Android SDK
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    else
        echo "Android SDK not found" >&2
        exit 1
    fi
fi

ADB="$ANDROID_HOME/platform-tools/adb"

if [ ! -f "$ADB" ]; then
    echo "ADB not found" >&2
    exit 1
fi

# Get first connected device
DEVICE=$($ADB devices 2>/dev/null | grep "device$" | head -1 | awk '{print $1}')

if [ -z "$DEVICE" ]; then
    echo "No device connected" >&2
    exit 1
fi

echo "$DEVICE"



