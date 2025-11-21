#!/bin/bash

# Track session scope and detect scope creep
# Usage: ./scripts/track-session-scope.sh [action] [args]
# Actions: start, update, check, report

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
SESSIONS_DIR="$METRICS_DIR/sessions"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Ensure directories exist
mkdir -p "$SESSIONS_DIR"

# Get or create session ID
SESSION_ID_FILE="$SESSIONS_DIR/.current-session-id"
if [ -f "$SESSION_ID_FILE" ]; then
    SESSION_ID=$(cat "$SESSION_ID_FILE")
else
    SESSION_ID="session_$(date +%s)"
    echo "$SESSION_ID" > "$SESSION_ID_FILE"
fi

SESSION_FILE="$SESSIONS_DIR/${SESSION_ID}.json"

# Initialize session file if it doesn't exist
if [ ! -f "$SESSION_FILE" ]; then
    cat > "$SESSION_FILE" <<EOF
{
  "sessionId": "$SESSION_ID",
  "startTime": "$TIMESTAMP",
  "originalTask": "",
  "originalFileCount": 0,
  "originalComplexity": "unknown",
  "currentTask": "",
  "currentFileCount": 0,
  "currentComplexity": "unknown",
  "tasksCompleted": [],
  "filesModified": [],
  "contextSwitches": 0,
  "lastUpdate": "$TIMESTAMP"
}
EOF
fi

# Functions
start_session() {
    local task="$1"
    local file_count="$2"
    local complexity="${3:-low}"
    
    # Count files if not provided
    if [ -z "$file_count" ]; then
        file_count=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ' || echo "0")
    fi
    
    cat > "$SESSION_FILE" <<EOF
{
  "sessionId": "$SESSION_ID",
  "startTime": "$TIMESTAMP",
  "originalTask": "$task",
  "originalFileCount": $file_count,
  "originalComplexity": "$complexity",
  "currentTask": "$task",
  "currentFileCount": $file_count,
  "currentComplexity": "$complexity",
  "tasksCompleted": [],
  "filesModified": [],
  "contextSwitches": 0,
  "lastUpdate": "$TIMESTAMP"
}
EOF
    
    echo "✅ Session started: $SESSION_ID"
    echo "   Task: $task"
    echo "   Files: $file_count"
    echo "   Complexity: $complexity"
}

update_session() {
    local task="$1"
    
    # Read current session
    local current_task=$(jq -r '.currentTask' "$SESSION_FILE")
    local context_switches=$(jq -r '.contextSwitches' "$SESSION_FILE")
    
    # Detect context switch
    if [ "$current_task" != "$task" ] && [ "$current_task" != "" ]; then
        context_switches=$((context_switches + 1))
    fi
    
    # Count modified files
    local file_count=$(git status --porcelain 2>/dev/null | grep -E '^[AM]' | wc -l | tr -d ' ' || echo "0")
    local all_files=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ' || echo "0")
    
    # Update session
    jq \
        --arg task "$task" \
        --argjson file_count "$file_count" \
        --argjson context_switches "$context_switches" \
        --arg timestamp "$TIMESTAMP" \
        '.currentTask = $task |
         .currentFileCount = $file_count |
         .contextSwitches = $context_switches |
         .lastUpdate = $timestamp' \
        "$SESSION_FILE" > "${SESSION_FILE}.tmp" && mv "${SESSION_FILE}.tmp" "$SESSION_FILE"
    
    echo "✅ Session updated: $SESSION_ID"
}

check_scope_creep() {
    # Read session data
    local original_file_count=$(jq -r '.originalFileCount' "$SESSION_FILE")
    local current_file_count=$(jq -r '.currentFileCount' "$SESSION_FILE")
    local original_task=$(jq -r '.originalTask' "$SESSION_FILE")
    local current_task=$(jq -r '.currentTask' "$SESSION_FILE")
    local context_switches=$(jq -r '.contextSwitches' "$SESSION_FILE")
    local start_time=$(jq -r '.startTime' "$SESSION_FILE")
    local original_complexity=$(jq -r '.originalComplexity' "$SESSION_FILE")
    
    # Calculate duration (in hours)
    local start_epoch=$(date -j -f "%Y-%m-%dT%H:%M:%SZ" "$start_time" +%s 2>/dev/null || date -d "$start_time" +%s 2>/dev/null || echo "0")
    local current_epoch=$(date +%s)
    local duration_seconds=$((current_epoch - start_epoch))
    # Use awk for floating point division (more portable than bc)
    local duration_hours=$(awk "BEGIN {printf \"%.2f\", $duration_seconds / 3600}")
    
    # Calculate metrics for context
    local file_growth=$((current_file_count - original_file_count))
    
    # Output context for AI evaluation
    echo "📊 Scope Creep Analysis (AI-Based Evaluation)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Session: $SESSION_ID"
    echo "Duration: ${duration_hours} hours"
    echo ""
    echo "Original Scope:"
    echo "  Task: $original_task"
    echo "  Files: $original_file_count"
    echo "  Complexity: $original_complexity"
    echo ""
    echo "Current Scope:"
    echo "  Task: $current_task"
    echo "  Files: $current_file_count"
    echo "  Context Switches: $context_switches"
    echo "  File Growth: +$file_growth files"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🤖 AI Evaluation Context:"
    echo ""
    echo "Please evaluate whether this session has experienced scope creep:"
    echo ""
    echo "Evaluation Questions:"
    echo "1. Are the new tasks related to the original task or unrelated?"
    echo "2. Is the expansion natural progression or scope creep?"
    echo "3. Are additional files necessary for the original task or tangential?"
    echo "4. Has the session become unfocused with multiple unrelated concerns?"
    echo "5. Would a new chat session help maintain focus and clarity?"
    echo "6. Is the accumulated context making responses slower or less accurate?"
    echo "7. Is the session still productive or becoming unwieldy?"
    echo ""
    echo "Based on this holistic evaluation, determine:"
    echo "- NO_SCOPE_CREEP: Expansion is natural, related, and manageable"
    echo "- MILD_SCOPE_CREEP: Some expansion, but still focused and manageable"
    echo "- MODERATE_SCOPE_CREEP: Significant expansion, consider new chat session"
    echo "- SEVERE_SCOPE_CREEP: Major expansion, strongly recommend new chat session"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "💡 Note: The Cursor AI agent will automatically evaluate this context"
    echo "   and provide recommendations based on understanding the relationship"
    echo "   between tasks, not just metrics."
    echo ""
    echo "To start a new chat session:"
    echo "  1. Commit current work: git commit -m 'WIP: [description]'"
    echo "  2. Click 'New Chat' in Cursor or press Cmd+L (Mac) / Ctrl+L (Windows/Linux)"
    echo "  3. Reference: 'Continuing from [previous task]'"
    
    # Save context to session file for AI evaluation
    jq \
        --argjson file_growth "$file_growth" \
        --argjson duration "$duration_hours" \
        '.fileGrowth = $file_growth |
         .durationHours = $duration |
         .lastEvaluation = "'"$TIMESTAMP"'"' \
        "$SESSION_FILE" > "${SESSION_FILE}.tmp" && mv "${SESSION_FILE}.tmp" "$SESSION_FILE"
}

report_session() {
    echo "📋 Session Report: $SESSION_ID"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    cat "$SESSION_FILE" | jq '.'
}

# Main command handling
case "${1:-check}" in
    start)
        start_session "${2:-}" "${3:-}" "${4:-}"
        ;;
    update)
        update_session "${2:-}"
        ;;
    check)
        check_scope_creep
        ;;
    report)
        report_session
        ;;
    *)
        echo "Usage: $0 [action] [args]"
        echo ""
        echo "Actions:"
        echo "  start [task] [file_count] [complexity]  - Start new session"
        echo "  update [task]                          - Update current task"
        echo "  check                                  - Check for scope creep (default)"
        echo "  report                                 - Show full session report"
        echo ""
        echo "Examples:"
        echo "  $0 start 'Fix login bug' 1 low"
        echo "  $0 update 'Fix login bug and add error handling'"
        echo "  $0 check"
        echo "  $0 report"
        exit 1
        ;;
esac

