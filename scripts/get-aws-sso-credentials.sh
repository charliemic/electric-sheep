#!/bin/bash

# Extract AWS SSO credentials for use in Cursor
# This script extracts temporary credentials from your SSO session

set -e

PROFILE="${AWS_PROFILE:-default}"

echo "🔑 Extracting AWS SSO Credentials"
echo "=================================="
echo ""

# Check if logged in
if ! aws sts get-caller-identity --profile "$PROFILE" &> /dev/null; then
    echo "❌ Not logged in to AWS SSO"
    echo ""
    echo "Please login first:"
    echo "  aws sso login --profile $PROFILE"
    exit 1
fi

echo "✅ SSO session active for profile: $PROFILE"
echo ""

# Export credentials in environment variable format
echo "📋 Credentials (copy these to Cursor settings):"
echo ""
echo "---"
aws configure export-credentials --profile "$PROFILE" --format env
echo "---"
echo ""

# Also show as JSON for easier parsing
echo "📋 Credentials (JSON format):"
echo ""
aws configure export-credentials --profile "$PROFILE" | jq '.' 2>/dev/null || \
aws configure export-credentials --profile "$PROFILE"
echo ""

# Check expiration
echo "⏰ Credential expiration:"
EXPIRY=$(aws configure export-credentials --profile "$PROFILE" | jq -r '.Expiration' 2>/dev/null || echo "unknown")
if [ "$EXPIRY" != "unknown" ] && [ "$EXPIRY" != "null" ]; then
    echo "   Expires: $EXPIRY"
    # Calculate time until expiry
    EXPIRY_EPOCH=$(date -j -f "%Y-%m-%dT%H:%M:%S" "${EXPIRY%Z}" "+%s" 2>/dev/null || \
                   date -d "$EXPIRY" "+%s" 2>/dev/null || echo "0")
    NOW_EPOCH=$(date "+%s")
    if [ "$EXPIRY_EPOCH" -gt "$NOW_EPOCH" ]; then
        SECONDS_LEFT=$((EXPIRY_EPOCH - NOW_EPOCH))
        HOURS_LEFT=$((SECONDS_LEFT / 3600))
        MINUTES_LEFT=$(((SECONDS_LEFT % 3600) / 60))
        echo "   Time remaining: ${HOURS_LEFT}h ${MINUTES_LEFT}m"
    fi
else
    echo "   Unable to determine expiration"
fi
echo ""

echo "💡 Instructions for Cursor:"
echo "   1. Copy the AWS_ACCESS_KEY_ID value"
echo "   2. Copy the AWS_SECRET_ACCESS_KEY value"
echo "   3. Copy the AWS_SESSION_TOKEN value (if present)"
echo "   4. Paste into Cursor Settings > AWS Bedrock"
echo "   5. Set AWS Region (e.g., us-east-1)"
echo ""
echo "⚠️  Note: These credentials expire. When they do:"
echo "   1. Run: aws sso login --profile $PROFILE"
echo "   2. Run this script again to get new credentials"
echo "   3. Update Cursor settings with new credentials"
