# Quick Start: Publishing Documents

## Publish a Document

### Step 1: Tag Your Document

```bash
# Tag a specific version
git tag publish-cognitive-journey-v1.0.0
git push origin publish-cognitive-journey-v1.0.0
```

### Step 2: Deploy as Latest (Optional)

```bash
# Make it the default/latest version
git tag publish-cognitive-journey-latest
git push origin publish-cognitive-journey-latest
```

## Example: Publishing `/tmp/cognitive-journey.md`

If you have a document at `/tmp/cognitive-journey.md`:

```bash
# 1. Copy to repository (if not already there)
cp /tmp/cognitive-journey.md docs/learning/cognitive-journey.md

# 2. Commit it
git add docs/learning/cognitive-journey.md
git commit -m "Add cognitive journey document"

# 3. Tag for publication
git tag publish-cognitive-journey-v1.0.0
git push origin publish-cognitive-journey-v1.0.0

# 4. Deploy as latest
git tag publish-cognitive-journey-latest
git push origin publish-cognitive-journey-latest
```

## Tag Format

- **Version**: `publish-<doc-name>-v<version>` (e.g., `publish-cognitive-journey-v1.0.0`)
- **Latest**: `publish-<doc-name>-latest` (e.g., `publish-cognitive-journey-latest`)

## Result

After tagging, the workflow will:
1. ✅ Find your document automatically
2. ✅ Convert it to HTML using the HTML Viewer tool
3. ✅ Deploy to GitHub Pages
4. ✅ Make it available at: `https://<username>.github.io/<repo>/cognitive-journey.html`

## Manual Publication

If automatic discovery doesn't work, use manual dispatch:

```bash
gh workflow run publish-document.yml \
  -f document=cognitive-journey \
  -f version=v1.0.0 \
  -f source_path=docs/learning/cognitive-journey.md
```

