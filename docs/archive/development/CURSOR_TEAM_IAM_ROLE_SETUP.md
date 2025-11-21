# Cursor Team Plan - AWS Bedrock IAM Role Setup

**Last Updated**: 2025-11-21  
**Status**: Active

This guide explains how to configure AWS Bedrock in Cursor using IAM roles (for Team/Enterprise plans).

## Documentation References

- ✅ **Correct Doc**: [Cursor API Keys Settings](https://cursor.com/docs/settings/api-keys#supported-providers)
  - Mentions IAM roles but doesn't explain the process
  - States: "A unique external ID is generated after validating your Bedrock IAM role"
  
- ❌ **Not Relevant**: [Cursor Cloud Agents](https://cursor.com/docs/cloud-agent)
  - This is for Cloud Agents (background agents in Cursor's infrastructure)
  - IAM roles here are for agents to access YOUR AWS resources
  - NOT about configuring Bedrock models for Cursor's chat/composer

## Understanding the Setup Process

Based on Cursor's documentation, the IAM role setup process appears to be:

1. **Enter IAM Role ARN in Cursor**
   - Go to `Cursor Settings > Models > AWS Bedrock`
   - Enter your IAM Role ARN (instead of access keys)
   - Click "Validate" or similar

2. **Cursor Generates External ID**
   - After validation, Cursor generates a unique external ID
   - This external ID is used for additional security in the trust policy

3. **Platform Team Updates IAM Role Trust Policy**
   - Add the external ID to the role's trust policy
   - This allows Cursor to assume the role securely

## Information for Platform Team

### AWS Account Details

- **Your AWS Account ID**: `509905662768`
- **Region**: `eu-west-1`
- **Cursor's AWS Account**: `289469326074`
- **Cursor's Role ARN**: `arn:aws:iam::289469326074:role/roleAssumer`

### IAM Role Permissions Policy

The IAM role needs these permissions for Bedrock:

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

### IAM Role Trust Policy Template

**Note**: The External ID will be provided by Cursor after you validate the role ARN in the UI.

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCursorAssume",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::289469326074:role/roleAssumer"
      },
      "Action": "sts:AssumeRole",
      "Condition": {
        "StringEquals": {
          "sts:ExternalId": "cursor-xxx-yyy-zzz"
        }
      }
    }
  ]
}
```

Replace `cursor-xxx-yyy-zzz` with the external ID that Cursor generates.

## Setup Steps

### Step 1: Platform Team Creates IAM Role

1. Create IAM role in AWS Console
2. Attach the permissions policy above
3. Note the Role ARN (e.g., `arn:aws:iam::509905662768:role/cursor-bedrock-role`)

### Step 2: Initial Trust Policy (Temporary)

Create a temporary trust policy without the External ID condition:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowCursorAssume",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::289469326074:role/roleAssumer"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

**Security Note**: This is temporary - we'll add the External ID condition in Step 4.

### Step 3: Enter Role ARN in Cursor

1. Open `Cursor Settings > Models > AWS Bedrock`
2. Look for "IAM Role ARN" field (may be separate from access key fields)
3. Enter the IAM Role ARN from Step 1
4. Click "Validate" or "Save"
5. Cursor should generate an External ID

### Step 4: Update Trust Policy with External ID

1. Get the External ID from Cursor (should be displayed after validation)
2. Update the IAM role's trust policy to include the External ID condition (see template above)
3. This adds an extra security layer

### Step 5: Configure Region and Model

1. Set Region: `eu-west-1`
2. Set Test Model: `anthropic.claude-sonnet-4-5-20250929-v1:0`
3. Save settings

## Troubleshooting

### Fields Are Greyed Out

- **For Team Plans**: Fields might be greyed out if IAM role configuration is required
- **Solution**: Look for "IAM Role ARN" field instead of access key fields
- **Alternative**: Toggle the switch OFF and ON again

### "Invalid IAM Role" Error

- Verify the role ARN is correct
- Check the trust policy allows Cursor's role to assume it
- Ensure the role has the Bedrock permissions policy attached

### External ID Not Generated

- Make sure you clicked "Validate" after entering the role ARN
- Check if there's a separate "Generate External ID" button
- Try saving the settings first, then check for external ID

### Can't Find IAM Role Field

- The UI might vary by Cursor version
- Try looking in team/workspace settings instead of individual settings
- Contact Cursor support if IAM role option isn't visible

## Security Best Practices

1. ✅ **Use IAM Roles** (not access keys) for Team plans
2. ✅ **Include External ID** in trust policy for additional security
3. ✅ **Minimal Permissions** - Only grant Bedrock read permissions
4. ✅ **Regular Audits** - Review role usage in CloudTrail
5. ✅ **Rotate if Needed** - Create new role if compromised

## Alternative: Using Access Keys

If IAM roles aren't working or aren't available:

1. Platform team can create IAM user with Bedrock permissions
2. Generate access keys for that user
3. Use access keys in Cursor Settings (fields should unlock)
4. **Note**: Less secure than IAM roles, requires managing key rotation

## Related Documentation

- [Cursor API Keys Settings](https://cursor.com/docs/settings/api-keys#supported-providers)
- [AWS Bedrock Setup Guide](./AWS_BEDROCK_CURSOR_SETUP.md)
- [Project Goals](./PROJECT_GOALS.md) - Security principles

