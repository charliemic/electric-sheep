# Google Drive Sync Setup

Using Google Drive's local sync folder for automatic file sync to Google Docs.

## Setup Options

### Option 1: Install Google Drive for Desktop (Recommended)

If you don't have Google Drive installed:

1. **Download Google Drive for Desktop**:
   - Go to: https://www.google.com/drive/download/
   - Download and install "Drive for desktop"

2. **Sign in and set up sync**:
   - Sign in with your Google account
   - Choose which folders to sync
   - Note the sync folder location (usually `~/Google Drive`)

3. **Use the script**:
   ```bash
   python3 scripts/google-docs-drive-sync.py docs/learning/A_WEEK_WITH_AI_CODING.html
   ```

### Option 2: Find Existing Google Drive Folder

If Google Drive is already installed:

1. **Find the folder**:
   ```bash
   ./scripts/find-google-drive.sh
   ```

2. **Or manually locate it**:
   - Open Finder
   - Look in sidebar for "Google Drive"
   - Or check: `~/Google Drive` or `~/Library/CloudStorage/`

3. **Use with explicit path**:
   ```bash
   python3 scripts/google-docs-drive-sync.py file.html --drive-path "/path/to/Google Drive"
   ```

### Option 3: Set Environment Variable

For convenience, set the path once:

```bash
# Add to ~/.zshrc or ~/.bashrc
export GOOGLE_DRIVE_PATH="$HOME/Google Drive"

# Then use without --drive-path
python3 scripts/google-docs-drive-sync.py file.html
```

## Workflow

Once set up:

1. **Upload to Drive**:
   ```bash
   python3 scripts/google-docs-drive-sync.py docs/learning/A_WEEK_WITH_AI_CODING.html
   ```

2. **Wait for sync** (check Drive icon in menu bar)

3. **Open in Google Docs**:
   - Go to https://docs.google.com
   - File → Import → Upload
   - Select the file from Google Drive
   - Or: https://drive.google.com → Right-click file → Open with → Google Docs

4. **Edit in Google Docs** (changes sync automatically)

5. **Download back** (if needed):
   - In Google Docs: File → Download → HTML
   - Or use Google Drive web interface

## Benefits

- ✅ No OAuth setup required
- ✅ No API credentials needed
- ✅ Automatic sync
- ✅ Works with existing Google account
- ✅ Simple file operations

## Troubleshooting

**"Google Drive folder not found"**:
- Install Google Drive for Desktop
- Or specify path with `--drive-path`

**"File not syncing"**:
- Check Google Drive icon in menu bar
- Ensure you're signed in
- Check sync status in Drive settings

**"Can't open in Google Docs"**:
- Use web interface: https://drive.google.com
- Right-click file → Open with → Google Docs

