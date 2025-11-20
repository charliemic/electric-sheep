#!/bin/bash

# Emulator Discovery Service for Electric Sheep
# Main entry point for agents to acquire emulators with automatic locking
# Usage: ./scripts/emulator-discovery.sh <command> [options]
#
# Commands:
#   acquire              - Acquire emulator for current agent (returns device ID)
#   release [device-id]  - Release emulator lock
#   status               - Show current agent's emulator status
#   cleanup              - Clean up stale locks

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Lock manager script
LOCK_MANAGER="$SCRIPT_DIR/emulator-lock-manager.sh"
EMULATOR_MANAGER="$SCRIPT_DIR/emulator-manager.sh"

# Track acquired device for cleanup
ACQUIRED_DEVICE=""

# Cleanup on exit
cleanup() {
    if [ -n "$ACQUIRED_DEVICE" ]; then
        "$LOCK_MANAGER" release "$ACQUIRED_DEVICE" > /dev/null 2>&1 || true
    fi
}
trap cleanup EXIT INT TERM

# Get feature name from branch/worktree
get_feature_name() {
    local feature_name=""
    
    if git rev-parse --git-dir > /dev/null 2>&1; then
        # Get current branch name
        local branch=$(git branch --show-current 2>/dev/null || echo "")
        
        # Extract feature name from branch (e.g., feature/mood-chart -> mood_chart)
        if [ -n "$branch" ]; then
            # Remove type prefix (feature/, fix/, etc.)
            if echo "$branch" | grep -qE "^(feature|fix|refactor|docs|test)/"; then
                feature_name=$(echo "$branch" | sed 's|^[^/]*/||' | sed 's|-|_|g')
            else
                feature_name=$(echo "$branch" | sed 's|-|_|g')
            fi
        fi
        
        # If no feature branch, check if we're in a worktree
        if [ -z "$feature_name" ]; then
            local worktree_path=$(git worktree list 2>/dev/null | grep "$(pwd)" | awk '{print $1}' || echo "")
            if [ -n "$worktree_path" ]; then
                local worktree_dir=$(basename "$worktree_path")
                if echo "$worktree_dir" | grep -q "electric-sheep-"; then
                    feature_name=$(echo "$worktree_dir" | sed 's|electric-sheep-||' | sed 's|-|_|g')
                fi
            fi
        fi
    fi
    
    echo "$feature_name"
}

# Get AVD name for feature
get_avd_name() {
    local feature_name="$1"
    
    if [ -n "$feature_name" ]; then
        echo "electric_sheep_${feature_name}"
    else
        echo "electric_sheep_dev"
    fi
}

# Find available device (not locked)
find_available_device() {
    # Get all running devices
    local devices=$("$EMULATOR_MANAGER" running 2>/dev/null | grep "emulator-" | awk '{print $2}' || echo "")
    
    if [ -z "$devices" ]; then
        # Try using ADB directly
        if [ -n "$ANDROID_HOME" ]; then
            devices=$("$ANDROID_HOME/platform-tools/adb" devices 2>/dev/null | grep "emulator.*device$" | awk '{print $1}' || echo "")
        else
            devices=$(adb devices 2>/dev/null | grep "emulator.*device$" | awk '{print $1}' || echo "")
        fi
    fi
    
    # Check each device for availability
    for device in $devices; do
        if [ -n "$device" ]; then
            local available=$("$LOCK_MANAGER" is-available "$device" 2>/dev/null || echo "false")
            if [ "$available" = "true" ]; then
                echo "$device"
                return 0
            fi
        fi
    done
    
    return 1
}

# Acquire emulator for current agent
acquire_emulator() {
    local feature_name=$(get_feature_name)
    local avd_name=$(get_avd_name "$feature_name")
    
    echo -e "${BLUE}Discovering emulator for agent...${NC}" >&2
    if [ -n "$feature_name" ]; then
        echo -e "${BLUE}Feature:${NC} $feature_name" >&2
        echo -e "${BLUE}Target AVD:${NC} $avd_name" >&2
    fi
    
    # Step 1: Check if feature-specific emulator exists and is running
    local device=""
    if [ -n "$feature_name" ]; then
        # Check if AVD exists
        if "$EMULATOR_MANAGER" list 2>/dev/null | grep -q "^${avd_name}$"; then
            # Check if it's running
            local running_devices=$("$EMULATOR_MANAGER" running 2>/dev/null | grep "emulator-" | awk '{print $2}' || echo "")
            for dev in $running_devices; do
                # Try to match device to AVD (this is approximate - we'll check lock instead)
                local available=$("$LOCK_MANAGER" is-available "$dev" 2>/dev/null || echo "false")
                if [ "$available" = "true" ]; then
                    # Try to get AVD name from device
                    if [ -n "$ANDROID_HOME" ]; then
                        local device_avd=$("$ANDROID_HOME/platform-tools/adb" -s "$dev" shell getprop ro.kernel.qemu.avd_name 2>/dev/null | tr -d '\r\n' || echo "")
                        if [ "$device_avd" = "$avd_name" ]; then
                            device="$dev"
                            break
                        fi
                    fi
                fi
            done
        fi
    fi
    
    # Step 2: If no feature-specific device, find any available device
    if [ -z "$device" ]; then
        device=$(find_available_device)
    fi
    
    # Step 3: If no available device, start feature-specific emulator
    if [ -z "$device" ]; then
        echo -e "${BLUE}No available emulator found. Starting feature-specific emulator...${NC}" >&2
        device=$("$EMULATOR_MANAGER" start "$avd_name" 2>/dev/null || echo "")
    fi
    
    # Step 4: If still no device, start default emulator
    if [ -z "$device" ]; then
        echo -e "${YELLOW}⚠${NC} Feature-specific emulator not available. Starting default emulator..." >&2
        device=$("$EMULATOR_MANAGER" ensure 2>/dev/null || echo "")
    fi
    
    if [ -z "$device" ]; then
        echo -e "${RED}✗${NC} Failed to acquire emulator" >&2
        exit 1
    fi
    
    # Step 5: Acquire lock for device
    echo -e "${BLUE}Acquiring lock for device: $device${NC}" >&2
    if ! "$LOCK_MANAGER" acquire "$device" > /dev/null 2>&1; then
        echo -e "${RED}✗${NC} Failed to acquire lock for $device" >&2
        echo -e "${BLUE}Trying to find another available device...${NC}" >&2
        
        # Try to find another device
        device=$(find_available_device)
        if [ -z "$device" ]; then
            echo -e "${RED}✗${NC} No available emulators" >&2
            exit 1
        fi
        
        # Try lock again
        if ! "$LOCK_MANAGER" acquire "$device" > /dev/null 2>&1; then
            echo -e "${RED}✗${NC} Failed to acquire lock" >&2
            exit 1
        fi
    fi
    
    ACQUIRED_DEVICE="$device"
    echo -e "${GREEN}✓${NC} Emulator acquired: $device" >&2
    echo "$device"
}

# Release emulator lock
release_emulator() {
    local device_id="${1:-$ACQUIRED_DEVICE}"
    
    if [ -z "$device_id" ]; then
        echo -e "${YELLOW}⚠${NC} No device ID provided and no acquired device tracked" >&2
        return 0
    fi
    
    echo -e "${BLUE}Releasing lock for device: $device_id${NC}" >&2
    "$LOCK_MANAGER" release "$device_id"
    
    if [ "$device_id" = "$ACQUIRED_DEVICE" ]; then
        ACQUIRED_DEVICE=""
    fi
}

# Show current agent's emulator status
show_status() {
    local feature_name=$(get_feature_name)
    local avd_name=$(get_avd_name "$feature_name")
    
    echo -e "${BLUE}Agent Emulator Status${NC}"
    echo ""
    
    if [ -n "$feature_name" ]; then
        echo -e "${BLUE}Feature:${NC} $feature_name"
        echo -e "${BLUE}Target AVD:${NC} $avd_name"
    else
        echo -e "${BLUE}Target AVD:${NC} $avd_name (default)"
    fi
    echo ""
    
    # Check for acquired device
    if [ -n "$ACQUIRED_DEVICE" ]; then
        echo -e "${GREEN}✓${NC} Acquired device: $ACQUIRED_DEVICE"
        "$LOCK_MANAGER" info "$ACQUIRED_DEVICE" 2>/dev/null || echo ""
    else
        echo -e "${YELLOW}⚠${NC} No device currently acquired"
    fi
    
    echo ""
    echo -e "${BLUE}All locks:${NC}"
    "$LOCK_MANAGER" list
}

# Clean up stale locks
cleanup_stale() {
    echo -e "${BLUE}Cleaning up stale locks...${NC}"
    "$LOCK_MANAGER" cleanup
}

# Main
COMMAND="${1:-acquire}"

case "$COMMAND" in
    acquire)
        acquire_emulator
        ;;
    release)
        release_emulator "${2:-}"
        ;;
    status)
        show_status
        ;;
    cleanup)
        cleanup_stale
        ;;
    *)
        echo -e "${BLUE}Emulator Discovery Service - Electric Sheep${NC}"
        echo ""
        echo "Usage: $0 <command> [options]"
        echo ""
        echo "Commands:"
        echo "  acquire              Acquire emulator for current agent (returns device ID)"
        echo "  release [device-id]   Release emulator lock"
        echo "  status                Show current agent's emulator status"
        echo "  cleanup               Clean up stale locks"
        echo ""
        echo "Examples:"
        echo "  DEVICE=\$($0 acquire)  # Acquire emulator"
        echo "  $0 release \$DEVICE      # Release emulator"
        echo ""
        exit 1
        ;;
esac

