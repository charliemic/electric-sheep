# AWS Bedrock Quick Start for Cursor

Quick reference for setting up AWS Bedrock in Cursor IDE.

## Prerequisites Checklist

- [ ] Cursor version 0.49+ installed
- [ ] Cursor Pro subscription active
- [ ] AWS account with Bedrock access
- [ ] AWS Access Key ID and Secret Access Key

## Quick Setup Steps

### 1. Enable Models in AWS
1. Go to [AWS Bedrock Console](https://console.aws.amazon.com/bedrock/home#/modelaccess)
2. Click **Model access**
3. Request access to desired models (e.g., Claude 3 Sonnet)
4. Accept EULAs if prompted

### 2. Verify AWS Setup
```bash
# Run verification script
./scripts/verify-aws-bedrock-setup.sh
```

### 3. Configure Cursor
1. Open Cursor Settings (`Cmd + ,` or `Ctrl + ,`)
2. Find **AWS Bedrock** section
3. Enter:
   - AWS Access Key ID
   - AWS Secret Access Key
   - AWS Region (e.g., `us-east-1`)
4. Select model (e.g., `anthropic.claude-3-sonnet-20240229-v1:0`)
5. Save settings

### 4. Test
- Use Cursor's AI features (Chat, Composer)
- Verify requests are working

## Common Model Identifiers

- **Claude 3 Opus**: `anthropic.claude-3-opus-20240229-v1:0`
- **Claude 3 Sonnet**: `anthropic.claude-3-sonnet-20240229-v1:0`
- **Claude 3 Haiku**: `anthropic.claude-3-haiku-20240307-v1:0`

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Invalid model identifier" | Check model ID matches AWS format exactly |
| "Access denied" | Enable model in AWS Bedrock console |
| "Region mismatch" | Ensure Cursor region matches AWS region |
| "Authentication failed" | Verify AWS credentials are correct |

## Full Documentation

See [AWS_BEDROCK_CURSOR_SETUP.md](./AWS_BEDROCK_CURSOR_SETUP.md) for complete setup guide.

