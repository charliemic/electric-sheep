#!/usr/bin/env bash

# Extract full agent usage from Cursor's database
# Analyzes chat history to estimate actual usage
# Usage: ./scripts/analysis/extract-cursor-usage.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
COSTS_DIR="$METRICS_DIR/agent-usage"
CURSOR_DB="$HOME/Library/Application Support/Cursor/User/globalStorage/state.vscdb"
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

# Check if sqlite3 is available
if ! command -v sqlite3 &> /dev/null; then
    echo "Error: sqlite3 is required but not found"
    echo "Install with: brew install sqlite3"
    exit 1
fi

# Check if Cursor database exists
if [ ! -f "$CURSOR_DB" ]; then
    echo "Error: Cursor database not found at: $CURSOR_DB"
    exit 1
fi

echo "📊 Extracting Full Agent Usage from Cursor Database"
echo "====================================================="
echo ""

# Extract all chat-related keys
echo "Extracting chat conversation data..."
sqlite3 "$CURSOR_DB" "SELECT key FROM ItemTable WHERE key LIKE 'chat.%' OR key LIKE 'cursor.%' OR key LIKE '%conversation%' OR key LIKE '%thread%';" > "$TEMP_DIR/chat_keys.txt" 2>/dev/null || true

CHAT_COUNT=$(wc -l < "$TEMP_DIR/chat_keys.txt" | tr -d ' ')
echo "Found $CHAT_COUNT chat-related entries"
echo ""

# Extract participant registry (contains conversation metadata)
echo "Analyzing chat participant registry..."
PARTICIPANT_DATA=$(sqlite3 "$CURSOR_DB" "SELECT value FROM ItemTable WHERE key = 'chat.participantNameRegistry';" 2>/dev/null || echo "")

if [ -n "$PARTICIPANT_DATA" ] && command -v jq &> /dev/null; then
    echo "$PARTICIPANT_DATA" | jq -r 'keys[]' 2>/dev/null | wc -l | xargs echo "  Conversations found:"
    echo ""
fi

# Count chat panels (each represents a conversation)
# Note: This counts ALL panels ever created, not just recent ones
CHAT_PANELS=$(sqlite3 "$CURSOR_DB" "SELECT COUNT(*) FROM ItemTable WHERE key LIKE 'workbench.panel.composerChatViewPane.%';" 2>/dev/null || echo "0")
echo "Total chat panels (all time): $CHAT_PANELS"
echo ""

# Get actual conversation threads from participant registry
if [ -n "$PARTICIPANT_DATA" ] && command -v jq &> /dev/null; then
    CONVERSATION_COUNT=$(echo "$PARTICIPANT_DATA" | jq -r 'to_entries | length' 2>/dev/null || echo "0")
    echo "Active conversation threads: $CONVERSATION_COUNT"
    echo ""
    
    # Use conversation count instead of panel count (more accurate)
    CHAT_PANELS=$CONVERSATION_COUNT
fi

# Try to extract actual conversation data
echo "Extracting conversation content..."
sqlite3 "$CURSOR_DB" "SELECT key, LENGTH(value) as size FROM ItemTable WHERE key LIKE 'chat.%' AND size > 100 ORDER BY size DESC LIMIT 20;" > "$TEMP_DIR/large_chats.txt" 2>/dev/null || true

LARGE_CHATS=$(wc -l < "$TEMP_DIR/large_chats.txt" | tr -d ' ')
echo "Found $LARGE_CHATS conversations with significant data"
echo ""

# Estimate usage from conversation count
# Each conversation typically has multiple messages (prompt + response)
# Estimate: 1 conversation = 2-5 interactions on average
ESTIMATED_INTERACTIONS=$((CHAT_PANELS * 3))
echo "=== Usage Estimation ==="
echo ""
echo "Based on chat panels:"
echo "  - Conversations: $CHAT_PANELS"
echo "  - Estimated interactions: ~$ESTIMATED_INTERACTIONS (2-5 per conversation)"
echo ""

# Estimate costs (assuming mix of models)
# Typical: 60% Sonnet, 30% Haiku, 10% Opus
SONNET_COUNT=$((ESTIMATED_INTERACTIONS * 60 / 100))
HAIKU_COUNT=$((ESTIMATED_INTERACTIONS * 30 / 100))
OPUS_COUNT=$((ESTIMATED_INTERACTIONS * 10 / 100))

# Average costs per interaction
SONNET_AVG=0.03
HAIKU_AVG=0.001
OPUS_AVG=0.15

SONNET_COST=$(echo "scale=2; $SONNET_COUNT * $SONNET_AVG" | bc)
HAIKU_COST=$(echo "scale=2; $HAIKU_COUNT * $HAIKU_AVG" | bc)
OPUS_COST=$(echo "scale=2; $OPUS_COUNT * $OPUS_AVG" | bc)
TOTAL_ESTIMATED=$(echo "scale=2; $SONNET_COST + $HAIKU_COST + $OPUS_COST" | bc)

echo "Estimated costs (last 7 days):"
echo "  - Sonnet: ~\$$SONNET_COST ($SONNET_COUNT interactions)"
echo "  - Haiku: ~\$$HAIKU_COST ($HAIKU_COUNT interactions)"
echo "  - Opus: ~\$$OPUS_COST ($OPUS_COUNT interactions)"
echo "  - Total Estimated: ~\$$TOTAL_ESTIMATED"
echo ""

# Compare with tracked data
TRACKED_COST=$(./scripts/analysis/analyze-costs.sh --period last-7-days --source json 2>/dev/null | grep "Total Cost:" | grep -oE '\$[0-9.]+' | sed 's/\$//' || echo "0")
TRACKED_INTERACTIONS=$(find "$COSTS_DIR" -type f -name "*.json" -mtime -7 2>/dev/null | wc -l | tr -d ' ')

echo "=== Comparison ==="
echo ""
echo "Tracked (actual):"
echo "  - Interactions: $TRACKED_INTERACTIONS"
echo "  - Cost: \$$TRACKED_COST"
echo ""
echo "Estimated (from Cursor database):"
echo "  - Interactions: ~$ESTIMATED_INTERACTIONS"
echo "  - Cost: ~\$$TOTAL_ESTIMATED"
echo ""
echo "Gap:"
GAP=$(echo "scale=2; $TOTAL_ESTIMATED - $TRACKED_COST" | bc)
echo "  - Missing: ~\$$GAP (~$((ESTIMATED_INTERACTIONS - TRACKED_INTERACTIONS)) interactions)"
echo ""

echo "⚠️  Note: This is an estimation based on conversation count"
echo "   Actual usage may vary. To get precise data, use the"
echo "   tracking system going forward."
echo ""

