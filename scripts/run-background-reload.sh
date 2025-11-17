#!/bin/bash

# Run the continuous build script in the background
# This will continue running even after the shell session ends
# Usage: ./scripts/run-background-reload.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

# Check if already running
if pgrep -f "continuous-build.sh" > /dev/null; then
    echo "⚠️  Continuous build is already running"
    echo "   To stop it, run: pkill -f continuous-build.sh"
    exit 1
fi

# Run in background with nohup
echo "🚀 Starting continuous build in background..."
echo "   Logs will be written to: $PROJECT_DIR/.build-watch.log"
echo "   To stop it, run: pkill -f continuous-build.sh"
echo "   To view logs: tail -f $PROJECT_DIR/.build-watch.log"
echo ""

nohup "$SCRIPT_DIR/continuous-build.sh" > "$PROJECT_DIR/.build-watch.log" 2>&1 &

echo "✅ Continuous build started (PID: $!)"
echo "   The build will continue running even if you close this terminal"

