#!/bin/bash

# Automatic Prompt Capture Wrapper
# This script is called automatically by agents to capture every user prompt
# Usage: ./scripts/metrics/auto-capture-prompt.sh "User's prompt text" [--model MODEL] [--task-type TYPE]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Get current session ID if available
SESSION_ID=""
if [ -f "$PROJECT_ROOT/development-metrics/sessions/.current_session" ]; then
    SESSION_ID=$(cat "$PROJECT_ROOT/development-metrics/sessions/.current_session" 2>/dev/null | head -1 || echo "")
fi

# If no session ID, try to get from active session
if [ -z "$SESSION_ID" ]; then
    # Try to find active session
    ACTIVE_SESSION=$(find "$PROJECT_ROOT/development-metrics/sessions" -name "session_*.json" -type f -exec sh -c 'jq -r "if .currentTask then .sessionId else empty end" "$1" 2>/dev/null' _ {} \; 2>/dev/null | head -1)
    if [ -n "$ACTIVE_SESSION" ]; then
        SESSION_ID="$ACTIVE_SESSION"
    fi
fi

# Build command with session ID if available
CAPTURE_CMD="$SCRIPT_DIR/capture-prompt-simple.sh"

# Add session ID if we have one
if [ -n "$SESSION_ID" ]; then
    # Pass all arguments and add session-id
    "$CAPTURE_CMD" "$@" --session-id "$SESSION_ID" 2>/dev/null || "$CAPTURE_CMD" "$@"
else
    # Just pass all arguments
    "$CAPTURE_CMD" "$@"
fi

