#!/bin/bash

# Automated Release Signing Setup
# This script automates the setup process for release signing
#
# Usage:
#   ./scripts/setup-release-signing.sh
#
# WHO SHOULD RUN THIS:
#   - Admin/Owner: Run once to set up keystore and GitHub Secrets
#   - Developers: Usually don't need to run this (CI/CD handles signing)
#   - Developers (optional): Only if they want to test release builds locally
#
# What it does:
#   1. Generates keystore (with prompts for passwords) - ADMIN ONLY
#   2. Configures local.properties - OPTIONAL for developers
#   3. Tests local signing - OPTIONAL
#   4. Optionally sets up GitHub Secrets (requires gh CLI) - ADMIN ONLY
#
# NOTE: There should be ONE keystore for the entire project, not one per developer.
#       Only the admin/owner should generate the keystore and set GitHub Secrets.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

KEYSTORE_NAME="release"
KEYSTORE_DIR="keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/$KEYSTORE_NAME.jks"
LOCAL_PROPERTIES="local.properties"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║      Automated Release Signing Setup                      ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# Check prerequisites
echo "🔍 Checking prerequisites..."

if ! command -v keytool &> /dev/null; then
    echo "❌ ERROR: keytool not found"
    echo "   keytool is part of the Java JDK"
    echo "   Please install Java JDK and ensure it's in your PATH"
    exit 1
fi

if ! command -v base64 &> /dev/null; then
    echo "❌ ERROR: base64 not found"
    echo "   base64 is required for GitHub Secrets setup"
    exit 1
fi

echo "✅ Prerequisites met"
echo ""

# Step 1: Generate keystore
echo "════════════════════════════════════════════════════════════"
echo "Step 1: Generate Keystore"
echo "════════════════════════════════════════════════════════════"
echo ""

if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  Keystore file already exists: $KEYSTORE_FILE"
    read -p "   Do you want to overwrite it? (yes/no): " overwrite
    if [ "$overwrite" != "yes" ]; then
        echo "   Using existing keystore file"
        SKIP_KEYSTORE_GEN=true
    else
        SKIP_KEYSTORE_GEN=false
    fi
else
    SKIP_KEYSTORE_GEN=false
fi

if [ "$SKIP_KEYSTORE_GEN" = false ]; then
    echo "📝 Running keystore generation script..."
    echo "   You'll be prompted for passwords and certificate information"
    echo ""
    "$SCRIPT_DIR/generate-keystore.sh" "$KEYSTORE_NAME"
    
    if [ ! -f "$KEYSTORE_FILE" ]; then
        echo "❌ ERROR: Keystore generation failed"
        exit 1
    fi
    
    echo ""
    echo "✅ Keystore generated successfully"
else
    echo "✅ Using existing keystore"
fi

echo ""
echo "⚠️  CRITICAL: Back up your keystore file now!"
echo "   Location: $KEYSTORE_FILE"
read -p "   Press Enter after you've backed up the keystore..."
echo ""

# Step 2: Get keystore information
echo "════════════════════════════════════════════════════════════"
echo "Step 2: Configure Local Development"
echo "════════════════════════════════════════════════════════════"
echo ""

# Prompt for keystore password
read -sp "Enter keystore password: " KEYSTORE_PASSWORD
echo ""
read -sp "Enter key password (can be same as keystore password): " KEY_PASSWORD
echo ""

# Get key alias (default: release_key)
read -p "Enter key alias [release_key]: " KEY_ALIAS
KEY_ALIAS=${KEY_ALIAS:-release_key}

# Step 3: Configure local.properties
echo ""
echo "📝 Configuring local.properties..."

# Create local.properties if it doesn't exist
if [ ! -f "$LOCAL_PROPERTIES" ]; then
    touch "$LOCAL_PROPERTIES"
fi

# Check if keystore config already exists
if grep -q "keystore.file" "$LOCAL_PROPERTIES"; then
    echo "⚠️  Keystore configuration already exists in local.properties"
    read -p "   Do you want to update it? (yes/no): " update
    if [ "$update" = "yes" ]; then
        # Remove existing keystore config
        sed -i.bak '/^# Keystore Configuration/,/^keystore\.key\.password=/d' "$LOCAL_PROPERTIES"
        rm -f "$LOCAL_PROPERTIES.bak"
    else
        echo "   Keeping existing configuration"
        SKIP_LOCAL_CONFIG=true
    fi
else
    SKIP_LOCAL_CONFIG=false
fi

if [ "$SKIP_LOCAL_CONFIG" = false ]; then
    # Add keystore configuration
    {
        echo ""
        echo "# Keystore Configuration (for local release builds)"
        echo "# Added by setup-release-signing.sh on $(date)"
        echo "keystore.file=$KEYSTORE_FILE"
        echo "keystore.password=$KEYSTORE_PASSWORD"
        echo "keystore.key.alias=$KEY_ALIAS"
        echo "keystore.key.password=$KEY_PASSWORD"
    } >> "$LOCAL_PROPERTIES"
    
    echo "✅ local.properties configured"
else
    echo "✅ Using existing local.properties configuration"
fi

# Verify local.properties is gitignored
if ! grep -q "local.properties" .gitignore 2>/dev/null; then
    echo "⚠️  WARNING: local.properties not found in .gitignore"
    echo "   (This is usually fine - Android projects gitignore it by default)"
fi

echo ""

# Step 4: Test local signing
echo "════════════════════════════════════════════════════════════"
echo "Step 3: Test Local Signing"
echo "════════════════════════════════════════════════════════════"
echo ""

read -p "Do you want to test local signing now? (yes/no) [yes]: " test_signing
test_signing=${test_signing:-yes}

if [ "$test_signing" = "yes" ]; then
    echo ""
    echo "🔨 Building release APK..."
    if ./gradlew assembleRelease --stacktrace; then
        echo ""
        echo "✅ Release APK built successfully"
        
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
        if [ -f "$APK_PATH" ]; then
            echo ""
            echo "🔍 Verifying APK signature..."
            if jarsigner -verify -verbose -certs "$APK_PATH" > /dev/null 2>&1; then
                echo "✅ APK is signed correctly"
            else
                echo "⚠️  WARNING: Could not verify APK signature"
                echo "   APK may still be signed - check manually:"
                echo "   jarsigner -verify -verbose -certs $APK_PATH"
            fi
        else
            echo "⚠️  WARNING: APK file not found at expected location"
        fi
    else
        echo "❌ ERROR: Release build failed"
        echo "   Check the error messages above"
        exit 1
    fi
else
    echo "⏭️  Skipping local signing test"
fi

echo ""

# Step 5: GitHub Secrets setup
echo "════════════════════════════════════════════════════════════"
echo "Step 4: GitHub Secrets Setup (Optional)"
echo "════════════════════════════════════════════════════════════"
echo ""

if command -v gh &> /dev/null; then
    read -p "Do you want to set up GitHub Secrets now? (yes/no) [no]: " setup_gh_secrets
    setup_gh_secrets=${setup_gh_secrets:-no}
    
    if [ "$setup_gh_secrets" = "yes" ]; then
        echo ""
        echo "🔐 Setting up GitHub Secrets..."
        
        # Check GitHub CLI authentication
        if ! gh auth status &> /dev/null; then
            echo "⚠️  GitHub CLI not authenticated"
            echo "   Run: gh auth login"
            echo "   Then run this script again"
            SKIP_GH_SECRETS=true
        else
            SKIP_GH_SECRETS=false
        fi
        
        if [ "$SKIP_GH_SECRETS" = false ]; then
            # Encode keystore to base64
            echo "📦 Encoding keystore to base64..."
            KEYSTORE_BASE64=$(base64 -i "$KEYSTORE_FILE")
            
            # Set GitHub Secrets
            echo "🔐 Adding GitHub Secrets..."
            
            echo "$KEYSTORE_BASE64" | gh secret set KEYSTORE_FILE
            echo "$KEYSTORE_PASSWORD" | gh secret set KEYSTORE_PASSWORD
            echo "$KEY_ALIAS" | gh secret set KEY_ALIAS
            echo "$KEY_PASSWORD" | gh secret set KEY_PASSWORD
            
            echo ""
            echo "✅ GitHub Secrets configured"
            echo ""
            echo "💡 To verify, check: https://github.com/$(gh repo view --json owner,name -q '.owner.login + "/" + .name')/settings/secrets/actions"
        fi
    else
        echo "⏭️  Skipping GitHub Secrets setup"
        echo ""
        echo "💡 To set up GitHub Secrets manually:"
        echo "   1. Encode keystore: base64 -i $KEYSTORE_FILE | pbcopy"
        echo "   2. Go to: GitHub Settings → Secrets and variables → Actions"
        echo "   3. Add 4 secrets: KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
    fi
else
    echo "⚠️  GitHub CLI (gh) not installed"
    echo "   Install: brew install gh (macOS) or see https://cli.github.com"
    echo ""
    echo "💡 To set up GitHub Secrets manually:"
    echo "   1. Encode keystore: base64 -i $KEYSTORE_FILE | pbcopy"
    echo "   2. Go to: GitHub Settings → Secrets and variables → Actions"
    echo "   3. Add 4 secrets: KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD"
fi

echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    Setup Complete                         ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

echo "✅ What's been configured:"
echo "   - Keystore file: $KEYSTORE_FILE"
echo "   - Local properties: $LOCAL_PROPERTIES"
if [ "$test_signing" = "yes" ]; then
    echo "   - Local signing: Tested ✅"
fi
if [ "${setup_gh_secrets:-no}" = "yes" ] && [ "${SKIP_GH_SECRETS:-true}" = false ]; then
    echo "   - GitHub Secrets: Configured ✅"
fi

echo ""
echo "📋 Next steps:"
if [ "${setup_gh_secrets:-no}" != "yes" ] || [ "${SKIP_GH_SECRETS:-true}" = true ]; then
    echo "   1. Set up GitHub Secrets (see manual guide if needed)"
fi
echo "   2. Test CI/CD signing: Push a commit and check GitHub Actions"
echo "   3. Verify signed AAB from CI/CD"
echo ""

echo "🔒 Security reminders:"
echo "   - ✅ Keystore backed up securely"
echo "   - ✅ Passwords stored in password manager"
echo "   - ✅ local.properties is gitignored"
echo ""

echo "📚 Documentation:"
echo "   - Setup guide: docs/development/setup/RELEASE_SIGNING_SETUP.md"
echo "   - Manual walkthrough: docs/development/setup/RELEASE_SIGNING_MANUAL_SETUP.md"
echo ""

echo "✨ Setup complete! You can now build signed release builds."

