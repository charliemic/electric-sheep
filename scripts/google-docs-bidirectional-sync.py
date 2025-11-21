#!/usr/bin/env python3
"""
Google Docs Bidirectional Sync Workflow

Seamless workflow:
A) Prompt in Cursor → B) Upload to Google Docs → C/D) Edit in Docs or Cursor → Auto-sync

Uses Google Drive local sync folder for automatic synchronization.

Usage:
    # Upload HTML to Google Drive (auto-syncs to Google Docs)
    python scripts/google-docs-bidirectional-sync.py upload file.html
    
    # Download from Google Drive (after editing in Google Docs)
    python scripts/google-docs-bidirectional-sync.py download "Document Title"
    
    # List synced documents
    python scripts/google-docs-bidirectional-sync.py list
    
    # Setup: Find and configure Google Drive folder
    python scripts/google-docs-bidirectional-sync.py setup
"""

import sys
import argparse
import json
import shutil
import subprocess
from pathlib import Path
from datetime import datetime
import webbrowser

# Configuration file
CONFIG_FILE = Path.home() / '.google-docs-sync-config.json'
SYNC_FOLDER_NAME = "AI Coding Docs"  # Folder name in Google Drive


def find_google_drive_folder():
    """Find Google Drive folder using macOS File Provider location."""
    # Check File Provider location (macOS 12.1+)
    cloud_storage = Path.home() / "Library/CloudStorage"
    if cloud_storage.exists():
        # Look for GoogleDrive-* folders
        for item in cloud_storage.iterdir():
            if item.is_dir() and 'GoogleDrive' in item.name or 'Google Drive' in item.name:
                return item
    
    # Check legacy location
    legacy_paths = [
        Path.home() / "Google Drive",
        Path("/Volumes/GoogleDrive"),
        Path.home() / "Drive",
    ]
    
    for path in legacy_paths:
        if path.exists() and path.is_dir():
            return path
    
    return None


def setup_google_drive():
    """Interactive setup to find and configure Google Drive folder."""
    print("\n" + "="*60)
    print("Google Drive Setup")
    print("="*60)
    print("\nThis will help you set up Google Drive sync for seamless workflow.")
    print("\nStep 1: Install Google Drive for Desktop (if not installed)")
    print("  Download: https://www.google.com/drive/download/")
    print("  Install and sign in with your Google account")
    print("\nStep 2: Enable Google Drive in Finder")
    print("  - Open Finder")
    print("  - In sidebar under 'Locations', click 'Google Drive'")
    print("  - Click 'Enable' if prompted")
    print("\nStep 3: Find your Google Drive folder")
    print("  On macOS, it's typically at:")
    print("    ~/Library/CloudStorage/GoogleDrive-*")
    print("  Or check Finder sidebar for 'Google Drive'")
    print("\n" + "="*60)
    
    # Try to auto-detect
    drive_folder = find_google_drive_folder()
    
    if drive_folder:
        print(f"\n✓ Found Google Drive folder: {drive_folder}")
        use_this = input(f"\nUse this folder? (y/n): ").strip().lower()
        if use_this == 'y':
            save_config({'drive_folder': str(drive_folder)})
            print(f"✓ Configuration saved!")
            return drive_folder
    else:
        print("\n✗ Could not auto-detect Google Drive folder")
    
    # Manual entry
    print("\nPlease enter your Google Drive folder path:")
    print("  (You can find it in Finder: right-click 'Google Drive' → Get Info)")
    path_input = input("Path: ").strip()
    
    if path_input:
        drive_path = Path(path_input).expanduser()
        if drive_path.exists():
            save_config({'drive_folder': str(drive_path)})
            print(f"✓ Configuration saved: {drive_path}")
            return drive_path
        else:
            print(f"✗ Path does not exist: {drive_path}")
    
    print("\nSetup incomplete. You can specify the path manually:")
    print("  python scripts/google-docs-bidirectional-sync.py upload file.html --drive-path '/path/to/Google Drive'")
    return None


def load_config():
    """Load configuration from file."""
    if CONFIG_FILE.exists():
        with open(CONFIG_FILE, 'r') as f:
            return json.load(f)
    return {}


def save_config(config):
    """Save configuration to file."""
    existing = load_config()
    existing.update(config)
    with open(CONFIG_FILE, 'w') as f:
        json.dump(existing, f, indent=2)


def get_sync_folder(drive_path=None):
    """Get or create the sync folder in Google Drive."""
    config = load_config()
    
    if not drive_path:
        drive_path = config.get('drive_folder')
        if drive_path:
            drive_path = Path(drive_path)
        else:
            drive_path = find_google_drive_folder()
    
    if not drive_path or not drive_path.exists():
        print("\n✗ Google Drive folder not found or not configured")
        print("Run setup: python scripts/google-docs-bidirectional-sync.py setup")
        return None, None
    
    sync_folder = Path(drive_path) / SYNC_FOLDER_NAME
    sync_folder.mkdir(exist_ok=True)
    
    return sync_folder, drive_path


def upload_to_drive(html_file_path, drive_path=None, title=None):
    """Upload HTML file to Google Drive sync folder."""
    html_path = Path(html_file_path)
    if not html_path.exists():
        print(f"Error: HTML file not found: {html_path}")
        sys.exit(1)
    
    sync_folder, drive_base = get_sync_folder(drive_path)
    if not sync_folder:
        sys.exit(1)
    
    title = title or html_path.stem.replace('_', ' ').title()
    dest_file = sync_folder / f"{title}.html"
    
    # Copy file
    print(f"Copying to Google Drive: {dest_file.name}")
    shutil.copy2(html_path, dest_file)
    print(f"✓ File copied to: {dest_file}")
    print(f"  (Syncing to Google Drive automatically...)")
    
    # Save metadata
    config = load_config()
    if 'documents' not in config:
        config['documents'] = {}
    
    config['documents'][title] = {
        'html_file': str(html_path),
        'drive_file': str(dest_file),
        'last_uploaded': datetime.now().isoformat()
    }
    save_config(config)
    
    # Instructions
    print("\n" + "="*60)
    print("Next Steps")
    print("="*60)
    print("\n1. Wait for Google Drive to sync (check Drive icon in menu bar)")
    print("2. Open in Google Docs:")
    print(f"   a) Go to https://drive.google.com")
    print(f"   b) Find '{dest_file.name}' in '{SYNC_FOLDER_NAME}' folder")
    print(f"   c) Right-click → Open with → Google Docs")
    print("\n3. Edit in Google Docs (changes sync automatically)")
    print("\n4. To download back:")
    print(f"   python scripts/google-docs-bidirectional-sync.py download '{title}'")
    print("="*60 + "\n")
    
    # Try to open Google Drive
    try:
        webbrowser.open("https://drive.google.com")
    except:
        pass
    
    return dest_file


def download_from_drive(title, drive_path=None, output_path=None):
    """Download HTML file from Google Drive sync folder."""
    sync_folder, _ = get_sync_folder(drive_path)
    if not sync_folder:
        sys.exit(1)
    
    # Find the file
    html_file = sync_folder / f"{title}.html"
    if not html_file.exists():
        print(f"Error: File not found: {html_file}")
        print(f"\nAvailable files in sync folder:")
        for f in sync_folder.glob("*.html"):
            print(f"  - {f.stem}")
        sys.exit(1)
    
    # Determine output path
    if not output_path:
        config = load_config()
        if 'documents' in config and title in config['documents']:
            original_path = config['documents'][title].get('html_file')
            if original_path and Path(original_path).parent.exists():
                output_path = original_path
            else:
                output_path = f"downloaded_{title.replace(' ', '_')}.html"
        else:
            output_path = f"downloaded_{title.replace(' ', '_')}.html"
    
    output_path = Path(output_path)
    
    # Copy file
    print(f"Downloading from Google Drive: {html_file.name}")
    shutil.copy2(html_file, output_path)
    print(f"✓ File downloaded to: {output_path}")
    
    # Update metadata
    config = load_config()
    if 'documents' in config and title in config['documents']:
        config['documents'][title]['last_downloaded'] = datetime.now().isoformat()
        config['documents'][title]['html_file'] = str(output_path)
        save_config(config)
    
    return output_path


def list_documents(drive_path=None):
    """List documents in sync folder."""
    sync_folder, _ = get_sync_folder(drive_path)
    if not sync_folder:
        sys.exit(1)
    
    html_files = list(sync_folder.glob("*.html"))
    
    if not html_files:
        print(f"\nNo documents found in sync folder: {sync_folder}")
        return
    
    print(f"\nDocuments in sync folder ({len(html_files)}):\n")
    
    config = load_config()
    for html_file in sorted(html_files):
        title = html_file.stem
        doc_info = config.get('documents', {}).get(title, {})
        
        print(f"  {title}")
        print(f"    File: {html_file.name}")
        if doc_info.get('last_uploaded'):
            print(f"    Last uploaded: {doc_info['last_uploaded']}")
        if doc_info.get('last_downloaded'):
            print(f"    Last downloaded: {doc_info['last_downloaded']}")
        print()
    
    print(f"Sync folder: {sync_folder}")
    print(f"Google Drive: https://drive.google.com")


def main():
    parser = argparse.ArgumentParser(
        description='Bidirectional sync between local HTML and Google Docs via Google Drive',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Workflow:
  1. Upload: python scripts/google-docs-bidirectional-sync.py upload file.html
  2. Edit in Google Docs (changes sync automatically to Drive)
  3. Download: python scripts/google-docs-bidirectional-sync.py download "Title"
  4. Edit locally or prompt changes in Cursor
  5. Repeat from step 1

Examples:
  # Upload
  python scripts/google-docs-bidirectional-sync.py upload docs/learning/A_WEEK_WITH_AI_CODING.html
  
  # Download after editing in Google Docs
  python scripts/google-docs-bidirectional-sync.py download "A Week with AI-Driven Coding: What I Learned"
  
  # List all documents
  python scripts/google-docs-bidirectional-sync.py list
  
  # Setup Google Drive folder
  python scripts/google-docs-bidirectional-sync.py setup
        """
    )
    
    subparsers = parser.add_subparsers(dest='command', help='Command')
    
    # Setup command
    setup_parser = subparsers.add_parser('setup', help='Set up Google Drive folder')
    
    # Upload command
    upload_parser = subparsers.add_parser('upload', help='Upload HTML to Google Drive')
    upload_parser.add_argument('html_file', help='Path to HTML file')
    upload_parser.add_argument('--title', help='Document title')
    upload_parser.add_argument('--drive-path', help='Google Drive folder path (overrides config)')
    
    # Download command
    download_parser = subparsers.add_parser('download', help='Download HTML from Google Drive')
    download_parser.add_argument('title', help='Document title (as it appears in Drive)')
    download_parser.add_argument('--output', help='Output file path')
    download_parser.add_argument('--drive-path', help='Google Drive folder path (overrides config)')
    
    # List command
    list_parser = subparsers.add_parser('list', help='List documents in sync folder')
    list_parser.add_argument('--drive-path', help='Google Drive folder path (overrides config)')
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    if args.command == 'setup':
        setup_google_drive()
    
    elif args.command == 'upload':
        upload_to_drive(args.html_file, getattr(args, 'drive_path', None), args.title)
    
    elif args.command == 'download':
        download_from_drive(args.title, getattr(args, 'drive_path', None), args.output)
    
    elif args.command == 'list':
        list_documents(getattr(args, 'drive_path', None))


if __name__ == '__main__':
    main()

