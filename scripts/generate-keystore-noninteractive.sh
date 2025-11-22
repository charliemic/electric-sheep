#!/bin/bash

# Generate Android Release Keystore (Non-Interactive)
# This version accepts passwords via environment variables for automation
#
# Usage:
#   KEYSTORE_PASSWORD=<pass> KEY_PASSWORD=<pass> KEY_ALIAS=<alias> \
#   CERT_NAME="Name" CERT_ORG="Org" CERT_CITY="City" CERT_STATE="State" CERT_COUNTRY="US" \
#   ./scripts/generate-keystore-noninteractive.sh [keystore-name]
#
# WARNING: Only use this in secure, automated environments
#          For manual setup, use generate-keystore.sh instead

set -e

KEYSTORE_NAME="${1:-release}"
KEYSTORE_DIR="keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/$KEYSTORE_NAME.jks"
KEY_ALIAS="${KEYSTORE_NAME}_key"
VALIDITY_YEARS=25

# Get values from environment or use defaults
KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD:-}"
KEY_PASSWORD="${KEY_PASSWORD:-${KEYSTORE_PASSWORD}}"
KEY_ALIAS="${KEY_ALIAS_ENV:-${KEY_ALIAS}}"
CERT_NAME="${CERT_NAME:-Electric Sheep}"
CERT_ORG="${CERT_ORG:-Electric Sheep}"
CERT_ORG_UNIT="${CERT_ORG_UNIT:-Development}"
CERT_CITY="${CERT_CITY:-London}"
CERT_STATE="${CERT_STATE:-England}"
CERT_COUNTRY="${CERT_COUNTRY:-GB}"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║   Android Release Keystore Generator (Non-Interactive)  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check prerequisites
if ! command -v keytool &> /dev/null; then
    echo "❌ ERROR: keytool not found"
    exit 1
fi

# Validate required passwords
if [ -z "$KEYSTORE_PASSWORD" ] || [ ${#KEYSTORE_PASSWORD} -lt 6 ]; then
    echo "❌ ERROR: KEYSTORE_PASSWORD must be at least 6 characters"
    echo "   Set via: KEYSTORE_PASSWORD=<password>"
    exit 1
fi

# Create keystore directory
mkdir -p "$KEYSTORE_DIR"

# Check if keystore exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  WARNING: Keystore file already exists: $KEYSTORE_FILE"
    echo "   Delete it first if you want to regenerate"
    exit 1
fi

echo "📝 Keystore Configuration:"
echo "   Keystore file: $KEYSTORE_FILE"
echo "   Key alias: $KEY_ALIAS"
echo "   Validity: $VALIDITY_YEARS years"
echo "   Certificate: $CERT_NAME, $CERT_ORG, $CERT_CITY, $CERT_STATE, $CERT_COUNTRY"
echo ""

# Generate keystore using stdin for passwords
echo "🔐 Generating keystore..."

# Create a temporary file with the certificate info
CERT_INFO="$(
    echo "$CERT_NAME"
    echo "$CERT_ORG_UNIT"
    echo "$CERT_ORG"
    echo "$CERT_CITY"
    echo "$CERT_STATE"
    echo "$CERT_COUNTRY"
    echo "yes"  # Confirm
)"

# Generate keystore (passwords via stdin)
echo -e "$KEYSTORE_PASSWORD\n$KEY_PASSWORD\n$KEY_PASSWORD\n$CERT_INFO" | \
keytool -genkey -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $((VALIDITY_YEARS * 365)) \
    -storetype JKS \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=$CERT_NAME, OU=$CERT_ORG_UNIT, O=$CERT_ORG, L=$CERT_CITY, ST=$CERT_STATE, C=$CERT_COUNTRY"

if [ $? -eq 0 ] && [ -f "$KEYSTORE_FILE" ]; then
    echo ""
    echo "✅ Keystore generated successfully!"
    echo "   Location: $KEYSTORE_FILE"
    echo ""
    echo "⚠️  SECURITY REMINDER:"
    echo "   - Back up your keystore file securely"
    echo "   - Store passwords in a password manager"
    echo "   - Never commit the keystore to git"
else
    echo ""
    echo "❌ ERROR: Failed to generate keystore"
    exit 1
fi

