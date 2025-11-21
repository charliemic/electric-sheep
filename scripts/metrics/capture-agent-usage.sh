#!/usr/bin/env bash

# Capture agent usage data for cost tracking
# Usage: ./scripts/metrics/capture-agent-usage.sh [options]
#
# Options:
#   --model MODEL_ID          Model identifier (required)
#   --input-tokens N          Input token count (optional, estimated if not provided)
#   --output-tokens N         Output token count (optional, estimated if not provided)
#   --prompt-length N         Prompt length in characters (for token estimation)
#   --task-type TYPE          Task type (optional)
#   --session-id ID           Session ID (optional)
#   --response-time N         Response time in seconds (optional)
#   --prompt-id ID            Link to prompt record (optional)
#   --no-supabase             Disable Supabase storage (fallback to JSON only)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Model pricing configuration (per million tokens)
# Using case statement for compatibility with bash 3.2 (macOS default)
get_model_input_price() {
    case "$1" in
        "anthropic.claude-3-5-haiku-20241022-v2:0")
            echo "0.80"
            ;;
        "anthropic.claude-sonnet-4-5-20250929-v1:0")
            echo "3.00"
            ;;
        "anthropic.claude-3-opus-20240229-v1:0")
            echo "15.00"
            ;;
        *)
            echo "3.00"  # Default
            ;;
    esac
}

get_model_output_price() {
    case "$1" in
        "anthropic.claude-3-5-haiku-20241022-v2:0")
            echo "4.00"
            ;;
        "anthropic.claude-sonnet-4-5-20250929-v1:0")
            echo "15.00"
            ;;
        "anthropic.claude-3-opus-20240229-v1:0")
            echo "75.00"
            ;;
        *)
            echo "15.00"  # Default
            ;;
    esac
}

get_model_company() {
    case "$1" in
        "anthropic.claude-3-5-haiku-20241022-v2:0"|"anthropic.claude-sonnet-4-5-20250929-v1:0"|"anthropic.claude-3-opus-20240229-v1:0")
            echo "Anthropic"
            ;;
        *)
            echo "Unknown"
            ;;
    esac
}

get_model_provider() {
    case "$1" in
        "anthropic.claude-3-5-haiku-20241022-v2:0"|"anthropic.claude-sonnet-4-5-20250929-v1:0"|"anthropic.claude-3-opus-20240229-v1:0")
            echo "AWS Bedrock"
            ;;
        *)
            echo "Unknown"
            ;;
    esac
}

get_model_region() {
    case "$1" in
        "anthropic.claude-3-5-haiku-20241022-v2:0"|"anthropic.claude-sonnet-4-5-20250929-v1:0"|"anthropic.claude-3-opus-20240229-v1:0")
            echo "eu-west-1"
            ;;
        *)
            echo "unknown"
            ;;
    esac
}

# Parse arguments
MODEL_ID=""
INPUT_TOKENS=""
OUTPUT_TOKENS=""
PROMPT_LENGTH=""
TASK_TYPE="unknown"
SESSION_ID=""
RESPONSE_TIME=""
PROMPT_ID=""
USE_SUPABASE=true

while [[ $# -gt 0 ]]; do
    case $1 in
        --model)
            MODEL_ID="$2"
            shift 2
            ;;
        --input-tokens)
            INPUT_TOKENS="$2"
            shift 2
            ;;
        --output-tokens)
            OUTPUT_TOKENS="$2"
            shift 2
            ;;
        --prompt-length)
            PROMPT_LENGTH="$2"
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
        --response-time)
            RESPONSE_TIME="$2"
            shift 2
            ;;
        --prompt-id)
            PROMPT_ID="$2"
            shift 2
            ;;
        --no-supabase)
            USE_SUPABASE=false
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate required arguments
if [ -z "$MODEL_ID" ]; then
    echo "Error: --model is required"
    echo "Usage: $0 --model MODEL_ID [options]"
    exit 1
fi

# Get model pricing and metadata
INPUT_PRICE=$(get_model_input_price "$MODEL_ID")
OUTPUT_PRICE=$(get_model_output_price "$MODEL_ID")
COMPANY=$(get_model_company "$MODEL_ID")
PROVIDER=$(get_model_provider "$MODEL_ID")
REGION=$(get_model_region "$MODEL_ID")

# Check if model is configured (default means unknown)
if [ "$COMPANY" = "Unknown" ]; then
    echo "Warning: Model $MODEL_ID not found in pricing configuration"
    echo "Using default pricing: input=\$3.00, output=\$15.00 per million tokens"
fi

# Estimate tokens if not provided
if [ -z "$INPUT_TOKENS" ]; then
    if [ -n "$PROMPT_LENGTH" ]; then
        # Rough estimation: ~4 characters per token
        INPUT_TOKENS=$((PROMPT_LENGTH / 4))
    else
        # Default estimation for unknown prompt length
        INPUT_TOKENS=500
        echo "Warning: No input tokens or prompt length provided, using default: $INPUT_TOKENS"
    fi
fi

if [ -z "$OUTPUT_TOKENS" ]; then
    # Estimate output tokens as 30-50% of input (typical for code generation)
    OUTPUT_TOKENS=$((INPUT_TOKENS / 3))
    echo "Warning: No output tokens provided, estimating: $OUTPUT_TOKENS"
fi

# Calculate costs
TOTAL_TOKENS=$((INPUT_TOKENS + OUTPUT_TOKENS))
INPUT_COST=$(echo "scale=6; $INPUT_TOKENS * $INPUT_PRICE / 1000000" | bc | sed 's/^\./0./')
OUTPUT_COST=$(echo "scale=6; $OUTPUT_TOKENS * $OUTPUT_PRICE / 1000000" | bc | sed 's/^\./0./')
TOTAL_COST=$(echo "scale=6; $INPUT_COST + $OUTPUT_COST" | bc | sed 's/^\./0./')

# Determine model name from ID
if [[ "$MODEL_ID" == *"haiku"* ]]; then
    MODEL_NAME="claude-3-5-haiku"
elif [[ "$MODEL_ID" == *"sonnet"* ]]; then
    MODEL_NAME="claude-3-5-sonnet"
elif [[ "$MODEL_ID" == *"opus"* ]]; then
    MODEL_NAME="claude-3-opus"
else
    MODEL_NAME="unknown"
fi

# Generate session ID if not provided
if [ -z "$SESSION_ID" ]; then
    SESSION_ID="session_$(date +%s)"
fi

# Source Supabase PostgREST library
LIB_DIR="$SCRIPT_DIR/../lib"
if [ -f "$LIB_DIR/supabase-postgrest.sh" ]; then
    source "$LIB_DIR/supabase-postgrest.sh"
else
    echo "Warning: supabase-postgrest.sh not found, cannot insert to Supabase"
    echo "Falling back to local JSON storage"
    USE_SUPABASE=false
fi

# Create usage record JSON (for Supabase insertion)
USAGE_JSON=$(cat <<EOF
{
  "prompt_id": ${PROMPT_ID:+"\"$PROMPT_ID\""}${PROMPT_ID:-null},
  "session_id": "$SESSION_ID",
  "agent_company": "$COMPANY",
  "agent_model": "$MODEL_NAME",
  "agent_model_id": "$MODEL_ID",
  "agent_provider": "$PROVIDER",
  "agent_region": "$REGION",
  "input_tokens": $INPUT_TOKENS,
  "output_tokens": $OUTPUT_TOKENS,
  "input_cost_per_million": $INPUT_PRICE,
  "output_cost_per_million": $OUTPUT_PRICE,
  "input_cost": $INPUT_COST,
  "output_cost": $OUTPUT_COST,
  "task_type": "$TASK_TYPE",
  "task_complexity": "unknown",
  "prompt_length": ${PROMPT_LENGTH:-null},
  "response_time_seconds": ${RESPONSE_TIME:-null},
  "success": true
}
EOF
)

# Store in Supabase (primary storage)
if [ "$USE_SUPABASE" = true ]; then
    if postgrest_insert "agent_usage" "$USAGE_JSON" "false"; then
        echo "✅ Agent usage recorded in Supabase"
    else
        echo "⚠️  Failed to insert to Supabase, check SUPABASE_URL and SUPABASE_SECRET_KEY"
        echo "   Falling back to local JSON storage"
        USE_SUPABASE=false
    fi
fi

# Fallback: Store locally as JSON only if Supabase failed (temporary)
if [ "$USE_SUPABASE" = false ]; then
    mkdir -p "$METRICS_DIR/agent-usage"
    USAGE_FILE="$METRICS_DIR/agent-usage/usage_${TIMESTAMP}.json"
    echo "$USAGE_JSON" > "$USAGE_FILE"
    echo "⚠️  Agent usage recorded locally (JSON): $USAGE_FILE"
    echo "   Note: Set SUPABASE_URL and SUPABASE_SECRET_KEY to use Supabase"
    echo "   JSON is temporary - sync to Supabase when ready"
fi

echo "Agent usage recorded:"
echo "  Model: $MODEL_NAME ($COMPANY)"
echo "  Tokens: $TOTAL_TOKENS (input: $INPUT_TOKENS, output: $OUTPUT_TOKENS)"
echo "  Cost: \$$(printf "%.6f" $TOTAL_COST)"
echo "  Task: $TASK_TYPE"

