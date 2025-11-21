#!/usr/bin/env python3
"""
Google Docs Drive Sync

Saves HTML file to Google Drive folder (local sync), which automatically syncs to Google Drive.
Then opens Google Docs to import the file.

Usage:
    python scripts/google-docs-drive-sync.py [html_file] [--drive-path "/path/to/Google Drive"]
"""

import sys
import argparse
import subprocess
from pathlib import Path
import shutil

# Common Google Drive locations
DRIVE_PATHS = [
    Path.home() / "Google Drive",
    Path.home() / "Library/CloudStorage/GoogleDrive-*",
    Path("/Volumes/GoogleDrive"),
    Path.home() / "Drive",
]


def find_google_drive_folder():
    """Find Google Drive folder on the system."""
    # Check common locations
    for pattern in DRIVE_PATHS:
        if '*' in str(pattern):
            # Try to expand glob
            import glob
            matches = glob.glob(str(pattern))
            if matches:
                return Path(matches[0])
        else:
            if pattern.exists() and pattern.is_dir():
                return pattern
    
    # Try to find via mdfind
    try:
        result = subprocess.run(
            ['mdfind', '-name', 'Google Drive', '-onlyin', str(Path.home())],
            capture_output=True,
            text=True,
            timeout=5
        )
        if result.returncode == 0:
            for line in result.stdout.strip().split('\n'):
                path = Path(line)
                if path.is_dir() and 'Google Drive' in path.name:
                    return path
    except:
        pass
    
    return None


def sync_to_drive(html_file_path, drive_path=None, title=None):
    """Copy HTML file to Google Drive folder."""
    html_path = Path(html_file_path)
    if not html_path.exists():
        print(f"Error: HTML file not found: {html_path}")
        sys.exit(1)
    
    title = title or html_path.stem.replace('_', ' ').title()
    
    # Find or use provided drive path
    if drive_path:
        drive_folder = Path(drive_path)
    else:
        drive_folder = find_google_drive_folder()
    
    if not drive_folder or not drive_folder.exists():
        print("\n" + "="*60)
        print("Google Drive Folder Not Found")
        print("="*60)
        print("\nPlease specify your Google Drive folder path:")
        print("  python scripts/google-docs-drive-sync.py file.html --drive-path '/path/to/Google Drive'")
        print("\nOr set it as an environment variable:")
        print("  export GOOGLE_DRIVE_PATH='/path/to/Google Drive'")
        print("\nCommon locations:")
        print("  ~/Google Drive")
        print("  ~/Library/CloudStorage/GoogleDrive-*")
        print("="*60 + "\n")
        sys.exit(1)
    
    print(f"Found Google Drive folder: {drive_folder}")
    
    # Create destination path
    # Optionally create a subfolder for these documents
    docs_folder = drive_folder / "AI Coding Docs"
    docs_folder.mkdir(exist_ok=True)
    
    dest_file = docs_folder / f"{title}.html"
    
    # Copy file
    print(f"Copying to Google Drive: {dest_file}")
    shutil.copy2(html_path, dest_file)
    print(f"✓ File copied to Google Drive")
    print(f"  Local: {dest_file}")
    print(f"  (Will sync to Google Drive automatically)")
    
    # Wait a moment for sync to start
    import time
    print("\nWaiting for sync to start...")
    time.sleep(2)
    
    # Generate Google Docs import URL
    # Note: We can't directly open the file in Docs, but we can provide instructions
    print("\n" + "="*60)
    print("Next Steps")
    print("="*60)
    print("\n1. Wait for Google Drive to sync (check the Drive icon in menu bar)")
    print("2. Go to https://docs.google.com")
    print("3. Click File → Import → Upload")
    print(f"4. Navigate to: {dest_file.name}")
    print("   (or search for it in Google Drive)")
    print("\nAlternatively, open Google Drive web interface:")
    print("  https://drive.google.com")
    print(f"  Look for: {dest_file.name}")
    print("  Right-click → Open with → Google Docs")
    print("="*60 + "\n")
    
    # Try to open Google Drive in browser
    try:
        import webbrowser
        webbrowser.open("https://drive.google.com")
        print("Opened Google Drive in browser")
    except:
        pass
    
    return dest_file


def main():
    parser = argparse.ArgumentParser(
        description='Sync HTML file to Google Drive folder for automatic upload'
    )
    parser.add_argument(
        'html_file',
        nargs='?',
        default='docs/learning/A_WEEK_WITH_AI_CODING.html',
        help='Path to HTML file'
    )
    parser.add_argument(
        '--drive-path',
        help='Path to Google Drive folder (auto-detected if not provided)',
        default=None
    )
    parser.add_argument(
        '--title',
        help='Document title (default: derived from filename)'
    )
    
    # Check for environment variable
    import os
    if not parser.parse_args().drive_path:
        drive_path = os.environ.get('GOOGLE_DRIVE_PATH')
        if drive_path:
            parser.set_defaults(drive_path=drive_path)
    
    args = parser.parse_args()
    sync_to_drive(args.html_file, args.drive_path, args.title)


if __name__ == '__main__':
    main()

