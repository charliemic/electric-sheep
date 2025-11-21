#!/usr/bin/env bash

# Analyze cost data from tracked interactions
# Usage: ./scripts/analysis/analyze-costs.sh [options]
#
# Options:
#   --period PERIOD          Time period (last-7-days, last-30-days, all)
#   --group-by FIELD         Group by field (model, company, task-type, provider)
#   --output FORMAT          Output format (text, json, csv)
#   --source SOURCE          Data source (supabase, json, auto) - default: auto

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
COSTS_DIR="$METRICS_DIR/agent-usage"
LIB_DIR="$PROJECT_ROOT/scripts/lib"

# Default options
PERIOD="last-7-days"
GROUP_BY=""
OUTPUT_FORMAT="text"
DATA_SOURCE="auto"

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --period)
            PERIOD="$2"
            shift 2
            ;;
        --group-by)
            GROUP_BY="$2"
            shift 2
            ;;
        --output)
            OUTPUT_FORMAT="$2"
            shift 2
            ;;
        --source)
            DATA_SOURCE="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Determine data source
USE_SUPABASE=false
if [ "$DATA_SOURCE" = "supabase" ]; then
    USE_SUPABASE=true
elif [ "$DATA_SOURCE" = "json" ]; then
    USE_SUPABASE=false
elif [ "$DATA_SOURCE" = "auto" ]; then
    # Try Supabase first, fallback to JSON
    if [ -f "$LIB_DIR/supabase-postgrest.sh" ]; then
        source "$LIB_DIR/supabase-postgrest.sh"
        if postgrest_validate_env 2>/dev/null; then
            USE_SUPABASE=true
        fi
    fi
fi

# Check if JSON directory exists (for fallback)
if [ "$USE_SUPABASE" = false ] && [ ! -d "$COSTS_DIR" ]; then
    echo "Error: Costs directory not found: $COSTS_DIR"
    echo "Run capture-agent-usage.sh first to track costs"
    exit 1
fi

# Calculate date range
case $PERIOD in
    last-7-days)
        CUTOFF_DATE=$(date -u -v-7d +"%Y-%m-%d" 2>/dev/null || date -u -d "7 days ago" +"%Y-%m-%d")
        ;;
    last-30-days)
        CUTOFF_DATE=$(date -u -v-30d +"%Y-%m-%d" 2>/dev/null || date -u -d "30 days ago" +"%Y-%m-%d")
        ;;
    all)
        CUTOFF_DATE="1970-01-01"
        ;;
    *)
        echo "Error: Invalid period: $PERIOD"
        echo "Valid periods: last-7-days, last-30-days, all"
        exit 1
        ;;
esac

# Aggregate costs
TOTAL_COST=0
TOTAL_INTERACTIONS=0

# Use case statements instead of associative arrays (bash 3.2 compatibility)
# We'll use a simpler approach with temporary files or direct calculation

# Temporary files for aggregation (bash 3.2 compatible - no associative arrays)
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Aggregate data from Supabase
if [ "$USE_SUPABASE" = true ]; then
    echo "Querying Supabase for agent usage data..." >&2
    
    # Calculate date filter
    case $PERIOD in
        last-7-days)
            CUTOFF_DATE=$(date -u -v-7d +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d "7 days ago" +"%Y-%m-%dT%H:%M:%SZ")
            ;;
        last-30-days)
            CUTOFF_DATE=$(date -u -v-30d +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d "30 days ago" +"%Y-%m-%dT%H:%M:%SZ")
            ;;
        all)
            CUTOFF_DATE="1970-01-01T00:00:00Z"
            ;;
    esac
    
    # Query Supabase via PostgREST
    FILTER="created_at=gte.$CUTOFF_DATE"
    SELECT="agent_model,agent_company,agent_provider,task_type,total_cost,total_tokens,input_tokens,output_tokens"
    
    if [ -f "$LIB_DIR/supabase-postgrest.sh" ]; then
        source "$LIB_DIR/supabase-postgrest.sh"
        if postgrest_query "agent_usage" "$FILTER" "$SELECT"; then
            if [ -n "$POSTGREST_QUERY_RESULT" ] && [ "$POSTGREST_QUERY_RESULT" != "[]" ]; then
                # Process Supabase results using jq
                if command -v jq &> /dev/null; then
                    # Aggregate using jq and write to temp files
                    echo "$POSTGREST_QUERY_RESULT" | jq -r '.[] | "\(.agent_model)|\(.agent_company)|\(.agent_provider)|\(.task_type)|\(.total_cost)|\(.total_tokens)|\(.input_tokens)|\(.output_tokens)"' | while IFS='|' read -r MODEL COMPANY PROVIDER TASK_TYPE COST TOKENS INPUT OUTPUT; do
                        # Skip invalid entries
                        if [ -z "$COST" ] || [ "$COST" = "null" ] || [ "$COST" = "0" ]; then
                            continue
                        fi
                        
                        # Accumulate totals
                        TOTAL_COST=$(echo "$TOTAL_COST + $COST" | bc)
                        TOTAL_INTERACTIONS=$((TOTAL_INTERACTIONS + 1))
                        
                        # Aggregate by model (using temp files)
                        MODEL_FILE="$TEMP_DIR/model_${MODEL//\//_}"
                        if [ -f "$MODEL_FILE" ]; then
                            OLD_COST=$(cat "$MODEL_FILE" | cut -d'|' -f1)
                            OLD_COUNT=$(cat "$MODEL_FILE" | cut -d'|' -f2)
                            NEW_COST=$(echo "$OLD_COST + $COST" | bc)
                            NEW_COUNT=$((OLD_COUNT + 1))
                            echo "$NEW_COST|$NEW_COUNT" > "$MODEL_FILE"
                        else
                            echo "$COST|1" > "$MODEL_FILE"
                        fi
                        
                        # Aggregate by company
                        COMPANY_FILE="$TEMP_DIR/company_${COMPANY//\//_}"
                        if [ -f "$COMPANY_FILE" ]; then
                            OLD_COST=$(cat "$COMPANY_FILE" | cut -d'|' -f1)
                            OLD_COUNT=$(cat "$COMPANY_FILE" | cut -d'|' -f2)
                            NEW_COST=$(echo "$OLD_COST + $COST" | bc)
                            NEW_COUNT=$((OLD_COUNT + 1))
                            echo "$NEW_COST|$NEW_COUNT" > "$COMPANY_FILE"
                        else
                            echo "$COST|1" > "$COMPANY_FILE"
                        fi
                        
                        # Aggregate by task type
                        TASK_FILE="$TEMP_DIR/task_${TASK_TYPE//\//_}"
                        if [ -f "$TASK_FILE" ]; then
                            OLD_COST=$(cat "$TASK_FILE" | cut -d'|' -f1)
                            OLD_COUNT=$(cat "$TASK_FILE" | cut -d'|' -f2)
                            NEW_COST=$(echo "$OLD_COST + $COST" | bc)
                            NEW_COUNT=$((OLD_COUNT + 1))
                            echo "$NEW_COST|$NEW_COUNT" > "$TASK_FILE"
                        else
                            echo "$COST|1" > "$TASK_FILE"
                        fi
                        
                        # Aggregate by provider
                        PROVIDER_FILE="$TEMP_DIR/provider_${PROVIDER//\//_}"
                        if [ -f "$PROVIDER_FILE" ]; then
                            OLD_COST=$(cat "$PROVIDER_FILE" | cut -d'|' -f1)
                            OLD_COUNT=$(cat "$PROVIDER_FILE" | cut -d'|' -f2)
                            NEW_COST=$(echo "$OLD_COST + $COST" | bc)
                            NEW_COUNT=$((OLD_COUNT + 1))
                            echo "$NEW_COST|$NEW_COUNT" > "$PROVIDER_FILE"
                        else
                            echo "$COST|1" > "$PROVIDER_FILE"
                        fi
                    done
                else
                    echo "Warning: jq not found, cannot parse Supabase results" >&2
                    echo "Install jq or use --source json to analyze local JSON files" >&2
                    USE_SUPABASE=false
                fi
            else
                echo "No data found in Supabase for period: $PERIOD" >&2
                USE_SUPABASE=false
            fi
        else
            echo "Warning: Failed to query Supabase, falling back to JSON" >&2
            USE_SUPABASE=false
        fi
    else
        echo "Warning: supabase-postgrest.sh not found, falling back to JSON" >&2
        USE_SUPABASE=false
    fi
fi

# Fallback to JSON files
if [ "$USE_SUPABASE" = false ]; then
    # Process JSON files (existing logic)
    for COST_FILE in "$COSTS_DIR"/usage_*.json; do
        if [ ! -f "$COST_FILE" ]; then
            continue
        fi
        
        FILE_DATE=$(basename "$COST_FILE" | sed 's/usage_\([0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}\).*/\1/')
        
        # Skip files outside date range
        if [[ "$FILE_DATE" < "$CUTOFF_DATE" ]]; then
            continue
        fi
        
        # Extract data using jq if available, otherwise use grep/sed
        if command -v jq &> /dev/null; then
            # Try multiple JSON formats: Supabase format (flat) or local format (nested)
            COST=$(jq -r '.total_cost // (.input_cost + .output_cost) // .cost.totalCost // 0' "$COST_FILE" 2>/dev/null || echo "0")
            MODEL=$(jq -r '.agent_model // .agent.model // "unknown"' "$COST_FILE" 2>/dev/null || echo "unknown")
            COMPANY=$(jq -r '.agent_company // .agent.company // "unknown"' "$COST_FILE" 2>/dev/null || echo "unknown")
            TASK_TYPE=$(jq -r '.task_type // .task.type // "unknown"' "$COST_FILE" 2>/dev/null || echo "unknown")
            PROVIDER=$(jq -r '.agent_provider // .agent.provider // "unknown"' "$COST_FILE" 2>/dev/null || echo "unknown")
        else
            # Fallback to grep/sed (less reliable)
            COST=$(grep -oE '"total_cost"|"totalCost":[0-9.]+' "$COST_FILE" | grep -oE '[0-9.]+' | head -1 || echo "0")
            MODEL=$(grep -oE '"agent_model"|"model":"[^"]+"' "$COST_FILE" | cut -d'"' -f4 | head -1 || echo "unknown")
            COMPANY=$(grep -oE '"agent_company"|"company":"[^"]+"' "$COST_FILE" | cut -d'"' -f4 | head -1 || echo "unknown")
            TASK_TYPE=$(grep -oE '"task_type"|"type":"[^"]+"' "$COST_FILE" | cut -d'"' -f4 | head -1 || echo "unknown")
            PROVIDER=$(grep -oE '"agent_provider"|"provider":"[^"]+"' "$COST_FILE" | cut -d'"' -f4 | head -1 || echo "unknown")
        fi
    
    if [ -z "$COST" ] || [ "$COST" = "null" ]; then
        continue
    fi
    
    TOTAL_COST=$(echo "$TOTAL_COST + $COST" | bc)
    TOTAL_INTERACTIONS=$((TOTAL_INTERACTIONS + 1))
    
    # Aggregate by model (using temp files for bash 3.2 compatibility)
    MODEL_FILE="$COSTS_DIR/.agg_model_${MODEL//\//_}"
    if [ ! -f "$MODEL_FILE" ]; then
        echo "0|0" > "$MODEL_FILE"
    fi
    OLD_VAL=$(cut -d'|' -f1 "$MODEL_FILE")
    OLD_COUNT=$(cut -d'|' -f2 "$MODEL_FILE")
    NEW_VAL=$(echo "$OLD_VAL + $COST" | bc)
    NEW_COUNT=$((OLD_COUNT + 1))
    echo "$NEW_VAL|$NEW_COUNT" > "$MODEL_FILE"
    
    # Aggregate by company
    COMPANY_FILE="$COSTS_DIR/.agg_company_${COMPANY//\//_}"
    if [ ! -f "$COMPANY_FILE" ]; then
        echo "0|0" > "$COMPANY_FILE"
    fi
    OLD_VAL=$(cut -d'|' -f1 "$COMPANY_FILE")
    OLD_COUNT=$(cut -d'|' -f2 "$COMPANY_FILE")
    NEW_VAL=$(echo "$OLD_VAL + $COST" | bc)
    NEW_COUNT=$((OLD_COUNT + 1))
    echo "$NEW_VAL|$NEW_COUNT" > "$COMPANY_FILE"
    
    # Aggregate by task type
    TASK_FILE="$COSTS_DIR/.agg_task_${TASK_TYPE//\//_}"
    if [ ! -f "$TASK_FILE" ]; then
        echo "0|0" > "$TASK_FILE"
    fi
    OLD_VAL=$(cut -d'|' -f1 "$TASK_FILE")
    OLD_COUNT=$(cut -d'|' -f2 "$TASK_FILE")
    NEW_VAL=$(echo "$OLD_VAL + $COST" | bc)
    NEW_COUNT=$((OLD_COUNT + 1))
    echo "$NEW_VAL|$NEW_COUNT" > "$TASK_FILE"
    
    # Aggregate by provider
    PROVIDER_FILE="$COSTS_DIR/.agg_provider_${PROVIDER//\//_}"
    if [ ! -f "$PROVIDER_FILE" ]; then
        echo "0|0" > "$PROVIDER_FILE"
    fi
    OLD_VAL=$(cut -d'|' -f1 "$PROVIDER_FILE")
    OLD_COUNT=$(cut -d'|' -f2 "$PROVIDER_FILE")
    NEW_VAL=$(echo "$OLD_VAL + $COST" | bc)
    NEW_COUNT=$((OLD_COUNT + 1))
    echo "$NEW_VAL|$NEW_COUNT" > "$PROVIDER_FILE"
    done
fi

# Calculate average cost
if [ $TOTAL_INTERACTIONS -gt 0 ]; then
    AVG_COST=$(echo "scale=4; $TOTAL_COST / $TOTAL_INTERACTIONS" | bc)
else
    AVG_COST=0
fi

# Output results
if [ "$OUTPUT_FORMAT" = "json" ]; then
    # JSON output
    echo "{"
    echo "  \"period\": \"$PERIOD\","
    echo "  \"totalCost\": $TOTAL_COST,"
    echo "  \"totalInteractions\": $TOTAL_INTERACTIONS,"
    echo "  \"averageCostPerInteraction\": $AVG_COST"
    echo "}"
elif [ "$OUTPUT_FORMAT" = "csv" ]; then
    # CSV output
    echo "Field,Value,Cost,Interactions,Percentage"
    echo "Total,,$TOTAL_COST,$TOTAL_INTERACTIONS,100.0"
    
    if [ "$GROUP_BY" = "model" ] || [ -z "$GROUP_BY" ]; then
        for MODEL_FILE in "$COSTS_DIR"/.agg_model_*; do
            if [ ! -f "$MODEL_FILE" ]; then
                continue
            fi
            MODEL=$(basename "$MODEL_FILE" | sed 's/^\.agg_model_//')
            COST_VAL=$(cut -d'|' -f1 "$MODEL_FILE")
            COUNT=$(cut -d'|' -f2 "$MODEL_FILE")
            PERCENTAGE=$(echo "scale=2; $COST_VAL * 100 / $TOTAL_COST" | bc)
            echo "Model,$MODEL,$COST_VAL,$COUNT,$PERCENTAGE"
        done
    fi
else
    # Text output (default)
    echo "Cost Analysis Report - $PERIOD"
    echo "===================================="
    echo ""
    echo "Total Cost: \$$(printf "%.2f" $TOTAL_COST)"
    echo "Total Interactions: $TOTAL_INTERACTIONS"
    echo "Average Cost per Interaction: \$$(printf "%.4f" $AVG_COST)"
    echo ""
    
    if [ "$GROUP_BY" = "model" ] || [ -z "$GROUP_BY" ]; then
        echo "By Model:"
        for MODEL_FILE in "$COSTS_DIR"/.agg_model_*; do
            if [ ! -f "$MODEL_FILE" ]; then
                continue
            fi
            MODEL=$(basename "$MODEL_FILE" | sed 's/^\.agg_model_//')
            COST_VAL=$(cut -d'|' -f1 "$MODEL_FILE")
            COUNT=$(cut -d'|' -f2 "$MODEL_FILE")
            PERCENTAGE=$(echo "scale=1; $COST_VAL * 100 / $TOTAL_COST" | bc)
            echo "  - $MODEL: \$$(printf "%.2f" $COST_VAL) ($PERCENTAGE%) - $COUNT interactions"
        done
        echo ""
    fi
    
    if [ "$GROUP_BY" = "company" ] || [ -z "$GROUP_BY" ]; then
        echo "By Company:"
        for COMPANY_FILE in "$COSTS_DIR"/.agg_company_*; do
            if [ ! -f "$COMPANY_FILE" ]; then
                continue
            fi
            COMPANY=$(basename "$COMPANY_FILE" | sed 's/^\.agg_company_//')
            COST_VAL=$(cut -d'|' -f1 "$COMPANY_FILE")
            COUNT=$(cut -d'|' -f2 "$COMPANY_FILE")
            PERCENTAGE=$(echo "scale=1; $COST_VAL * 100 / $TOTAL_COST" | bc)
            echo "  - $COMPANY: \$$(printf "%.2f" $COST_VAL) ($PERCENTAGE%) - $COUNT interactions"
        done
        echo ""
    fi
    
    if [ "$GROUP_BY" = "task-type" ] || [ -z "$GROUP_BY" ]; then
        echo "By Task Type:"
        for TASK_FILE in "$COSTS_DIR"/.agg_task_*; do
            if [ ! -f "$TASK_FILE" ]; then
                continue
            fi
            TASK_TYPE=$(basename "$TASK_FILE" | sed 's/^\.agg_task_//')
            COST_VAL=$(cut -d'|' -f1 "$TASK_FILE")
            COUNT=$(cut -d'|' -f2 "$TASK_FILE")
            PERCENTAGE=$(echo "scale=1; $COST_VAL * 100 / $TOTAL_COST" | bc)
            echo "  - $TASK_TYPE: \$$(printf "%.2f" $COST_VAL) ($PERCENTAGE%) - $COUNT interactions"
        done
        echo ""
    fi
    
    if [ "$GROUP_BY" = "provider" ] || [ -z "$GROUP_BY" ]; then
        echo "By Provider:"
        for PROVIDER_FILE in "$COSTS_DIR"/.agg_provider_*; do
            if [ ! -f "$PROVIDER_FILE" ]; then
                continue
            fi
            PROVIDER=$(basename "$PROVIDER_FILE" | sed 's/^\.agg_provider_//')
            COST_VAL=$(cut -d'|' -f1 "$PROVIDER_FILE")
            COUNT=$(cut -d'|' -f2 "$PROVIDER_FILE")
            PERCENTAGE=$(echo "scale=1; $COST_VAL * 100 / $TOTAL_COST" | bc)
            echo "  - $PROVIDER: \$$(printf "%.2f" $COST_VAL) ($PERCENTAGE%) - $COUNT interactions"
        done
    fi
    
    # Clean up temp aggregation files
    rm -f "$COSTS_DIR"/.agg_*
fi

