#!/bin/bash
# Find Google Drive folder location

echo "Searching for Google Drive folder..."
echo ""

# Check common locations
LOCATIONS=(
    "$HOME/Google Drive"
    "$HOME/Library/CloudStorage/GoogleDrive-*"
    "/Volumes/GoogleDrive"
    "$HOME/Drive"
)

for loc in "${LOCATIONS[@]}"; do
    if [[ "$loc" == *"*"* ]]; then
        # Expand glob
        for expanded in $loc; do
            if [ -d "$expanded" ]; then
                echo "✓ Found: $expanded"
                exit 0
            fi
        done
    else
        if [ -d "$loc" ]; then
            echo "✓ Found: $loc"
            exit 0
        fi
    fi
done

# Try mdfind
echo "Searching with Spotlight..."
RESULT=$(mdfind -name "Google Drive" -onlyin "$HOME" 2>/dev/null | grep -i "drive" | head -1)
if [ -n "$RESULT" ] && [ -d "$RESULT" ]; then
    echo "✓ Found: $RESULT"
    exit 0
fi

echo "✗ Google Drive folder not found in common locations"
echo ""
echo "Please find it manually:"
echo "1. Open Finder"
echo "2. Look for 'Google Drive' in sidebar or check:"
echo "   - ~/Google Drive"
echo "   - ~/Library/CloudStorage/"
echo "3. Right-click → Get Info to see full path"
echo ""
echo "Then run:"
echo "  python3 scripts/google-docs-drive-sync.py file.html --drive-path '/path/to/Google Drive'"

