#!/usr/bin/env bash

# Import historical agent usage from Cursor's chat history
# This script attempts to extract and estimate usage from Cursor's database
# Usage: ./scripts/analysis/import-cursor-history.sh [options]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
COSTS_DIR="$METRICS_DIR/agent-usage"
CURSOR_DB="$HOME/Library/Application Support/Cursor/User/globalStorage/state.vscdb"

# Check if sqlite3 is available
if ! command -v sqlite3 &> /dev/null; then
    echo "Error: sqlite3 is required but not found"
    echo "Install with: brew install sqlite3"
    exit 1
fi

# Check if Cursor database exists
if [ ! -f "$CURSOR_DB" ]; then
    echo "Warning: Cursor database not found at: $CURSOR_DB"
    echo "Cannot import historical data"
    exit 1
fi

echo "📊 Importing historical agent usage from Cursor database..."
echo ""

# Extract chat/conversation keys from database
echo "Extracting chat history keys..."
KEYS=$(sqlite3 "$CURSOR_DB" "SELECT key FROM ItemTable WHERE key LIKE '%chat%' OR key LIKE '%conversation%' OR key LIKE '%agent%' LIMIT 100;" 2>/dev/null || echo "")

if [ -z "$KEYS" ]; then
    echo "No chat history found in Cursor database"
    exit 0
fi

echo "Found chat history entries"
echo ""

# Note: Cursor's database structure is complex and may not contain direct usage data
# This is a placeholder for future implementation when we understand the structure better
echo "⚠️  Note: Cursor's database structure is complex"
echo "   Direct usage data may not be available in the database"
echo "   This script is a placeholder for future implementation"
echo ""
echo "To track historical usage:"
echo "  1. Use the capture-agent-usage.sh script going forward"
echo "  2. Configure Supabase to store all future usage"
echo "  3. Historical data may need manual estimation or import"
echo ""

# For now, just report what we found
echo "Found chat-related keys in database:"
echo "$KEYS" | head -10
echo ""

echo "✅ Import script ready (implementation pending database structure analysis)"

