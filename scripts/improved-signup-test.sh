#!/bin/bash

# Improved sign-up test with proper waiting logic
# This demonstrates the correct approach: wait for async operations

DEVICE_ID="${1:-emulator-5554}"

echo "=== Improved Sign-Up Test ==="
echo ""

# Helper function to wait for loading
wait_for_loading() {
    local timeout="${1:-25}"
    local start_time=$(date +%s)
    local current_time
    local elapsed_time
    
    echo "Waiting for loading to complete (timeout: ${timeout}s)..."
    
    while true; do
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        
        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "⚠️  Timeout waiting for loading"
            return 1
        fi
        
        UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
        
        # Check if loading indicators are gone
        if ! echo "$UI_DUMP" | grep -qiE "loading|creating.*account|please.*wait"; then
            # Check if we're on a different screen (success) or error
            if echo "$UI_DUMP" | grep -qiE "Mood Management|mood.*score|error"; then
                echo "✅ Loading complete"
                return 0
            fi
        fi
        
        sleep 0.5
    done
}

# Helper to wait for authentication
wait_for_auth() {
    local timeout="${1:-15}"
    local start_time=$(date +%s)
    local current_time
    local elapsed_time
    
    echo "Waiting for authentication (timeout: ${timeout}s)..."
    
    while true; do
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        
        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "❌ Timeout waiting for authentication"
            return 1
        fi
        
        UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
        
        if echo "$UI_DUMP" | grep -qiE "Mood Management.*screen heading|mood.*score|how are you feeling"; then
            echo "✅ Authentication detected"
            return 0
        fi
        
        sleep 0.5
    done
}

# Launch app
echo "1. Launching app..."
adb -s "$DEVICE_ID" shell am start -n com.electricsheep.app/.MainActivity
sleep 3

# Navigate
echo "2. Navigating to mood management..."
adb -s "$DEVICE_ID" shell input tap 490 1406
sleep 2

# Expand form
echo "3. Expanding email/password form..."
adb -s "$DEVICE_ID" shell input tap 540 909
sleep 1

# Fill email
TIMESTAMP=$(date +%s)
EMAIL="test${TIMESTAMP}@gmail.com"
echo "4. Filling email: $EMAIL"
adb -s "$DEVICE_ID" shell input tap 540 1253 && sleep 0.5
adb -s "$DEVICE_ID" shell input keyevent KEYCODE_CTRL_A && sleep 0.2
adb -s "$DEVICE_ID" shell input keyevent KEYCODE_DEL && sleep 0.2
adb -s "$DEVICE_ID" shell input text "$EMAIL" && sleep 2

# Wait for button enable
echo "5. Waiting for button to enable..."
for i in {1..20}; do
    UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
    if echo "$UI_DUMP" | grep -A 5 "Create Account" | grep -q 'enabled="true"'; then
        echo "   ✅ Button enabled"
        break
    fi
    sleep 0.5
done

# Fill password
echo "6. Filling password..."
adb -s "$DEVICE_ID" shell input tap 540 1463 && sleep 0.5
adb -s "$DEVICE_ID" shell input keyevent KEYCODE_CTRL_A && sleep 0.2
adb -s "$DEVICE_ID" shell input keyevent KEYCODE_DEL && sleep 0.2
adb -s "$DEVICE_ID" shell input text "TestPassword123!" && sleep 2

# Wait for button enable again
echo "7. Waiting for button to enable after password..."
for i in {1..20}; do
    UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
    if echo "$UI_DUMP" | grep -A 5 "Create Account" | grep -q 'enabled="true"'; then
        echo "   ✅ Button enabled"
        break
    fi
    sleep 0.5
done

# Click Create Account
echo "8. Clicking Create Account..."
adb -s "$DEVICE_ID" logcat -c
adb -s "$DEVICE_ID" shell input tap 540 1633
sleep 1

# Wait for loading to complete (KEY IMPROVEMENT)
echo "9. Waiting for sign-up to complete..."
if wait_for_loading 30; then
    echo "   ✅ Loading completed"
else
    echo "   ⚠️  Loading timeout"
fi

# Check for errors
echo "10. Checking for errors..."
UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
if echo "$UI_DUMP" | grep -qiE "error|exception|fail"; then
    echo "   ❌ Error detected:"
    echo "$UI_DUMP" | grep -iE "error|exception|fail" | head -3
    exit 1
fi

# Wait for authentication
echo "11. Waiting for authentication..."
if wait_for_auth 15; then
    echo ""
    echo "✅✅✅ SIGN-UP TEST PASSED ✅✅✅"
    exit 0
else
    echo ""
    echo "❌ SIGN-UP TEST FAILED"
    echo ""
    echo "Logcat output:"
    adb -s "$DEVICE_ID" logcat -d -t 50 | grep -iE "MoodManagementViewModel|SupabaseAuthProvider" | tail -20
    exit 1
fi

