#!/bin/bash

# Simple Prompt Capture Helper
# Easy-to-use wrapper for capturing prompts with minimal typing
#
# Usage:
#   ./scripts/metrics/capture-prompt-simple.sh "Your prompt text"
#   ./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet
#   ./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model haiku --task bug_fix
#
# Model shortcuts:
#   --model haiku   → anthropic.claude-3-5-haiku-20241022-v2:0
#   --model sonnet  → anthropic.claude-sonnet-4-5-20250929-v1:0
#   --model opus    → anthropic.claude-3-opus-20240229-v1:0

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Parse arguments
PROMPT_TEXT=""
MODEL_SHORTCUT=""
TASK_TYPE="unknown"

# Simple argument parsing
if [ $# -eq 0 ]; then
    echo "Usage: $0 \"prompt text\" [--model haiku|sonnet|opus] [--task-type TYPE]"
    exit 1
fi

PROMPT_TEXT="$1"
shift

while [[ $# -gt 0 ]]; do
    case $1 in
        --model)
            MODEL_SHORTCUT="$2"
            shift 2
            ;;
        --task-type)
            TASK_TYPE="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Convert model shortcut to full model ID
MODEL_ID=""
case "$MODEL_SHORTCUT" in
    haiku)
        MODEL_ID="anthropic.claude-3-5-haiku-20241022-v2:0"
        ;;
    sonnet)
        MODEL_ID="anthropic.claude-sonnet-4-5-20250929-v1:0"
        ;;
    opus)
        MODEL_ID="anthropic.claude-3-opus-20240229-v1:0"
        ;;
    "")
        # No model specified - just capture prompt
        MODEL_ID=""
        ;;
    *)
        # Assume it's already a full model ID
        MODEL_ID="$MODEL_SHORTCUT"
        ;;
esac

# Use unified capture-metrics.sh
if [ -n "$MODEL_ID" ]; then
    "$SCRIPT_DIR/capture-metrics.sh" prompt "$PROMPT_TEXT" \
        --model "$MODEL_ID" \
        --task-type "$TASK_TYPE"
else
    "$SCRIPT_DIR/capture-metrics.sh" prompt "$PROMPT_TEXT"
fi

