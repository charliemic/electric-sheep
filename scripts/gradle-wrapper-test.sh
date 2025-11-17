#!/bin/bash

# Gradle test wrapper that captures metrics automatically
# Usage: ./scripts/gradle-wrapper-test.sh [gradle-args...]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Run tests with metrics capture
"$SCRIPT_DIR/metrics/capture-test-metrics.sh"
TEST_EXIT=$?

# Collect all metrics
"$SCRIPT_DIR/collect-metrics.sh"

exit $TEST_EXIT

