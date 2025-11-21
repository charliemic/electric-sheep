#!/bin/bash

# Collect development metrics after build/test execution
# This script should be run after builds or tests to capture metrics

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Ensure metrics directory exists
mkdir -p "$METRICS_DIR"/{prompts,agent-usage,builds,tests,complexity,accessibility,coverage}

# Function to log metrics
log_metric() {
    local category=$1
    local filename=$2
    local content=$3
    echo "$content" > "$METRICS_DIR/$category/${filename}_${TIMESTAMP}.json"
}

# Collect build metrics if build was just run
if [ -f "$PROJECT_ROOT/.build-info.tmp" ]; then
    BUILD_INFO=$(cat "$PROJECT_ROOT/.build-info.tmp")
    log_metric "builds" "build" "$BUILD_INFO"
    rm "$PROJECT_ROOT/.build-info.tmp"
fi

# Collect test metrics if tests were just run
if [ -f "$PROJECT_ROOT/.test-info.tmp" ]; then
    TEST_INFO=$(cat "$PROJECT_ROOT/.test-info.tmp")
    log_metric "tests" "test" "$TEST_INFO"
    rm "$PROJECT_ROOT/.test-info.tmp"
fi

echo "Metrics collected at $TIMESTAMP"

