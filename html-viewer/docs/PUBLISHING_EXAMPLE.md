# Publishing Example: Cognitive Journey One-Page

This document shows how we published the cognitive journey document to GitHub Pages.

## Steps Taken

### 1. Copy Document to Repository

```bash
cp /tmp/cognitive-journey-one-page.md docs/learning/cognitive-journey-one-page.md
```

### 2. Test Conversion Locally

```bash
# Convert using existing Python script
python3 scripts/md-to-html.py \
  docs/learning/cognitive-journey-one-page.md \
  /tmp/cognitive-journey-one-page.html \
  "🧠 Test Automation: Human-Like Cognitive Process"
```

### 3. Commit to Repository

```bash
git add docs/learning/cognitive-journey-one-page.md
git commit -m "docs: Add cognitive journey one-page document for publishing"
```

### 4. Tag for Publication

```bash
# Tag version 1.0.0
git tag publish-cognitive-journey-one-page-v1.0.0

# Tag as latest (for root deployment)
git tag publish-cognitive-journey-one-page-latest
```

### 5. Push Tags (Triggers Workflow)

```bash
git push origin publish-cognitive-journey-one-page-v1.0.0
git push origin publish-cognitive-journey-one-page-latest
```

## Result

After pushing the tags:
- ✅ GitHub Actions workflow (`.github/workflows/publish-document.yml`) triggers automatically
- ✅ Document is converted to HTML using the HTML Viewer tool
- ✅ Deployed to GitHub Pages
- ✅ Available at: `https://<username>.github.io/electric-sheep/cognitive-journey-one-page.html`

## Tag Format Used

- **Version**: `publish-cognitive-journey-one-page-v1.0.0`
- **Latest**: `publish-cognitive-journey-one-page-latest`

## Workflow Process

The `.github/workflows/publish-document.yml` workflow:
1. **Detects tag**: Recognizes `publish-*-v*` or `publish-*-latest` pattern
2. **Extracts info**: Document name = `cognitive-journey-one-page`, version = `v1.0.0` or `latest`
3. **Finds source**: Automatically searches for `docs/**/cognitive-journey-one-page.md`
4. **Converts**: Uses HTML Viewer tool (or Python fallback) to convert to HTML
5. **Deploys**: Publishes to GitHub Pages

## Reusability

This same process works for **any document**:

```bash
# 1. Copy document to docs/ directory
cp /path/to/document.md docs/learning/my-document.md

# 2. Commit it
git add docs/learning/my-document.md
git commit -m "docs: Add my document"

# 3. Tag for publication
git tag publish-my-document-v1.0.0
git tag publish-my-document-latest

# 4. Push tags (triggers deployment)
git push origin publish-my-document-v1.0.0
git push origin publish-my-document-latest
```

## Current Status

✅ Document added to repository  
✅ Tags created locally  
⏳ **Next step**: Push tags to trigger deployment

```bash
git push origin publish-cognitive-journey-one-page-v1.0.0
git push origin publish-cognitive-journey-one-page-latest
```

## Verification

After pushing, check:
1. **GitHub Actions**: Go to Actions tab → See workflow run
2. **GitHub Pages**: Settings → Pages → Verify deployment
3. **Live URL**: Visit `https://<username>.github.io/electric-sheep/cognitive-journey-one-page.html`
