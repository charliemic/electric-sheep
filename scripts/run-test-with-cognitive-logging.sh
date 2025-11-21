#!/bin/bash

# Run test with cognitive process logging streamed to console
# Shows real-time cognitive processes: Perception, Intention, Action, Feedback

set -e

SCENARIO_NAME="${1:-signup-tech-novice}"
DEVICE_ID="${2:-emulator-5556}"

echo ""
echo "═══════════════════════════════════════════════════════════════"
echo "🧠 TEST WITH COGNITIVE PROCESS LOGGING"
echo "═══════════════════════════════════════════════════════════════"
echo ""
echo "📋 Scenario: $SCENARIO_NAME"
echo "📱 Device: $DEVICE_ID"
echo ""
echo "👁️  PERCEPTION - What we see"
echo "🧠 INTENTION - What we want"
echo "✋ ACTION - What we do"
echo "🔄 FEEDBACK - What happened"
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

# Focus emulator removed - not needed and distracting

# Run test with filtered output showing cognitive processes
cd test-automation

echo "🚀 Starting test..."
echo ""

../gradlew run --args="--task '$TASK' --context '$CONTEXT' --device $DEVICE_ID" 2>&1 | while IFS= read -r line; do
    # Always show cognitive process markers
    if echo "$line" | grep -qE "(👁️|🧠|✋|🔄|🎯|OBSERVE|ORIENT|DECIDE|ACT|PERCEPTION|INTENTION|ACTION|FEEDBACK)"; then
        echo "$line"
    # Show OODA loop markers
    elif echo "$line" | grep -qE "(OODA LOOP|Planning iteration|Iteration)"; then
        echo "$line"
    # Show important status
    elif echo "$line" | grep -qE "(✅|❌|ERROR|WARN|Failed|Success|Task|persona)"; then
        echo "$line"
    # Show action execution
    elif echo "$line" | grep -qE "(Executing|Tapping|Typing|Swipe|Wait|Verify)"; then
        echo "$line"
    fi
done

cd ..

echo ""
echo "✅ Test complete"

