#!/bin/bash

# Stream test logs in real-time with cognitive process highlighting

set -e

SCENARIO_NAME="${1:-signup-tech-novice}"
DEVICE_ID="${2:-emulator-5556}"

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "🧠 REAL-TIME COGNITIVE PROCESS STREAMING"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "📋 Scenario: $SCENARIO_NAME"
echo "📱 Device: $DEVICE_ID"
echo ""
echo "Streaming logs with cognitive process markers..."
echo "Press Ctrl+C to stop"
echo ""

# Load scenario
SCENARIO_FILE="test-scenarios/${SCENARIO_NAME}.yaml"
if [ -f "$SCENARIO_FILE" ]; then
    TASK=$(grep "^task:" "$SCENARIO_FILE" | sed 's/task: *"\(.*\)"/\1/' | sed "s/task: *'\(.*\)'/\1/" | sed 's/task: *\(.*\)/\1/')
    CONTEXT=$(grep "^context:" "$SCENARIO_FILE" | sed 's/context: *"\(.*\)"/\1/' | sed "s/context: *'\(.*\)'/\1/" | sed 's/context: *\(.*\)/\1/')
else
    TASK="Sign up and add a mood value"
    CONTEXT="tech_novice persona"
fi

# Focus emulator
./scripts/focus-emulator.sh

# Run test and stream logs with highlighting
cd test-automation

# Use unbuffered output for real-time streaming
stdbuf -oL -eL ../gradlew run --args="--task '$TASK' --context '$CONTEXT' --device $DEVICE_ID" 2>&1 | while IFS= read -r line; do
    # Color code cognitive processes
    if echo "$line" | grep -qE "👁️.*PERCEPTION"; then
        echo -e "\033[36m$line\033[0m"  # Cyan for perception
    elif echo "$line" | grep -qE "🧠.*INTENTION|🧠.*ORIENT"; then
        echo -e "\033[35m$line\033[0m"  # Magenta for intention/orientation
    elif echo "$line" | grep -qE "✋.*ACTION"; then
        echo -e "\033[33m$line\033[0m"  # Yellow for action
    elif echo "$line" | grep -qE "🔄.*FEEDBACK|🔄.*State change"; then
        echo -e "\033[32m$line\033[0m"  # Green for feedback
    elif echo "$line" | grep -qE "🎯.*DECIDE|OODA LOOP"; then
        echo -e "\033[34m$line\033[0m"  # Blue for decision
    elif echo "$line" | grep -qE "ERROR|WARN|Failed|Exception"; then
        echo -e "\033[31m$line\033[0m"  # Red for errors
    elif echo "$line" | grep -qE "✅|Success"; then
        echo -e "\033[32m$line\033[0m"  # Green for success
    elif echo "$line" | grep -qE "Planning|Iteration|Generated plan"; then
        echo -e "\033[1m$line\033[0m"  # Bold for planning
    else
        echo "$line"  # Normal output
    fi
done

cd ..

