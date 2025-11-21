# AWS Bedrock Setup for Cursor IDE

This guide explains how to configure AWS Bedrock models in Cursor IDE to use AWS Bedrock instead of default models when you run out of tokens.

## Prerequisites

### 1. Cursor Version Requirements
- ✅ **Cursor version 0.49 or later** - AWS Bedrock support was introduced in version 0.49
- Check your version: `Cursor > About Cursor` (macOS) or `Help > About` (Windows/Linux)
- Update if needed: Cursor should auto-update, or download from [cursor.sh](https://cursor.sh)

### 2. Subscription Requirements
- ✅ **Cursor Pro subscription** - AWS Bedrock integration requires a Pro subscription
- Free plan users cannot access AWS Bedrock models
- Upgrade at: `Cursor > Settings > Account`

### 3. AWS Account Setup
- ✅ AWS account with Bedrock access
- ✅ AWS Access Key ID and Secret Access Key
- ✅ Appropriate AWS region selected (where Bedrock models are available)
- ✅ Model access enabled in AWS Bedrock console

## Step-by-Step Setup

### Step 1: Enable Models in AWS Bedrock Console

1. **Log into AWS Console**
   - Go to [AWS Console](https://console.aws.amazon.com/)
   - Navigate to **Amazon Bedrock** service

2. **Request Model Access**
   - Go to **Model access** in the left sidebar
   - Select the models you want to use (e.g., Claude 3 Sonnet, Claude 3 Haiku, etc.)
   - Click **Request model access**
   - Accept any required End User License Agreements (EULAs)

3. **Verify Model Availability**
   - Ensure models show as "Access granted" or "Available"
   - Note the exact model identifiers (e.g., `anthropic.claude-3-sonnet-20240229-v1:0`)

### Step 2: Configure AWS Credentials

**Option A: Using AWS IAM User (Recommended for Development)**

1. **Create IAM User** (if you don't have one):
   ```bash
   # Using AWS CLI
   aws iam create-user --user-name cursor-bedrock-user
   
   # Create access key
   aws iam create-access-key --user-name cursor-bedrock-user
   ```

2. **Attach Bedrock Permissions Policy**:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "bedrock:InvokeModel",
           "bedrock:InvokeModelWithResponseStream"
         ],
         "Resource": "arn:aws:bedrock:*::foundation-model/*"
       },
       {
         "Effect": "Allow",
         "Action": [
           "bedrock:ListFoundationModels"
         ],
         "Resource": "*"
       }
     ]
   }
   ```

**Option B: Using AWS Profile (Alternative)**

If you have AWS CLI configured with profiles:
```bash
# List your profiles
aws configure list-profiles

# Use a specific profile
export AWS_PROFILE=your-profile-name
```

### Step 3: Configure Cursor Settings

1. **Open Cursor Settings**
   - macOS: `Cursor > Settings` or `Cmd + ,`
   - Windows/Linux: `File > Preferences > Settings` or `Ctrl + ,`

2. **Navigate to AI/Model Settings**
   - Look for **"Models"** or **"AI"** section in settings
   - Find **"AWS Bedrock"** or **"Custom Models"** option

3. **Enter AWS Credentials**
   - **AWS Access Key ID**: Enter your AWS access key
   - **AWS Secret Access Key**: Enter your AWS secret key
   - **AWS Region**: Select the region where your Bedrock models are available
     - Common regions: `us-east-1`, `us-west-2`, `eu-west-1`, `ap-southeast-1`
     - **Important**: The region must match where you enabled model access

4. **Select Model**
   - Choose from available Bedrock models
   - Model identifiers should match AWS format exactly
   - Example formats:
     - Claude 3 Sonnet: `anthropic.claude-3-sonnet-20240229-v1:0`
     - Claude 3 Haiku: `anthropic.claude-3-haiku-20240307-v1:0`
     - Claude 3 Opus: `anthropic.claude-3-opus-20240229-v1:0`

5. **Save Settings**
   - Click **Save** or **Apply**
   - Cursor should validate the connection

### Step 4: Verify Configuration

1. **Test the Connection**
   - Try using Cursor's AI features (Chat, Composer, etc.)
   - Check if requests are going through Bedrock

2. **Check for Errors**
   - Look for error messages in Cursor's output/logs
   - Common issues are documented in Troubleshooting section below

## AWS IAM Permissions

### Minimum Required Permissions

Your AWS IAM user/role needs these permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": "arn:aws:bedrock:*::foundation-model/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:ListFoundationModels"
      ],
      "Resource": "*"
    }
  ]
}
```

### Creating IAM Policy

1. **Via AWS Console**:
   - Go to IAM > Policies > Create policy
   - Use JSON editor and paste the policy above
   - Name it: `CursorBedrockAccess`
   - Attach to your IAM user

2. **Via AWS CLI**:
   ```bash
   # Create policy
   aws iam create-policy \
     --policy-name CursorBedrockAccess \
     --policy-document file://bedrock-policy.json
   
   # Attach to user
   aws iam attach-user-policy \
     --user-name cursor-bedrock-user \
     --policy-arn arn:aws:iam::YOUR_ACCOUNT_ID:policy/CursorBedrockAccess
   ```

## Troubleshooting

### Issue: "The provided model identifier is invalid"

**Causes:**
- Model identifier doesn't match AWS format exactly
- Model not enabled in your AWS account
- Wrong region selected

**Solutions:**
1. Verify model identifier in AWS Bedrock console
2. Ensure model access is granted
3. Check region matches where model is available
4. Try a different model identifier format

### Issue: Region Defaults to us-east-1

**Causes:**
- Cursor may default to us-east-1 even after selecting another region
- Settings not saved properly

**Solutions:**
1. Double-check region selection in settings
2. Save settings and restart Cursor
3. Verify region in AWS console matches Cursor setting
4. Try manually entering region code instead of selecting from dropdown

### Issue: Authentication Errors

**Causes:**
- Invalid AWS credentials
- Credentials expired
- Insufficient IAM permissions

**Solutions:**
1. Verify credentials are correct (no extra spaces)
2. Check IAM user has Bedrock permissions
3. Test credentials with AWS CLI:
   ```bash
   aws bedrock list-foundation-models --region us-east-1
   ```
4. Regenerate access keys if needed

### Issue: "Model Access Denied"

**Causes:**
- Model not enabled in AWS Bedrock console
- EULA not accepted
- Model not available in selected region

**Solutions:**
1. Go to AWS Bedrock > Model access
2. Request access to the model
3. Accept any required EULAs
4. Wait for access to be granted (may take a few minutes)
5. Verify model is available in your selected region

### Issue: Subscription/Plan Errors

**Causes:**
- Free plan doesn't support AWS Bedrock
- Pro subscription not active

**Solutions:**
1. Verify Pro subscription is active
2. Check account status in Cursor settings
3. Upgrade to Pro if needed

## Available Bedrock Models

Common models available in AWS Bedrock:

### Anthropic Claude Models
- `anthropic.claude-3-opus-20240229-v1:0` - Claude 3 Opus (most capable)
- `anthropic.claude-3-sonnet-20240229-v1:0` - Claude 3 Sonnet (balanced)
- `anthropic.claude-3-haiku-20240307-v1:0` - Claude 3 Haiku (fastest)

### Amazon Titan Models
- `amazon.titan-text-lite-v1`
- `amazon.titan-text-express-v1`
- `amazon.titan-embed-text-v1`

### Meta Llama Models
- `meta.llama2-13b-chat-v1`
- `meta.llama2-70b-chat-v1`

**Note**: Model availability varies by region. Check AWS Bedrock console for models available in your region.

## Best Practices

### Security
- ✅ **Never commit AWS credentials** to version control
- ✅ Use IAM users with minimal required permissions
- ✅ Rotate access keys regularly
- ✅ Use separate IAM users for different environments

### Cost Management
- ✅ Monitor AWS Bedrock usage in AWS Cost Explorer
- ✅ Set up billing alerts
- ✅ Use appropriate model tiers (Haiku for simple tasks, Opus for complex)
- ✅ Review AWS Bedrock pricing: [AWS Bedrock Pricing](https://aws.amazon.com/bedrock/pricing/)

### Performance
- ✅ Choose region closest to you for lower latency
- ✅ Use Claude Haiku for faster responses on simple tasks
- ✅ Use Claude Opus for complex coding tasks requiring deep understanding

## Verification Checklist

Before using AWS Bedrock in Cursor:

- [ ] Cursor version 0.49 or later installed
- [ ] Cursor Pro subscription active
- [ ] AWS account with Bedrock access
- [ ] Models enabled in AWS Bedrock console
- [ ] IAM user created with Bedrock permissions
- [ ] AWS credentials configured in Cursor
- [ ] Correct region selected in Cursor
- [ ] Model identifier matches AWS format
- [ ] Test request successful in Cursor

## Additional Resources

- [Cursor Forum - AWS Bedrock Discussion](https://forum.cursor.com/t/amazon-bedrock-models-support-for-cursor/68243)
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [AWS Bedrock Model Access](https://console.aws.amazon.com/bedrock/home#/modelaccess)
- [AWS IAM Best Practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)

## Support

If you encounter issues:

1. Check Cursor's error messages/logs
2. Verify AWS credentials with AWS CLI
3. Check AWS CloudWatch logs for Bedrock errors
4. Review [Cursor Forum](https://forum.cursor.com/) for similar issues
5. Contact Cursor support if issue persists

