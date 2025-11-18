# Testing Feature Flags Before Merge

This guide explains how to test feature flag changes from feature branches before merging to main.

## Overview

Feature flags can be tested in the staging environment before merging to ensure they work correctly. This allows you to:

1. Test flag changes in isolation
2. Verify flag behavior with the app
3. Catch issues before they reach production

## Testing Methods

### Method 1: Manual Workflow Dispatch (Recommended)

The easiest way to test feature flags from a feature branch:

1. **Push your feature branch** with flag changes:
   ```bash
   git push origin feature/your-branch-name
   ```

2. **Trigger the workflow manually**:
   - Go to: https://github.com/YOUR_USERNAME/electric-sheep/actions/workflows/deploy-feature-flags.yml
   - Click "Run workflow"
   - Select your feature branch from the dropdown
   - Choose "staging" as the environment
   - Click "Run workflow"

3. **The workflow will**:
   - Validate your flags.yaml syntax
   - Check BuildConfig fallback values exist
   - Deploy flags to staging Supabase
   - You can then test in the app

### Method 2: PR Comment Trigger

If you want to trigger from a Pull Request:

1. **Create/update your PR** with flag changes
2. **Add `[deploy-flags]` to the PR description** or in a comment
3. **The workflow will automatically deploy** to staging

Note: This requires the workflow file to be on the base branch (main) first.

### Method 3: Local Testing

For quick local testing without deploying:

1. **Update BuildConfig** in `app/build.gradle.kts`:
   ```kotlin
   buildConfigField("boolean", "SHOW_FEATURE_FLAG_INDICATOR_MODE", "true")
   ```

2. **Build and test locally**:
   ```bash
   ./gradlew :app:assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **The app will use BuildConfig fallback** (since Supabase won't have the flag yet)

## Workflow Behavior

### Automatic Deployments

- **`main` branch push** → Deploys to production
- **`develop` branch push** → Deploys to staging
- **PR to `main`/`develop`** → Only validates (doesn't deploy unless `[deploy-flags]` is in description)

### Manual Deployments

- **`workflow_dispatch` from feature branch** → Deploys to staging (preview/test)
- **`workflow_dispatch` from `main`** → Can deploy to staging or production (based on input)

## Testing Checklist

Before merging feature flag changes:

- [ ] Flags validated (syntax, BuildConfig fallback exists)
- [ ] Flags deployed to staging
- [ ] App tested with staging flags
- [ ] Flag values verified in Supabase dashboard
- [ ] Cache behavior tested (TTL, version invalidation)
- [ ] Fallback behavior tested (when Supabase unavailable)

## Troubleshooting

### Workflow Not Found

If the workflow doesn't appear in GitHub Actions:
- The workflow file must exist on the base branch (`main`)
- Push the workflow file to `main` first, then create your feature branch

### Flags Not Appearing in Supabase

1. Check workflow logs for errors
2. Verify `SUPABASE_PROJECT_REF_STAGING` secret is set
3. Check database connection string format
4. Verify RLS policies allow inserts/updates

### App Not Using Supabase Flags

1. Clear app cache: `adb shell pm clear com.electricsheep.app`
2. Wait for TTL to expire (5 minutes default)
3. Check app logs for Supabase connection errors
4. Verify `SUPABASE_URL` and `SUPABASE_ANON_KEY` are set in BuildConfig

## Best Practices

1. **Always test in staging first** before merging to main
2. **Use descriptive flag keys** that indicate their purpose
3. **Include BuildConfig fallbacks** for all flags
4. **Document flag behavior** in the description field
5. **Test both enabled and disabled states** before merging
6. **Verify cache invalidation** works correctly when flags change

