#!/bin/bash

# Enhanced test execution with MCP command logging
# This script runs alongside mobile-mcp tests to log all MCP interactions
#
# Usage:
#   ./scripts/execute-test-with-logging.sh <scenario_name> [device_id]
#
# This creates a test environment where:
#   1. Video recording is active
#   2. All MCP commands are logged
#   3. State detection happens automatically
#   4. Execution log shows AI decisions

set -e

SCENARIO_NAME="${1:-default}"
DEVICE_ID="${2:-emulator-5554}"
OUTPUT_DIR="/tmp/test_results/${SCENARIO_NAME}_$(date +%Y%m%d_%H%M%S)"

mkdir -p "$OUTPUT_DIR"

EXECUTION_LOG="$OUTPUT_DIR/execution.log"
MCP_COMMANDS_LOG="$OUTPUT_DIR/mcp_commands.log"
STATE_LOG="$OUTPUT_DIR/state_changes.log"
VIDEO_FILE="$OUTPUT_DIR/test_journey.mp4"
DEVICE_VIDEO="/sdcard/test_$(date +%s).mp4"

log() {
    TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
    echo "[$TIMESTAMP] $*" | tee -a "$EXECUTION_LOG"
}

log_mcp_command() {
    TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
    echo "[$TIMESTAMP] MCP Command: $*" | tee -a "$MCP_COMMANDS_LOG" | tee -a "$EXECUTION_LOG"
}

log_state_change() {
    TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
    echo "[$TIMESTAMP] State: $*" | tee -a "$STATE_LOG" | tee -a "$EXECUTION_LOG"
}

# Start video recording
start_recording() {
    log "🎥 Starting video recording..."
    log "   Device: $DEVICE_ID"
    log "   Output: $VIDEO_FILE"
    adb -s "$DEVICE_ID" shell screenrecord --time-limit 300 "$DEVICE_VIDEO" &
    RECORD_PID=$!
    echo "$RECORD_PID" > "$OUTPUT_DIR/record_pid.txt"
    echo "$DEVICE_VIDEO" > "$OUTPUT_DIR/device_video.txt"
    sleep 2
    log "✅ Recording started (PID: $RECORD_PID)"
}

# Stop and pull video
stop_recording() {
    log "⏹️  Stopping video recording..."
    adb -s "$DEVICE_ID" shell killall screenrecord 2>/dev/null || true
    sleep 5  # Wait longer for file to finalize
    
    DEVICE_VIDEO=$(cat "$OUTPUT_DIR/device_video.txt" 2>/dev/null || echo "$DEVICE_VIDEO")
    log "📥 Pulling video from device..."
    TEMP_VIDEO="$OUTPUT_DIR/temp_video.mp4"
    if adb -s "$DEVICE_ID" pull "$DEVICE_VIDEO" "$TEMP_VIDEO" 2>/dev/null; then
        # Convert to QuickTime-compatible format
        log "🔄 Converting video to QuickTime-compatible format..."
        if ffmpeg -i "$TEMP_VIDEO" -c:v libx264 -preset medium -crf 23 -c:a aac -b:a 128k -movflags +faststart "$VIDEO_FILE" -y 2>/dev/null; then
            rm -f "$TEMP_VIDEO"
            FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
            log "✅ Video saved (QuickTime-compatible): $VIDEO_FILE ($FILE_SIZE)"
        else
            # If conversion fails, keep original
            mv "$TEMP_VIDEO" "$VIDEO_FILE"
            FILE_SIZE=$(ls -lh "$VIDEO_FILE" | awk '{print $5}')
            log "✅ Video saved (original format): $VIDEO_FILE ($FILE_SIZE)"
            log "⚠️  Note: Video may not be QuickTime-compatible"
        fi
        adb -s "$DEVICE_ID" shell rm "$DEVICE_VIDEO" 2>/dev/null || true
    else
        log "⚠️  Failed to pull video"
    fi
}

# Detect current app state
detect_state() {
    log_state_change "Detecting app state..."
    
    # Get UI elements
    ELEMENTS=$(adb -s "$DEVICE_ID" shell "uiautomator dump && cat /sdcard/window_dump.xml" 2>/dev/null | head -200)
    
    # Check authentication
    if echo "$ELEMENTS" | grep -qiE "signed in as|logout|sign out"; then
        AUTH_STATE="authenticated"
        EMAIL=$(echo "$ELEMENTS" | grep -oE "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}" | head -1 || echo "unknown")
        log_state_change "Authenticated as: $EMAIL"
    else
        AUTH_STATE="not_authenticated"
        log_state_change "Not authenticated"
    fi
    
    # Detect screen
    if echo "$ELEMENTS" | grep -qiE "sign in|sign up|email.*password"; then
        SCREEN="authentication"
    elif echo "$ELEMENTS" | grep -qiE "mood.*management|mood score|how are you feeling"; then
        SCREEN="mood_management"
    elif echo "$ELEMENTS" | grep -qiE "electric sheep|landing|mood management.*card"; then
        SCREEN="landing"
    else
        SCREEN="unknown"
    fi
    log_state_change "Current screen: $SCREEN"
    
    echo "AUTH_STATE=$AUTH_STATE" > "$OUTPUT_DIR/current_state.txt"
    echo "SCREEN=$SCREEN" >> "$OUTPUT_DIR/current_state.txt"
    if [ -n "$EMAIL" ]; then
        echo "EMAIL=$EMAIL" >> "$OUTPUT_DIR/current_state.txt"
    fi
}

# Calculate test mood score based on experience smoothness
calculate_mood_score() {
    if [ -f "scripts/calculate-test-mood.sh" ]; then
        MOOD_SCORE=$(./scripts/calculate-test-mood.sh "$OUTPUT_DIR")
        echo "$MOOD_SCORE" > "$OUTPUT_DIR/mood_score.txt"
        log "🎭 Test experience mood score: $MOOD_SCORE/10"
        echo "$MOOD_SCORE"
    else
        echo "10"  # Default: perfect
    fi
}

# Generate test report
generate_report() {
    REPORT_FILE="$OUTPUT_DIR/test_report.md"
    MOOD_SCORE=$(calculate_mood_score)
    
    cat > "$REPORT_FILE" << EOF
# Test Execution Report

**Scenario**: $SCENARIO_NAME  
**Date**: $(date)  
**Device**: $DEVICE_ID  
**Test Experience Mood**: $MOOD_SCORE/10 🎭

## Test Result

$(if [ -f "$OUTPUT_DIR/test_result.txt" ]; then
    TEST_RESULT=$(cat "$OUTPUT_DIR/test_result.txt")
    if [ "$TEST_RESULT" = "PASSED" ]; then
        echo "**✅ TEST PASSED**"
    elif [ "$TEST_RESULT" = "FAILED" ]; then
        echo "**❌ TEST FAILED**"
    else
        echo "**⚠️  TEST RESULT: UNKNOWN**"
    fi
else
    echo "**⚠️  Test result not available**"
fi)

## Execution Summary

- **Video Recording**: $(if [ -f "$VIDEO_FILE" ]; then echo "✅ Recorded ($(ls -lh "$VIDEO_FILE" | awk '{print $5}'))"; else echo "❌ Not available"; fi)
- **Execution Log**: ✅ Available ($(wc -l < "$EXECUTION_LOG" | xargs) lines)
- **MCP Commands Log**: ✅ Available ($(if [ -f "$MCP_COMMANDS_LOG" ]; then wc -l < "$MCP_COMMANDS_LOG" | xargs; else echo "0"; fi) commands)
- **State Changes**: ✅ Available ($(wc -l < "$STATE_LOG" | xargs) changes)
- **Stalls Detected**: $(if [ -f "$OUTPUT_DIR/stalls.log" ]; then wc -l < "$OUTPUT_DIR/stalls.log" | xargs; else echo "0"; fi)

## Test Experience Analysis

**Mood Score: $MOOD_SCORE/10**

The mood score reflects how smooth the test execution was:
- **10**: Perfect, no issues
- **8-9**: Very smooth, minor hiccups
- **6-7**: Some issues but manageable
- **4-5**: Multiple problems encountered
- **1-3**: Significant difficulties

Factors considered:
- Number of stalls
- Errors encountered
- Retries needed
- Overall execution time

## Files Generated

- \`test_journey.mp4\` - Video recording of test execution
- \`execution.log\` - Complete execution log with timestamps
- \`mcp_commands.log\` - All MCP commands executed
- \`state_changes.log\` - App state transitions
- \`current_state.txt\` - Final app state
- \`mood_score.txt\` - Test experience mood score
- \`stalls.log\` - Detected stalls (if any)

## How to Review

1. **Watch the video**: Open \`test_journey.mp4\` to see the visual execution
2. **Read the logs**: Check \`execution.log\` to see step-by-step actions
3. **Review MCP commands**: See \`mcp_commands.log\` for all mobile-mcp interactions
4. **Check state changes**: Review \`state_changes.log\` to understand state transitions
5. **Check mood score**: See \`mood_score.txt\` for test experience rating

## Next Steps

To execute a test scenario using mobile-mcp in Cursor:

1. Start this script: \`./scripts/execute-test-with-logging.sh scenario_name\`
2. In Cursor, use mobile-mcp to execute the test steps
3. All MCP commands will be logged automatically
4. Press Ctrl+C when test completes
5. Review the generated report and video

EOF

    log "📄 Test report generated: $REPORT_FILE"
}

# Cleanup
cleanup() {
    log ""
    log "=========================================="
    log "Finalizing test execution"
    log "=========================================="
    
    # Stop monitor
    if [ -f "$OUTPUT_DIR/monitor_pid.txt" ]; then
        MONITOR_PID=$(cat "$OUTPUT_DIR/monitor_pid.txt" 2>/dev/null || echo "")
        if [ -n "$MONITOR_PID" ] && kill -0 "$MONITOR_PID" 2>/dev/null; then
            log "Stopping monitor..."
            kill "$MONITOR_PID" 2>/dev/null || true
            wait "$MONITOR_PID" 2>/dev/null || true
        fi
    fi
    
    stop_recording
    detect_state
    generate_report
    
    # Check for stalls
    if [ -f "$OUTPUT_DIR/stalls.log" ]; then
        STALL_COUNT=$(wc -l < "$OUTPUT_DIR/stalls.log" | xargs)
        if [ "$STALL_COUNT" -gt 0 ]; then
            log ""
            log "⚠️  Stalls detected: $STALL_COUNT (see $OUTPUT_DIR/stalls.log)"
        fi
    fi
    
    log ""
    log "✅ Test execution complete!"
    log "📁 All files saved to: $OUTPUT_DIR"
    log ""
    log "Files:"
    ls -lh "$OUTPUT_DIR" | tail -n +2 | awk '{print "   " $9 " (" $5 ")"}'
}

trap cleanup EXIT INT TERM

# Main execution
log "=========================================="
log "Test Execution with Logging"
log "=========================================="
log "Scenario: $SCENARIO_NAME"
log "Device: $DEVICE_ID"
log "Output: $OUTPUT_DIR"
log ""

start_recording
detect_state

log ""
log "=========================================="
log "Ready for Test Execution"
log "=========================================="
log ""
log "✅ Video recording: Active"
log "✅ State detection: Complete"
log "✅ Logging: Active"
# Start monitor in background
log ""
log "Starting test execution monitor..."
MONITOR_PID=""
if [ -f "scripts/monitor-test-execution.sh" ]; then
    ./scripts/monitor-test-execution.sh "$OUTPUT_DIR" "$DEVICE_ID" &
    MONITOR_PID=$!
    echo "$MONITOR_PID" > "$OUTPUT_DIR/monitor_pid.txt"
    log "Monitor started (PID: $MONITOR_PID)"
fi

log ""
log "Now execute your test using mobile-mcp in Cursor."
log "All MCP commands and state changes will be logged."
log "Stall monitoring: Active (check $OUTPUT_DIR/stalls.log)"
log ""
log "Press Ctrl+C when test is complete."
log ""

# Keep script running to maintain recording and logging
while true; do
    sleep 5
    # Check if monitor is still running
    if [ -n "$MONITOR_PID" ] && ! kill -0 "$MONITOR_PID" 2>/dev/null; then
        log "⚠️  Monitor process stopped"
    fi
done

