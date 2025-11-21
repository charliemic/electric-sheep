# Google OAuth Setup for Docs Workflow

Step-by-step guide to set up OAuth credentials for the Google Docs workflow.

## Quick Setup (5 minutes)

### Step 1: Go to Google Cloud Console
Open: https://console.cloud.google.com/

### Step 2: Create or Select Project
- If you have an existing project, select it from the dropdown
- Otherwise, click "Create Project"
  - Project name: "Google Docs Workflow" (or any name)
  - Click "Create"

### Step 3: Enable Required APIs
1. In the left sidebar, go to **"APIs & Services"** → **"Library"**
2. Search for **"Google Drive API"**
   - Click on it
   - Click **"ENABLE"** button
3. Search for **"Google Docs API"**
   - Click on it
   - Click **"ENABLE"** button

### Step 4: Create OAuth 2.0 Credentials
1. Go to **"APIs & Services"** → **"Credentials"**
2. Click **"+ CREATE CREDENTIALS"** at the top
3. Select **"OAuth client ID"**
4. If prompted, configure OAuth consent screen:
   - User Type: **"External"** (or "Internal" if using Google Workspace)
   - App name: **"Google Docs Workflow"**
   - User support email: Your email
   - Developer contact: Your email
   - Click **"SAVE AND CONTINUE"**
   - Scopes: Click **"SAVE AND CONTINUE"** (no scopes needed here)
   - Test users: Add your email, click **"SAVE AND CONTINUE"**
   - Summary: Click **"BACK TO DASHBOARD"**
5. Back at credentials:
   - Application type: **"Desktop app"**
   - Name: **"Google Docs Workflow"** (or any name)
   - Click **"CREATE"**
6. **IMPORTANT**: A popup will show your credentials
   - Click the **download icon (⬇)** to download the JSON file
   - Save it as: `~/.google-docs-credentials.json`
   - On Mac: `/Users/YOUR_USERNAME/.google-docs-credentials.json`

### Step 5: Verify Setup
Run the workflow script:
```bash
python3 scripts/google-docs-workflow.py list
```

The first time, it will:
1. Open your browser
2. Ask you to sign in with Google
3. Request permissions (Drive and Docs)
4. Save credentials for future use

## File Locations

- **Credentials file**: `~/.google-docs-credentials.json` (you download this)
- **Token file**: `~/.google-docs-token.pickle` (created automatically after first auth)
- **Workflow config**: `~/.google-docs-workflow.json` (stores document IDs)

## Troubleshooting

### "Credentials file not found"
- Make sure you saved the JSON file as `~/.google-docs-credentials.json`
- Check the path: `ls ~/.google-docs-credentials.json`

### "Access blocked" or "App not verified"
- This is normal for personal projects
- Click "Advanced" → "Go to [Your App] (unsafe)"
- This is safe for your personal use

### "Invalid credentials"
- Delete the token file: `rm ~/.google-docs-token.pickle`
- Run the script again to re-authenticate

## Next Steps

Once set up, you can use the workflow:
- `python3 scripts/google-docs-workflow.py upload file.html`
- `python3 scripts/google-docs-workflow.py download DOC_ID --output file.html`
- `python3 scripts/google-docs-workflow.py list`

