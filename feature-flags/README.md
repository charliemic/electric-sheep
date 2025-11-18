# Feature Flags

This directory contains version-controlled feature flag definitions.

## Structure

- `flags.yaml` - Feature flag definitions in YAML format
- `README.md` - This file

## Adding a New Feature Flag

1. Edit `feature-flags/flags.yaml`
2. Add a new flag entry:
   ```yaml
   flags:
     - key: my_new_feature
       value_type: boolean
       boolean_value: false
       enabled: true
       description: "Enable my new feature"
       segment_id: null
       user_id: null
   ```
3. Commit and push to trigger automatic deployment

## Flag Properties

- `key` (required): Unique identifier for the flag
- `value_type` (required): One of `boolean`, `string`, or `int`
- `boolean_value` / `string_value` / `int_value` (required): Value matching the type
- `enabled` (required): Whether the flag is active
- `description` (optional): Human-readable description
- `segment_id` (optional): Future: Link to user segment (null = global)
- `user_id` (optional): User-specific flag (null = applies to all users)

## Deployment

Feature flags are automatically deployed via GitHub Actions when:
- Changes are pushed to `main` or `develop` branches
- Changes are made to files in `feature-flags/` directory

The workflow:
1. Validates YAML syntax and flag structure
2. Deploys to staging (on `develop` branch)
3. Deploys to production (on `main` branch)

## Manual Deployment

To manually sync flags:

```bash
# Set environment variables
export SUPABASE_DB_URL="postgresql://..."
# Or
export SUPABASE_URL="https://..."
export SUPABASE_SERVICE_ROLE_KEY="..."

# Run sync script
./scripts/sync-feature-flags.sh feature-flags/flags.yaml
```

## Future: Segmentation

The schema supports future segmentation via `segment_id`. When segments are implemented:
- Create a `segments` table
- Link flags to segments via `segment_id`
- Update RLS policies to check segment membership

