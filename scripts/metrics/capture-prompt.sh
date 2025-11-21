#!/bin/bash

# Capture user prompt/request for metrics tracking
# Usage: ./scripts/metrics/capture-prompt.sh "prompt text" [--model MODEL_ID] [--task-type TYPE]
#
# Options:
#   --model MODEL_ID          Model identifier (for agent usage tracking)
#   --task-type TYPE          Task type (for agent usage tracking)
#   --session-id ID           Session ID (for linking prompts and agent usage)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Parse arguments
PROMPT_TEXT=""
MODEL_ID=""
TASK_TYPE=""
SESSION_ID=""

# Simple argument parsing (prompt text is first positional arg)
if [ $# -eq 0 ]; then
    echo "Usage: $0 \"prompt text\" [--model MODEL_ID] [--task-type TYPE] [--session-id ID]"
    exit 1
fi

PROMPT_TEXT="$1"
shift

while [[ $# -gt 0 ]]; do
    case $1 in
        --model)
            MODEL_ID="$2"
            shift 2
            ;;
        --task-type)
            TASK_TYPE="$2"
            shift 2
            ;;
        --session-id)
            SESSION_ID="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Generate session ID if not provided
if [ -z "$SESSION_ID" ]; then
    SESSION_ID="session_$(date +%s)"
fi

# Create prompt record
PROMPT_JSON=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "prompt": $(echo "$PROMPT_TEXT" | jq -Rs .),
  "promptLength": ${#PROMPT_TEXT},
  "wordCount": $(echo "$PROMPT_TEXT" | wc -w | tr -d ' '),
  "sessionId": "$SESSION_ID"
}
EOF
)

# Store prompt
PROMPT_FILE="$METRICS_DIR/prompts/prompt_${TIMESTAMP}.json"
echo "$PROMPT_JSON" > "$PROMPT_FILE"
PROMPT_ID="prompt_${TIMESTAMP}"

echo "Prompt recorded: $TIMESTAMP"

# If model is provided, also capture agent usage
if [ -n "$MODEL_ID" ]; then
    echo "Capturing agent usage for model: $MODEL_ID"
    "$SCRIPT_DIR/capture-agent-usage.sh" \
        --model "$MODEL_ID" \
        --prompt-length ${#PROMPT_TEXT} \
        --task-type "${TASK_TYPE:-unknown}" \
        --session-id "$SESSION_ID" \
        --prompt-id "$PROMPT_ID"
fi

