#!/bin/bash

# Start the development metrics dashboard server with hot reloading
# Uses nodemon to automatically restart on file changes

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Check if Node.js is available
if ! command -v node &> /dev/null; then
    echo "❌ Error: Node.js is required but not found"
    echo "   Please install Node.js to run the dashboard server"
    exit 1
fi

# Check if nodemon is installed
if [ ! -f "$SCRIPT_DIR/node_modules/.bin/nodemon" ]; then
    echo "📦 Installing nodemon for hot reloading..."
    cd "$SCRIPT_DIR"
    npm install --save-dev nodemon
fi

# Check if Fastify is installed
if [ ! -f "$SCRIPT_DIR/node_modules/fastify/index.js" ]; then
    echo "📦 Installing Fastify..."
    cd "$SCRIPT_DIR"
    npm install fastify
fi

# Start the server with nodemon (hot reloading)
cd "$PROJECT_ROOT"
echo "🚀 Starting dashboard server with hot reloading..."
echo "📊 Dashboard: http://localhost:8080/"
echo "📡 API: http://localhost:8080/api/status"
echo ""
echo "💡 Hot reloading enabled - server will restart on file changes"
echo "   Press Ctrl+C to stop"
echo ""

"$SCRIPT_DIR/node_modules/.bin/nodemon" "$SCRIPT_DIR/dashboard-server-fastify.js"

