#!/usr/bin/env python3
"""
Google Docs API Upload - Simplified OAuth

Uses Google's API with a one-time token setup. No project creation needed if you
can get an access token from your existing Google session.

Alternative: Use Google's "Import from URL" feature with a temporary file host.

Usage:
    # Option 1: Get token from browser (one-time setup)
    python scripts/google-docs-api-upload.py setup-token
    
    # Option 2: Upload with existing token
    python scripts/google-docs-api-upload.py upload file.html --token YOUR_TOKEN
    
    # Option 3: Use import URL (requires hosting file temporarily)
    python scripts/google-docs-api-upload.py upload-url file.html
"""

import sys
import argparse
import json
import requests
from pathlib import Path
import webbrowser
import urllib.parse

TOKEN_FILE = Path.home() / '.google-docs-access-token.txt'


def get_token_from_browser():
    """
    Guide user to get access token from browser.
    Uses Google OAuth playground or manual token extraction.
    """
    print("\n" + "="*60)
    print("Getting Google Access Token")
    print("="*60)
    print("\nMethod 1: Google OAuth Playground (Easiest)")
    print("1. Go to: https://developers.google.com/oauthplayground/")
    print("2. In 'Step 1', find and select:")
    print("   - https://www.googleapis.com/auth/drive.file")
    print("   - https://www.googleapis.com/auth/documents")
    print("3. Click 'Authorize APIs'")
    print("4. Sign in with your Google account")
    print("5. Click 'Exchange authorization code for tokens'")
    print("6. Copy the 'Access token' value")
    print("\nMethod 2: Browser Developer Tools")
    print("1. Open Google Docs in your browser")
    print("2. Open Developer Tools (F12)")
    print("3. Go to Network tab")
    print("4. Filter by 'docs.google.com'")
    print("5. Look for requests with 'Authorization: Bearer' header")
    print("6. Copy the token value")
    print("\nOnce you have the token, save it:")
    print(f"  echo 'YOUR_TOKEN' > {TOKEN_FILE}")
    print("="*60 + "\n")


def upload_via_api(html_file_path, access_token, title=None):
    """Upload HTML to Google Docs using Drive API."""
    html_path = Path(html_file_path)
    if not html_path.exists():
        print(f"Error: HTML file not found: {html_path}")
        sys.exit(1)
    
    title = title or html_path.stem.replace('_', ' ').title()
    
    # Read HTML content
    with open(html_path, 'r', encoding='utf-8') as f:
        html_content = f.read()
    
    print(f"Uploading '{title}' to Google Drive...")
    
    # Step 1: Upload HTML file to Drive
    upload_url = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
    
    metadata = {
        'name': f'{title}.html',
        'mimeType': 'text/html'
    }
    
    # Create multipart request
    boundary = '----WebKitFormBoundary7MA4YWxkTrZu0gW'
    body = (
        f'--{boundary}\r\n'
        f'Content-Disposition: form-data; name="metadata"\r\n'
        f'Content-Type: application/json; charset=UTF-8\r\n\r\n'
        f'{json.dumps(metadata)}\r\n'
        f'--{boundary}\r\n'
        f'Content-Disposition: form-data; name="file"; filename="{title}.html"\r\n'
        f'Content-Type: text/html\r\n\r\n'
        f'{html_content}\r\n'
        f'--{boundary}--\r\n'
    )
    
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Content-Type': f'multipart/related; boundary={boundary}',
        'Content-Length': str(len(body.encode('utf-8')))
    }
    
    try:
        response = requests.post(upload_url, headers=headers, data=body.encode('utf-8'))
        response.raise_for_status()
        file_data = response.json()
        file_id = file_data['id']
        print(f"✓ File uploaded (ID: {file_id})")
    except requests.exceptions.RequestException as e:
        print(f"Error uploading file: {e}")
        if hasattr(e.response, 'text'):
            print(f"Response: {e.response.text}")
        sys.exit(1)
    
    # Step 2: Convert to Google Docs format
    print("Converting to Google Docs format...")
    copy_url = f"https://www.googleapis.com/drive/v3/files/{file_id}/copy"
    
    copy_metadata = {
        'name': title,
        'mimeType': 'application/vnd.google-apps.document'
    }
    
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Content-Type': 'application/json'
    }
    
    try:
        response = requests.post(copy_url, headers=headers, json=copy_metadata)
        response.raise_for_status()
        doc_data = response.json()
        doc_id = doc_data['id']
        print(f"✓ Google Doc created (ID: {doc_id})")
    except requests.exceptions.RequestException as e:
        print(f"Error converting to Docs: {e}")
        if hasattr(e.response, 'text'):
            print(f"Response: {e.response.text}")
        sys.exit(1)
    
    # Step 3: Delete temporary HTML file
    print("Cleaning up temporary file...")
    delete_url = f"https://www.googleapis.com/drive/v3/files/{file_id}"
    headers = {'Authorization': f'Bearer {access_token}'}
    requests.delete(delete_url, headers=headers)
    print("✓ Temporary file deleted")
    
    # Return document URL
    doc_url = f"https://docs.google.com/document/d/{doc_id}/edit"
    
    print("\n" + "="*60)
    print("SUCCESS!")
    print("="*60)
    print(f"\nDocument: {title}")
    print(f"ID: {doc_id}")
    print(f"URL: {doc_url}")
    print("="*60 + "\n")
    
    return doc_id, doc_url


def upload_via_import_url(html_file_path, title=None):
    """
    Alternative: Use Google Docs import URL feature.
    Requires hosting the file temporarily (e.g., GitHub Gist, pastebin, etc.)
    """
    print("\n" + "="*60)
    print("Import URL Method")
    print("="*60)
    print("\nThis method requires hosting the HTML file temporarily.")
    print("Options:")
    print("1. GitHub Gist (if file is in a repo)")
    print("2. Pastebin or similar service")
    print("3. Your own web server")
    print("\nOnce you have a URL, use Google Docs:")
    print("1. Go to https://docs.google.com")
    print("2. File → Import")
    print("3. Enter the URL")
    print("="*60 + "\n")


def main():
    parser = argparse.ArgumentParser(
        description='Upload HTML to Google Docs via API (simplified token method)'
    )
    subparsers = parser.add_subparsers(dest='command', help='Command')
    
    # Setup token command
    setup_parser = subparsers.add_parser('setup-token', help='Guide for getting access token')
    
    # Upload command
    upload_parser = subparsers.add_parser('upload', help='Upload HTML file')
    upload_parser.add_argument('html_file', help='Path to HTML file')
    upload_parser.add_argument('--token', help='Access token (or use saved token)')
    upload_parser.add_argument('--title', help='Document title')
    
    # Import URL command
    url_parser = subparsers.add_parser('upload-url', help='Get instructions for URL import')
    url_parser.add_argument('html_file', help='Path to HTML file')
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    if args.command == 'setup-token':
        get_token_from_browser()
    
    elif args.command == 'upload':
        # Get token
        if args.token:
            access_token = args.token
        elif TOKEN_FILE.exists():
            with open(TOKEN_FILE, 'r') as f:
                access_token = f.read().strip()
        else:
            print(f"Error: No access token found.")
            print(f"Run 'python scripts/google-docs-api-upload.py setup-token' for instructions")
            print(f"Or use --token YOUR_TOKEN")
            sys.exit(1)
        
        upload_via_api(args.html_file, access_token, args.title)
    
    elif args.command == 'upload-url':
        upload_via_import_url(args.html_file, args.title)


if __name__ == '__main__':
    main()

