# Google Docs Workflow

Seamless bidirectional workflow: Cursor (Markdown) ↔ Google Docs

## Quick Start

```bash
# Convert Markdown to DOCX and sync to Google Drive
./scripts/md-to-google-doc.sh docs/learning/A_WEEK_WITH_AI_CODING.md
```

That's it! The file will:
1. Convert from Markdown to DOCX
2. Copy to your CloudFiles folder
3. Sync to Google Drive automatically
4. Open in Google Docs (double-click or via web)

## The Workflow

### A) Prompt in Cursor
- Write/edit in Markdown (`.md` files)
- Use Cursor's AI to generate or refine content

### B) Upload to Google Docs
```bash
./scripts/md-to-google-doc.sh your-file.md
```

This:
- Converts Markdown → DOCX
- Copies to `~/CloudFiles/`
- Syncs to Google Drive automatically

### C) Edit in Google Docs
- Go to https://drive.google.com
- Find your `.docx` file
- Double-click or Right-click → Open with → Google Docs
- Edit in Google Docs (changes sync back to CloudFiles automatically)

### D) Download Back (if needed)
- The DOCX in CloudFiles updates automatically when you save in Google Docs
- Or download from Google Drive web interface
- Convert back to Markdown if needed (manual or via script)

## File Locations

- **Source**: `docs/learning/A_WEEK_WITH_AI_CODING.md` (Markdown)
- **Sync folder**: `~/CloudFiles/` (DOCX files)
- **Google Drive**: Automatically synced from CloudFiles

## Benefits

✅ **Direct conversion** - Markdown → DOCX (no HTML intermediate step)
✅ **Automatic sync** - Google Drive handles syncing
✅ **Native Google Docs** - DOCX opens directly in Google Docs
✅ **Bidirectional** - Edit in Docs, changes sync back
✅ **Simple** - One command to upload

## Scripts

- **`scripts/md-to-google-doc.sh`** - Main workflow script (Markdown → DOCX → CloudFiles)
- **`scripts/convert-to-docx.py`** - Conversion engine (Markdown → DOCX)

## Example

```bash
# 1. Edit in Cursor (Markdown)
vim docs/learning/my-blog.md

# 2. Upload to Google Docs
./scripts/md-to-google-doc.sh docs/learning/my-blog.md

# 3. Edit in Google Docs (via https://drive.google.com)
# Changes sync automatically to ~/CloudFiles/

# 4. Continue editing locally or in Docs
# Repeat as needed
```

