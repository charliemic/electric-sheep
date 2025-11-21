# Cost Tracking System for Smart Agent Usage

**Purpose**: Track and analyze AI agent costs and usage patterns alongside existing prompt metrics.

## Overview

This system extends the existing metrics collection (`development-metrics/`) to track agent usage and costs:
- **Integrated with Prompt Tracking**: Agent usage tracked alongside prompts
- **Cost Tracking**: Monitor actual costs by model, company, and usage pattern
- **Usage Tracking**: Track token usage, interaction patterns, and task types
- **Supabase Storage**: Data stored directly in Supabase (primary and preferred)
- **JSON Temporary**: JSON files only created if Supabase unavailable (should sync later)
- **Future**: Risk assessment and what-if scenarios (to be added after we have data)

## Architecture

### Data Model

**Cost Record Structure:**
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
    "complexity": "standard",
    "description": "Implement new settings screen"
  },
  "metadata": {
    "promptLength": 250,
    "responseTime": 2.5,
    "success": true
  }
}
```

### Cost Configuration

**Model Pricing (Current):**
```json
{
  "models": {
    "anthropic.claude-3-5-haiku-20241022-v2:0": {
      "company": "Anthropic",
      "provider": "AWS Bedrock",
      "inputPricePerMillion": 0.80,
      "outputPricePerMillion": 4.00,
      "region": "eu-west-1"
    },
    "anthropic.claude-sonnet-4-5-20250929-v1:0": {
      "company": "Anthropic",
      "provider": "AWS Bedrock",
      "inputPricePerMillion": 3.00,
      "outputPricePerMillion": 15.00,
      "region": "eu-west-1"
    },
    "anthropic.claude-3-opus-20240229-v1:0": {
      "company": "Anthropic",
      "provider": "AWS Bedrock",
      "inputPricePerMillion": 15.00,
      "outputPricePerMillion": 75.00,
      "region": "eu-west-1"
    }
  }
}
```

## Components

### Phase 1: Cost & Usage Tracking (Current)

**Extended Prompt Tracking**: `scripts/metrics/capture-prompt.sh`
- Now accepts `--model` and `--task-type` options
- Automatically captures agent usage when model is specified
- Links prompts with agent usage via session IDs

**Agent Usage Tracking**: `scripts/metrics/capture-agent-usage.sh`
- Captures cost data from each agent interaction
- Stores locally in `development-metrics/agent-usage/` (JSON)
- Can sync to Supabase `agent_usage` table
- Calculates costs based on token usage and model pricing
- Tracks usage patterns (tokens, task types, models)

**Supabase Table**: `agent_usage`
- Stores agent usage data for querying and analysis
- Linked to prompts via `prompt_id` and `session_id`
- Enables SQL-based cost analysis and reporting

**Analysis Script**: `scripts/analysis/analyze-costs.sh`
- Aggregates costs by model, company, time period
- Shows usage patterns and trends
- Basic cost breakdowns and summaries
- Can query Supabase or local JSON files

### Phase 2: Risk Assessment (Future - After We Have Data)

**Planned**: `scripts/analysis/assess-cost-risks.sh`
- Analyze impact of cost changes
- Model scenarios (e.g., "What if Sonnet costs increase 50%?")
- Identify dependencies and vulnerabilities
- **Status**: To be implemented after we have sufficient tracking data

### Phase 3: What-If Scenarios (Future - After We Have Data)

**Planned**: `scripts/analysis/cost-what-if.sh`
- Model different cost scenarios
- Project future costs under different assumptions
- Compare optimization strategies
- **Status**: To be implemented after we have sufficient tracking data and risk assessment

## Future: Risk Categories (To Be Implemented)

Once we have sufficient tracking data, we'll implement risk assessment for:

### 1. Cost Increase Risks
- Model price increases
- Provider price changes
- Currency fluctuations

### 2. Availability Risks
- Model deprecation
- Provider service disruption
- Regional availability changes

### 3. Company/Provider Risks
- Company policy changes
- Provider changes
- Vendor lock-in risks

### 4. Usage Pattern Risks
- Usage growth
- Pattern changes
- Inefficient usage

**Note**: Risk assessment will be added after we have sufficient historical data to make meaningful predictions.

## Usage

### Track Prompts with Agent Usage (Recommended)

```bash
# Capture prompt and automatically track agent usage
./scripts/metrics/capture-prompt.sh \
  "Implement a new settings screen" \
  --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
  --task-type "feature_implementation"
```

This will:
1. Store the prompt in `development-metrics/prompts/`
2. Automatically capture agent usage (estimating tokens from prompt length)
3. Link them via session ID

### Track Agent Usage Separately

```bash
# Capture agent usage with known token counts
./scripts/metrics/capture-agent-usage.sh \
  --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
  --input-tokens 1000 \
  --output-tokens 500 \
  --task-type "feature_implementation" \
  --session-id "session_123" \
  --prompt-length 250 \
  --response-time 2.5
```

### Token Estimation

If token counts aren't known:
- Provide `--prompt-length` for better estimation
- Script estimates ~4 characters per token
- Output tokens estimated as ~30% of input tokens (typical for code generation)

### Analyze Costs (Phase 1 - Current)

```bash
# Analyze costs for a time period
./scripts/analysis/analyze-costs.sh --period "last-30-days"

# Analyze by model
./scripts/analysis/analyze-costs.sh --group-by model

# Analyze by company
./scripts/analysis/analyze-costs.sh --group-by company

# Output as JSON
./scripts/analysis/analyze-costs.sh --output json

# Output as CSV
./scripts/analysis/analyze-costs.sh --output csv
```

### Future: Risk Assessment (After We Have Data)

```bash
# To be implemented after we have sufficient tracking data
# ./scripts/analysis/assess-cost-risks.sh --risk-type "cost-increase"
```

### Future: What-If Scenarios (After We Have Data)

```bash
# To be implemented after we have sufficient tracking data
# ./scripts/analysis/cost-what-if.sh --model "sonnet" --input-price-change "+50%"
```

## Output Examples

### Cost Summary

```
Cost Analysis Report - Last 30 Days
====================================

Total Cost: $45.30
Total Interactions: 1,250
Average Cost per Interaction: $0.036

By Model:
  - Sonnet: $38.50 (85%) - 1,000 interactions
  - Haiku: $4.20 (9%) - 200 interactions
  - Opus: $2.60 (6%) - 50 interactions

By Company:
  - Anthropic: $45.30 (100%)

By Task Type:
  - Feature Implementation: $20.00 (44%)
  - Bug Fixes: $12.00 (27%)
  - Documentation: $8.00 (18%)
  - Code Review: $5.30 (11%)

Trends:
  - Daily average: $1.51
  - Weekly average: $10.57
  - Monthly projection: $45.30
```

### Future: Risk Assessment Output

```
Risk Assessment Report (To Be Implemented)
===========================================

Will show:
- Cost increase risks and impact
- Availability risks and alternatives
- Provider risks and dependencies
- Usage pattern risks and optimization opportunities
```

### Future: What-If Scenario Output

```
What-If Scenario (To Be Implemented)
=====================================

Will show:
- Projected costs under different scenarios
- Impact of price changes
- Optimization opportunities
- Recommendations
```

## Integration

### With Existing Metrics

- **Prompts**: Link costs to prompts tracked in `development-metrics/prompts/`
- **Sessions**: Link costs to sessions in `development-metrics/sessions/`
- **Builds/Tests**: Track costs associated with development activities

### With AWS Cost Explorer

- Compare tracked costs with AWS Cost Explorer data
- Validate cost calculations
- Identify discrepancies

## Future Enhancements (After Phase 1 Data Collection)

### Phase 2: Risk Assessment (After We Have Data)
1. **Cost Increase Risk Analysis**: Model impact of price changes
2. **Availability Risk Analysis**: Assess impact of model/provider changes
3. **Dependency Analysis**: Identify vendor lock-in risks
4. **Usage Pattern Risk Analysis**: Identify inefficient usage patterns

### Phase 3: Advanced Features (After Risk Assessment)
1. **Real-time Cost Monitoring**: Dashboard showing current session costs
2. **Budget Alerts**: Notify when costs exceed thresholds
3. **Automated Optimization**: Suggest model switches based on task complexity
4. **Multi-Provider Support**: Track costs across different providers
5. **Historical Trend Analysis**: Long-term cost trend visualization
6. **What-If Scenarios**: Model different cost scenarios based on real usage data

## Related Documentation

- `docs/development/setup/AWS_BEDROCK_MODEL_OPTIMIZATION.md` - Model optimization guide
- `docs/development/setup/AWS_BEDROCK_QUICK_REFERENCE.md` - Quick reference
- `development-metrics/README.md` - Metrics system overview

