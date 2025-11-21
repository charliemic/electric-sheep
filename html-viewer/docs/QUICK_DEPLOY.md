# Quick Deploy Guide

## Deploy a New Version

### Option 1: Tag a Version (Recommended)

```bash
# 1. Make sure you're on the branch with your changes
git checkout feature/html-viewer-tool
git merge main  # or your feature branch

# 2. Tag the version
git tag html-viewer-v1.0.0

# 3. Push the tag (triggers deployment)
git push origin html-viewer-v1.0.0
```

**Result**: Deploys to `/v1.0.0/` path

### Option 2: Deploy as Latest

```bash
# Update latest tag to point to current commit
git tag html-viewer-latest -f
git push origin html-viewer-latest -f
```

**Result**: Deploys to root path (overwrites previous latest)

### Option 3: Manual Deployment via GitHub UI

1. Go to **Actions** tab in GitHub
2. Select **Deploy HTML Viewer to GitHub Pages**
3. Click **Run workflow**
4. Enter version (e.g., `v1.0.0` or `latest`)
5. Check "Also deploy as latest" if desired
6. Click **Run workflow**

## Check Deployment Status

1. Go to **Actions** tab
2. Find the workflow run
3. Check for green checkmark
4. Visit the deployment URL (shown in workflow summary)

## Common Commands

```bash
# List all html-viewer tags
git tag | grep html-viewer

# Delete a tag (if needed)
git tag -d html-viewer-v1.0.0
git push origin :refs/tags/html-viewer-v1.0.0

# See what commit a tag points to
git show html-viewer-v1.0.0
```

