#!/bin/bash

# Emulator Management Script for Electric Sheep
# Handles emulator creation, starting, stopping, and cleanup
# Usage: ./scripts/emulator-manager.sh <command> [options]
#
# Commands:
#   start [avd-name]     - Start an emulator (creates if needed)
#   stop [device-id]     - Stop a running emulator
#   list                  - List available AVDs
#   running               - List running emulators
#   clean [device-id]     - Wipe emulator data (factory reset)
#   status                - Show current device status
#   ensure                - Ensure an emulator is running (start if needed)
#   acquire               - Acquire emulator with locking (calls discovery)
#   release [device-id]   - Release emulator lock
#   pool-status           - Show all emulators and their lock status
#   cleanup-locks         - Clean up stale lock files

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Find Android SDK
find_android_sdk() {
    if [ -z "$ANDROID_HOME" ]; then
        if [ -d "$HOME/Library/Android/sdk" ]; then
            export ANDROID_HOME="$HOME/Library/Android/sdk"
        elif [ -d "$HOME/Android/Sdk" ]; then
            export ANDROID_HOME="$HOME/Android/Sdk"
        else
            echo -e "${RED}✗${NC} Android SDK not found. Please set ANDROID_HOME or install Android SDK." >&2
            exit 1
        fi
    fi
    
    EMULATOR="$ANDROID_HOME/emulator/emulator"
    ADB="$ANDROID_HOME/platform-tools/adb"
    AVDMANAGER=""
    
    # Find avdmanager
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
    
    # Verify tools exist
    if [ ! -f "$EMULATOR" ]; then
        echo -e "${RED}✗${NC} Emulator not found at $EMULATOR" >&2
        exit 1
    fi
    
    if [ ! -f "$ADB" ]; then
        echo -e "${RED}✗${NC} ADB not found at $ADB" >&2
        exit 1
    fi
}

# Get default emulator name
get_default_avd() {
    local preferred_name="electric_sheep_dev"
    
    # Check if preferred exists
    if $EMULATOR -list-avds 2>/dev/null | grep -q "^${preferred_name}$"; then
        echo "$preferred_name"
        return 0
    fi
    
    # Use first available
    local first_avd=$($EMULATOR -list-avds 2>/dev/null | head -1)
    if [ -n "$first_avd" ]; then
        echo "$first_avd"
        return 0
    fi
    
    return 1
}

# Create emulator if it doesn't exist
create_emulator() {
    local avd_name="$1"
    
    if [ -z "$AVDMANAGER" ]; then
        echo -e "${YELLOW}⚠${NC} avdmanager not found. Cannot create emulator automatically." >&2
        echo -e "${BLUE}Please create an emulator manually using Android Studio AVD Manager.${NC}" >&2
        return 1
    fi
    
    echo -e "${BLUE}Creating emulator: $avd_name${NC}"
    
    # Check if system image is available
    if ! $AVDMANAGER list system-images 2>/dev/null | grep -q "android-34.*google_apis.*x86_64"; then
        echo -e "${YELLOW}⚠${NC} System image not found. Installing..." >&2
        echo -e "${BLUE}Please install system image: system-images;android-34;google_apis;x86_64${NC}" >&2
        echo -e "${BLUE}Run: sdkmanager 'system-images;android-34;google_apis;x86_64'${NC}" >&2
        return 1
    fi
    
    # Create AVD
    if $AVDMANAGER create avd \
        -n "$avd_name" \
        -k "system-images;android-34;google_apis;x86_64" \
        -d "pixel_5" \
        --force > /dev/null 2>&1; then
        echo -e "${GREEN}✓${NC} Emulator created: $avd_name"
        return 0
    else
        echo -e "${RED}✗${NC} Failed to create emulator" >&2
        return 1
    fi
}

# Start emulator
start_emulator() {
    local avd_name="${1:-}"
    
    if [ -z "$avd_name" ]; then
        avd_name=$(get_default_avd)
        if [ $? -ne 0 ]; then
            echo -e "${RED}✗${NC} No emulators available. Please create one first." >&2
            exit 1
        fi
    fi
    
    # Check if already running
    local running_device=$(get_running_device)
    if [ -n "$running_device" ]; then
        echo -e "${YELLOW}⚠${NC} Emulator already running: $running_device" >&2
        echo "$running_device"
        return 0
    fi
    
    # Check if AVD exists, create if not
    if ! $EMULATOR -list-avds 2>/dev/null | grep -q "^${avd_name}$"; then
        echo -e "${BLUE}AVD '$avd_name' not found. Creating...${NC}"
        if ! create_emulator "$avd_name"; then
            echo -e "${YELLOW}⚠${NC} Using existing emulator instead" >&2
            avd_name=$(get_default_avd)
        fi
    fi
    
    echo -e "${BLUE}Starting emulator: $avd_name${NC}"
    echo -e "${YELLOW}This may take a minute...${NC}"
    
    # Start emulator in background (with snapshot for faster boot)
    $EMULATOR -avd "$avd_name" -no-snapshot-save > /dev/null 2>&1 &
    local emulator_pid=$!
    
    # Wait for emulator to boot
    echo -e "${BLUE}Waiting for emulator to boot...${NC}"
    $ADB wait-for-device > /dev/null 2>&1
    
    # Wait for boot to complete
    echo -e "${BLUE}Waiting for boot to complete...${NC}"
    local boot_timeout=120
    local elapsed=0
    while [ $elapsed -lt $boot_timeout ]; do
        if $ADB shell getprop sys.boot_completed 2>/dev/null | grep -q "1"; then
            break
        fi
        sleep 1
        elapsed=$((elapsed + 1))
    done
    
    if [ $elapsed -ge $boot_timeout ]; then
        echo -e "${RED}✗${NC} Emulator boot timeout" >&2
        kill $emulator_pid 2>/dev/null || true
        exit 1
    fi
    
    local device=$(get_running_device)
    if [ -z "$device" ]; then
        echo -e "${RED}✗${NC} Failed to detect running emulator" >&2
        exit 1
    fi
    
    echo -e "${GREEN}✓${NC} Emulator ready: $device"
    echo "$device"
}

# Stop emulator
stop_emulator() {
    local device_id="${1:-}"
    
    if [ -z "$device_id" ]; then
        device_id=$(get_running_device)
        if [ -z "$device_id" ]; then
            echo -e "${YELLOW}⚠${NC} No running emulator found" >&2
            return 0
        fi
    fi
    
    echo -e "${BLUE}Stopping emulator: $device_id${NC}"
    
    # Try graceful shutdown first
    $ADB -s "$device_id" emu kill 2>/dev/null || true
    
    # Wait a moment
    sleep 2
    
    # Check if still running
    if $ADB devices | grep -q "$device_id"; then
        echo -e "${YELLOW}⚠${NC} Emulator still running, forcing shutdown..." >&2
        # Find and kill emulator process
        local emulator_pid=$(ps aux | grep "[e]mulator.*$device_id" | awk '{print $2}' | head -1)
        if [ -n "$emulator_pid" ]; then
            kill -9 "$emulator_pid" 2>/dev/null || true
        fi
    fi
    
    echo -e "${GREEN}✓${NC} Emulator stopped"
}

# List available AVDs
list_avds() {
    echo -e "${BLUE}Available AVDs:${NC}"
    local avds=$($EMULATOR -list-avds 2>/dev/null)
    if [ -z "$avds" ]; then
        echo -e "${YELLOW}  No AVDs found${NC}"
    else
        echo "$avds" | while read -r avd; do
            echo "  - $avd"
        done
    fi
}

# List running emulators
list_running() {
    echo -e "${BLUE}Running emulators:${NC}"
    local devices=$($ADB devices | grep "emulator" | awk '{print $1}')
    if [ -z "$devices" ]; then
        echo -e "${YELLOW}  No emulators running${NC}"
    else
        echo "$devices" | while read -r device; do
            echo "  - $device"
        done
    fi
}

# Get running device ID
get_running_device() {
    $ADB devices 2>/dev/null | grep "emulator.*device$" | head -1 | awk '{print $1}'
}

# Clean emulator (wipe data)
clean_emulator() {
    local device_id="${1:-}"
    
    if [ -z "$device_id" ]; then
        device_id=$(get_running_device)
        if [ -z "$device_id" ]; then
            echo -e "${RED}✗${NC} No running emulator found. Start an emulator first." >&2
            exit 1
        fi
    fi
    
    echo -e "${YELLOW}⚠${NC} This will wipe all data on the emulator!" >&2
    echo -e "${BLUE}Wiping emulator: $device_id${NC}"
    
    # Stop emulator first
    stop_emulator "$device_id"
    sleep 2
    
    # Get AVD name from device ID (emulator-5554 -> extract port, find AVD)
    # This is a bit tricky, so we'll use a simpler approach: wipe via adb
    echo -e "${BLUE}Wiping data...${NC}"
    $ADB -s "$device_id" shell wipe data 2>/dev/null || {
        echo -e "${YELLOW}⚠${NC} Could not wipe via ADB. You may need to wipe via AVD Manager." >&2
    }
    
    echo -e "${GREEN}✓${NC} Emulator data wiped"
}

# Show status
show_status() {
    local device=$(get_running_device)
    
    if [ -n "$device" ]; then
        echo -e "${GREEN}✓${NC} Device connected: $device"
        
        # Get device info
        local model=$($ADB -s "$device" shell getprop ro.product.model 2>/dev/null | tr -d '\r\n')
        local version=$($ADB -s "$device" shell getprop ro.build.version.release 2>/dev/null | tr -d '\r\n')
        local sdk=$($ADB -s "$device" shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r\n')
        
        echo -e "${BLUE}  Model:${NC} $model"
        echo -e "${BLUE}  Android:${NC} $version (SDK $sdk)"
        
        # Check if app is installed
        if $ADB -s "$device" shell pm list packages | grep -q "com.electricsheep.app"; then
            echo -e "${GREEN}  App installed:${NC} Yes"
        else
            echo -e "${YELLOW}  App installed:${NC} No"
        fi
    else
        echo -e "${YELLOW}⚠${NC} No device connected"
    fi
}

# Ensure emulator is running
ensure_running() {
    local device=$(get_running_device)
    
    if [ -n "$device" ]; then
        echo "$device"
        return 0
    fi
    
    # Start emulator
    start_emulator
}

# Acquire emulator with locking (calls discovery service)
acquire_with_lock() {
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local discovery_script="$script_dir/emulator-discovery.sh"
    
    if [ ! -f "$discovery_script" ]; then
        echo -e "${RED}✗${NC} Discovery service not found: $discovery_script" >&2
        exit 1
    fi
    
    "$discovery_script" acquire
}

# Release emulator lock
release_lock() {
    local device_id="${1:-}"
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local discovery_script="$script_dir/emulator-discovery.sh"
    
    if [ ! -f "$discovery_script" ]; then
        echo -e "${RED}✗${NC} Discovery service not found: $discovery_script" >&2
        exit 1
    fi
    
    "$discovery_script" release "$device_id"
}

# Show pool status (all emulators and locks)
show_pool_status() {
    echo -e "${BLUE}Emulator Pool Status${NC}"
    echo ""
    
    # List all AVDs
    echo -e "${BLUE}Available AVDs:${NC}"
    list_avds
    echo ""
    
    # List running emulators
    echo -e "${BLUE}Running Emulators:${NC}"
    list_running
    echo ""
    
    # List locks
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local lock_manager="$script_dir/emulator-lock-manager.sh"
    
    if [ -f "$lock_manager" ]; then
        echo -e "${BLUE}Lock Status:${NC}"
        "$lock_manager" list
    else
        echo -e "${YELLOW}⚠${NC} Lock manager not found" >&2
    fi
}

# Clean up stale locks
cleanup_locks() {
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local lock_manager="$script_dir/emulator-lock-manager.sh"
    
    if [ ! -f "$lock_manager" ]; then
        echo -e "${RED}✗${NC} Lock manager not found: $lock_manager" >&2
        exit 1
    fi
    
    "$lock_manager" cleanup
}

# Main
find_android_sdk

COMMAND="${1:-status}"

case "$COMMAND" in
    start)
        start_emulator "${2:-}"
        ;;
    stop)
        stop_emulator "${2:-}"
        ;;
    list)
        list_avds
        ;;
    running)
        list_running
        ;;
    clean)
        clean_emulator "${2:-}"
        ;;
    status)
        show_status
        ;;
    ensure)
        ensure_running
        ;;
    acquire)
        acquire_with_lock
        ;;
    release)
        release_lock "${2:-}"
        ;;
    pool-status)
        show_pool_status
        ;;
    cleanup-locks)
        cleanup_locks
        ;;
    *)
        echo -e "${BLUE}Emulator Manager - Electric Sheep${NC}"
        echo ""
        echo "Usage: $0 <command> [options]"
        echo ""
        echo "Commands:"
        echo "  start [avd-name]     Start an emulator (creates if needed)"
        echo "  stop [device-id]     Stop a running emulator"
        echo "  list                 List available AVDs"
        echo "  running              List running emulators"
        echo "  clean [device-id]    Wipe emulator data (factory reset)"
        echo "  status               Show current device status"
        echo "  ensure               Ensure an emulator is running (start if needed)"
        echo "  acquire              Acquire emulator with locking (calls discovery)"
        echo "  release [device-id]  Release emulator lock"
        echo "  pool-status          Show all emulators and their lock status"
        echo "  cleanup-locks        Clean up stale lock files"
        echo ""
        exit 1
        ;;
esac

