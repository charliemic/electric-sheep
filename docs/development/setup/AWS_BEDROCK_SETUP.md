# AWS Bedrock Setup for Cursor

Quick guide for using AWS Bedrock models in Cursor IDE.

## Prerequisites

- ✅ Invited to Cursor Team plan
- ✅ Cursor version 0.49+ installed

## Setup (2 minutes)

**Note**: Cursor settings are stored locally (`.cursor/settings.json` is gitignored) and must be configured manually through the UI.

1. **Open Cursor Settings**
   - Press `Cmd + ,` (or `Cursor > Settings`)

2. **Enable AWS Bedrock**
   - Go to `Models > AWS Bedrock`
   - Toggle **AWS Bedrock** ON

3. **Configure Region**
   - Set **Region** to: `eu-west-1`
   - Set **Test Model** to: `anthropic.claude-sonnet-4-5-20250929-v1:0`

4. **Save and Test**
   - Click **Save** or **Verify**
   - Try Cursor's AI features (Chat, Composer) to confirm it's working

That's it! The team plan handles authentication automatically via IAM roles.

**Important**: The automatic model optimization rule (`.cursor/rules/bedrock-model-optimization.mdc`) will work once Bedrock is configured. Settings are per-user and stored locally, so each team member needs to configure this once.

## Troubleshooting

- **Can't find AWS Bedrock option**: Make sure you're invited to the Cursor Team plan
- **Fields greyed out**: Toggle the switch OFF then ON again
- **"Model not found"**: Verify model access in AWS Bedrock console (contact platform team if needed)

## For Platform Team

See `docs/archive/development/CURSOR_TEAM_IAM_ROLE_SETUP.md` for IAM role configuration details.
