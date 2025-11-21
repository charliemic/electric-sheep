#!/usr/bin/env python3
"""
Upload HTML document to Google Docs using Google Drive API.

This script:
1. Authenticates with Google using OAuth2
2. Converts HTML to Google Docs format
3. Uploads/creates the document in Google Drive

Requirements:
    pip install google-api-python-client google-auth-httplib2 google-auth-oauthlib

Usage:
    python scripts/upload-to-google-docs.py [html_file] [--title "Document Title"]
"""

import os
import sys
import argparse
from pathlib import Path

try:
    from google.oauth2.credentials import Credentials
    from google_auth_oauthlib.flow import InstalledAppFlow
    from google.auth.transport.requests import Request
    from googleapiclient.discovery import build
    from googleapiclient.http import MediaIoBaseUpload
    import io
    import pickle
except ImportError:
    print("Error: Required packages not installed.")
    print("Install with: pip install google-api-python-client google-auth-httplib2 google-auth-oauthlib")
    sys.exit(1)

# Scopes required for Google Drive and Docs API
SCOPES = [
    'https://www.googleapis.com/auth/drive.file',
    'https://www.googleapis.com/auth/documents'
]

# Token storage location
TOKEN_FILE = Path.home() / '.google-docs-token.pickle'
CREDENTIALS_FILE = Path.home() / '.google-docs-credentials.json'


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
                print("Google OAuth Credentials Setup")
                print("="*60)
                print("\nTo use this script, you need to:")
                print("1. Go to https://console.cloud.google.com/")
                print("2. Create a new project (or select existing)")
                print("3. Enable 'Google Drive API' and 'Google Docs API'")
                print("4. Create OAuth 2.0 credentials (Desktop app)")
                print("5. Download credentials as JSON")
                print(f"6. Save as: {CREDENTIALS_FILE}")
                print("\nAlternatively, use the web interface:")
                print("  1. Open Google Docs: https://docs.google.com")
                print("  2. File → Import → Upload")
                print("  3. Select your HTML file")
                print("="*60 + "\n")
                sys.exit(1)
            
            flow = InstalledAppFlow.from_client_secrets_file(
                str(CREDENTIALS_FILE), SCOPES)
            creds = flow.run_local_server(port=0)
        
        # Save credentials for next run
        with open(TOKEN_FILE, 'wb') as token:
            pickle.dump(creds, token)
    
    return creds


def html_to_google_docs_format(html_content):
    """
    Convert HTML to Google Docs format.
    Google Docs API expects requests in a specific format.
    For now, we'll upload as HTML and let Google convert it.
    """
    # Google Docs can import HTML directly, so we'll use that approach
    return html_content


def create_google_doc_from_html(service, html_content, title):
    """Create a Google Doc from HTML content."""
    # Method 1: Import HTML file to Google Docs
    # This requires uploading to Drive first, then converting
    
    # Create a temporary HTML file in memory
    html_file = io.BytesIO(html_content.encode('utf-8'))
    media = MediaIoBaseUpload(
        html_file,
        mimetype='text/html',
        resumable=True
    )
    
    # Upload to Drive
    file_metadata = {
        'name': f'{title}.html',
        'mimeType': 'text/html'
    }
    
    print(f"Uploading {title} to Google Drive...")
    file = service.files().create(
        body=file_metadata,
        media_body=media,
        fields='id'
    ).execute()
    
    file_id = file.get('id')
    print(f"File uploaded with ID: {file_id}")
    
    # Convert HTML to Google Docs format
    print("Converting to Google Docs format...")
    doc_file = service.files().copy(
        fileId=file_id,
        body={
            'name': title,
            'mimeType': 'application/vnd.google-apps.document'
        }
    ).execute()
    
    doc_id = doc_file.get('id')
    print(f"Google Doc created with ID: {doc_id}")
    
    # Delete the temporary HTML file
    service.files().delete(fileId=file_id).execute()
    print("Temporary HTML file deleted")
    
    return doc_id


def main():
    parser = argparse.ArgumentParser(
        description='Upload HTML document to Google Docs'
    )
    parser.add_argument(
        'html_file',
        nargs='?',
        default='docs/learning/A_WEEK_WITH_AI_CODING.html',
        help='Path to HTML file (default: docs/learning/A_WEEK_WITH_AI_CODING.html)'
    )
    parser.add_argument(
        '--title',
        default='A Week with AI-Driven Coding: What I Learned',
        help='Title for the Google Doc (default: A Week with AI-Driven Coding: What I Learned)'
    )
    
    args = parser.parse_args()
    
    html_path = Path(args.html_file)
    if not html_path.exists():
        print(f"Error: HTML file not found: {html_path}")
        sys.exit(1)
    
    # Read HTML content
    print(f"Reading HTML file: {html_path}")
    with open(html_path, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    # Authenticate
    print("\nAuthenticating with Google...")
    creds = get_credentials()
    
    # Build service
    print("Building Google Drive service...")
    service = build('drive', 'v3', credentials=creds)
    
    # Create Google Doc
    doc_id = create_google_doc_from_html(service, html_content, args.title)
    
    # Get document URL
    doc_url = f"https://docs.google.com/document/d/{doc_id}/edit"
    
    print("\n" + "="*60)
    print("SUCCESS!")
    print("="*60)
    print(f"\nDocument created: {args.title}")
    print(f"URL: {doc_url}")
    print("\nYou can now:")
    print("  - Share it with your team")
    print("  - Edit it in Google Docs")
    print("  - Export it in various formats")
    print("="*60 + "\n")


if __name__ == '__main__':
    main()

