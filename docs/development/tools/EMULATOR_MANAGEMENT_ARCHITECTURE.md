# Emulator Management Architecture

**Last Updated**: 2025-11-19  
**Status**: Architecture Proposal

## Problem Statement

Multiple AI agents working simultaneously on the Electric Sheep codebase are experiencing conflicts when managing Android emulators:

1. **No Isolation**: Agents start emulators without knowing if another agent is using them
2. **Hardcoded Device IDs**: Scripts hardcode `emulator-5554`, causing conflicts
3. **No Locking**: Multiple agents can use the same emulator simultaneously
4. **No Ownership Tracking**: No way to know which agent/process owns which emulator
5. **Resource Conflicts**: Multiple agents competing for the same emulator resources

## Architecture Goals

1. **Agent Isolation**: Each agent should have its own emulator instance
2. **Automatic Discovery**: Agents should automatically find or create their own emulator
3. **Locking Mechanism**: Prevent multiple agents from using the same emulator
4. **Resource Management**: Track and clean up emulators when agents finish
5. **Backward Compatibility**: Existing scripts should continue to work

## Architecture Design

### Core Principles

1. **One Emulator Per Agent**: Each agent gets its own emulator instance
2. **Feature-Based Naming**: Emulators named after feature branches/worktrees
3. **Lock Files**: Use lock files to track emulator ownership
4. **Automatic Cleanup**: Clean up locks when processes exit
5. **Graceful Degradation**: Fall back to shared emulator if needed

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Emulator Management Layer                 │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Lock       │  │   Discovery  │  │   Lifecycle  │      │
│  │   Manager    │  │   Service    │  │   Manager    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         │                 │                 │                │
│         └─────────────────┼─────────────────┘                │
│                           │                                   │
│                  ┌────────▼────────┐                         │
│                  │  Emulator Pool   │                         │
│                  │   Manager       │                         │
│                  └──────────────────┘                         │
│                                                               │
└─────────────────────────────────────────────────────────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │   Android Emulators     │
              │  (AVD Instances)       │
              └────────────────────────┘
```

### Key Components

#### 1. Lock Manager

**Purpose**: Track which agent/process owns which emulator

**Location**: `scripts/emulator-lock-manager.sh`

**Responsibilities**:
- Create lock files for emulator ownership
- Check if emulator is available
- Release locks when processes exit
- Clean up stale locks

**Lock File Format**:
```
/tmp/electric-sheep-emulator-locks/<device-id>.lock
```

**Lock File Contents**:
```json
{
  "device_id": "emulator-5554",
  "avd_name": "electric_sheep_feature_name",
  "pid": 12345,
  "branch": "feature/feature-name",
  "worktree": "../electric-sheep-feature-name",
  "acquired_at": "2025-11-19T10:30:00Z",
  "agent_id": "auto-generated-uuid"
}
```

#### 2. Discovery Service

**Purpose**: Find or create an emulator for the current agent

**Location**: `scripts/emulator-discovery.sh`

**Responsibilities**:
- Detect current branch/worktree
- Find available emulator for this agent
- Create new emulator if needed
- Return device ID for use

**Discovery Strategy**:
1. Check if feature-specific emulator exists and is available
2. Check if any emulator is available (not locked)
3. Create new emulator if none available
4. Fall back to shared emulator with coordination

#### 3. Lifecycle Manager

**Purpose**: Manage emulator lifecycle (start, stop, cleanup)

**Location**: Enhanced `scripts/emulator-manager.sh`

**Responsibilities**:
- Start emulator with locking
- Stop emulator and release lock
- Clean up stale locks
- Monitor emulator health

#### 4. Emulator Pool Manager

**Purpose**: Manage pool of available emulators

**Location**: `scripts/emulator-pool-manager.sh`

**Responsibilities**:
- List all emulators (available, in-use, stale)
- Create emulators on demand
- Clean up unused emulators
- Report emulator status

### Lock File System

#### Lock Directory
```
/tmp/electric-sheep-emulator-locks/
├── emulator-5554.lock
├── emulator-5556.lock
└── emulator-5558.lock
```

#### Lock Acquisition Flow

```
1. Agent calls: emulator-discovery.sh acquire
2. Discovery checks for feature-specific emulator
3. If found and available (no lock or stale lock):
   - Create lock file
   - Start emulator if not running
   - Return device ID
4. If not found:
   - Create new emulator
   - Create lock file
   - Start emulator
   - Return device ID
5. If all emulators in use:
   - Wait for available emulator (with timeout)
   - Or create new emulator
```

#### Lock Release Flow

```
1. Agent calls: emulator-lock-manager.sh release <device-id>
2. Check if lock exists and belongs to this process
3. Stop emulator (optional, configurable)
4. Remove lock file
5. Clean up any stale locks
```

#### Stale Lock Detection

```
1. Check lock file exists
2. Check if PID is still running
3. If PID not running:
   - Lock is stale
   - Can be acquired by new agent
4. Clean up stale locks periodically
```

### Emulator Naming Convention

#### Feature-Based Naming

**Format**: `electric_sheep_<feature_name>`

**Examples**:
- Branch: `feature/mood-chart` → AVD: `electric_sheep_mood_chart`
- Branch: `fix/login-bug` → AVD: `electric_sheep_login_bug`
- Worktree: `../electric-sheep-test-helpers` → AVD: `electric_sheep_test_helpers`

**Fallback**:
- If no feature detected: `electric_sheep_dev`
- If multiple agents on same feature: `electric_sheep_<feature>_<agent-id>`

### API Design

#### New Script: `emulator-discovery.sh`

**Purpose**: Main entry point for agents to acquire emulators

**Usage**:
```bash
# Acquire emulator for current agent
DEVICE_ID=$(./scripts/emulator-discovery.sh acquire)

# Release emulator when done
./scripts/emulator-discovery.sh release "$DEVICE_ID"

# Check status
./scripts/emulator-discovery.sh status
```

**Commands**:
- `acquire` - Acquire emulator for current agent (returns device ID)
- `release [device-id]` - Release emulator lock
- `status` - Show current agent's emulator status
- `cleanup` - Clean up stale locks

#### Enhanced: `emulator-manager.sh`

**New Commands**:
- `acquire` - Acquire emulator with locking (calls discovery)
- `release [device-id]` - Release emulator lock
- `pool-status` - Show all emulators and their status
- `cleanup-locks` - Clean up stale lock files

**Existing Commands** (enhanced):
- `start [avd-name]` - Start with lock acquisition
- `stop [device-id]` - Stop and release lock

#### New Script: `emulator-lock-manager.sh`

**Purpose**: Low-level lock management

**Usage**:
```bash
# Check if device is available
./scripts/emulator-lock-manager.sh is-available emulator-5554

# Acquire lock
./scripts/emulator-lock-manager.sh acquire emulator-5554

# Release lock
./scripts/emulator-lock-manager.sh release emulator-5554

# List all locks
./scripts/emulator-lock-manager.sh list

# Clean up stale locks
./scripts/emulator-lock-manager.sh cleanup
```

### Integration Points

#### Updated Scripts

1. **`dev-reload.sh`**
   ```bash
   # OLD:
   DEVICE=$(./scripts/get-device-id.sh)
   
   # NEW:
   DEVICE=$(./scripts/emulator-discovery.sh acquire)
   # ... use device ...
   # Lock automatically released on script exit (trap)
   ```

2. **`run-persona-test-with-video.sh`**
   ```bash
   # OLD:
   DEVICE_ID="${2:-emulator-5554}"
   
   # NEW:
   DEVICE_ID="${2:-$(./scripts/emulator-discovery.sh acquire)}"
   # ... use device ...
   # Release on exit
   ```

3. **`load-test-data.sh`**
   ```bash
   # OLD:
   DEVICE_ID=$("$SCRIPT_DIR/get-device-id.sh")
   
   # NEW:
   DEVICE_ID=$("$SCRIPT_DIR/emulator-discovery.sh" acquire)
   # ... use device ...
   ```

4. **Test Automation (Kotlin)**
   ```kotlin
   // OLD:
   val deviceName = getArg("device") ?: "emulator-5554"
   
   // NEW:
   val deviceName = getArg("device") ?: runShellCommand(
       "./scripts/emulator-discovery.sh acquire"
   )
   ```

### Lock Cleanup Strategy

#### Automatic Cleanup

1. **On Script Exit**: Use `trap` to release locks
2. **Stale Lock Detection**: Check if PID is still running
3. **Periodic Cleanup**: Background process cleans up stale locks
4. **Manual Cleanup**: `emulator-lock-manager.sh cleanup`

#### Cleanup Implementation

```bash
# In scripts, use trap for automatic cleanup
cleanup() {
    if [ -n "$ACQUIRED_DEVICE" ]; then
        ./scripts/emulator-discovery.sh release "$ACQUIRED_DEVICE"
    fi
}
trap cleanup EXIT INT TERM
```

### Error Handling

#### Scenarios

1. **No Emulators Available**
   - Wait with timeout (configurable, default 60s)
   - Create new emulator if possible
   - Error if timeout exceeded

2. **Lock File Conflicts**
   - Check if lock is stale (PID not running)
   - Acquire if stale
   - Error if lock is active

3. **Emulator Start Failure**
   - Release lock
   - Try next available emulator
   - Error if all fail

4. **Process Crash**
   - Stale lock detection handles this
   - Next agent can acquire after cleanup

### Configuration

#### Environment Variables

```bash
# Lock directory
EMULATOR_LOCK_DIR="${EMULATOR_LOCK_DIR:-/tmp/electric-sheep-emulator-locks}"

# Lock timeout (seconds)
EMULATOR_LOCK_TIMEOUT="${EMULATOR_LOCK_TIMEOUT:-60}"

# Auto-cleanup on exit
EMULATOR_AUTO_CLEANUP="${EMULATOR_AUTO_CLEANUP:-true}"

# Stop emulator on release
EMULATOR_STOP_ON_RELEASE="${EMULATOR_STOP_ON_RELEASE:-false}"
```

### Migration Path

#### Phase 1: Add Locking (Non-Breaking)
- Add lock files alongside existing behavior
- Scripts continue to work without changes
- Locking is opt-in

#### Phase 2: Update Scripts
- Update scripts to use discovery service
- Remove hardcoded device IDs
- Add automatic cleanup

#### Phase 3: Enforce Locking
- Make locking mandatory
- Remove fallback to unlocked emulators
- Full isolation

### Benefits

1. **Agent Isolation**: Each agent has its own emulator
2. **No Conflicts**: Locking prevents simultaneous use
3. **Automatic Management**: Agents don't need to coordinate manually
4. **Resource Efficiency**: Reuse emulators when agents finish
5. **Backward Compatible**: Existing scripts continue to work

### Implementation Checklist

- [ ] Create `emulator-lock-manager.sh` (lock file operations)
- [ ] Create `emulator-discovery.sh` (discovery and acquisition)
- [ ] Enhance `emulator-manager.sh` (integrate locking)
- [ ] Update `dev-reload.sh` (use discovery)
- [ ] Update `run-persona-test-with-video.sh` (use discovery)
- [ ] Update `load-test-data.sh` (use discovery)
- [ ] Update test automation Kotlin code (use discovery)
- [ ] Add automatic cleanup (trap handlers)
- [ ] Add stale lock detection
- [ ] Add documentation
- [ ] Add tests

### Future Enhancements

1. **Emulator Pooling**: Pre-create emulators for faster startup
2. **Health Monitoring**: Monitor emulator health and restart if needed
3. **Resource Limits**: Limit number of emulators per agent
4. **Priority System**: Allow priority for certain agents
5. **Metrics**: Track emulator usage and performance

## Related Documentation

- `docs/development/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow guidelines
- `docs/development/AGENT_COORDINATION.md` - Agent coordination tracking
- `scripts/emulator-manager.sh` - Current emulator management script
- `.cursor/rules/emulator-workflow.mdc` - Emulator workflow rules

