#!/bin/bash

# Run a persona-driven test with video recording and natural language activity logging
#
# Usage:
#   ./scripts/run-persona-test-with-video.sh [scenario_name] [device_id]
#
# Example:
#   ./scripts/run-persona-test-with-video.sh signup-and-mood-entry emulator-5554

set -e

SCENARIO_NAME="${1:-signup-and-mood-entry}"
# Use discovery service if device ID not provided, otherwise use provided device
if [ -z "$2" ]; then
    DEVICE_ID=$(./scripts/emulator-discovery.sh acquire)
    ACQUIRED_DEVICE="$DEVICE_ID"
else
    DEVICE_ID="$2"
    ACQUIRED_DEVICE=""
fi
OUTPUT_DIR="/tmp/persona_test_results/${SCENARIO_NAME}_$(date +%Y%m%d_%H%M%S)"

mkdir -p "$OUTPUT_DIR"

# Load scenario if it exists
SCENARIO_FILE="test-scenarios/${SCENARIO_NAME}.yaml"
if [ -f "$SCENARIO_FILE" ]; then
    echo "📋 Loading scenario: $SCENARIO_FILE"
    TASK=$(grep "^task:" "$SCENARIO_FILE" | sed 's/task: *"\(.*\)"/\1/' | sed "s/task: *'\(.*\)'/\1/" | sed 's/task: *\(.*\)/\1/')
    CONTEXT=$(grep "^context:" "$SCENARIO_FILE" | sed 's/context: *"\(.*\)"/\1/' | sed "s/context: *'\(.*\)'/\1/" | sed 's/context: *\(.*\)/\1/')
    PERSONA=$(grep "^persona:" "$SCENARIO_FILE" | sed 's/persona: *"\(.*\)"/\1/' | sed "s/persona: *'\(.*\)'/\1/" | sed 's/persona: *\(.*\)/\1/')
    
    if [ -n "$PERSONA" ]; then
        CONTEXT="$CONTEXT (persona: $PERSONA)"
    fi
else
    echo "⚠️  Scenario file not found: $SCENARIO_FILE"
    echo "Using default task..."
    TASK="Sign up and add mood value"
    CONTEXT="tech_novice persona"
fi

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "🎬 PERSONA-DRIVEN TEST WITH VIDEO RECORDING"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "📋 Scenario: $SCENARIO_NAME"
echo "👤 Persona: ${PERSONA:-tech_novice}"
echo "📝 Task: $TASK"
echo "💭 Context: $CONTEXT"
echo "📱 Device: $DEVICE_ID"
echo "📁 Output: $OUTPUT_DIR"
echo ""

# Log files
EXECUTION_LOG="$OUTPUT_DIR/execution.log"
NATURAL_LANGUAGE_LOG="$OUTPUT_DIR/natural_language_activities.log"
VIDEO_FILE="$OUTPUT_DIR/test_journey.mp4"
DEVICE_VIDEO="/sdcard/persona_test_$(date +%s).mp4"

log() {
    TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
    echo "[$TIMESTAMP] $*" | tee -a "$EXECUTION_LOG"
}

log_natural_language() {
    TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
    echo "[$TIMESTAMP] 👤 USER ACTIVITY: $*" | tee -a "$NATURAL_LANGUAGE_LOG" | tee -a "$EXECUTION_LOG"
}

# Start video recording
start_recording() {
    log "🎥 Starting video recording..."
    log "   Device: $DEVICE_ID"
    log "   Output: $VIDEO_FILE"
    # Use time-limit to ensure proper finalization, and run in background on device
    # The time-limit ensures screenrecord will finalize properly when it reaches the limit
    adb -s "$DEVICE_ID" shell "screenrecord --time-limit 300 --bit-rate 4000000 $DEVICE_VIDEO" &
    RECORD_PID=$!
    echo "$RECORD_PID" > "$OUTPUT_DIR/record_pid.txt"
    echo "$DEVICE_VIDEO" > "$OUTPUT_DIR/device_video.txt"
    sleep 2
    log "✅ Recording started (PID: $RECORD_PID)"
}

# Stop and pull video
stop_recording() {
    log "⏹️  Stopping video recording..."
    DEVICE_VIDEO=$(cat "$OUTPUT_DIR/device_video.txt" 2>/dev/null || echo "$DEVICE_VIDEO")
    
    # CRITICAL FIX: screenrecord runs ON THE DEVICE, not on the host
    # We must send SIGINT to the process ON THE DEVICE, not the local adb process
    # Android's screenrecord responds to SIGINT by finalizing the file (writing moov atom)
    
    # Find screenrecord PID on device (matching our video file)
    SCREENRECORD_PID=$(adb -s "$DEVICE_ID" shell "pgrep -f 'screenrecord.*$DEVICE_VIDEO'" 2>/dev/null | tr -d '\r\n ' | head -1)
    
    if [ -n "$SCREENRECORD_PID" ] && [ "$SCREENRECORD_PID" != "" ]; then
        log "   Found screenrecord on device (PID: $SCREENRECORD_PID)"
        log "   Sending SIGINT for graceful finalization..."
        adb -s "$DEVICE_ID" shell "kill -INT $SCREENRECORD_PID" 2>/dev/null || true
        sleep 3
    else
        # Fallback: try to find any screenrecord process
        SCREENRECORD_PID=$(adb -s "$DEVICE_ID" shell "pgrep -f screenrecord" 2>/dev/null | tr -d '\r\n ' | head -1)
        if [ -n "$SCREENRECORD_PID" ] && [ "$SCREENRECORD_PID" != "" ]; then
            log "   Found screenrecord process, sending SIGINT..."
            adb -s "$DEVICE_ID" shell "kill -INT $SCREENRECORD_PID" 2>/dev/null || true
            sleep 3
        fi
    fi
    
    # Check if still running
    if adb -s "$DEVICE_ID" shell "pgrep -f screenrecord" > /dev/null 2>&1; then
        log "   Screenrecord still running, using killall with SIGINT..."
        adb -s "$DEVICE_ID" shell "killall -INT screenrecord" 2>/dev/null || true
        sleep 2
        if adb -s "$DEVICE_ID" shell "pgrep -f screenrecord" > /dev/null 2>&1; then
            log "   Warning: Force killing screenrecord (may corrupt video)"
            adb -s "$DEVICE_ID" shell killall screenrecord 2>/dev/null || true
        fi
    fi
    
    # CRITICAL: Wait for moov atom to be written (required for Mac/QuickTime)
    log "   Waiting for moov atom finalization (required for Mac/QuickTime)..."
    sleep 20  # Increased wait - moov atom write is critical for playback on Mac
    
    log "📥 Pulling video from device..."
    TEMP_VIDEO="$OUTPUT_DIR/temp_video.mp4"
    if adb -s "$DEVICE_ID" pull "$DEVICE_VIDEO" "$TEMP_VIDEO" 2>/dev/null; then
        # Convert to QuickTime-compatible format with activity overlays
        log "🔄 Converting video and adding activity overlays..."
        if command -v ffmpeg > /dev/null 2>&1; then
            # First, convert to compatible format
            CONVERTED_VIDEO="${TEMP_VIDEO%.*}_converted.mp4"
            if ffmpeg -i "$TEMP_VIDEO" \
                -c:v libx264 \
                -preset medium \
                -crf 23 \
                -c:a aac \
                -b:a 128k \
                -movflags +faststart \
                -pix_fmt yuv420p \
                -y \
                "$CONVERTED_VIDEO" 2>/dev/null; then
                
                # Add activity overlays if script exists
                if [ -f "$(dirname "$0")/add-activity-overlays.sh" ]; then
                    log "📝 Adding activity overlays..."
                    chmod +x "$(dirname "$0")/add-activity-overlays.sh"
                    if "$(dirname "$0")/add-activity-overlays.sh" "$CONVERTED_VIDEO" "$NATURAL_LANGUAGE_LOG" "$VIDEO_FILE" 2>/dev/null; then
                        rm -f "$CONVERTED_VIDEO" "$TEMP_VIDEO"
                        FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
                        log "✅ Video with overlays saved: $VIDEO_FILE ($FILE_SIZE)"
                    else
                        mv "$CONVERTED_VIDEO" "$VIDEO_FILE"
                        rm -f "$TEMP_VIDEO"
                        FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
                        log "✅ Video saved (overlays failed): $VIDEO_FILE ($FILE_SIZE)"
                    fi
                else
                    mv "$CONVERTED_VIDEO" "$VIDEO_FILE"
                    rm -f "$TEMP_VIDEO"
                    FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
                    log "✅ Video saved (QuickTime-compatible): $VIDEO_FILE ($FILE_SIZE)"
                fi
            else
                mv "$TEMP_VIDEO" "$VIDEO_FILE"
                FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
                log "✅ Video saved (original format): $VIDEO_FILE ($FILE_SIZE)"
            fi
        else
            mv "$TEMP_VIDEO" "$VIDEO_FILE"
            FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
            log "✅ Video saved: $VIDEO_FILE ($FILE_SIZE)"
            log "⚠️  Note: Install ffmpeg for QuickTime-compatible format and overlays"
        fi
        adb -s "$DEVICE_ID" shell rm "$DEVICE_VIDEO" 2>/dev/null || true
    else
        log "⚠️  Failed to pull video"
    fi
}

# Check if Appium is running
check_appium() {
    if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
        log "⚠️  Appium server not running!"
        log "   Starting Appium server..."
        cd test-automation
        export ANDROID_HOME=/Users/CharlieCalver/Library/Android/sdk
        export ANDROID_SDK_ROOT=$ANDROID_HOME
        ./start-appium.sh 4723 &
        sleep 5
        cd ..
        
        if ! curl -s http://localhost:4723/status > /dev/null 2>&1; then
            log "❌ Failed to start Appium server"
            exit 1
        fi
        log "✅ Appium server started"
    else
        log "✅ Appium server is running"
    fi
}

# Main execution
main() {
    log "═══════════════════════════════════════════════════════════════"
    log "🚀 Starting Persona-Driven Test"
    log "═══════════════════════════════════════════════════════════════"
    
    # Log natural language activity
    log_natural_language "User wants to: $TASK"
    if [ -n "$CONTEXT" ]; then
        log_natural_language "Context: $CONTEXT"
    fi
    
    # Check prerequisites
    check_appium
    
    # Bring emulator window to front (macOS)
    if [[ "$OSTYPE" == "darwin"* ]]; then
        log "🖥️  Bringing emulator window to front..."
        osascript -e 'tell application "System Events" to tell process "qemu-system-aarch64" to set frontmost to true' 2>/dev/null || \
        osascript -e 'tell application "System Events" to tell process "qemu-system-x86_64" to set frontmost to true' 2>/dev/null || \
        osascript -e 'tell application "System Events" to set frontmost to true of (first process whose name contains "qemu" or name contains "emulator")' 2>/dev/null || \
        log "⚠️  Could not bring emulator to front (may not be needed)"
    fi
    
    # Start recording
    start_recording
    
    # Log natural language activity
    log_natural_language "Opening the Electric Sheep app..."
    log_natural_language "Looking for the sign-up option..."
    
    # Run the test
    log ""
    log "═══════════════════════════════════════════════════════════════"
    log "🧪 Running Test Automation (with cognitive process logging)"
    log "═══════════════════════════════════════════════════════════════"
    log ""
    
    cd test-automation
    
    # Capture test output with real-time streaming and parse for natural language activities
    # Use unbuffered output to see logs in real-time
    TEST_OUTPUT=$(stdbuf -oL -eL ../gradlew run --args="--task '$TASK' --context '$CONTEXT' --device $DEVICE_ID" 2>&1 | tee -a "$OUTPUT_DIR/test_output.log" | while IFS= read -r line; do
        # Stream cognitive process markers to console
        if echo "$line" | grep -qE "(👁️|🧠|✋|🔄|🎯|OBSERVE|ORIENT|DECIDE|ACT|PERCEPTION|INTENTION|ACTION|FEEDBACK)"; then
            echo "$line"
        fi
        # Also show errors and important info
        if echo "$line" | grep -qE "(ERROR|WARN|✅|❌|Planning|Iteration)"; then
            echo "$line"
        fi
        echo "$line"  # Still output everything to file
    done)
    
    cd ..
    
    # Parse test output for natural language activities (from logger output)
    echo "$TEST_OUTPUT" | grep "🎬 USER ACTIVITY:" | sed 's/.*🎬 USER ACTIVITY: //' | while read activity; do
        if [ -n "$activity" ]; then
            log_natural_language "$activity"
        fi
    done
    
    # Parse persona thoughts
    echo "$TEST_OUTPUT" | grep "💭 PERSONA THOUGHT:" | sed 's/.*💭 PERSONA THOUGHT: //' | while read thought; do
        if [ -n "$thought" ]; then
            log_natural_language "💭 Thinking: $thought"
        fi
    done
    
    # Also capture plan generation
    echo "$TEST_OUTPUT" | grep "📋 Generated plan\|Will tap\|Will type\|Will swipe\|Will wait\|Will verify\|Will go back" | while read line; do
        if echo "$line" | grep -q "📋 Generated plan"; then
            PLAN_INFO=$(echo "$line" | sed 's/.*📋 Generated plan //')
            log_natural_language "Planning my approach: $PLAN_INFO"
        elif echo "$line" | grep -q "Will "; then
            ACTION=$(echo "$line" | sed 's/.*\[[0-9]*\] //')
            log_natural_language "$ACTION"
        fi
    done
    
    # Stop recording
    stop_recording
    
    # Summary
    log ""
    log "═══════════════════════════════════════════════════════════════"
    log "✅ Test Execution Complete"
    log "═══════════════════════════════════════════════════════════════"
    log ""
    log "📁 Output Directory: $OUTPUT_DIR"
    log "📹 Video: $VIDEO_FILE"
    log "📝 Execution Log: $EXECUTION_LOG"
    log "💬 Natural Language Activities: $NATURAL_LANGUAGE_LOG"
    log ""
    log "🎬 To view the video:"
    log "   open '$VIDEO_FILE'"
    log ""
    log "📖 To review natural language activities:"
    log "   cat '$NATURAL_LANGUAGE_LOG'"
    log ""
    
    # Open video if on macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if [ -f "$VIDEO_FILE" ]; then
            log "🎬 Opening video in QuickTime..."
            open "$VIDEO_FILE" 2>/dev/null || log "⚠️  Could not open video automatically"
        fi
    fi
}

# Cleanup on exit
cleanup() {
    log "🧹 Cleaning up..."
    adb -s "$DEVICE_ID" shell killall screenrecord 2>/dev/null || true
    # Release emulator lock if we acquired it
    if [ -n "$ACQUIRED_DEVICE" ]; then
        ./scripts/emulator-discovery.sh release "$ACQUIRED_DEVICE" > /dev/null 2>&1 || true
    fi
}
trap cleanup EXIT

# Run main
main

