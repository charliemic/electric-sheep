# GitHub Actions Integration for Agent Usage Tracking

## Overview

The agent usage tracking system automatically works in GitHub Actions using the same environment variable pattern as other Supabase workflows.

## Automatic Configuration

When running in GitHub Actions, the system uses existing workflow patterns:

### Environment Variables

**Staging Environment:**
```yaml
env:
  SUPABASE_URL: ${{ steps.supabase-url.outputs.supabase_url }}
  SUPABASE_SECRET_KEY: ${{ secrets.SUPABASE_SECRET_KEY_STAGING || secrets.SUPABASE_SECRET_KEY }}
```

**Production Environment:**
```yaml
env:
  SUPABASE_URL: ${{ steps.supabase-url.outputs.supabase_url }}
  SUPABASE_SECRET_KEY: ${{ secrets.SUPABASE_SECRET_KEY }}
```

### Getting Supabase URL

The URL is typically obtained from a workflow step:

```yaml
- name: Get Supabase URL
  id: supabase-url
  run: |
    SUPABASE_URL=$(supabase status 2>/dev/null | grep "API URL" | awk '{print $3}' || echo "")
    if [ -z "$SUPABASE_URL" ]; then
      PROJECT_REF="${{ secrets.SUPABASE_PROJECT_REF_STAGING || secrets.SUPABASE_PROJECT_REF }}"
      SUPABASE_URL="https://${PROJECT_REF}.supabase.co"
    fi
    echo "supabase_url=$SUPABASE_URL" >> $GITHUB_OUTPUT
```

## Usage in Workflows

### Example: Track Agent Usage in CI

```yaml
- name: Track agent usage
  env:
    SUPABASE_URL: ${{ steps.supabase-url.outputs.supabase_url }}
    SUPABASE_SECRET_KEY: ${{ secrets.SUPABASE_SECRET_KEY_STAGING || secrets.SUPABASE_SECRET_KEY }}
  run: |
    ./scripts/metrics/capture-agent-usage.sh \
      --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
      --input-tokens 1000 \
      --output-tokens 500 \
      --task-type "ci_test"
```

### Example: Analyze Costs in Workflow

```yaml
- name: Analyze agent usage costs
  env:
    SUPABASE_URL: ${{ steps.supabase-url.outputs.supabase_url }}
    SUPABASE_SECRET_KEY: ${{ secrets.SUPABASE_SECRET_KEY_STAGING || secrets.SUPABASE_SECRET_KEY }}
  run: |
    ./scripts/analysis/analyze-costs.sh \
      --period last-7-days \
      --source supabase \
      --output json > costs.json
```

## GitHub Secrets Required

The following secrets should be configured in GitHub repository settings:

- `SUPABASE_SECRET_KEY` - Production secret key
- `SUPABASE_SECRET_KEY_STAGING` - Staging secret key (optional, falls back to production)
- `SUPABASE_PROJECT_REF` - Production project reference ID
- `SUPABASE_PROJECT_REF_STAGING` - Staging project reference ID (optional)

## Pattern Consistency

This follows the same pattern as existing Supabase workflows:
- `test-data-initial-seed.yml`
- `test-data-nightly-update.yml`
- `supabase-feature-flags-deploy.yml`
- `supabase-schema-deploy.yml`

All use the same environment variable names and secret patterns for consistency.

