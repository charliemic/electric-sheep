#!/usr/bin/env bats

# Unit tests for emulator-lock-manager.sh

load 'test_helpers.bash'

setup() {
    setup_test_env
    SCRIPT_DIR="$(get_script_dir)"
    LOCK_MANAGER="$SCRIPT_DIR/emulator-lock-manager.sh"
}

teardown() {
    cleanup_test_env
}

@test "lock manager script exists" {
    [ -f "$LOCK_MANAGER" ]
}

@test "lock manager is executable" {
    [ -x "$LOCK_MANAGER" ]
}

@test "acquire lock creates lock file" {
    run "$LOCK_MANAGER" acquire "emulator-5554"
    
    [ "$status" -eq 0 ]
    lock_file_exists "emulator-5554"
}

@test "acquire lock creates valid JSON structure" {
    run "$LOCK_MANAGER" acquire "emulator-5554"
    
    [ "$status" -eq 0 ]
    assert_lock_structure "emulator-5554"
}

@test "acquire lock includes required fields" {
    run "$LOCK_MANAGER" acquire "emulator-5554"
    
    [ "$status" -eq 0 ]
    assert_lock_field "emulator-5554" "device_id" "emulator-5554"
    assert_lock_field "emulator-5554" "pid" "$$"
}

@test "acquire lock fails if device already locked" {
    # First acquisition
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Second acquisition should fail
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -ne 0 ]
    [[ "$output" == *"already locked"* ]]
}

@test "release lock removes lock file" {
    # Acquire lock
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Release lock
    run "$LOCK_MANAGER" release "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Lock file should be gone
    ! lock_file_exists "emulator-5554"
}

@test "release lock handles non-existent lock gracefully" {
    run "$LOCK_MANAGER" release "emulator-9999"
    
    [ "$status" -eq 0 ]
    [[ "$output" == *"No lock found"* ]]
}

@test "is-available returns true for unlocked device" {
    run "$LOCK_MANAGER" is-available "emulator-5554"
    
    [ "$status" -eq 0 ]
    [ "$output" = "true" ]
}

@test "is-available returns false for locked device" {
    # Acquire lock
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Check availability
    run "$LOCK_MANAGER" is-available "emulator-5554"
    
    [ "$status" -eq 1 ]
    [ "$output" = "false" ]
}

@test "list shows no locks when empty" {
    run "$LOCK_MANAGER" list
    
    [ "$status" -eq 0 ]
    [[ "$output" == *"No locks found"* ]]
}

@test "list shows active locks" {
    # Create a lock
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # List locks
    run "$LOCK_MANAGER" list
    
    [ "$status" -eq 0 ]
    [[ "$output" == *"emulator-5554"* ]]
}

@test "cleanup removes stale locks" {
    # Create a stale lock (PID that doesn't exist)
    create_mock_lock "emulator-5554" "999999" "test-agent"
    
    # Verify lock exists
    lock_file_exists "emulator-5554"
    
    # Cleanup
    run "$LOCK_MANAGER" cleanup
    
    [ "$status" -eq 0 ]
    [[ "$output" == *"Cleaned up"* ]]
    
    # Lock should be removed
    ! lock_file_exists "emulator-5554"
}

@test "cleanup keeps active locks" {
    # Create an active lock (current PID)
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Cleanup
    run "$LOCK_MANAGER" cleanup
    
    [ "$status" -eq 0 ]
    
    # Lock should still exist
    lock_file_exists "emulator-5554"
}

@test "info shows lock information" {
    # Create a lock
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Get info
    run "$LOCK_MANAGER" info "emulator-5554"
    
    [ "$status" -eq 0 ]
    [[ "$output" == *"emulator-5554"* ]]
    [[ "$output" == *"device_id"* ]]
}

@test "info fails for non-existent lock" {
    run "$LOCK_MANAGER" info "emulator-9999"
    
    [ "$status" -ne 0 ]
    [[ "$output" == *"No lock found"* ]]
}

@test "acquire requires device ID" {
    run "$LOCK_MANAGER" acquire
    
    [ "$status" -ne 0 ]
    [[ "$output" == *"Device ID required"* ]]
}

@test "release requires device ID" {
    run "$LOCK_MANAGER" release
    
    [ "$status" -ne 0 ]
    [[ "$output" == *"Device ID required"* ]]
}

@test "multiple devices can be locked simultaneously" {
    # Acquire multiple locks
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    run "$LOCK_MANAGER" acquire "emulator-5556"
    [ "$status" -eq 0 ]
    
    run "$LOCK_MANAGER" acquire "emulator-5558"
    [ "$status" -eq 0 ]
    
    # All locks should exist
    lock_file_exists "emulator-5554"
    lock_file_exists "emulator-5556"
    lock_file_exists "emulator-5558"
}

@test "lock file contains agent ID" {
    run "$LOCK_MANAGER" acquire "emulator-5554"
    
    [ "$status" -eq 0 ]
    local agent_id=$(jq -r '.agent_id' "$TEST_LOCK_DIR/emulator-5554.lock")
    [ -n "$agent_id" ]
    [[ "$agent_id" == agent-* ]]
}

@test "lock file contains timestamp" {
    run "$LOCK_MANAGER" acquire "emulator-5554"
    
    [ "$status" -eq 0 ]
    local timestamp=$(jq -r '.acquired_at' "$TEST_LOCK_DIR/emulator-5554.lock")
    [ -n "$timestamp" ]
    # Should be ISO 8601 format
    [[ "$timestamp" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z$ ]]
}

