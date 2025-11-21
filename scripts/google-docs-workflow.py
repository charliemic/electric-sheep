#!/usr/bin/env python3
"""
Google Docs Workflow Manager

Supports bidirectional workflow:
A) Prompt in Cursor → B) Upload to Google Docs → C/D) Edit in Docs or Cursor → Repeat

Usage:
    # Upload HTML to Google Docs (creates or updates)
    python scripts/google-docs-workflow.py upload [html_file] [--title "Title"] [--doc-id "existing_doc_id"]
    
    # Download Google Doc back to HTML
    python scripts/google-docs-workflow.py download [doc_id] [--output html_file]
    
    # List your Google Docs
    python scripts/google-docs-workflow.py list [--search "query"]
    
    # Get document info
    python scripts/google-docs-workflow.py info [doc_id]
"""

import os
import sys
import argparse
import json
from pathlib import Path

try:
    from google.oauth2.credentials import Credentials
    from google_auth_oauthlib.flow import InstalledAppFlow
    from google.auth.transport.requests import Request
    from googleapiclient.discovery import build
    from googleapiclient.http import MediaIoBaseUpload, MediaIoBaseDownload
    import io
    import pickle
except ImportError:
    print("Error: Required packages not installed.")
    print("Install with: pip3 install google-api-python-client google-auth-httplib2 google-auth-oauthlib --user")
    sys.exit(1)

# Scopes required for Google Drive and Docs API
SCOPES = [
    'https://www.googleapis.com/auth/drive.file',
    'https://www.googleapis.com/auth/documents.readonly'
]

# Storage locations
TOKEN_FILE = Path.home() / '.google-docs-token.pickle'
CREDENTIALS_FILE = Path.home() / '.google-docs-credentials.json'
WORKFLOW_CONFIG = Path.home() / '.google-docs-workflow.json'


def get_credentials():
    """Get valid user credentials from storage or OAuth flow."""
    creds = None
    
    # Load existing token if available
    if TOKEN_FILE.exists():
        with open(TOKEN_FILE, 'rb') as token:
            creds = pickle.load(token)
    
    # If there are no (valid) credentials available, let the user log in
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            if not CREDENTIALS_FILE.exists():
                print("\n" + "="*60)
                print("Google OAuth Credentials Setup Required")
                print("="*60)
                print("\nStep 1: Go to https://console.cloud.google.com/")
                print("Step 2: Create a new project (or select existing)")
                print("Step 3: Enable APIs:")
                print("   - Go to 'APIs & Services' → 'Library'")
                print("   - Search for and enable 'Google Drive API'")
                print("   - Search for and enable 'Google Docs API'")
                print("Step 4: Create OAuth 2.0 credentials:")
                print("   - Go to 'APIs & Services' → 'Credentials'")
                print("   - Click '+ CREATE CREDENTIALS' → 'OAuth client ID'")
                print("   - Application type: 'Desktop app'")
                print("   - Name: 'Google Docs Workflow' (or any name)")
                print("   - Click 'CREATE'")
                print("Step 5: Download credentials:")
                print("   - Click the download icon (⬇) next to your OAuth client")
                print(f"   - Save the JSON file as: {CREDENTIALS_FILE}")
                print("\nOnce you've saved the credentials file, run this script again.")
                print("="*60 + "\n")
                sys.exit(1)
            
            print("\nOpening browser for Google authentication...")
            print("Please sign in and grant permissions.\n")
            flow = InstalledAppFlow.from_client_secrets_file(
                str(CREDENTIALS_FILE), SCOPES)
            creds = flow.run_local_server(port=0)
        
        # Save credentials for next run
        with open(TOKEN_FILE, 'wb') as token:
            pickle.dump(creds, token)
        print("✓ Credentials saved for future use\n")
    
    return creds


def load_workflow_config():
    """Load workflow configuration (document IDs, etc.)"""
    if WORKFLOW_CONFIG.exists():
        with open(WORKFLOW_CONFIG, 'r') as f:
            return json.load(f)
    return {}


def save_workflow_config(config):
    """Save workflow configuration"""
    with open(WORKFLOW_CONFIG, 'w') as f:
        json.dump(config, f, indent=2)


def upload_to_google_docs(service, html_content, title, doc_id=None):
    """Upload HTML to Google Docs, creating new or updating existing."""
    if doc_id:
        # Update existing document
        print(f"Updating existing document: {doc_id}")
        # For updates, we need to use Docs API to replace content
        # For now, we'll create a new version and delete the old one
        # (Google Docs API doesn't support direct HTML import for updates)
        print("Note: Google Docs API doesn't support direct HTML updates.")
        print("Creating new version...")
        doc_id = None  # Fall through to create new
    
    # Create new document
    html_file = io.BytesIO(html_content.encode('utf-8'))
    media = MediaIoBaseUpload(
        html_file,
        mimetype='text/html',
        resumable=True
    )
    
    file_metadata = {
        'name': f'{title}.html',
        'mimeType': 'text/html'
    }
    
    print(f"Uploading '{title}' to Google Drive...")
    file = service.files().create(
        body=file_metadata,
        media_body=media,
        fields='id'
    ).execute()
    
    temp_file_id = file.get('id')
    print(f"✓ Temporary file uploaded (ID: {temp_file_id})")
    
    # Convert to Google Docs format
    print("Converting to Google Docs format...")
    doc_file = service.files().copy(
        fileId=temp_file_id,
        body={
            'name': title,
            'mimeType': 'application/vnd.google-apps.document'
        }
    ).execute()
    
    new_doc_id = doc_file.get('id')
    print(f"✓ Google Doc created (ID: {new_doc_id})")
    
    # Delete temporary HTML file
    service.files().delete(fileId=temp_file_id).execute()
    print("✓ Temporary file deleted")
    
    return new_doc_id


def download_from_google_docs(service, doc_id, output_path):
    """Download Google Doc as HTML."""
    print(f"Downloading document {doc_id}...")
    
    # Export as HTML
    request = service.files().export_media(
        fileId=doc_id,
        mimeType='text/html'
    )
    
    fh = io.BytesIO()
    downloader = MediaIoBaseDownload(fh, request)
    done = False
    while done is False:
        status, done = downloader.next_chunk()
        if status:
            print(f"  Download progress: {int(status.progress() * 100)}%")
    
    fh.seek(0)
    html_content = fh.read().decode('utf-8')
    
    # Save to file
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(html_content)
    
    print(f"✓ Document downloaded to: {output_path}")
    return html_content


def list_documents(service, search_query=None):
    """List Google Docs."""
    query = "mimeType='application/vnd.google-apps.document'"
    if search_query:
        query += f" and name contains '{search_query}'"
    
    results = service.files().list(
        q=query,
        pageSize=20,
        fields="files(id, name, modifiedTime)"
    ).execute()
    
    files = results.get('files', [])
    
    if not files:
        print("No documents found.")
        return
    
    print(f"\nFound {len(files)} document(s):\n")
    for file in files:
        doc_id = file['id']
        name = file['name']
        modified = file.get('modifiedTime', 'Unknown')
        url = f"https://docs.google.com/document/d/{doc_id}/edit"
        print(f"  {name}")
        print(f"    ID: {doc_id}")
        print(f"    Modified: {modified}")
        print(f"    URL: {url}\n")


def get_document_info(service, doc_id):
    """Get information about a document."""
    try:
        file = service.files().get(
            fileId=doc_id,
            fields="id, name, createdTime, modifiedTime, webViewLink"
        ).execute()
        
        print(f"\nDocument: {file['name']}")
        print(f"  ID: {file['id']}")
        print(f"  Created: {file.get('createdTime', 'Unknown')}")
        print(f"  Modified: {file.get('modifiedTime', 'Unknown')}")
        print(f"  URL: {file.get('webViewLink', 'N/A')}\n")
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)


def main():
    parser = argparse.ArgumentParser(
        description='Google Docs Workflow Manager - Bidirectional sync between HTML and Google Docs',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Upload HTML to Google Docs
  python scripts/google-docs-workflow.py upload docs/learning/A_WEEK_WITH_AI_CODING.html
  
  # Upload with custom title
  python scripts/google-docs-workflow.py upload file.html --title "My Document"
  
  # Download Google Doc to HTML
  python scripts/google-docs-workflow.py download DOCUMENT_ID --output file.html
  
  # List your Google Docs
  python scripts/google-docs-workflow.py list
  
  # Search for documents
  python scripts/google-docs-workflow.py list --search "AI Coding"
  
  # Get document info
  python scripts/google-docs-workflow.py info DOCUMENT_ID
        """
    )
    
    subparsers = parser.add_subparsers(dest='command', help='Command to execute')
    
    # Upload command
    upload_parser = subparsers.add_parser('upload', help='Upload HTML to Google Docs')
    upload_parser.add_argument('html_file', help='Path to HTML file')
    upload_parser.add_argument('--title', help='Title for the Google Doc')
    upload_parser.add_argument('--doc-id', help='Document ID to update (creates new if not provided)')
    
    # Download command
    download_parser = subparsers.add_parser('download', help='Download Google Doc as HTML')
    download_parser.add_argument('doc_id', help='Google Doc ID')
    download_parser.add_argument('--output', help='Output HTML file path')
    
    # List command
    list_parser = subparsers.add_parser('list', help='List your Google Docs')
    list_parser.add_argument('--search', help='Search query to filter documents')
    
    # Info command
    info_parser = subparsers.add_parser('info', help='Get document information')
    info_parser.add_argument('doc_id', help='Google Doc ID')
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    # Authenticate
    print("Authenticating with Google...")
    creds = get_credentials()
    
    # Build services
    drive_service = build('drive', 'v3', credentials=creds)
    
    # Execute command
    if args.command == 'upload':
        html_path = Path(args.html_file)
        if not html_path.exists():
            print(f"Error: HTML file not found: {html_path}")
            sys.exit(1)
        
        with open(html_path, 'r', encoding='utf-8') as f:
            html_content = f.read()
        
        title = args.title or html_path.stem.replace('_', ' ').title()
        doc_id = args.doc_id
        
        doc_id = upload_to_google_docs(drive_service, html_content, title, doc_id)
        doc_url = f"https://docs.google.com/document/d/{doc_id}/edit"
        
        # Save to workflow config
        config = load_workflow_config()
        config[title] = {
            'doc_id': doc_id,
            'html_file': str(html_path),
            'url': doc_url
        }
        save_workflow_config(config)
        
        print("\n" + "="*60)
        print("SUCCESS!")
        print("="*60)
        print(f"\nDocument: {title}")
        print(f"ID: {doc_id}")
        print(f"URL: {doc_url}")
        print(f"\nSaved to workflow config. Use this ID for future updates:")
        print(f"  python scripts/google-docs-workflow.py upload {html_path} --doc-id {doc_id}")
        print("="*60 + "\n")
    
    elif args.command == 'download':
        output_path = args.output or f"downloaded_{args.doc_id}.html"
        download_from_google_docs(drive_service, args.doc_id, output_path)
        print(f"\n✓ You can now edit this file and re-upload it.")
    
    elif args.command == 'list':
        list_documents(drive_service, args.search)
    
    elif args.command == 'info':
        get_document_info(drive_service, args.doc_id)


if __name__ == '__main__':
    main()

