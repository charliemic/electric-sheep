#!/bin/bash

# Unified Metrics Capture Helper
# Simple, feature-rich metrics collection for prompts, agent usage, and sessions
#
# Usage:
#   ./scripts/metrics/capture-metrics.sh prompt "Your prompt text" [--model MODEL] [--task-type TYPE]
#   ./scripts/metrics/capture-metrics.sh agent --model MODEL --input-tokens N --output-tokens N
#   ./scripts/metrics/capture-metrics.sh session start "Task description"
#   ./scripts/metrics/capture-metrics.sh session update
#   ./scripts/metrics/capture-metrics.sh all  # Capture all available metrics

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"

# Ensure metrics directories exist
mkdir -p "$METRICS_DIR"/{prompts,agent-usage,sessions,complexity,tests,builds,accessibility}

# Get current session ID if available
SESSION_ID_FILE="$METRICS_DIR/sessions/.current-session-id"
if [ -f "$SESSION_ID_FILE" ]; then
    CURRENT_SESSION_ID=$(cat "$SESSION_ID_FILE")
else
    CURRENT_SESSION_ID=""
fi

# Function: Capture prompt
capture_prompt() {
    local prompt_text="$1"
    shift
    
    local model_id=""
    local task_type="unknown"
    local session_id="$CURRENT_SESSION_ID"
    
    # Parse remaining arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --model)
                model_id="$2"
                shift 2
                ;;
            --task-type)
                task_type="$2"
                shift 2
                ;;
            --session-id)
                session_id="$2"
                shift 2
                ;;
            *)
                echo "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Use capture-prompt.sh
    if [ -n "$model_id" ]; then
        "$SCRIPT_DIR/capture-prompt.sh" "$prompt_text" \
            --model "$model_id" \
            --task-type "$task_type" \
            --session-id "${session_id:-session_$(date +%s)}"
    else
        "$SCRIPT_DIR/capture-prompt.sh" "$prompt_text" \
            --session-id "${session_id:-session_$(date +%s)}"
    fi
}

# Function: Capture agent usage
capture_agent() {
    local model_id=""
    local input_tokens=""
    local output_tokens=""
    local task_type="unknown"
    local session_id="$CURRENT_SESSION_ID"
    local prompt_length=""
    local response_time=""
    
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --model)
                model_id="$2"
                shift 2
                ;;
            --input-tokens)
                input_tokens="$2"
                shift 2
                ;;
            --output-tokens)
                output_tokens="$2"
                shift 2
                ;;
            --task-type)
                task_type="$2"
                shift 2
                ;;
            --session-id)
                session_id="$2"
                shift 2
                ;;
            --prompt-length)
                prompt_length="$2"
                shift 2
                ;;
            --response-time)
                response_time="$2"
                shift 2
                ;;
            *)
                echo "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    if [ -z "$model_id" ]; then
        echo "Error: --model is required for agent usage tracking"
        exit 1
    fi
    
    # Build command
    local cmd="$SCRIPT_DIR/capture-agent-usage.sh --model \"$model_id\" --task-type \"$task_type\""
    
    [ -n "$input_tokens" ] && cmd="$cmd --input-tokens \"$input_tokens\""
    [ -n "$output_tokens" ] && cmd="$cmd --output-tokens \"$output_tokens\""
    [ -n "$prompt_length" ] && cmd="$cmd --prompt-length \"$prompt_length\""
    [ -n "$response_time" ] && cmd="$cmd --response-time \"$response_time\""
    [ -n "$session_id" ] && cmd="$cmd --session-id \"$session_id\""
    
    eval "$cmd"
}

# Function: Manage session
capture_session() {
    local action="$1"
    shift
    
    case "$action" in
        start)
            local task_description="$1"
            if [ -z "$task_description" ]; then
                echo "Error: Task description required for session start"
                echo "Usage: $0 session start \"Task description\""
                exit 1
            fi
            "$SCRIPT_DIR/../track-session-scope.sh" start "$task_description"
            ;;
        update)
            "$SCRIPT_DIR/../track-session-scope.sh" update
            ;;
        check)
            "$SCRIPT_DIR/../track-session-scope.sh" check
            ;;
        report)
            "$SCRIPT_DIR/../track-session-scope.sh" report
            ;;
        *)
            echo "Unknown session action: $action"
            echo "Usage: $0 session [start|update|check|report]"
            exit 1
            ;;
    esac
}

# Function: Capture all metrics
capture_all() {
    echo "📊 Capturing all available metrics..."
    
    # Capture complexity metrics
    if [ -f "$SCRIPT_DIR/capture-complexity-metrics.sh" ]; then
        echo "  - Code complexity..."
        "$SCRIPT_DIR/capture-complexity-metrics.sh" > /dev/null 2>&1 || true
    fi
    
    # Capture accessibility metrics
    if [ -f "$SCRIPT_DIR/capture-accessibility-metrics.sh" ]; then
        echo "  - Accessibility..."
        "$SCRIPT_DIR/capture-accessibility-metrics.sh" > /dev/null 2>&1 || true
    fi
    
    # Note: Build and test metrics should be captured during their execution
    # via gradle-wrapper-build.sh and gradle-wrapper-test.sh
    
    echo "✅ Metrics collection complete"
}

# Main command routing
case "${1:-}" in
    prompt)
        shift
        if [ $# -eq 0 ]; then
            echo "Error: Prompt text required"
            echo "Usage: $0 prompt \"Your prompt text\" [--model MODEL] [--task-type TYPE]"
            exit 1
        fi
        capture_prompt "$@"
        ;;
    agent)
        shift
        capture_agent "$@"
        ;;
    session)
        shift
        capture_session "$@"
        ;;
    all)
        capture_all
        ;;
    *)
        echo "Unified Metrics Capture Helper"
        echo ""
        echo "Usage:"
        echo "  $0 prompt \"Your prompt text\" [--model MODEL] [--task-type TYPE]"
        echo "  $0 agent --model MODEL --input-tokens N --output-tokens N [--task-type TYPE]"
        echo "  $0 session start \"Task description\""
        echo "  $0 session update"
        echo "  $0 session check"
        echo "  $0 session report"
        echo "  $0 all  # Capture all available metrics"
        echo ""
        echo "Examples:"
        echo "  # Capture a prompt"
        echo "  $0 prompt \"Implement user authentication\""
        echo ""
        echo "  # Capture prompt with agent usage"
        echo "  $0 prompt \"Fix login bug\" --model \"anthropic.claude-sonnet-4-5-20250929-v1:0\" --task-type \"bug_fix\""
        echo ""
        echo "  # Capture agent usage separately"
        echo "  $0 agent --model \"anthropic.claude-3-5-haiku-20241022-v2:0\" --input-tokens 1000 --output-tokens 500"
        echo ""
        echo "  # Start session tracking"
        echo "  $0 session start \"Implement new feature\""
        echo ""
        echo "  # Capture all metrics"
        echo "  $0 all"
        exit 1
        ;;
esac

