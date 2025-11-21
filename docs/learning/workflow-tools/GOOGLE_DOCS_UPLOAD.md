# Uploading to Google Docs

This guide explains how to upload the blog post to Google Docs using your Google SSO.

## Quick Method (Recommended)

The simplest way is to use Google Docs' web interface:

1. **Open Google Docs**: https://docs.google.com
2. **File → Import → Upload**
3. **Select**: `docs/learning/A_WEEK_WITH_AI_CODING.html`
4. Google Docs will automatically convert the HTML to a Google Doc

## Automated Method (Using Script)

If you prefer automation, use the Python script:

### Prerequisites

1. **Install Python packages**:
   ```bash
   pip install google-api-python-client google-auth-httplib2 google-auth-oauthlib
   ```

2. **Set up Google OAuth credentials**:
   - Go to https://console.cloud.google.com/
   - Create a new project (or select existing)
   - Enable "Google Drive API" and "Google Docs API"
   - Create OAuth 2.0 credentials (Desktop app type)
   - Download credentials as JSON
   - Save as: `~/.google-docs-credentials.json`

### Run the Script

```bash
# Default (uses A_WEEK_WITH_AI_CODING.html)
python scripts/upload-to-google-docs.py

# Custom file and title
python scripts/upload-to-google-docs.py path/to/file.html --title "My Document Title"
```

### First Run

On first run, the script will:
1. Open your browser for Google OAuth authentication
2. Ask you to sign in with your Google account
3. Request permissions for Google Drive and Docs
4. Save credentials for future use (in `~/.google-docs-token.pickle`)

### What It Does

1. Authenticates with Google using OAuth2
2. Uploads the HTML file to Google Drive
3. Converts it to Google Docs format
4. Deletes the temporary HTML file
5. Provides you with the document URL

## Files

- **HTML version**: `docs/learning/A_WEEK_WITH_AI_CODING.html` (ready for upload)
- **Script**: `scripts/upload-to-google-docs.py` (automated upload)
- **Original**: `docs/learning/A_WEEK_WITH_AI_CODING.md` (source markdown)

## Notes

- The HTML file is formatted for Google Docs import
- Google Docs will preserve headings, lists, and basic formatting
- You may need to adjust formatting after import (spacing, fonts, etc.)
- The script uses OAuth2 and stores credentials locally (secure)

