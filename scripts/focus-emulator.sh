#!/bin/bash

# Bring Android emulator window to front (best-effort, never fails)
# Works on macOS using AppleScript
# CRITICAL: This script NEVER fails - it's best-effort only

# Don't use set -e - we want to always succeed even if focus fails
set +e

if [[ "$OSTYPE" != "darwin"* ]]; then
    # Not macOS - silently succeed (no-op)
    exit 0
fi

# Try different emulator process names (all errors ignored)
osascript -e 'tell application "System Events" to tell process "qemu-system-aarch64" to set frontmost to true' 2>/dev/null && echo "✅ Focused qemu-system-aarch64" && exit 0

osascript -e 'tell application "System Events" to tell process "qemu-system-x86_64" to set frontmost to true' 2>/dev/null && echo "✅ Focused qemu-system-x86_64" && exit 0

# Try to find any emulator process (errors ignored)
osascript -e 'tell application "System Events"
    set emulatorProcess to first process whose name contains "qemu" or name contains "emulator"
    set frontmost of emulatorProcess to true
end tell' 2>/dev/null && echo "✅ Focused emulator window" && exit 0

# If we get here, focus failed - but that's OK, test can continue
# Silently succeed (don't print warning, don't fail)
exit 0

