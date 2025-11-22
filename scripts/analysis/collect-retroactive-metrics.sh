#!/usr/bin/env bash

# Collect Retroactive Metrics from Historical Data
# Attempts to derive metrics from git history, cursor database, and existing reports
# Usage: ./scripts/analysis/collect-retroactive-metrics.sh [options]
#
# Options:
#   --source git|cursor|reports|all    Data source to use (default: all)
#   --since DATE                        Only collect data since this date (YYYY-MM-DD)
#   --output-dir DIR                    Output directory (default: development-metrics/retroactive)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
OUTPUT_DIR="${METRICS_DIR}/retroactive"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Default options
SOURCE="all"
SINCE_DATE=""
OUTPUT_DIR_ARG=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --source)
            SOURCE="$2"
            shift 2
            ;;
        --since)
            SINCE_DATE="$2"
            shift 2
            ;;
        --output-dir)
            OUTPUT_DIR_ARG="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

[ -n "$OUTPUT_DIR_ARG" ] && OUTPUT_DIR="$OUTPUT_DIR_ARG"

mkdir -p "$OUTPUT_DIR"

echo "📊 Collecting Retroactive Metrics"
echo "=================================="
echo ""
echo "Sources: $SOURCE"
echo "Output: $OUTPUT_DIR"
[ -n "$SINCE_DATE" ] && echo "Since: $SINCE_DATE"
echo ""

# Function: Collect from git history
collect_from_git() {
    echo "🔍 Collecting from Git History..."
    echo ""
    
    local git_prompts="$OUTPUT_DIR/git_prompts.json"
    local temp_file=$(mktemp)
    
    # Build git log command
    if [ -n "$SINCE_DATE" ]; then
        git log --pretty=format:'%H|%ai|%an|%s|%b' --all --since="$SINCE_DATE" > "$temp_file"
    else
        git log --pretty=format:'%H|%ai|%an|%s|%b' --all > "$temp_file"
    fi
    
    # Extract commits that might contain prompts or AI-related work
    echo "  - Extracting commits..."
    > "$git_prompts"  # Initialize file
    local first=true
    
    while IFS='|' read -r hash date author subject body; do
        # Look for AI-related keywords in commit messages
        if echo "$subject $body" | grep -qiE "(prompt|ai|agent|claude|sonnet|haiku|opus|cursor|assistant)"; then
            # Create a prompt-like record
            local prompt_text="$subject"
            [ -n "$body" ] && prompt_text="$subject. $body"
            
            # Estimate task type from commit message
            local task_type="unknown"
            if echo "$subject" | grep -qiE "^(feat|feature)"; then
                task_type="feature_implementation"
            elif echo "$subject" | grep -qiE "^(fix|bug)"; then
                task_type="bug_fix"
            elif echo "$subject" | grep -qiE "^(refactor)"; then
                task_type="refactoring"
            elif echo "$subject" | grep -qiE "^(docs|doc)"; then
                task_type="documentation"
            fi
            
            # Create JSON record
            local prompt_json=$(cat <<EOF
{
  "source": "git",
  "timestamp": "$date",
  "commit_hash": "$hash",
  "author": "$author",
  "prompt": $(echo "$prompt_text" | jq -Rs .),
  "promptLength": ${#prompt_text},
  "wordCount": $(echo "$prompt_text" | wc -w | tr -d ' '),
  "taskType": "$task_type",
  "estimated": true
}
EOF
)
            
            if [ "$first" = true ]; then
                echo "[$prompt_json" > "$git_prompts"
                first=false
            else
                echo ",$prompt_json" >> "$git_prompts"
            fi
        fi
    done < "$temp_file"
    
    [ "$first" = false ] && echo "]" >> "$git_prompts"
    rm -f "$temp_file"
    
    local count=$(jq 'length' "$git_prompts" 2>/dev/null || echo "0")
    echo "  ✅ Found $count potential prompts in git history"
    echo ""
}

# Function: Collect from Cursor database
collect_from_cursor() {
    echo "🔍 Collecting from Cursor Database..."
    echo ""
    
    local cursor_db="$HOME/Library/Application Support/Cursor/User/globalStorage/state.vscdb"
    
    if [ ! -f "$cursor_db" ]; then
        echo "  ⚠️  Cursor database not found at: $cursor_db"
        echo "     Skipping Cursor database collection"
        echo ""
        return
    fi
    
    if ! command -v sqlite3 &> /dev/null; then
        echo "  ⚠️  sqlite3 not found, skipping Cursor database collection"
        echo ""
        return
    fi
    
    local cursor_output="$OUTPUT_DIR/cursor_conversations.json"
    
    # Extract conversation metadata
    echo "  - Extracting conversation metadata..."
    local participant_data=$(sqlite3 "$cursor_db" "SELECT value FROM ItemTable WHERE key = 'chat.participantNameRegistry';" 2>/dev/null || echo "")
    
    if [ -n "$participant_data" ] && command -v jq &> /dev/null; then
        # Count conversations
        local conv_count=$(echo "$participant_data" | jq -r 'to_entries | length' 2>/dev/null || echo "0")
        echo "  ✅ Found $conv_count conversations in Cursor database"
        
        # Extract conversation IDs and timestamps if available
        echo "$participant_data" | jq -r 'to_entries[] | {id: .key, data: .value}' 2>/dev/null > "$cursor_output" || true
        
        echo "  ✅ Extracted conversation metadata"
    else
        echo "  ⚠️  Could not parse participant registry (jq may be required)"
    fi
    
    echo ""
}

# Function: Collect from test automation reports
collect_from_reports() {
    echo "🔍 Collecting from Test Automation Reports..."
    echo ""
    
    local reports_dir="$PROJECT_ROOT/test-automation/test-automation/test-results/reports"
    local reports_output="$OUTPUT_DIR/test_reports_prompts.json"
    
    if [ ! -d "$reports_dir" ]; then
        echo "  ⚠️  Reports directory not found: $reports_dir"
        echo "     Skipping test reports collection"
        echo ""
        return
    fi
    
    local count=0
    find "$reports_dir" -name "cursor_report_prompt_*.md" -type f | while read -r report_file; do
        # Extract prompt from report file
        local prompt=$(grep -i "prompt:" "$report_file" | head -1 | sed 's/.*[Pp]rompt:[[:space:]]*//' || echo "")
        
        if [ -n "$prompt" ]; then
            local filename=$(basename "$report_file")
            local timestamp=$(echo "$filename" | grep -oE '[0-9]+' | head -1)
            
            # Convert timestamp to ISO format if possible
            local iso_date=""
            if [ -n "$timestamp" ] && [ ${#timestamp} -eq 13 ]; then
                # Timestamp is in milliseconds
                iso_date=$(date -r $((timestamp / 1000)) -u +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || echo "")
            fi
            
            cat >> "$reports_output" <<EOF
{
  "source": "test_reports",
  "timestamp": "${iso_date:-unknown}",
  "report_file": "$filename",
  "prompt": $(echo "$prompt" | jq -Rs .),
  "promptLength": ${#prompt},
  "wordCount": $(echo "$prompt" | wc -w | tr -d ' '),
  "taskType": "test_automation",
  "estimated": true
}
EOF
            count=$((count + 1))
        fi
    done
    
    echo "  ✅ Found $count prompts in test reports"
    echo ""
}

# Function: Estimate agent usage from prompts
estimate_agent_usage() {
    echo "🔍 Estimating Agent Usage from Collected Prompts..."
    echo ""
    
    local prompts_file="$OUTPUT_DIR/all_prompts.json"
    local usage_file="$OUTPUT_DIR/estimated_usage.json"
    
    # Combine all prompt sources
    jq -s 'flatten' "$OUTPUT_DIR"/*_prompts.json "$OUTPUT_DIR"/*prompts.json 2>/dev/null > "$prompts_file" || echo "[]" > "$prompts_file"
    
    local prompt_count=$(jq 'length' "$prompts_file" 2>/dev/null || echo "0")
    
    if [ "$prompt_count" -eq 0 ]; then
        echo "  ⚠️  No prompts found to estimate usage from"
        echo ""
        return
    fi
    
    echo "  - Estimating usage for $prompt_count prompts..."
    
    # Estimate usage for each prompt
    jq -r '.[] | @json' "$prompts_file" | while read -r prompt_json; do
        local prompt_text=$(echo "$prompt_json" | jq -r '.prompt')
        local prompt_length=$(echo "$prompt_json" | jq -r '.promptLength // (length)')
        local task_type=$(echo "$prompt_json" | jq -r '.taskType // "unknown"')
        local timestamp=$(echo "$prompt_json" | jq -r '.timestamp')
        
        # Estimate tokens (rough: ~4 chars per token)
        local input_tokens=$((prompt_length / 4))
        local output_tokens=$((input_tokens / 3))  # Typical output is ~30% of input
        
        # Estimate model based on task type
        local model_id="anthropic.claude-sonnet-4-5-20250929-v1:0"  # Default
        local model_name="claude-3-5-sonnet"
        
        # Simple heuristics for model selection
        if echo "$task_type" | grep -qiE "(bug_fix|documentation|simple)"; then
            model_id="anthropic.claude-3-5-haiku-20241022-v2:0"
            model_name="claude-3-5-haiku"
        elif echo "$task_type" | grep -qiE "(architecture|complex|security)"; then
            model_id="anthropic.claude-3-opus-20240229-v1:0"
            model_name="claude-3-opus"
        fi
        
        # Calculate costs
        local input_price=3.00
        local output_price=15.00
        case "$model_id" in
            *haiku*)
                input_price=0.80
                output_price=4.00
                ;;
            *opus*)
                input_price=15.00
                output_price=75.00
                ;;
        esac
        
        local input_cost=$(echo "scale=6; $input_tokens * $input_price / 1000000" | bc)
        local output_cost=$(echo "scale=6; $output_tokens * $output_price / 1000000" | bc)
        local total_cost=$(echo "scale=6; $input_cost + $output_cost" | bc)
        
        # Create usage record
        cat >> "$usage_file" <<EOF
{
  "source": "estimated",
  "timestamp": "$timestamp",
  "agent_company": "Anthropic",
  "agent_model": "$model_name",
  "agent_model_id": "$model_id",
  "agent_provider": "AWS Bedrock",
  "agent_region": "eu-west-1",
  "input_tokens": $input_tokens,
  "output_tokens": $output_tokens,
  "input_cost_per_million": $input_price,
  "output_cost_per_million": $output_price,
  "input_cost": $input_cost,
  "output_cost": $output_cost,
  "total_cost": $total_cost,
  "task_type": "$task_type",
  "prompt_length": $prompt_length,
  "estimated": true
}
EOF
    done
    
    local usage_count=$(jq -s 'length' "$usage_file" 2>/dev/null || echo "0")
    echo "  ✅ Estimated usage for $usage_count prompts"
    echo ""
}

# Main collection
echo "Starting retroactive data collection..."
echo ""

case "$SOURCE" in
    git)
        collect_from_git
        ;;
    cursor)
        collect_from_cursor
        ;;
    reports)
        collect_from_reports
        ;;
    all)
        collect_from_git
        collect_from_cursor
        collect_from_reports
        estimate_agent_usage
        ;;
    *)
        echo "Unknown source: $SOURCE"
        exit 1
        ;;
esac

echo "✅ Retroactive data collection complete"
echo ""
echo "Output directory: $OUTPUT_DIR"
echo ""
echo "Files created:"
ls -lh "$OUTPUT_DIR" 2>/dev/null | tail -n +2 | awk '{print "  - " $9 " (" $5 ")"}'
echo ""
echo "💡 Next steps:"
echo "  1. Review collected data: cat $OUTPUT_DIR/*.json | jq"
echo "  2. Import into metrics system (if desired)"
echo "  3. Compare with tracked data"

