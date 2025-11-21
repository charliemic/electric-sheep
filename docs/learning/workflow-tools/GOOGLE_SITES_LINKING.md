# Linking Google Sites to Google Docs/HTML

Step-by-step guide for connecting your Google Site to the documents you're producing.

## Method 1: Link to Google Doc (Recommended)

This creates a clickable link that opens the Google Doc directly.

### Steps

1. **Get the Google Doc URL**
   - Open your document in Google Docs: https://docs.google.com
   - Click "Share" button (top right)
   - Set sharing to "Anyone with the link" (if you want public access)
   - Copy the document URL (looks like: `https://docs.google.com/document/d/DOC_ID/edit`)

2. **Add Link to Google Site**
   - Go to your Google Site: https://sites.google.com
   - Edit the page where you want the link
   - Click "Insert" → "Link"
   - Paste the Google Doc URL
   - Add link text (e.g., "Read the full article")
   - Click "Apply"

3. **Result**
   - Visitors click the link and the Google Doc opens in a new tab
   - They can view and (if permissions allow) edit the document

## Method 2: Embed HTML File from Google Drive

This embeds the HTML content directly in your site.

### Steps

1. **Upload HTML to Google Drive** (if not already there)
   - The HTML file should already be in your CloudFiles folder
   - It syncs to Google Drive automatically
   - Or manually upload: https://drive.google.com → Upload

2. **Get Shareable Link**
   - In Google Drive, right-click the HTML file
   - Click "Get link" or "Share"
   - Set to "Anyone with the link can view"
   - Copy the link (looks like: `https://drive.google.com/file/d/FILE_ID/view`)

3. **Embed in Google Site**
   - Edit your Google Site page
   - Click "Insert" → "Embed"
   - Choose "By URL"
   - Paste the Google Drive link
   - Click "Insert"

4. **Result**
   - The HTML content appears embedded in your site
   - Visitors can view it without leaving the page

## Method 3: Direct HTML Embed (Advanced)

Embed the HTML content directly in the site.

### Steps

1. **Get HTML Content**
   - Open the HTML file from CloudFiles
   - Copy the HTML content (or use the file directly)

2. **Embed in Google Site**
   - Edit your Google Site page
   - Click "Insert" → "Embed"
   - Choose "Embed code"
   - Paste the HTML content
   - Click "Insert"

3. **Result**
   - HTML renders directly in the site
   - Fully integrated, no external links

## Method 4: Link to HTML File for Download

Simple link that lets visitors download the HTML file.

### Steps

1. **Get HTML File Link**
   - In Google Drive, right-click the HTML file
   - Click "Get link"
   - Set permissions as needed
   - Copy the link

2. **Add Download Link**
   - In Google Site, edit your page
   - Click "Insert" → "Link"
   - Paste the Google Drive link
   - Link text: "Download HTML version"
   - Click "Apply"

3. **Result**
   - Visitors click to download/open the HTML file
   - Opens in their browser or downloads

## Recommended Workflow

For your use case, I'd recommend **Method 1 (Link to Google Doc)** because:

✅ **Always up-to-date** - Link always points to latest version
✅ **Easy collaboration** - People can comment/edit if you allow
✅ **Simple setup** - Just copy/paste the Google Doc URL
✅ **Works well** - Google Sites handles Google Doc links nicely

### Quick Setup

1. Open your Google Doc: https://docs.google.com
2. Find your document (or create it from the DOCX)
3. Click "Share" → Set permissions → Copy link
4. In Google Site: Insert → Link → Paste URL → Done

## Keeping Content in Sync

Since you're maintaining both formats:

1. **Edit markdown** in Cursor
2. **Run script**: `./scripts/md-to-both.sh file.md`
3. **Both formats update** in CloudFiles
4. **Google Drive syncs** automatically
5. **Google Doc updates** (if you opened the DOCX in Docs)
6. **Google Site link** always points to latest version

## Tips

- **Use Google Doc link** for the main content (always current)
- **Use HTML link** for downloadable/shareable version
- **Set sharing permissions** appropriately (team-only vs. public)
- **Test the links** after publishing your site
- **Update links** if you move files in Google Drive

## Example Setup

```
Google Site Homepage
├── Link: "Read the Article" → Google Doc
├── Link: "Download HTML" → HTML file in Drive
└── Embedded: Preview snippet (optional)
```

This gives visitors multiple ways to access your content while keeping everything in sync.

