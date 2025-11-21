#!/usr/bin/env python3
"""
Google Docs Browser Automation Upload

Uses browser automation to upload HTML to Google Docs via the web interface.
No OAuth setup required - uses your existing browser session.

Requirements:
    pip install playwright
    playwright install chromium

Usage:
    python scripts/google-docs-browser-upload.py [html_file] [--title "Title"]
"""

import sys
import argparse
import time
from pathlib import Path

try:
    from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout
except ImportError:
    print("Error: Playwright not installed.")
    print("Install with: pip3 install playwright && playwright install chromium")
    sys.exit(1)


def upload_via_browser(html_file_path, title=None):
    """Upload HTML file to Google Docs using browser automation."""
    html_path = Path(html_file_path)
    if not html_path.exists():
        print(f"Error: HTML file not found: {html_path}")
        sys.exit(1)
    
    abs_path = html_path.resolve()
    title = title or html_path.stem.replace('_', ' ').title()
    
    print(f"Uploading '{title}' to Google Docs...")
    print("This will open a browser window. Please ensure you're logged into Google.")
    print()
    
    with sync_playwright() as p:
        # Launch browser (visible so user can see what's happening)
        browser = p.chromium.launch(headless=False)
        context = browser.new_context()
        page = context.new_page()
        
        try:
            # Navigate to Google Docs
            print("Opening Google Docs...")
            page.goto("https://docs.google.com", wait_until="networkidle")
            time.sleep(2)  # Give page time to load
            
            # Click File menu
            print("Clicking File menu...")
            try:
                # Try to find the File menu button
                file_button = page.locator("button:has-text('File')").first
                if not file_button.is_visible(timeout=5000):
                    # Alternative: look for menu button
                    file_button = page.locator("[aria-label*='File'], [aria-label*='file']").first
                file_button.click(timeout=10000)
            except PlaywrightTimeout:
                print("Could not find File menu. Trying alternative approach...")
                # Try keyboard shortcut
                page.keyboard.press("Alt+F")
                time.sleep(1)
            
            # Wait for menu to appear and click Import
            print("Clicking Import...")
            time.sleep(1)
            try:
                import_option = page.locator("text=Import, text=Upload").first
                import_option.click(timeout=5000)
            except PlaywrightTimeout:
                # Try keyboard navigation
                page.keyboard.press("ArrowDown")
                page.keyboard.press("Enter")
                time.sleep(1)
            
            # Wait for upload dialog
            print("Waiting for upload dialog...")
            time.sleep(2)
            
            # Set file input
            print(f"Selecting file: {abs_path}")
            file_input = page.locator('input[type="file"]').first
            file_input.set_input_files(str(abs_path))
            
            print("File selected. Waiting for upload to complete...")
            
            # Wait for document to load (look for editor or document title)
            try:
                # Wait for the document editor to appear
                page.wait_for_selector('[contenteditable="true"], [role="textbox"]', timeout=30000)
                print("✓ Document uploaded and converted!")
            except PlaywrightTimeout:
                print("Upload may have completed, but couldn't detect document editor.")
            
            # Get the document URL
            current_url = page.url
            if 'document/d/' in current_url:
                doc_id = current_url.split('document/d/')[1].split('/')[0]
                doc_url = f"https://docs.google.com/document/d/{doc_id}/edit"
                print(f"\n✓ Document created!")
                print(f"  URL: {doc_url}")
                print(f"  ID: {doc_id}")
                print(f"\nYou can now edit the document in Google Docs.")
                print("Press Enter to close the browser, or close it manually.")
                input()
            else:
                print(f"\nDocument URL: {current_url}")
                print("Please check the browser window for the document.")
            
        except Exception as e:
            print(f"\nError during upload: {e}")
            print("Please check the browser window and try manual upload if needed.")
            input("Press Enter to close...")
        finally:
            browser.close()


def main():
    parser = argparse.ArgumentParser(
        description='Upload HTML to Google Docs via browser automation (no OAuth required)'
    )
    parser.add_argument(
        'html_file',
        nargs='?',
        default='docs/learning/A_WEEK_WITH_AI_CODING.html',
        help='Path to HTML file'
    )
    parser.add_argument(
        '--title',
        help='Title for the document (default: derived from filename)'
    )
    
    args = parser.parse_args()
    upload_via_browser(args.html_file, args.title)


if __name__ == '__main__':
    main()

