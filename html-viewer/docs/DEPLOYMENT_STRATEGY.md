# HTML Viewer Deployment Strategy

> **Note**: This document describes the old approach. See [DOCUMENT_VERSIONING.md](./DOCUMENT_VERSIONING.md) for the current strategy that versions documents (not the tool).

## Overview

This document outlines the versioning and deployment strategy for the HTML Viewer tool using GitHub Pages with CI/CD integration.

**Current Approach**: The HTML Viewer tool is a static utility. Documents are versioned using tags like `publish-<doc-name>-v<version>`. See [DOCUMENT_VERSIONING.md](./DOCUMENT_VERSIONING.md) for details.

## Deployment Strategy

### Core Principle

**GitHub Pages Limitation**: GitHub Pages can only serve from one directory at a time. Therefore:
- **Latest** deploys to root path (main deployment)
- **Versioned** builds are stored as artifacts (downloadable) or can be deployed by updating the "latest" tag

### Deployment Triggers

#### 1. Version Tags (Creates Artifacts)

Deploy specific versions using tags:

```bash
# Create and push a version tag
git tag html-viewer-v1.0.0
git push origin html-viewer-v1.0.0
```

**Result:**
- ✅ Builds the version
- ✅ Stores as downloadable artifact: `html-viewer-v1.0.0`
- ✅ Available in GitHub Actions artifacts (90 days retention)
- ❌ Does NOT deploy to GitHub Pages (preserves current latest)

#### 2. Latest Deployment (Deploys to GitHub Pages)

Deploy as "latest" (root path) using:

**Option A: Special Tag**
```bash
git tag html-viewer-latest
git push origin html-viewer-latest
```

**Option B: Branch**
```bash
git checkout -b html-viewer-latest
git push origin html-viewer-latest
```

**Option C: Manual with Version**
```bash
# Via GitHub UI or CLI
gh workflow run html-viewer-deploy.yml \
  -f version=v1.0.0 \
  -f deploy_as_latest=true
```

**Result:**
- ✅ Deploys to: `https://<username>.github.io/<repo>/` (root)
- ✅ Overwrites previous "latest" deployment
- ✅ This becomes the live version

### Recommended Workflow

1. **Develop and test** on feature branch
2. **Tag version** for record-keeping:
   ```bash
   git tag html-viewer-v1.0.0
   git push origin html-viewer-v1.0.0
   ```
   This creates a build artifact you can download later.

3. **Deploy as latest** when ready for production:
   ```bash
   # Option 1: Tag as latest (points to same commit)
   git tag html-viewer-latest -f
   git push origin html-viewer-latest -f
   
   # Option 2: Manual deployment
   gh workflow run html-viewer-deploy.yml \
     -f version=v1.0.0 \
     -f deploy_as_latest=true
   ```

## Versioning Strategy

### Tag Naming Convention

- **Version tags**: `html-viewer-v<version>` (e.g., `html-viewer-v1.0.0`, `html-viewer-v1.2.3`)
- **Latest tag**: `html-viewer-latest`
- **Semantic versioning**: Use `MAJOR.MINOR.PATCH` format

### Examples

```bash
# 1. Tag version (creates artifact, doesn't deploy)
git tag html-viewer-v1.0.0
git push origin html-viewer-v1.0.0

# 2. Deploy as latest (makes it live)
git tag html-viewer-latest -f
git push origin html-viewer-latest -f

# 3. Later, tag new version
git tag html-viewer-v1.1.0
git push origin html-viewer-v1.1.0

# 4. Deploy new version as latest
git tag html-viewer-latest -f
git push origin html-viewer-latest -f
```

## URL Structure

### GitHub Pages URL

Assuming repository: `electric-sheep` and GitHub username: `yourusername`

- **Latest (Live)**: `https://yourusername.github.io/electric-sheep/`
- **Versioned Artifacts**: Download from GitHub Actions artifacts tab

### Accessing Versioned Builds

1. Go to **Actions** tab in GitHub
2. Find workflow run for the version tag
3. Download artifact: `html-viewer-v1.0.0`
4. Extract and serve locally or host elsewhere

## Benefits

✅ **Version Control**: Keep old versions as downloadable artifacts  
✅ **Stable Latest**: Root path always has current production version  
✅ **CI/CD Integration**: Automatic deployment on tag push  
✅ **Manual Control**: Deploy on-demand via workflow_dispatch  
✅ **No Manual Steps**: No need to manually configure GitHub Pages  
✅ **Artifact Retention**: Versioned builds kept for 90 days  

## Configuration

### GitHub Pages Settings

1. Go to repository **Settings → Pages**
2. **Source**: Select **GitHub Actions** (not branch)
3. The workflow handles deployment automatically

### Workflow Permissions

The workflow requires:
- `contents: read` - Read repository
- `pages: write` - Deploy to GitHub Pages
- `id-token: write` - OIDC authentication

These are set in the workflow file.

## Troubleshooting

### Deployment Not Triggering

- ✅ Check tag format: Must match `html-viewer-v*` or be `html-viewer-latest`
- ✅ Verify workflow file exists: `.github/workflows/html-viewer-deploy.yml`
- ✅ Check GitHub Actions tab for workflow runs
- ✅ Ensure GitHub Pages is enabled in repository settings

### Wrong Version Deployed

- ✅ Verify tag name matches expected format
- ✅ Check workflow logs for version detection
- ✅ Use manual dispatch to override: `deploy_as_latest=true`

### 404 Errors

- ✅ Wait a few minutes for deployment to propagate (GitHub Pages can take 1-2 minutes)
- ✅ Check GitHub Pages source is set to "GitHub Actions" (not branch)
- ✅ Verify workflow completed successfully
- ✅ Check deployment URL in workflow summary

### Artifact Not Found

- ✅ Artifacts are only created for version tags (not latest)
- ✅ Artifacts expire after 90 days
- ✅ Check Actions tab → Artifacts section

## Best Practices

1. **Always tag versions** before deploying to production
2. **Test locally** before tagging: `npm run build && npm run preview`
3. **Use semantic versioning** (MAJOR.MINOR.PATCH)
4. **Update latest** only for stable releases
5. **Keep version tags** for historical reference
6. **Document changes** in release notes or commit messages
7. **Use manual dispatch** for quick fixes to latest

## Alternative: Multiple Deployments

If you need multiple versions live simultaneously, consider:

1. **Separate repository** for each major version
2. **Subdomain strategy**: `v1.html-viewer.yourdomain.com`
3. **Version selector** in the app UI (redirects to different deployments)
4. **External hosting** (Netlify, Vercel) with better multi-version support

For most use cases, the "latest + artifacts" strategy is sufficient.
