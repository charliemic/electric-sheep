# Agent Usage Tracking - Quick Start

**Status**: Integrated with existing metrics system

## Overview

Agent usage tracking extends the existing prompt metrics to capture cost and usage data for AI agent interactions. Data is stored both locally (JSON) and in Supabase for analysis.

## Quick Start

### Track a Prompt with Agent Usage

```bash
# Simple: Just track the prompt
./scripts/metrics/capture-prompt.sh "Your prompt here"

# With agent usage: Specify the model
./scripts/metrics/capture-prompt.sh \
  "Implement a new feature" \
  --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
  --task-type "feature_implementation"
```

### What Gets Tracked

**Prompt Data** (stored in `development-metrics/prompts/`):
- Prompt text
- Timestamp
- Prompt length and word count
- Session ID

**Agent Usage Data** (stored in `development-metrics/agent-usage/`):
- Model information (company, model, provider, region)
- Token usage (input, output, total)
- Cost (calculated from token usage and model pricing)
- Task type and complexity
- Metadata (prompt length, response time, success)

## Integration with Existing Metrics

Agent usage tracking is integrated with:
- **Prompt tracking**: Prompts and agent usage are linked via session IDs
- **Metrics collection**: Part of the overall metrics system
- **Supabase**: Data can be synced to `agent_usage` table for SQL analysis

## Data Storage

### Primary: Supabase (Database)
- **Table**: `agent_usage`
- **Purpose**: SQL queries, aggregations, cost analysis
- **Migration**: `supabase/migrations/20251121210227_create_agent_usage_table.sql`
- **Insertion**: Uses PostgREST API via `scripts/lib/supabase-postgrest.sh`

### Temporary: Local JSON (Only if Supabase unavailable)
- **Location**: `development-metrics/agent-usage/usage_*.json`
- **Purpose**: Temporary storage if Supabase env vars not set
- **Note**: JSON is temporary - should sync to Supabase when `SUPABASE_URL` and `SUPABASE_SECRET_KEY` are configured
- **Action**: Once Supabase is configured, JSON files can be synced or deleted

### Prompts (Separate System)
- **Prompts**: `development-metrics/prompts/prompt_*.json` (existing system, unchanged)

## Model Detection

The system recognizes these models:
- `anthropic.claude-3-5-haiku-20241022-v2:0` (Haiku)
- `anthropic.claude-sonnet-4-5-20250929-v1:0` (Sonnet - default)
- `anthropic.claude-3-opus-20240229-v1:0` (Opus)

If an unknown model is used, default pricing is applied ($3.00/$15.00 per million tokens).

## Token Estimation

If token counts aren't provided:
- **Input tokens**: Estimated from prompt length (~4 characters per token)
- **Output tokens**: Estimated as ~30% of input tokens (typical for code generation)

For accurate tracking, provide actual token counts when available.

## Task Types

Common task types:
- `feature_implementation`
- `bug_fix`
- `refactoring`
- `documentation`
- `code_review`
- `testing`
- `debugging`

## Session Tracking

Sessions group related interactions:
- Automatically generated if not provided
- Links prompts and agent usage together
- Enables session-level cost analysis

## Next Steps

1. **Start Tracking**: Use `capture-prompt.sh` with `--model` flag
2. **Collect Data**: Track for 1-2 weeks to build baseline
3. **Sync to Supabase**: When ready, sync local JSON to Supabase
4. **Analyze**: Use SQL queries or analysis scripts to understand costs

## Related Documentation

- `COST_ANALYSIS_SYSTEM.md` - Complete system documentation
- `../setup/AWS_BEDROCK_MODEL_OPTIMIZATION.md` - Model pricing and optimization
- `../../development-metrics/README.md` - Metrics system overview

