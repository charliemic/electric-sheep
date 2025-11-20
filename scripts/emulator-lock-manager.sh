#!/bin/bash

# Emulator Lock Manager for Electric Sheep
# Low-level lock file management for emulator ownership tracking
# Usage: ./scripts/emulator-lock-manager.sh <command> [options]
#
# Commands:
#   acquire <device-id>  - Acquire lock for device
#   release <device-id>  - Release lock for device
#   is-available <device-id> - Check if device is available
#   list                 - List all locks
#   cleanup              - Clean up stale locks
#   info <device-id>     - Show lock information

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Lock directory
LOCK_DIR="${EMULATOR_LOCK_DIR:-/tmp/electric-sheep-emulator-locks}"

# Initialize lock directory
init_lock_dir() {
    mkdir -p "$LOCK_DIR"
}

# Generate agent ID (unique identifier for this agent/process)
generate_agent_id() {
    # Use process ID + timestamp for uniqueness
    echo "agent-$$-$(date +%s)"
}

# Get current branch/worktree info
get_branch_info() {
    local branch=""
    local worktree=""
    
    if git rev-parse --git-dir > /dev/null 2>&1; then
        branch=$(git branch --show-current 2>/dev/null || echo "")
        
        # Check if we're in a worktree
        local worktree_path=$(git worktree list 2>/dev/null | grep "$(pwd)" | awk '{print $1}' || echo "")
        if [ -n "$worktree_path" ]; then
            worktree=$(basename "$worktree_path")
        fi
    fi
    
    echo "$branch|$worktree"
}

# Check if lock is stale (PID not running)
is_lock_stale() {
    local lock_file="$1"
    
    if [ ! -f "$lock_file" ]; then
        return 1  # Not stale (doesn't exist)
    fi
    
    # Read PID from lock file
    local pid=$(jq -r '.pid' "$lock_file" 2>/dev/null || echo "")
    
    if [ -z "$pid" ]; then
        return 0  # Stale (invalid lock file)
    fi
    
    # Check if process is still running
    if kill -0 "$pid" 2>/dev/null; then
        return 1  # Not stale (process is running)
    else
        return 0  # Stale (process is not running)
    fi
}

# Acquire lock for device
acquire_lock() {
    local device_id="$1"
    
    if [ -z "$device_id" ]; then
        echo -e "${RED}✗${NC} Device ID required" >&2
        echo "Usage: $0 acquire <device-id>" >&2
        exit 1
    fi
    
    init_lock_dir
    
    local lock_file="${LOCK_DIR}/${device_id}.lock"
    
    # Check if lock exists and is not stale
    if [ -f "$lock_file" ]; then
        if ! is_lock_stale "$lock_file"; then
            echo -e "${RED}✗${NC} Device $device_id is already locked" >&2
            local owner=$(jq -r '.agent_id' "$lock_file" 2>/dev/null || echo "unknown")
            echo -e "${BLUE}Locked by:${NC} $owner" >&2
            exit 1
        else
            # Lock is stale, remove it
            echo -e "${YELLOW}⚠${NC} Removing stale lock for $device_id" >&2
            rm -f "$lock_file"
        fi
    fi
    
    # Get branch/worktree info
    local branch_info=$(get_branch_info)
    local branch=$(echo "$branch_info" | cut -d'|' -f1)
    local worktree=$(echo "$branch_info" | cut -d'|' -f2)
    
    # Get AVD name from device (if possible)
    local avd_name=""
    if command -v adb > /dev/null 2>&1; then
        local adb_path=$(which adb)
        if [ -n "$ANDROID_HOME" ]; then
            adb_path="$ANDROID_HOME/platform-tools/adb"
        fi
        avd_name=$($adb_path -s "$device_id" shell getprop ro.kernel.qemu.avd_name 2>/dev/null | tr -d '\r\n' || echo "")
    fi
    
    # Create lock file
    local agent_id=$(generate_agent_id)
    local lock_data=$(jq -n \
        --arg device_id "$device_id" \
        --arg avd_name "$avd_name" \
        --arg pid "$$" \
        --arg branch "$branch" \
        --arg worktree "$worktree" \
        --arg acquired_at "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        --arg agent_id "$agent_id" \
        '{
            device_id: $device_id,
            avd_name: $avd_name,
            pid: ($pid | tonumber),
            branch: $branch,
            worktree: $worktree,
            acquired_at: $acquired_at,
            agent_id: $agent_id
        }')
    
    echo "$lock_data" > "$lock_file"
    
    echo -e "${GREEN}✓${NC} Lock acquired for $device_id"
    echo "$device_id"
}

# Release lock for device
release_lock() {
    local device_id="$1"
    
    if [ -z "$device_id" ]; then
        echo -e "${RED}✗${NC} Device ID required" >&2
        echo "Usage: $0 release <device-id>" >&2
        exit 1
    fi
    
    init_lock_dir
    
    local lock_file="${LOCK_DIR}/${device_id}.lock"
    
    if [ ! -f "$lock_file" ]; then
        echo -e "${YELLOW}⚠${NC} No lock found for $device_id" >&2
        return 0
    fi
    
    # Check if lock belongs to this process
    local lock_pid=$(jq -r '.pid' "$lock_file" 2>/dev/null || echo "")
    if [ "$lock_pid" != "$$" ] && [ -n "$lock_pid" ]; then
        # In test mode (TEST_LOCK_DIR set), allow release regardless of PID
        # In production, only allow release if process is not running (stale lock)
        if [ -z "$TEST_LOCK_DIR" ] && [ -z "$EMULATOR_LOCK_DIR" ] || [ "$EMULATOR_LOCK_DIR" != "$TEST_LOCK_DIR" ]; then
            # Production mode: check if process is still running
            if kill -0 "$lock_pid" 2>/dev/null; then
                # Process is still running and it's not us - don't release
                echo -e "${YELLOW}⚠${NC} Lock for $device_id belongs to different process (PID: $lock_pid)" >&2
                echo -e "${BLUE}Not releasing lock${NC}" >&2
                return 0
            fi
        fi
        # Test mode or stale lock - allow release
    fi
    
    rm -f "$lock_file"
    echo -e "${GREEN}✓${NC} Lock released for $device_id"
}

# Check if device is available
is_available() {
    local device_id="$1"
    
    if [ -z "$device_id" ]; then
        echo -e "${RED}✗${NC} Device ID required" >&2
        exit 1
    fi
    
    init_lock_dir
    
    local lock_file="${LOCK_DIR}/${device_id}.lock"
    
    if [ ! -f "$lock_file" ]; then
        echo "true"
        return 0
    fi
    
    if is_lock_stale "$lock_file"; then
        echo "true"
        return 0
    fi
    
    echo "false"
    return 1
}

# List all locks
list_locks() {
    init_lock_dir
    
    local locks=("$LOCK_DIR"/*.lock)
    
    if [ ! -e "${locks[0]}" ]; then
        echo -e "${BLUE}No locks found${NC}"
        return 0
    fi
    
    echo -e "${BLUE}Active locks:${NC}"
    echo ""
    
    for lock_file in "${locks[@]}"; do
        if [ -f "$lock_file" ]; then
            local device_id=$(basename "$lock_file" .lock)
            local agent_id=$(jq -r '.agent_id' "$lock_file" 2>/dev/null || echo "unknown")
            local pid=$(jq -r '.pid' "$lock_file" 2>/dev/null || echo "unknown")
            local branch=$(jq -r '.branch' "$lock_file" 2>/dev/null || echo "")
            local worktree=$(jq -r '.worktree' "$lock_file" 2>/dev/null || echo "")
            local acquired_at=$(jq -r '.acquired_at' "$lock_file" 2>/dev/null || echo "")
            
            # Check if stale
            if is_lock_stale "$lock_file"; then
                echo -e "${YELLOW}⚠${NC} $device_id (STALE - PID $pid not running)"
            else
                echo -e "${GREEN}✓${NC} $device_id"
            fi
            
            echo "  Agent: $agent_id"
            echo "  PID: $pid"
            if [ -n "$branch" ]; then
                echo "  Branch: $branch"
            fi
            if [ -n "$worktree" ]; then
                echo "  Worktree: $worktree"
            fi
            if [ -n "$acquired_at" ]; then
                echo "  Acquired: $acquired_at"
            fi
            echo ""
        fi
    done
}

# Clean up stale locks
cleanup_locks() {
    init_lock_dir
    
    local cleaned=0
    local locks=("$LOCK_DIR"/*.lock)
    
    if [ ! -e "${locks[0]}" ]; then
        echo -e "${BLUE}No locks to clean${NC}"
        return 0
    fi
    
    for lock_file in "${locks[@]}"; do
        if [ -f "$lock_file" ] && is_lock_stale "$lock_file"; then
            local device_id=$(basename "$lock_file" .lock)
            echo -e "${YELLOW}Removing stale lock:${NC} $device_id"
            rm -f "$lock_file"
            cleaned=$((cleaned + 1))
        fi
    done
    
    if [ $cleaned -eq 0 ]; then
        echo -e "${GREEN}✓${NC} No stale locks found"
    else
        echo -e "${GREEN}✓${NC} Cleaned up $cleaned stale lock(s)"
    fi
}

# Show lock information
show_lock_info() {
    local device_id="$1"
    
    if [ -z "$device_id" ]; then
        echo -e "${RED}✗${NC} Device ID required" >&2
        exit 1
    fi
    
    init_lock_dir
    
    local lock_file="${LOCK_DIR}/${device_id}.lock"
    
    if [ ! -f "$lock_file" ]; then
        echo -e "${YELLOW}⚠${NC} No lock found for $device_id" >&2
        exit 1
    fi
    
    echo -e "${BLUE}Lock information for $device_id:${NC}"
    echo ""
    cat "$lock_file" | jq '.'
    
    if is_lock_stale "$lock_file"; then
        echo ""
        echo -e "${YELLOW}⚠${NC} This lock is STALE (process not running)"
    fi
}

# Main
COMMAND="${1:-}"

case "$COMMAND" in
    acquire)
        acquire_lock "${2:-}"
        ;;
    release)
        release_lock "${2:-}"
        ;;
    is-available)
        is_available "${2:-}"
        ;;
    list)
        list_locks
        ;;
    cleanup)
        cleanup_locks
        ;;
    info)
        show_lock_info "${2:-}"
        ;;
    *)
        echo -e "${BLUE}Emulator Lock Manager - Electric Sheep${NC}"
        echo ""
        echo "Usage: $0 <command> [options]"
        echo ""
        echo "Commands:"
        echo "  acquire <device-id>     Acquire lock for device"
        echo "  release <device-id>      Release lock for device"
        echo "  is-available <device-id> Check if device is available"
        echo "  list                     List all locks"
        echo "  cleanup                  Clean up stale locks"
        echo "  info <device-id>         Show lock information"
        echo ""
        exit 1
        ;;
esac

