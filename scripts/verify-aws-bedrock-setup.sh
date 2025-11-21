#!/bin/bash

# Verify AWS Bedrock Setup for Cursor
# This script helps verify that AWS Bedrock is properly configured

set -e

echo "🔍 Verifying AWS Bedrock Setup for Cursor"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed${NC}"
    echo "   Install it from: https://aws.amazon.com/cli/"
    exit 1
fi

echo -e "${GREEN}✅ AWS CLI is installed${NC}"

# Check for AWS profile (SSO support)
AWS_PROFILE="${AWS_PROFILE:-default}"
if [ -n "$AWS_PROFILE" ] && [ "$AWS_PROFILE" != "default" ]; then
    echo "Using AWS profile: $AWS_PROFILE"
    export AWS_PROFILE
fi

# Check AWS credentials
echo ""
echo "Checking AWS credentials..."
if ! aws sts get-caller-identity ${AWS_PROFILE:+--profile $AWS_PROFILE} &> /dev/null; then
    echo -e "${RED}❌ AWS credentials not configured or invalid${NC}"
    echo ""
    if [ "$AWS_PROFILE" != "default" ]; then
        echo "   For SSO, try: aws sso login --profile $AWS_PROFILE"
    else
        echo "   Configure with: aws configure"
        echo "   Or for SSO: aws configure sso"
        echo "   Or set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables"
    fi
    exit 1
fi

AWS_ACCOUNT=$(aws sts get-caller-identity ${AWS_PROFILE:+--profile $AWS_PROFILE} --query Account --output text)
AWS_USER=$(aws sts get-caller-identity ${AWS_PROFILE:+--profile $AWS_PROFILE} --query Arn --output text)
echo -e "${GREEN}✅ AWS credentials valid${NC}"
echo "   Account: $AWS_ACCOUNT"
echo "   Identity: $AWS_USER"

# Check default region
DEFAULT_REGION=$(aws configure get region ${AWS_PROFILE:+--profile $AWS_PROFILE} 2>/dev/null || echo "us-east-1")
echo ""
echo "Checking AWS region..."
echo "   Default region: $DEFAULT_REGION"
echo -e "${YELLOW}⚠️  Make sure this matches the region configured in Cursor${NC}"

# Test Bedrock access
echo ""
echo "Testing Bedrock access..."
if aws bedrock list-foundation-models --region "$DEFAULT_REGION" ${AWS_PROFILE:+--profile $AWS_PROFILE} &> /dev/null; then
    echo -e "${GREEN}✅ Bedrock API access successful${NC}"
else
    echo -e "${RED}❌ Bedrock API access failed${NC}"
    echo "   Check IAM permissions for bedrock:ListFoundationModels"
    if [ "$AWS_PROFILE" != "default" ]; then
        echo "   Make sure you're logged in: aws sso login --profile $AWS_PROFILE"
    fi
    exit 1
fi

# List available models
echo ""
echo "Fetching available Bedrock models..."
MODELS=$(aws bedrock list-foundation-models --region "$DEFAULT_REGION" ${AWS_PROFILE:+--profile $AWS_PROFILE} --query 'modelSummaries[*].[modelId,providerName]' --output text 2>/dev/null || echo "")

if [ -z "$MODELS" ]; then
    echo -e "${YELLOW}⚠️  No models found or access denied${NC}"
    echo "   You may need to enable model access in AWS Bedrock console"
else
    echo -e "${GREEN}✅ Found models:${NC}"
    echo "$MODELS" | head -10 | while read -r model_id provider; do
        echo "   - $model_id ($provider)"
    done
    if [ $(echo "$MODELS" | wc -l) -gt 10 ]; then
        echo "   ... and more (total: $(echo "$MODELS" | wc -l) models)"
    fi
fi

# Check specific permissions
echo ""
echo "Checking IAM permissions..."
HAS_INVOKE=$(aws iam get-user-policy --user-name "$(echo $AWS_USER | cut -d'/' -f2)" --policy-name CursorBedrockAccess 2>/dev/null || echo "not found")
if [ "$HAS_INVOKE" = "not found" ]; then
    echo -e "${YELLOW}⚠️  Could not verify specific IAM policy${NC}"
    echo "   Make sure your IAM user/role has:"
    echo "   - bedrock:InvokeModel"
    echo "   - bedrock:InvokeModelWithResponseStream"
    echo "   - bedrock:ListFoundationModels"
else
    echo -e "${GREEN}✅ IAM policy found${NC}"
fi

# Test model invocation (optional, requires specific model)
echo ""
echo "Summary:"
echo "========="
echo -e "${GREEN}✅ AWS CLI installed${NC}"
echo -e "${GREEN}✅ AWS credentials configured${NC}"
echo -e "${GREEN}✅ Bedrock API accessible${NC}"
echo ""
echo "Next steps:"
echo "1. Ensure models are enabled in AWS Bedrock console:"
echo "   https://console.aws.amazon.com/bedrock/home#/modelaccess"
echo "2. Configure Cursor settings:"
echo "   - Open Cursor Settings"
echo "   - Find AWS Bedrock section"
echo "   - Enter AWS Access Key ID and Secret Access Key"
echo "   - Select region: $DEFAULT_REGION"
echo "   - Choose a model from the list above"
echo "3. Test in Cursor by using AI features"
echo ""
echo "For detailed setup instructions, see:"
echo "docs/development/AWS_BEDROCK_CURSOR_SETUP.md"

