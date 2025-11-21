# Cost Tracking & Analysis System

**Status**: Phase 1 - Cost & Usage Tracking (Integrated with Metrics)

## Approach

We're building this incrementally, integrated with existing metrics:

1. **Phase 1 (Current)**: Track costs and usage
   - ✅ Extended prompt tracking to capture agent usage
   - ✅ Store locally (JSON) and in Supabase
   - ✅ Basic aggregation and reporting
   - ✅ Integrated with existing `development-metrics/` system

2. **Phase 2 (Future)**: Risk Assessment
   - After we have sufficient data, evaluate if risk assessment is needed
   - Analyze impact of cost changes
   - Identify dependencies and vulnerabilities

3. **Phase 3 (Future)**: Bridge Tracking & Risk
   - Connect historical data with risk scenarios
   - What-if analysis based on real usage patterns
   - Optimization recommendations

## Current Status

### ✅ Implemented

- **Extended Prompt Tracking**: `scripts/metrics/capture-prompt.sh`
  - Now accepts `--model` and `--task-type` options
  - Automatically captures agent usage when model is specified
  - Links prompts with agent usage via session IDs

- **Agent Usage Tracking**: `scripts/metrics/capture-agent-usage.sh`
  - Captures cost data from agent interactions
  - Calculates costs based on token usage and model pricing
  - Stores locally in `development-metrics/agent-usage/`
  - Can sync to Supabase `agent_usage` table

- **Supabase Table**: `agent_usage`
  - Migration: `supabase/migrations/20251121210227_create_agent_usage_table.sql`
  - Stores agent usage for SQL-based analysis

- **Basic Analysis**: `scripts/analysis/analyze-costs.sh`
  - Aggregates costs by model, company, task type, provider
  - Shows usage patterns and trends
  - Basic cost breakdowns

### 📋 Next Steps

1. Start tracking costs in real usage
2. Collect data for 1-2 weeks
3. Evaluate if risk assessment is needed
4. If needed, implement Phase 2 (risk assessment)
5. Bridge tracking data with risk scenarios

## Usage

### Track a Prompt with Agent Usage (Recommended)

```bash
# Track prompt and automatically capture agent usage
./scripts/metrics/capture-prompt.sh \
  "Implement a new feature" \
  --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
  --task-type "feature_implementation"
```

### Track Agent Usage Separately

```bash
# If you know token counts
./scripts/metrics/capture-agent-usage.sh \
  --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
  --input-tokens 1000 \
  --output-tokens 500 \
  --task-type "feature_implementation"
```

### Analyze Costs

```bash
# Analyze last 30 days (from local JSON files)
./scripts/analysis/analyze-costs.sh --period "last-30-days"

# Group by model
./scripts/analysis/analyze-costs.sh --group-by model

# Group by company
./scripts/analysis/analyze-costs.sh --group-by company
```

## Data Structure

Cost records are stored in `development-metrics/costs/` as JSON files:

```json
{
  "timestamp": "2025-01-20T12:00:00Z",
  "sessionId": "session_123",
  "agent": {
    "company": "Anthropic",
    "model": "claude-3-5-sonnet",
    "modelId": "anthropic.claude-sonnet-4-5-20250929-v1:0",
    "provider": "AWS Bedrock",
    "region": "eu-west-1"
  },
  "usage": {
    "inputTokens": 1000,
    "outputTokens": 500,
    "totalTokens": 1500
  },
  "cost": {
    "inputCost": 0.003,
    "outputCost": 0.0075,
    "totalCost": 0.0105,
    "currency": "USD"
  },
  "task": {
    "type": "feature_implementation",
    "complexity": "standard"
  }
}
```

## Related Documentation

- `COST_ANALYSIS_SYSTEM.md` - Complete system documentation
- `../setup/AWS_BEDROCK_MODEL_OPTIMIZATION.md` - Model pricing and optimization
- `../../development-metrics/README.md` - Metrics system overview

