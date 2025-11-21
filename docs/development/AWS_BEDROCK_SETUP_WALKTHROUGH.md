# AWS Bedrock Setup Walkthrough

Interactive step-by-step guide to set up AWS Bedrock in Cursor.

## Current Status Check

✅ **Cursor Version**: 2.0.77 (supports Bedrock)  
✅ **AWS CLI**: Installed  
❌ **AWS Credentials**: Not configured  

## Step 1: Get AWS Credentials

You need AWS Access Key ID and Secret Access Key. Choose one:

### Option A: Use Existing AWS Account

If you already have AWS credentials:
1. Go to [AWS IAM Console](https://console.aws.amazon.com/iam/)
2. Click **Users** → Select your user → **Security credentials** tab
3. Click **Create access key**
4. Choose **Command Line Interface (CLI)** as use case
5. Copy the **Access Key ID** and **Secret Access Key** (you'll only see the secret once!)

### Option B: Create New IAM User (Recommended)

1. Go to [AWS IAM Console](https://console.aws.amazon.com/iam/)
2. Click **Users** → **Create user**
3. Name: `cursor-bedrock-user`
4. Click **Next**
5. Under **Set permissions**, select **Attach policies directly**
6. Click **Create policy** (opens new tab)
7. Switch to **JSON** tab and paste:

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

8. Click **Next** → Name: `CursorBedrockAccess` → **Create policy**
9. Go back to user creation tab, refresh, select `CursorBedrockAccess` policy
10. Click **Next** → **Create user**
11. Click on the new user → **Security credentials** tab
12. Click **Create access key** → **Command Line Interface (CLI)** → **Next**
13. Copy **Access Key ID** and **Secret Access Key**

## Step 2: Configure AWS CLI

Run this command and enter your credentials when prompted:

```bash
aws configure
```

You'll be asked for:
- **AWS Access Key ID**: [paste your access key]
- **AWS Secret Access Key**: [paste your secret key]
- **Default region name**: `us-east-1` (or your preferred region)
- **Default output format**: `json` (or just press Enter)

**Verify it worked:**
```bash
aws sts get-caller-identity
```

You should see your AWS account ID and user ARN.

## Step 3: Enable Models in AWS Bedrock

1. Go to [AWS Bedrock Console](https://console.aws.amazon.com/bedrock/home#/modelaccess)
2. Click **Model access** in the left sidebar
3. You'll see a list of available models. For Claude models, look for:
   - **Claude 3 Opus** (most capable, most expensive)
   - **Claude 3 Sonnet** (balanced, recommended)
   - **Claude 3 Haiku** (fastest, cheapest)
4. Click **Request model access** for the models you want
5. Accept any End User License Agreements (EULAs)
6. Wait a few minutes for access to be granted (status will change to "Access granted")

**Note**: You can request multiple models - they're free to request, you only pay when you use them.

## Step 4: Verify AWS Bedrock Access

Run the verification script:

```bash
./scripts/verify-aws-bedrock-setup.sh
```

This should show:
- ✅ AWS CLI installed
- ✅ AWS credentials valid
- ✅ Bedrock API accessible
- List of available models

If you see errors, check:
- Are your credentials correct?
- Is the model access granted in AWS console?
- Does your IAM user have Bedrock permissions?

## Step 5: Configure Cursor Settings

1. **Open Cursor Settings**:
   - macOS: `Cursor > Settings` or press `Cmd + ,`
   - Windows/Linux: `File > Preferences > Settings` or press `Ctrl + ,`

2. **Find AWS Bedrock Section**:
   - Look for **"Models"** or **"AI"** in the settings sidebar
   - Find **"AWS Bedrock"** or **"Custom Models"** option
   - If you don't see it, make sure you have Cursor Pro subscription

3. **Enter AWS Credentials**:
   - **AWS Access Key ID**: Enter your access key
   - **AWS Secret Access Key**: Enter your secret key
   - **AWS Region**: Select the region (e.g., `us-east-1`)
     - **Important**: Must match the region where you enabled model access

4. **Select Model**:
   - Choose from the dropdown or enter model identifier
   - Recommended: `anthropic.claude-3-sonnet-20240229-v1:0`
   - Other options:
     - `anthropic.claude-3-opus-20240229-v1:0` (most capable)
     - `anthropic.claude-3-haiku-20240307-v1:0` (fastest)

5. **Save Settings**:
   - Click **Save** or **Apply**
   - Cursor should validate the connection

## Step 6: Test the Setup

1. **Try Cursor's AI Features**:
   - Open Cursor Chat (usually `Cmd + L` or `Ctrl + L`)
   - Ask a simple question: "Hello, can you hear me?"
   - If it responds, Bedrock is working! 🎉

2. **Check for Errors**:
   - If you see errors, check Cursor's output/logs
   - Common issues:
     - "Invalid model identifier" → Check model ID matches exactly
     - "Access denied" → Verify model is enabled in AWS console
     - "Region mismatch" → Ensure Cursor region matches AWS region

## Troubleshooting

### "AWS credentials not configured"
- Run `aws configure` and enter your credentials
- Or set environment variables:
  ```bash
  export AWS_ACCESS_KEY_ID=your-key
  export AWS_SECRET_ACCESS_KEY=your-secret
  ```

### "The provided model identifier is invalid"
- Check the exact model ID in AWS Bedrock console
- Ensure it matches the format exactly (including version numbers)
- Try a different model to test

### "Model access denied"
- Go to AWS Bedrock > Model access
- Ensure you've requested and been granted access
- Accept any required EULAs
- Wait a few minutes for access to propagate

### "Region defaults to us-east-1"
- After selecting region in Cursor, save and restart Cursor
- Verify region in AWS console matches Cursor setting
- Try manually entering region code

### "Authentication failed"
- Verify credentials are correct (no extra spaces)
- Test with: `aws sts get-caller-identity`
- Check IAM user has Bedrock permissions

## Next Steps

Once everything is working:
- ✅ You can use Cursor's AI features with AWS Bedrock
- ✅ Monitor usage in AWS Cost Explorer
- ✅ Set up billing alerts if needed
- ✅ Consider using Claude Haiku for simple tasks (cheaper/faster)

## Need Help?

- See full documentation: `docs/development/AWS_BEDROCK_CURSOR_SETUP.md`
- Quick reference: `docs/development/AWS_BEDROCK_QUICK_START.md`
- Run verification: `./scripts/verify-aws-bedrock-setup.sh`

