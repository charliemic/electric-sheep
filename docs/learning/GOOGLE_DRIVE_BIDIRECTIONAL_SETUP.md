# Google Drive Bidirectional Sync Setup

Complete setup guide for seamless workflow: Cursor → Google Docs → Cursor

## Overview

This workflow uses Google Drive's local sync folder to automatically sync files between your local machine and Google Drive. When you edit files in Google Docs, they sync back to your local Drive folder automatically.

**Workflow:**
1. **Prompt in Cursor** → Generate/edit HTML
2. **Upload to Drive** → File syncs to Google Drive automatically
3. **Edit in Google Docs** → Changes sync back to Drive folder automatically
4. **Download from Drive** → Get updated file back to local
5. **Repeat** → Seamless round-trip editing

## Step-by-Step Setup

### Step 1: Install Google Drive for Desktop

1. **Download**: https://www.google.com/drive/download/
2. **Install** the application
3. **Sign in** with your Google account
4. **Enable sync** (choose which folders to sync)

### Step 2: Enable Google Drive in Finder (macOS)

According to [Google's documentation](https://support.google.com/drive/answer/12178485?hl=en-GB):

1. **Open Finder**
2. In the sidebar under **"Locations"**, click **"Google Drive"**
3. Click **"Enable"** if prompted
4. If you don't see it, you may need to give macOS permission:
   - Apple menu → **System Settings** → **Privacy and Security**
   - **Files and Folders** → Enable Google Drive

### Step 3: Find Your Google Drive Folder

On macOS with File Provider (macOS 12.1+), the folder is typically at:
```
~/Library/CloudStorage/GoogleDrive-*
```

**To find it:**
1. Open Finder
2. Look in sidebar for "Google Drive" under "Locations"
3. Right-click → **Get Info** to see the full path
4. Or check: `~/Library/CloudStorage/` for folders starting with "GoogleDrive"

**Alternative locations:**
- `~/Google Drive` (legacy)
- `/Volumes/GoogleDrive` (legacy)

### Step 4: Configure the Script

Run the setup:
```bash
python3 scripts/google-docs-bidirectional-sync.py setup
```

Or manually specify the path:
```bash
# Find your path first
ls ~/Library/CloudStorage/

# Then use it
python3 scripts/google-docs-bidirectional-sync.py upload file.html --drive-path "/Users/YOUR_USERNAME/Library/CloudStorage/GoogleDrive-XXXXX"
```

## Usage

### Upload HTML to Google Drive

```bash
python3 scripts/google-docs-bidirectional-sync.py upload docs/learning/A_WEEK_WITH_AI_CODING.html
```

This will:
- Copy the HTML file to your Google Drive sync folder
- File automatically syncs to Google Drive cloud
- Script opens Google Drive in browser
- You can then open it in Google Docs

### Open in Google Docs

After upload:
1. Go to https://drive.google.com
2. Find the file in the "AI Coding Docs" folder
3. Right-click → **Open with** → **Google Docs**
4. Edit in Google Docs (changes sync automatically!)

### Download After Editing

After editing in Google Docs, download the updated file:

```bash
python3 scripts/google-docs-bidirectional-sync.py download "A Week with AI-Driven Coding: What I Learned"
```

This downloads the synced file back to your local machine.

### List Documents

See all documents in your sync folder:

```bash
python3 scripts/google-docs-bidirectional-sync.py list
```

## Complete Workflow Example

```bash
# 1. Generate/edit HTML in Cursor (or prompt AI to create it)
#    File: docs/learning/A_WEEK_WITH_AI_CODING.html

# 2. Upload to Google Drive
python3 scripts/google-docs-bidirectional-sync.py upload docs/learning/A_WEEK_WITH_AI_CODING.html

# 3. Wait for sync (check Drive icon in menu bar)
# 4. Open in Google Docs via https://drive.google.com
# 5. Edit in Google Docs (changes sync automatically)

# 6. Download updated file
python3 scripts/google-docs-bidirectional-sync.py download "A Week with AI-Driven Coding: What I Learned"

# 7. Continue editing locally or prompt more changes
# 8. Repeat from step 2
```

## How It Works

1. **Local → Drive**: Script copies HTML to `~/Library/CloudStorage/GoogleDrive-*/AI Coding Docs/`
2. **Drive → Cloud**: Google Drive for Desktop automatically syncs to cloud
3. **Cloud → Docs**: You open the file in Google Docs (web interface)
4. **Docs → Cloud**: Changes in Google Docs save to Google Drive cloud
5. **Cloud → Drive**: Google Drive for Desktop syncs changes back to local folder
6. **Drive → Local**: Script downloads the updated file back to your project

## Troubleshooting

### "Google Drive folder not found"

**Solution**: 
1. Make sure Google Drive for Desktop is installed and running
2. Check Finder sidebar for "Google Drive"
3. Find the path: Right-click "Google Drive" in Finder → Get Info
4. Use `--drive-path` flag to specify it manually

### "File not syncing"

**Solution**:
1. Check Google Drive icon in menu bar (should show sync status)
2. Ensure you're signed in to Google Drive
3. Check sync settings in Google Drive preferences
4. Wait a few moments for sync to complete

### "Can't find file in Google Drive"

**Solution**:
1. Files are in the "AI Coding Docs" folder
2. Check: https://drive.google.com → Look for "AI Coding Docs" folder
3. Use the `list` command to see all synced files

### "Changes not appearing after editing in Docs"

**Solution**:
1. Make sure you saved in Google Docs (Ctrl+S / Cmd+S)
2. Wait for Google Drive to sync (check menu bar icon)
3. The file in `~/Library/CloudStorage/GoogleDrive-*/AI Coding Docs/` should update automatically
4. Then run the download command

## Benefits

✅ **No OAuth setup** - Uses your existing Google account
✅ **No API credentials** - Just file system operations
✅ **Automatic sync** - Google Drive handles all syncing
✅ **Bidirectional** - Edit in Docs or locally, changes sync both ways
✅ **Simple** - Just copy files to/from a folder

## File Locations

- **Sync folder**: `~/Library/CloudStorage/GoogleDrive-*/AI Coding Docs/`
- **Config file**: `~/.google-docs-sync-config.json`
- **Script**: `scripts/google-docs-bidirectional-sync.py`

## Next Steps

1. Install Google Drive for Desktop
2. Run setup: `python3 scripts/google-docs-bidirectional-sync.py setup`
3. Upload your first document
4. Start the seamless workflow!

