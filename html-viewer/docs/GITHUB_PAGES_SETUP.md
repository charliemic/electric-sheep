# GitHub Pages Setup Guide

## Configuration

### Source Selection

In GitHub repository **Settings → Pages**:

1. **Source**: Select **"GitHub Actions"** ✅
   - This allows our custom workflow to deploy
   - Don't select "Deploy from a branch" - we use Actions

2. **Workflow**: Our workflow (`.github/workflows/publish-document.yml`) will handle deployment

### Why GitHub Actions?

- ✅ **Custom Build Process**: We convert markdown to HTML
- ✅ **Version Control**: Tags control what gets deployed
- ✅ **Flexible**: Can deploy multiple documents
- ✅ **Automated**: No manual steps needed

## Directory Structure

### Deployment Directory: `.github-pages/`

All published documents are built into `.github-pages/` directory:

```
.github-pages/
├── index.html                    # Redirects to latest document
├── cognitive-journey-one-page.html  # Latest document
└── cognitive-journey-one-page/     # Versioned documents
    └── v1.0.0.html
```

### Why `.github-pages/`?

- ✅ **Dedicated**: Separate from source code
- ✅ **Clear Purpose**: Obviously for GitHub Pages
- ✅ **Git Ignored**: Won't clutter repository (add to .gitignore)
- ✅ **Standard**: Follows GitHub conventions

## Workflow Process

1. **Tag Pushed**: `publish-<doc>-v1.0.0` or `publish-<doc>-latest`
2. **Workflow Triggers**: `.github/workflows/publish-document.yml`
3. **Document Found**: Automatically locates source markdown
4. **Conversion**: Converts to HTML using HTML Viewer tool
5. **Build**: Outputs to `.github-pages/` directory
6. **Deploy**: GitHub Actions deploys from `.github-pages/` to GitHub Pages

## URL Structure

After deployment:
- **Latest**: `https://<username>.github.io/electric-sheep/cognitive-journey-one-page.html`
- **Versioned**: `https://<username>.github.io/electric-sheep/cognitive-journey-one-page/v1.0.0.html`
- **Root**: `https://<username>.github.io/electric-sheep/` (redirects to latest)

## Alternative Options (Not Recommended)

### ❌ "Static HTML" Workflow
- **Why not**: Requires files already in repository
- **Our need**: We convert markdown → HTML dynamically
- **Result**: Doesn't fit our use case

### ❌ "Deploy from a branch"
- **Why not**: Would require manual branch management
- **Our need**: Tag-based versioning is cleaner
- **Result**: Less flexible than Actions

## Verification

After first deployment:

1. **Check Settings**: Settings → Pages → Source = "GitHub Actions" ✅
2. **Check Workflow**: Actions tab → "Publish Document to GitHub Pages" → Should show success
3. **Check URL**: Visit `https://<username>.github.io/electric-sheep/`
4. **Verify Content**: Document should render correctly

## Troubleshooting

### "No workflow found"
- ✅ Ensure `.github/workflows/publish-document.yml` exists
- ✅ Check workflow file syntax is valid
- ✅ Verify workflow has `pages: write` permission

### "Deployment failed"
- ✅ Check workflow logs in Actions tab
- ✅ Verify document source file exists
- ✅ Check conversion script works locally

### "404 Not Found"
- ✅ Wait 1-2 minutes for deployment to propagate
- ✅ Verify GitHub Pages is enabled
- ✅ Check URL matches document name

## Best Practices

1. ✅ **Always use GitHub Actions** as source (not branch)
2. ✅ **Keep `.github-pages/` in .gitignore** (it's build output)
3. ✅ **Tag before pushing** (ensures clean deployment)
4. ✅ **Test locally first** (convert document before tagging)
5. ✅ **Monitor workflow** (check Actions tab after pushing tags)

