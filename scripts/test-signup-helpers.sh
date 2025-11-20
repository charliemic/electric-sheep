#!/bin/bash

# Helper functions for sign-up test interactions
# These functions handle timing and state detection better than direct adb commands

DEVICE_ID="${1:-emulator-5554}"

# Wait for UI element to appear with specific text or content description
# Usage: wait_for_element "text_or_desc" [timeout_seconds]
wait_for_element() {
    local search_term="$1"
    local timeout="${2:-15}"
    local start_time=$(date +%s)
    local current_time
    local elapsed_time
    
    echo "Waiting for element: '$search_term' (timeout: ${timeout}s)..."
    
    while true; do
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        
        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "❌ Timeout waiting for element: '$search_term'"
            return 1
        fi
        
        UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
        
        if echo "$UI_DUMP" | grep -qiE "$search_term"; then
            echo "✅ Element found: '$search_term'"
            return 0
        fi
        
        sleep 0.5
    done
}

# Wait for element to disappear
# Usage: wait_for_element_disappear "text_or_desc" [timeout_seconds]
wait_for_element_disappear() {
    local search_term="$1"
    local timeout="${2:-10}"
    local start_time=$(date +%s)
    local current_time
    local elapsed_time
    
    echo "Waiting for element to disappear: '$search_term' (timeout: ${timeout}s)..."
    
    while true; do
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        
        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "⚠️  Element still present: '$search_term'"
            return 1
        fi
        
        UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
        
        if ! echo "$UI_DUMP" | grep -qiE "$search_term"; then
            echo "✅ Element disappeared: '$search_term'"
            return 0
        fi
        
        sleep 0.5
    done
}

# Wait for loading state to complete
# Usage: wait_for_loading_complete [timeout_seconds]
wait_for_loading_complete() {
    local timeout="${1:-20}"
    local start_time=$(date +%s)
    local current_time
    local elapsed_time
    
    echo "Waiting for loading to complete (timeout: ${timeout}s)..."
    
    while true; do
        current_time=$(date +%s)
        elapsed_time=$((current_time - start_time))
        
        if [ "$elapsed_time" -ge "$timeout" ]; then
            echo "❌ Timeout waiting for loading to complete"
            return 1
        fi
        
        UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
        
        # Check if loading indicators are gone
        if ! echo "$UI_DUMP" | grep -qiE "loading|creating.*account|please.*wait"; then
            # Also check if error or success state is present
            if echo "$UI_DUMP" | grep -qiE "error|signed in|mood.*score"; then
                echo "✅ Loading complete (state detected)"
                return 0
            fi
        fi
        
        sleep 0.5
    done
}

# Check for error messages on screen
# Usage: check_for_errors
check_for_errors() {
    UI_DUMP=$(adb -s "$DEVICE_ID" shell "uiautomator dump /sdcard/window_dump.xml && cat /sdcard/window_dump.xml" 2>/dev/null)
    
    if echo "$UI_DUMP" | grep -qiE "error|exception|fail"; then
        echo "⚠️  Error detected on screen:"
        echo "$UI_DUMP" | grep -iE "error|exception|fail" | head -3
        return 1
    fi
    
    return 0
}

# Wait for authentication state
# Usage: wait_for_authenticated [timeout_seconds]
wait_for_authenticated() {
    local timeout="${1:-20}"
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
        
        # Check for signs of authentication
        if echo "$UI_DUMP" | grep -qiE "signed in|mood.*score|how are you feeling|enter.*mood"; then
            echo "✅ Authentication detected"
            return 0
        fi
        
        # Check for error (authentication failed)
        if echo "$UI_DUMP" | grep -qiE "error.*sign|sign.*error|authentication.*fail"; then
            echo "❌ Authentication error detected"
            return 1
        fi
        
        sleep 0.5
    done
}

# Fill form field with better state handling
# Usage: fill_form_field x y "text" [field_name]
fill_form_field() {
    local x="$1"
    local y="$2"
    local text="$3"
    local field_name="${4:-field}"
    
    echo "Filling $field_name..."
    adb -s "$DEVICE_ID" shell input tap "$x" "$y"
    sleep 0.5
    adb -s "$DEVICE_ID" shell input keyevent KEYCODE_CTRL_A
    sleep 0.2
    adb -s "$DEVICE_ID" shell input keyevent KEYCODE_DEL
    sleep 0.2
    adb -s "$DEVICE_ID" shell input text "$text"
    sleep 1
    
    # Wait for UI to update
    sleep 0.5
    echo "✅ $field_name filled"
}

# Click button and wait for state change
# Usage: click_and_wait x y "expected_state" [timeout]
click_and_wait() {
    local x="$1"
    local y="$2"
    local expected_state="$3"
    local timeout="${4:-20}"
    
    echo "Clicking button at ($x, $y) and waiting for: $expected_state"
    adb -s "$DEVICE_ID" shell input tap "$x" "$y"
    
    # Wait for loading to start (if any)
    sleep 1
    
    # Wait for expected state
    case "$expected_state" in
        "authenticated")
            wait_for_authenticated "$timeout"
            ;;
        "loading_complete")
            wait_for_loading_complete "$timeout"
            ;;
        *)
            wait_for_element "$expected_state" "$timeout"
            ;;
    esac
}

