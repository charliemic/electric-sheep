# Emulator Script Review

**Last Updated**: 2025-11-19  
**Purpose**: Review all scripts that interact with emulators and identify required changes

## Summary

This document reviews all scripts in the repository that interact with Android emulators, identifies issues, and documents required changes for the new emulator management architecture.

## Scripts Review

### 1. `scripts/emulator-manager.sh` ⚠️ **NEEDS ENHANCEMENT**

**Current State**:
- Main emulator management script
- Supports start, stop, list, running, clean, status, ensure, rename
- Has basic feature-based emulator detection
- Allows multiple emulators but warns at 3+
- No locking mechanism

**Issues**:
- ❌ No locking to prevent multiple agents from using same emulator
- ❌ `get_running_device()` returns first available (not agent-specific)
- ❌ No ownership tracking
- ⚠️ Feature-based naming exists but not enforced

**Required Changes**:
- [ ] Add lock acquisition on `start` command
- [ ] Add lock release on `stop` command
- [ ] Integrate with `emulator-discovery.sh` for acquisition
- [ ] Add `acquire` command (calls discovery)
- [ ] Add `release` command (releases lock)
- [ ] Add `pool-status` command (show all emulators and locks)
- [ ] Add `cleanup-locks` command (clean stale locks)

**Priority**: **HIGH** - Core emulator management

---

### 2. `scripts/get-device-id.sh` ⚠️ **NEEDS REPLACEMENT**

**Current State**:
- Returns first available device ID
- Used by many scripts as fallback
- No locking or ownership

**Issues**:
- ❌ Returns first device (might be used by another agent)
- ❌ No locking mechanism
- ❌ No agent-specific device selection

**Required Changes**:
- [ ] **DEPRECATE** - Replace with `emulator-discovery.sh acquire`
- [ ] Keep for backward compatibility (with warning)
- [ ] Update all callers to use discovery service

**Priority**: **HIGH** - Used by many scripts

---

### 3. `scripts/dev-reload.sh` ⚠️ **NEEDS UPDATE**

**Current State**:
```bash
DEVICE=$(./scripts/get-device-id.sh 2>/dev/null || ./scripts/emulator-manager.sh ensure 2>/dev/null || echo "")
```

**Issues**:
- ❌ Uses `get-device-id.sh` (no locking)
- ❌ Falls back to `emulator-manager.sh ensure` (no locking)
- ❌ No lock release on exit

**Required Changes**:
```bash
# NEW:
DEVICE=$(./scripts/emulator-discovery.sh acquire)
# ... use device ...
# Lock automatically released via trap
```

- [ ] Replace `get-device-id.sh` with `emulator-discovery.sh acquire`
- [ ] Add trap handler to release lock on exit
- [ ] Store acquired device ID in variable for cleanup

**Priority**: **HIGH** - Primary development workflow

---

### 4. `scripts/run-persona-test-with-video.sh` ⚠️ **NEEDS UPDATE**

**Current State**:
```bash
DEVICE_ID="${2:-emulator-5554}"
```

**Issues**:
- ❌ Hardcoded default `emulator-5554`
- ❌ No locking mechanism
- ❌ No lock release on exit

**Required Changes**:
```bash
# NEW:
DEVICE_ID="${2:-$(./scripts/emulator-discovery.sh acquire)}"
# ... use device ...
# Lock automatically released via trap
```

- [ ] Replace hardcoded default with discovery service
- [ ] Add trap handler to release lock on exit
- [ ] Document that device ID can be passed explicitly (for coordination)

**Priority**: **HIGH** - Test automation workflow

---

### 5. `scripts/load-test-data.sh` ⚠️ **NEEDS UPDATE**

**Current State**:
```bash
DEVICE_ID=$("$SCRIPT_DIR/get-device-id.sh" 2>/dev/null || echo "")
if [ -z "$DEVICE_ID" ]; then
    "$SCRIPT_DIR/emulator-manager.sh" ensure
    DEVICE_ID=$("$SCRIPT_DIR/get-device-id.sh")
fi
```

**Issues**:
- ❌ Uses `get-device-id.sh` (no locking)
- ❌ Falls back to `emulator-manager.sh ensure` (no locking)
- ❌ No lock release on exit

**Required Changes**:
```bash
# NEW:
DEVICE_ID=$("$SCRIPT_DIR/emulator-discovery.sh" acquire)
# ... use device ...
# Lock automatically released via trap
```

- [ ] Replace with discovery service
- [ ] Add trap handler to release lock on exit

**Priority**: **MEDIUM** - Test data loading

---

### 6. `scripts/setup-test-emulator.sh` ⚠️ **NEEDS UPDATE**

**Current State**:
```bash
DEVICE=$($ADB devices | grep "emulator" | head -1 | cut -f1)
```

**Issues**:
- ❌ Gets first available device (no locking)
- ❌ No ownership tracking
- ❌ No lock release

**Required Changes**:
```bash
# NEW:
DEVICE=$(./scripts/emulator-discovery.sh acquire)
# ... use device ...
# Lock automatically released via trap
```

- [ ] Replace with discovery service
- [ ] Add trap handler to release lock on exit

**Priority**: **MEDIUM** - Test setup script

---

### 7. `scripts/execute-test-with-logging.sh` ⚠️ **NEEDS UPDATE**

**Current State**:
```bash
DEVICE_ID="${2:-emulator-5554}"
```

**Issues**:
- ❌ Hardcoded default `emulator-5554`
- ❌ No locking mechanism
- ❌ No lock release on exit

**Required Changes**:
```bash
# NEW:
DEVICE_ID="${2:-$(./scripts/emulator-discovery.sh acquire)}"
# ... use device ...
# Lock automatically released via trap (already has cleanup function)
```

- [ ] Replace hardcoded default with discovery service
- [ ] Add lock release to existing cleanup function
- [ ] Document that device ID can be passed explicitly

**Priority**: **MEDIUM** - Test execution script

---

### 8. `scripts/improved-signup-test.sh` ⚠️ **NEEDS UPDATE**

**Current State**:
```bash
DEVICE_ID="${1:-emulator-5554}"
```

**Issues**:
- ❌ Hardcoded default `emulator-5554`
- ❌ No locking mechanism
- ❌ No lock release on exit

**Required Changes**:
```bash
# NEW:
DEVICE_ID="${1:-$(./scripts/emulator-discovery.sh acquire)}"
# ... use device ...
# Lock automatically released via trap
```

- [ ] Replace hardcoded default with discovery service
- [ ] Add trap handler to release lock on exit

**Priority**: **LOW** - Specific test script

---

### 9. `test-automation/src/main/kotlin/com/electricsheep/testautomation/Main.kt` ⚠️ **NEEDS UPDATE**

**Current State**:
```kotlin
val deviceName = getArg("device") ?: "emulator-5554"
```

**Issues**:
- ❌ Hardcoded default `emulator-5554`
- ❌ No locking mechanism
- ❌ No lock release on exit

**Required Changes**:
```kotlin
// NEW:
val deviceName = getArg("device") ?: runShellCommand(
    "./scripts/emulator-discovery.sh acquire"
).trim()
// ... use device ...
// Lock automatically released via trap (JVM shutdown hook)
```

- [ ] Replace hardcoded default with discovery service call
- [ ] Add shutdown hook to release lock
- [ ] Document that device ID can be passed via `--device` argument

**Priority**: **HIGH** - Test automation entry point

---

### 10. `test-automation/src/main/kotlin/com/electricsheep/testautomation/AppiumDriverManager.kt` ⚠️ **NEEDS UPDATE**

**Current State**:
```kotlin
deviceName: String = "emulator-5554",
```

**Issues**:
- ❌ Hardcoded default `emulator-5554`
- ⚠️ This is just a default parameter (caller should provide)

**Required Changes**:
- [ ] Change default to empty string or null
- [ ] Document that caller should use discovery service
- [ ] Add validation that device name is provided

**Priority**: **LOW** - Default parameter only

---

### 11. `test-automation/src/main/kotlin/com/electricsheep/testautomation/actions/ActionExecutor.kt` ⚠️ **NEEDS UPDATE**

**Current State**:
```kotlin
val deviceId = driver.capabilities.getCapability("deviceUDID") as? String ?: "emulator-5554"
```

**Issues**:
- ❌ Hardcoded fallback `emulator-5554`
- ⚠️ This is a fallback for ADB commands

**Required Changes**:
- [ ] Get device ID from driver capabilities (should be set by caller)
- [ ] Remove hardcoded fallback (fail if device ID not available)
- [ ] Document that device ID must be provided via driver capabilities

**Priority**: **LOW** - Fallback only, should not be reached

---

## New Scripts Required

### 1. `scripts/emulator-discovery.sh` ⭐ **NEW - CRITICAL**

**Purpose**: Main entry point for agents to acquire emulators

**Commands**:
- `acquire` - Acquire emulator for current agent (returns device ID)
- `release [device-id]` - Release emulator lock
- `status` - Show current agent's emulator status
- `cleanup` - Clean up stale locks

**Implementation**:
- Detect current branch/worktree
- Find or create feature-specific emulator
- Acquire lock via lock manager
- Start emulator if needed
- Return device ID

**Priority**: **CRITICAL** - Core component

---

### 2. `scripts/emulator-lock-manager.sh` ⭐ **NEW - CRITICAL**

**Purpose**: Low-level lock file management

**Commands**:
- `acquire <device-id>` - Acquire lock for device
- `release <device-id>` - Release lock for device
- `is-available <device-id>` - Check if device is available
- `list` - List all locks
- `cleanup` - Clean up stale locks
- `info <device-id>` - Show lock information

**Implementation**:
- Create/read/delete lock files
- Check if PID is still running (stale lock detection)
- JSON lock file format
- Lock directory: `/tmp/electric-sheep-emulator-locks/`

**Priority**: **CRITICAL** - Core component

---

### 3. `scripts/emulator-pool-manager.sh` ⭐ **NEW - OPTIONAL**

**Purpose**: Manage pool of available emulators

**Commands**:
- `list` - List all emulators with status
- `status` - Show pool status
- `cleanup` - Clean up unused emulators

**Implementation**:
- Query all emulators (available, in-use, stale)
- Report emulator status
- Clean up unused emulators

**Priority**: **LOW** - Nice to have

---

## Implementation Priority

### Phase 1: Core Locking (Critical)
1. ✅ Create `emulator-lock-manager.sh` (lock file operations)
2. ✅ Create `emulator-discovery.sh` (discovery and acquisition)
3. ✅ Enhance `emulator-manager.sh` (integrate locking)

### Phase 2: Update High-Priority Scripts
4. ✅ Update `dev-reload.sh`
5. ✅ Update `run-persona-test-with-video.sh`
6. ✅ Update `test-automation/Main.kt`

### Phase 3: Update Medium-Priority Scripts
7. ✅ Update `load-test-data.sh`
8. ✅ Update `setup-test-emulator.sh`
9. ✅ Update `execute-test-with-logging.sh`

### Phase 4: Update Low-Priority Scripts
10. ✅ Update `improved-signup-test.sh`
11. ✅ Update `AppiumDriverManager.kt` (default parameter)
12. ✅ Update `ActionExecutor.kt` (fallback)

### Phase 5: Cleanup and Documentation
13. ✅ Deprecate `get-device-id.sh` (with warning)
14. ✅ Add documentation
15. ✅ Add tests

---

## Common Patterns

### Pattern 1: Acquire Device at Start

**Before**:
```bash
DEVICE_ID="${1:-emulator-5554}"
```

**After**:
```bash
DEVICE_ID="${1:-$(./scripts/emulator-discovery.sh acquire)}"
```

### Pattern 2: Cleanup on Exit

**Before**:
```bash
# No cleanup
```

**After**:
```bash
cleanup() {
    if [ -n "$ACQUIRED_DEVICE" ]; then
        ./scripts/emulator-discovery.sh release "$ACQUIRED_DEVICE" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM
ACQUIRED_DEVICE="$DEVICE_ID"
```

### Pattern 3: Kotlin/Java Integration

**Before**:
```kotlin
val deviceName = getArg("device") ?: "emulator-5554"
```

**After**:
```kotlin
val deviceName = getArg("device") ?: runShellCommand(
    "./scripts/emulator-discovery.sh acquire"
).trim()

// Add shutdown hook
Runtime.getRuntime().addShutdownHook(Thread {
    runShellCommand("./scripts/emulator-discovery.sh release $deviceName")
})
```

---

## Testing Checklist

- [ ] Test single agent acquiring emulator
- [ ] Test multiple agents acquiring different emulators
- [ ] Test lock release on script exit
- [ ] Test stale lock cleanup
- [ ] Test emulator creation on demand
- [ ] Test feature-based emulator naming
- [ ] Test fallback to shared emulator
- [ ] Test error handling (no emulators available)
- [ ] Test backward compatibility

---

## Migration Notes

### Backward Compatibility

During migration, scripts should:
1. Try new discovery service first
2. Fall back to old method if discovery fails
3. Log warning about using old method

### Rollout Strategy

1. **Phase 1**: Add new scripts alongside old ones (non-breaking)
2. **Phase 2**: Update scripts one by one (test thoroughly)
3. **Phase 3**: Remove old methods (after all scripts updated)

---

## Related Documentation

- `docs/development/EMULATOR_MANAGEMENT_ARCHITECTURE.md` - Architecture design
- `docs/development/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow
- `scripts/emulator-manager.sh` - Current emulator management
- `.cursor/rules/emulator-workflow.mdc` - Emulator workflow rules

