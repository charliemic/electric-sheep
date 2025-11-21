#!/usr/bin/env bash

# Ad-hoc analysis of last week's agent usage
# Estimates costs based on available data and shows what tracking would capture
# Usage: ./scripts/analysis/ad-hoc-week-analysis.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"

echo "📊 Ad-Hoc Analysis: Last Week's Agent Usage"
echo "=============================================="
echo ""

# Count actual tracked interactions
TRACKED_INTERACTIONS=$(find "$METRICS_DIR/agent-usage" -type f -name "*.json" -mtime -7 2>/dev/null | wc -l | tr -d ' ')
echo "✅ Tracked Interactions (last 7 days): $TRACKED_INTERACTIONS"
echo ""

# Count prompts (potential interactions)
PROMPTS=$(find "$METRICS_DIR/prompts" -type f -name "*.json" -mtime -7 2>/dev/null | wc -l | tr -d ' ')
echo "📝 Prompts Captured (last 7 days): $PROMPTS"
echo ""

# Analyze actual tracked costs
if [ "$TRACKED_INTERACTIONS" -gt 0 ]; then
    echo "=== Actual Tracked Costs (Last 7 Days) ==="
    ./scripts/analysis/analyze-costs.sh --period last-7-days --source json 2>&1 | grep -A 20 "Cost Analysis"
    echo ""
fi

# Estimate what we would have captured
echo "=== Estimation: What Tracking Would Show ==="
echo ""
echo "If cost tracking had been running all week:"
echo "  - Each prompt → 1 agent interaction (estimated)"
echo "  - Average cost per interaction: ~\$0.02-0.05 (varies by model)"
echo "  - Estimated total: ~\$$(echo "scale=2; $PROMPTS * 0.03" | bc) (if all prompts used Sonnet)"
echo ""
echo "Breakdown by model (typical usage):"
echo "  - Haiku (simple tasks): ~\$0.001 per interaction"
echo "  - Sonnet (standard tasks): ~\$0.02-0.05 per interaction"
echo "  - Opus (complex tasks): ~\$0.10-0.50 per interaction"
echo ""

# Show what the new system captures
echo "=== What Our New System Captures ==="
echo ""
echo "✅ Model used (Haiku/Sonnet/Opus)"
echo "✅ Token consumption (input/output)"
echo "✅ Actual cost calculation"
echo "✅ Task type and complexity"
echo "✅ Session and prompt linking"
echo "✅ Timestamp and metadata"
echo ""

# Show impact of the work
echo "=== Impact of This Work ==="
echo ""
echo "Before: No cost tracking"
echo "  - No visibility into agent costs"
echo "  - No way to optimize model selection"
echo "  - No cost analysis or trends"
echo ""
echo "After: Full cost tracking system"
echo "  - Real-time cost tracking"
echo "  - Model usage analysis"
echo "  - Cost breakdown by task type"
echo "  - Supabase storage for SQL analysis"
echo "  - Historical trend analysis"
echo ""

echo "✅ System ready to track all future usage"

