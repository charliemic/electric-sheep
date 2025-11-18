# Configuring Google OAuth in Supabase Dashboard

## Quick Steps

1. **Open Supabase Dashboard**:
   - Go to: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd
   - Or navigate: Dashboard → Your Project → Authentication

2. **Navigate to Providers**:
   - Click **"Authentication"** in the left sidebar
   - Click the **"Providers"** tab

3. **Enable Google Provider**:
   - Find **"Google"** in the list of providers
   - Toggle the switch to **ON** (enabled)

4. **Enter OAuth Credentials**:
   - **Client ID (for OAuth)**: Paste your Google OAuth Client ID here
   - **Client Secret (for OAuth)**: Paste your Google OAuth Client Secret here
   - **Redirect URL**: This should already be pre-filled with:
     ```
     https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback
     ```
     - Verify this matches the redirect URI you added in Google Cloud Console
     - If different, update it to match exactly

5. **Save Configuration**:
   - Click **"Save"** button at the bottom of the form
   - You should see a success message

## Verification

After saving, you can verify the setup:

1. **Check the provider status**: Google should show as "Enabled"
2. **Test in your app**: 
   - Run the app: `./scripts/dev-reload.sh`
   - Navigate to Mood Management
   - Click "Sign in with Google"
   - Complete the OAuth flow

## Important Notes

- **Client Secret**: Keep this secure! It's already stored securely in Supabase.
- **Redirect URI**: Must match exactly between Google Console and Supabase
- **Test Users**: If using External app type in Google, make sure your test email is added

## Troubleshooting

**"redirect_uri_mismatch" error**:
- Verify the redirect URI in Supabase matches Google Console exactly
- Check for trailing slashes: should be `/auth/v1/callback` (no trailing slash)

**"invalid_client" error**:
- Double-check Client ID and Client Secret are correct
- Ensure no extra spaces when copying/pasting
- Verify credentials are for "Web application" type (not Android)

**"access_denied" error**:
- If using External app type, ensure your email is in test users list
- Check OAuth consent screen is properly configured

## Next Steps

After configuration:
1. Test Google sign-in in your app
2. Verify user appears in Supabase Auth → Users
3. Test that mood entries are properly scoped to the authenticated user

