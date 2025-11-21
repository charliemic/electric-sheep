# Emulator Workflow Guide

This guide covers the optimized emulator workflow for Electric Sheep development and testing.

## Quick Start

### Start Development Session
```bash
# Option 1: Automatic (recommended)
./scripts/dev-reload.sh

# Option 2: Manual
./scripts/emulator-manager.sh start
./scripts/dev-reload.sh
```

### Check Status
```bash
./scripts/emulator-manager.sh status
```

### Stop Emulator
```bash
./scripts/emulator-manager.sh stop
```

## Emulator Management

### Primary Script: `emulator-manager.sh`

The emulator manager provides a unified interface for all emulator operations.

#### Commands

**Start Emulator**
```bash
# Start default emulator (creates if needed)
./scripts/emulator-manager.sh start

# Start specific AVD
./scripts/emulator-manager.sh start electric_sheep_dev
```

**Stop Emulator**
```bash
# Stop current emulator
./scripts/emulator-manager.sh stop

# Stop specific device
./scripts/emulator-manager.sh stop emulator-5554
```

**List Emulators**
```bash
# List available AVDs
./scripts/emulator-manager.sh list

# List running emulators
./scripts/emulator-manager.sh running
```

**Clean Emulator**
```bash
# Wipe emulator data (factory reset)
./scripts/emulator-manager.sh clean

# Clean specific device
./scripts/emulator-manager.sh clean emulator-5554
```

**Check Status**
```bash
# Show current device status
./scripts/emulator-manager.sh status
```

**Ensure Running**
```bash
# Ensure emulator is running (start if needed)
./scripts/emulator-manager.sh ensure
```

### Device ID Helper: `get-device-id.sh`

Get the current connected device ID programmatically:

```bash
# Get device ID (exits with error if none)
DEVICE=$(./scripts/get-device-id.sh)

# Use in scripts
./scripts/execute-test-with-logging.sh my-test "$DEVICE"
```

**Benefits:**
- No hardcoded device IDs
- Works with any connected device
- Automatic device detection

## Development Workflows

### Standard Development Loop

**Quick Iteration:**
```bash
# Make code changes, then:
./scripts/dev-reload.sh
```

**Clean Build:**
```bash
./scripts/dev-reload.sh --clean
```

**Fresh Start (Clear App Data):**
```bash
./scripts/dev-reload.sh --fresh
```

**Clean Build + Fresh Start:**
```bash
./scripts/dev-reload.sh --clean --fresh
```

### Testing Workflow

**Before Tests:**
```bash
# Ensure emulator is running
./scripts/emulator-manager.sh ensure

# Get device ID
DEVICE=$(./scripts/get-device-id.sh)
```

**Run Tests:**
```bash
# Using device ID
./scripts/execute-test-with-logging.sh my-test "$DEVICE"

# Or let script detect device
./scripts/execute-test-with-logging.sh my-test
```

**After Tests:**
```bash
# Clean emulator if needed
./scripts/emulator-manager.sh clean

# Stop emulator to free resources
./scripts/emulator-manager.sh stop
```

### Debugging Workflow

**Check Device Status:**
```bash
./scripts/emulator-manager.sh status
```

**View Logs:**
```bash
# Real-time logs
DEVICE=$(./scripts/get-device-id.sh)
adb -s "$DEVICE" logcat | grep -i electric

# Or filtered logs
adb -s "$DEVICE" logcat | grep -i "MoodManagementViewModel\|SupabaseAuthProvider"
```

**Access Database:**
```bash
DEVICE=$(./scripts/get-device-id.sh)
adb -s "$DEVICE" shell
run-as com.electricsheep.app
cd /data/data/com.electricsheep.app/databases
sqlite3 electric_sheep_database
```

## Common Scenarios

### Scenario 1: Fresh Development Session

```bash
# 1. Start emulator
./scripts/emulator-manager.sh start

# 2. Build and install
./scripts/dev-reload.sh --clean --fresh

# 3. Start developing
# Make changes, then:
./scripts/dev-reload.sh
```

### Scenario 2: Quick Testing

```bash
# 1. Ensure emulator is running
./scripts/emulator-manager.sh ensure

# 2. Run test
DEVICE=$(./scripts/get-device-id.sh)
./scripts/execute-test-with-logging.sh my-test "$DEVICE"
```

### Scenario 3: Testing Database Migrations

```bash
# 1. Clean emulator
./scripts/emulator-manager.sh clean

# 2. Start fresh
./scripts/emulator-manager.sh start

# 3. Install app
./scripts/dev-reload.sh --fresh
```

### Scenario 4: Multiple Emulators

```bash
# 1. List available AVDs
./scripts/emulator-manager.sh list

# 2. Start specific AVD
./scripts/emulator-manager.sh start my_custom_avd

# 3. Check running emulators
./scripts/emulator-manager.sh running
```

## Integration with Existing Scripts

### Updated Scripts

**`dev-reload.sh`**
- Now automatically ensures emulator is running
- Uses device ID helper for consistent device detection
- Shows emulator management commands in help text

**Test Scripts**
- Should accept device ID as parameter
- Use `get-device-id.sh` as default if not provided
- Example: `./scripts/execute-test-with-logging.sh <test-name> [device-id]`

### Best Practices

**DO ✅**
- Use `emulator-manager.sh` for all emulator operations
- Use `get-device-id.sh` instead of hardcoding device IDs
- Check device status before running tests
- Clean emulator data when testing migrations
- Stop emulators when done to free resources

**DON'T ❌**
- Don't hardcode device IDs like `emulator-5554`
- Don't manually manage emulator lifecycle
- Don't leave emulators running unnecessarily
- Don't assume a specific device ID is available

## Troubleshooting

### No Device Found

```bash
# Check status
./scripts/emulator-manager.sh status

# Ensure emulator is running
./scripts/emulator-manager.sh ensure

# List running devices
./scripts/emulator-manager.sh running
```

### Emulator Won't Start

```bash
# Check available AVDs
./scripts/emulator-manager.sh list

# Try creating new emulator
./scripts/emulator-manager.sh start electric_sheep_dev

# Check Android SDK
echo $ANDROID_HOME
```

### Device Not Detected

```bash
# Check ADB connection
adb devices

# Restart ADB server
adb kill-server
adb start-server

# Check device status
./scripts/emulator-manager.sh status
```

### Build Fails with "No Device"

```bash
# Ensure emulator is running
./scripts/emulator-manager.sh ensure

# Verify device is connected
./scripts/emulator-manager.sh status

# Try again
./scripts/dev-reload.sh
```

## Advanced Usage

### Custom AVD Configuration

Create a custom AVD for Electric Sheep:

```bash
# Using Android Studio AVD Manager
# Or using command line:
avdmanager create avd \
  -n electric_sheep_dev \
  -k "system-images;android-34;google_apis;x86_64" \
  -d "pixel_5"
```

Then use it:
```bash
./scripts/emulator-manager.sh start electric_sheep_dev
```

### Script Integration

Use emulator manager in your own scripts:

```bash
#!/bin/bash
# Ensure emulator is running
DEVICE=$(./scripts/emulator-manager.sh ensure)

# Use device ID
adb -s "$DEVICE" install app.apk
```

### Background Emulator

Start emulator in background and continue working:

```bash
# Start emulator (runs in background)
./scripts/emulator-manager.sh start

# Continue with other tasks
# Emulator will be ready when you need it
```

## Related Documentation

- `.cursor/rules/emulator-workflow.mdc` - Cursor rules for emulator workflow
- `scripts/emulator-manager.sh` - Main emulator management script
- `scripts/get-device-id.sh` - Device ID helper
- `scripts/dev-reload.sh` - Development reload script
- `README.md` - General development setup



