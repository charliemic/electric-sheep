# Document Versioning Strategy

## Overview

This system versions **documents/content** (not the HTML Viewer tool itself). The tool is always available in source control and CI/CD, while tags control which document versions get published to GitHub Pages.

## Architecture

```
┌─────────────────────────────────────┐
│  HTML Viewer Tool (Static Utility)  │
│  - Always in source control         │
│  - Available in CI/CD               │
│  - Not versioned                    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Documents (Versioned Content)     │
│  - Markdown files                   │
│  - Log files                        │
│  - Other content                    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Tags Control Deployment            │
│  - publish-<doc>-v1.0.0             │
│  - publish-<doc>-latest              │
└─────────────────────────────────────┘
```

## Tag Format

### Versioned Documents

```bash
publish-<document-name>-v<version>
```

**Examples:**
- `publish-cognitive-journey-v1.0.0`
- `publish-test-results-v2.1.0`
- `publish-weekly-report-v1.5.0`

### Latest Version

```bash
publish-<document-name>-latest
```

**Examples:**
- `publish-cognitive-journey-latest`
- `publish-test-results-latest`

## Workflow

### 1. Prepare Document

Your document should be in the repository (e.g., `docs/learning/cognitive-journey.md`)

### 2. Tag for Publication

```bash
# Tag a specific version
git tag publish-cognitive-journey-v1.0.0
git push origin publish-cognitive-journey-v1.0.0
```

**Result:**
- ✅ Converts document to HTML
- ✅ Deploys to: `https://<username>.github.io/<repo>/cognitive-journey/v1.0.0.html`
- ✅ Creates downloadable artifact

### 3. Deploy as Latest

```bash
# Tag as latest (makes it the default)
git tag publish-cognitive-journey-latest
git push origin publish-cognitive-journey-latest
```

**Result:**
- ✅ Converts document to HTML
- ✅ Deploys to: `https://<username>.github.io/<repo>/cognitive-journey.html`
- ✅ Becomes the default/latest version

## Document Organization

### Recommended Structure

```
docs/
├── learning/
│   ├── cognitive-journey.md          # Source document
│   └── weekly-report.md
├── reports/
│   └── test-results.md
└── ...

# Published versions appear on GitHub Pages:
# - cognitive-journey.html (latest)
# - cognitive-journey/v1.0.0.html (versioned)
```

### Finding Documents

The workflow automatically searches for documents:
1. Uses `source_path` if provided in manual dispatch
2. Searches for files matching document name pattern
3. Checks common locations: `docs/**/<doc-name>.md`

## Manual Publication

Use GitHub Actions UI or CLI:

```bash
gh workflow run publish-document.yml \
  -f document=cognitive-journey \
  -f version=v1.0.0 \
  -f source_path=docs/learning/cognitive-journey.md
```

Or deploy as latest:

```bash
gh workflow run publish-document.yml \
  -f document=cognitive-journey \
  -f version=latest \
  -f source_path=docs/learning/cognitive-journey.md
```

## URL Structure

### GitHub Pages URLs

Assuming repository: `electric-sheep` and username: `yourusername`

**Latest:**
- `https://yourusername.github.io/electric-sheep/cognitive-journey.html`

**Versioned:**
- `https://yourusername.github.io/electric-sheep/cognitive-journey/v1.0.0.html`
- `https://yourusername.github.io/electric-sheep/cognitive-journey/v1.1.0.html`

## HTML Viewer Tool

### Location

The HTML Viewer tool is always available at:
- **Source**: `html-viewer/` directory
- **Scripts**: `html-viewer/scripts/convert-document.*`
- **CI/CD**: Automatically installed in workflow

### Usage

**Local:**
```bash
# Using shell script
./html-viewer/scripts/convert-document.sh \
  docs/learning/cognitive-journey.md \
  output.html \
  "Cognitive Journey" \
  light

# Using Node.js script
node html-viewer/scripts/convert-document.js \
  docs/learning/cognitive-journey.md \
  output.html \
  "Cognitive Journey" \
  light
```

**CI/CD:**
The workflow automatically:
1. Checks out the tool
2. Installs dependencies
3. Uses it to convert documents

## Benefits

✅ **Tool Always Available**: HTML Viewer in source control, not versioned  
✅ **Document Versioning**: Tags control which document versions deploy  
✅ **Automatic Discovery**: Workflow finds documents automatically  
✅ **Flexible**: Manual dispatch for custom deployments  
✅ **Version History**: Old versions remain accessible  
✅ **Latest Always Available**: Root path has current version  

## Examples

### Example 1: Publish a Learning Document

```bash
# 1. Document exists: docs/learning/cognitive-journey.md

# 2. Tag version
git tag publish-cognitive-journey-v1.0.0
git push origin publish-cognitive-journey-v1.0.0

# 3. Deploy as latest
git tag publish-cognitive-journey-latest
git push origin publish-cognitive-journey-latest
```

### Example 2: Update Existing Document

```bash
# 1. Make changes to docs/learning/cognitive-journey.md
# 2. Commit changes
git add docs/learning/cognitive-journey.md
git commit -m "Update cognitive journey"

# 3. Tag new version
git tag publish-cognitive-journey-v1.1.0
git push origin publish-cognitive-journey-v1.1.0

# 4. Update latest
git tag publish-cognitive-journey-latest -f
git push origin publish-cognitive-journey-latest -f
```

### Example 3: Multiple Documents

```bash
# Publish different documents independently
git tag publish-cognitive-journey-v1.0.0
git tag publish-test-results-v2.0.0
git tag publish-weekly-report-v1.5.0

git push origin --tags
```

## Best Practices

1. **Use Semantic Versioning**: `v1.0.0`, `v1.1.0`, `v2.0.0`
2. **Tag After Review**: Only tag documents that are ready for publication
3. **Update Latest Carefully**: Only update latest for stable, reviewed content
4. **Document Changes**: Include change notes in commit messages
5. **Keep Source Documents**: Always keep `.md` source files in repository
6. **Test Locally**: Convert and preview locally before tagging

## Troubleshooting

### Document Not Found

- ✅ Check document path matches tag name pattern
- ✅ Verify document exists in repository
- ✅ Use manual dispatch with explicit `source_path`

### Conversion Fails

- ✅ Check HTML Viewer tool dependencies are installed
- ✅ Verify document is valid markdown
- ✅ Check workflow logs for specific errors

### Wrong Version Deployed

- ✅ Verify tag format: `publish-<doc>-v<version>`
- ✅ Check workflow logs for version detection
- ✅ Use manual dispatch to override

