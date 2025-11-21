# Sharing HTML Documents via Google

Guide for maintaining and sharing HTML versions of documents using Google Drive.

## Quick Setup

Generate both DOCX and HTML formats:

```bash
# Generate both formats at once
./scripts/md-to-both.sh docs/learning/A_WEEK_WITH_AI_CODING.md

# Or generate individually
./scripts/md-to-google-doc.sh file.md  # DOCX for Google Docs
./scripts/md-to-html.sh file.md        # HTML for sharing
```

## Sharing HTML via Google Drive

### Method 1: Direct Share (Recommended)

1. **Upload HTML to Google Drive**
   - File is automatically synced from `~/CloudFiles/` to Google Drive
   - Or manually upload via https://drive.google.com

2. **Share the HTML File**
   - Right-click the HTML file in Google Drive
   - Click "Share" or "Get link"
   - Set permissions (Anyone with link, Specific people, etc.)
   - Copy the shareable link

3. **Viewers Can**
   - Download the HTML file
   - Open it in any web browser
   - View formatted content without needing Google Docs

### Method 2: Google Sites Embed

1. **Create a Google Site**
   - Go to https://sites.google.com
   - Create a new site

2. **Embed HTML Content**
   - Add an "Embed" block
   - Upload the HTML file or link to it
   - Or copy/paste HTML content directly

3. **Share the Site**
   - Publish the site
   - Share the site URL
   - Anyone can view the formatted HTML content

### Method 3: Google Docs Import

1. **Import HTML to Google Docs**
   - Go to https://docs.google.com
   - File → Import → Upload
   - Select your HTML file
   - Google Docs converts it to a native document

2. **Share the Google Doc**
   - Standard Google Docs sharing
   - Viewers see formatted content
   - Can edit if given permissions

## Workflow

### Maintain Parallel Formats

```bash
# After editing markdown, regenerate both formats
./scripts/md-to-both.sh docs/learning/A_WEEK_WITH_AI_CODING.md
```

Both files sync to Google Drive automatically:
- `a-week-with-ai-driven-coding.docx` → Edit in Google Docs
- `a-week-with-ai-driven-coding.html` → Share/view in browser

### Update Process

1. Edit markdown file in Cursor
2. Run `./scripts/md-to-both.sh file.md`
3. Both formats update in CloudFiles
4. Google Drive syncs automatically
5. Share either format as needed

## Benefits

✅ **DOCX**: Native Google Docs editing, collaboration
✅ **HTML**: Direct browser viewing, easy sharing, no conversion needed
✅ **Parallel**: Both formats stay in sync
✅ **Automatic**: Google Drive handles syncing

## File Locations

- **Source**: `docs/learning/A_WEEK_WITH_AI_CODING.md`
- **DOCX**: `~/CloudFiles/a-week-with-ai-driven-coding.docx`
- **HTML**: `~/CloudFiles/a-week-with-ai-driven-coding.html`
- **Google Drive**: Both files sync automatically

