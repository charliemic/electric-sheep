#!/bin/bash

# Capture user prompt/request for metrics tracking
# Usage: ./scripts/metrics/capture-prompt.sh "prompt text"

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

PROMPT_TEXT="${1:-}"

if [ -z "$PROMPT_TEXT" ]; then
    echo "Usage: $0 \"prompt text\""
    exit 1
fi

# Create prompt record
PROMPT_JSON=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "prompt": $(echo "$PROMPT_TEXT" | jq -Rs .),
  "promptLength": ${#PROMPT_TEXT},
  "wordCount": $(echo "$PROMPT_TEXT" | wc -w | tr -d ' ')
}
EOF
)

# Store prompt
echo "$PROMPT_JSON" > "$METRICS_DIR/prompts/prompt_${TIMESTAMP}.json"

echo "Prompt recorded: $TIMESTAMP"

