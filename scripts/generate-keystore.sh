#!/bin/bash

# Generate Android Release Keystore
# This script generates a keystore file for signing Android release builds
#
# Usage:
#   ./scripts/generate-keystore.sh [keystore-name]
#
# Example:
#   ./scripts/generate-keystore.sh release
#
# The keystore will be created in the keystore/ directory (which is gitignored)
# You'll be prompted for:
#   - Keystore password
#   - Key alias
#   - Key password
#   - Certificate information (name, organization, etc.)

set -e

KEYSTORE_NAME="${1:-release}"
KEYSTORE_DIR="keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/$KEYSTORE_NAME.jks"
KEY_ALIAS="${KEYSTORE_NAME}_key"
VALIDITY_YEARS=25  # Android Play Store requires at least 25 years

echo "╔════════════════════════════════════════════════════════════╗"
echo "║      Android Release Keystore Generator                   ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo "❌ ERROR: keytool not found"
    echo "   keytool is part of the Java JDK"
    echo "   Please install Java JDK and ensure it's in your PATH"
    exit 1
fi

# Create keystore directory if it doesn't exist
mkdir -p "$KEYSTORE_DIR"

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  WARNING: Keystore file already exists: $KEYSTORE_FILE"
    echo ""
    read -p "   Do you want to overwrite it? (yes/no): " overwrite
    if [ "$overwrite" != "yes" ]; then
        echo "   Aborted. Keystore generation cancelled."
        exit 0
    fi
    echo ""
fi

echo "📝 Keystore Configuration:"
echo "   Keystore file: $KEYSTORE_FILE"
echo "   Key alias: $KEY_ALIAS"
echo "   Validity: $VALIDITY_YEARS years"
echo ""
echo "⚠️  IMPORTANT:"
echo "   - Keep your keystore file and passwords secure"
echo "   - Back up your keystore file (you'll need it for updates)"
echo "   - Never commit the keystore file to git"
echo "   - Store passwords securely (password manager, secure notes)"
echo ""

# Generate keystore
echo "🔐 Generating keystore..."
echo "   You'll be prompted for:"
echo "   1. Keystore password (store this securely!)"
echo "   2. Key alias (default: $KEY_ALIAS)"
echo "   3. Key password (can be same as keystore password)"
echo "   4. Certificate information"
echo ""

keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $((VALIDITY_YEARS * 365)) \
    -storetype JKS

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keystore generated successfully!"
    echo ""
    echo "📋 Next Steps:"
    echo "   1. Add keystore configuration to local.properties:"
    echo "      keystore.file=$KEYSTORE_FILE"
    echo "      keystore.password=<YOUR_KEYSTORE_PASSWORD>"
    echo "      keystore.key.alias=$KEY_ALIAS"
    echo "      keystore.key.password=<your-key-password>"
    echo ""
    echo "   2. For CI/CD, add these as GitHub Secrets:"
    echo "      KEYSTORE_FILE (base64 encoded keystore file)"
    echo "      KEYSTORE_PASSWORD"
    echo "      KEY_ALIAS"
    echo "      KEY_PASSWORD"
    echo ""
    echo "   3. Test the release build:"
    echo "      ./gradlew assembleRelease"
    echo ""
    echo "⚠️  SECURITY REMINDER:"
    echo "   - Never commit $KEYSTORE_FILE to git"
    echo "   - Back up your keystore file securely"
    echo "   - Store passwords in a password manager"
else
    echo ""
    echo "❌ ERROR: Failed to generate keystore"
    exit 1
fi

