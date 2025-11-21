# AWS Bedrock Setup Walkthrough

Interactive step-by-step guide to set up AWS Bedrock in Cursor.

## Current Status Check

✅ **Cursor Version**: 2.0.77 (supports Bedrock)  
✅ **AWS CLI**: Installed  
❌ **AWS Credentials**: Not configured  

## Step 1: Configure AWS SSO

Since you're using AWS SSO, we'll configure the AWS CLI to use SSO authentication.

### Configure AWS SSO

Run this command to start SSO configuration:

```bash
aws configure sso
```

You'll be prompted for:
1. **SSO session name** (Recommended): `bigchange` (or any name you prefer)
2. **SSO start URL**: `https://bigchange.awsapps.com/start`
3. **SSO region**: The AWS region where your SSO is configured (usually `us-east-1` or `eu-west-1`)
4. **SSO registration scopes**: `sso:account:access` (default, just press Enter)
5. **CLI default client Region**: `us-east-1` (or your preferred region)
6. **CLI default output format**: `json` (or just press Enter)
7. **CLI profile name**: `default` (or a custom name like `bigchange`)

### Login to AWS SSO

After configuration, log in to AWS SSO:

```bash
aws sso login --profile default
```

Or if you used a custom profile name:

```bash
aws sso login --profile bigchange
```

This will:
1. Open your browser to the SSO login page
2. Authenticate you through your organization's SSO
3. Store temporary credentials locally

**Verify it worked:**
```bash
aws sts get-caller-identity --profile default
```

You should see your AWS account ID and user ARN.

**Note**: SSO credentials expire (usually after 8-12 hours). When they expire, just run `aws sso login` again.

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

**Important**: Make sure you're logged in to SSO first:

```bash
aws sso login --profile default
```

Then run the verification script:

```bash
AWS_PROFILE=default ./scripts/verify-aws-bedrock-setup.sh
```

Or if using a custom profile:

```bash
AWS_PROFILE=bigchange ./scripts/verify-aws-bedrock-setup.sh
```

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

## Step 5: Get Temporary Credentials for Cursor

Since Cursor needs credentials (not SSO directly), we need to extract temporary credentials from your SSO session.

**Extract SSO Credentials:**

1. **Make sure you're logged in** (if not already):
   ```bash
   aws sso login --profile default
   ```

2. **Run the credential extraction script**:
   ```bash
   ./scripts/get-aws-sso-credentials.sh
   ```

   This will display:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `AWS_SESSION_TOKEN` (important for SSO!)
   - Credential expiration time

3. **Copy these values** - you'll need them for Cursor settings

**Important Notes:**
- These credentials expire (usually 8-12 hours)
- When they expire, you'll need to:
  1. Run `aws sso login --profile default` again
  2. Run `./scripts/get-aws-sso-credentials.sh` again
  3. Update Cursor settings with new credentials
- Some organizations may require you to use session tokens - check if Cursor supports them

## Step 6: Configure Cursor Settings

1. **Open Cursor Settings**:
   - macOS: `Cursor > Settings` or press `Cmd + ,`
   - Windows/Linux: `File > Preferences > Settings` or press `Ctrl + ,`

2. **Find AWS Bedrock Section**:
   - Look for **"Models"** or **"AI"** in the settings sidebar
   - Find **"AWS Bedrock"** or **"Custom Models"** option
   - If you don't see it, make sure you have Cursor Pro subscription

3. **Enter AWS Credentials**:
   - **AWS Access Key ID**: Enter the `AWS_ACCESS_KEY_ID` from Step 5
   - **AWS Secret Access Key**: Enter the `AWS_SECRET_ACCESS_KEY` from Step 5
   - **AWS Session Token**: Enter the `AWS_SESSION_TOKEN` from Step 5 (if Cursor supports it)
   - **AWS Region**: Select the region (e.g., `us-east-1`)
     - **Important**: Must match the region where you enabled model access

   **Note**: If Cursor doesn't support session tokens, you may need to use a credential helper or request long-term credentials from your admin.

4. **Select Model**:
   - Choose from the dropdown or enter model identifier
   - Recommended: `anthropic.claude-3-sonnet-20240229-v1:0`
   - Other options:
     - `anthropic.claude-3-opus-20240229-v1:0` (most capable)
     - `anthropic.claude-3-haiku-20240307-v1:0` (fastest)

5. **Save Settings**:
   - Click **Save** or **Apply**
   - Cursor should validate the connection

## Step 7: Test the Setup

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
- Run `aws sso login --profile default` to authenticate
- Extract credentials: `aws configure export-credentials --profile default --format env`
- Update Cursor settings with the extracted credentials
- **Note**: SSO credentials expire - you'll need to refresh them periodically

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

