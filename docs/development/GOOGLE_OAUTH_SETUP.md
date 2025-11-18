# Google OAuth Setup for Supabase

This guide walks you through configuring Google OAuth in Google Cloud Console for use with Supabase authentication.

## Step-by-Step Setup

### 1. OAuth Consent Screen - Scopes

**Current Step: You're here!**

On the **Scopes** page, you need to add the following scopes:

1. **Click "Add or Remove Scopes"**

2. **Select these scopes** (use the filter/search if needed):
   - ✅ `.../auth/userinfo.email` - See your primary Google Account email address
   - ✅ `.../auth/userinfo.profile` - See your personal info, including any personal info you've made publicly available

3. **Click "Update"** to save the scopes

4. **Click "Save and Continue"** to proceed

**Note**: These are the minimum scopes needed. Supabase will use them to:
- Get the user's email address
- Get the user's name/profile information

### 2. OAuth Consent Screen - Test Users (if External app type)

If you selected "External" as the user type:

1. **Add Test Users**:
   - Click "Add Users"
   - Add your own Google email address
   - Add any other test email addresses you want to use
   - Click "Add"

2. **Click "Save and Continue"**

**Note**: Test users can sign in even before the app is published. For production, you'll need to publish the app.

### 3. OAuth Consent Screen - Summary

1. **Review the information**
2. **Click "Back to Dashboard"**

### 4. Create OAuth 2.0 Credentials

1. **Navigate to Credentials**:
   - In the left sidebar, go to **APIs & Services** → **Credentials**

2. **Create Web Application Credentials** (for Supabase):
   - Click **"+ CREATE CREDENTIALS"**
   - Select **"OAuth client ID"**
   - Choose **"Web application"** as the application type
   - **Name**: `Supabase Web Client` (or any name you prefer)
   - **Authorized redirect URIs**: 
     ```
     https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback
     ```
     - Replace `mvuzvoyvijsdqsfqjgpd` with your actual Supabase project reference ID if different
   - Click **"CREATE"**
   - **IMPORTANT**: Copy the **Client ID** and **Client Secret** - you'll need these for Supabase!

3. **Create Android Application Credentials** (optional, for future use):
   - Click **"+ CREATE CREDENTIALS"** again
   - Select **"OAuth client ID"**
   - Choose **"Android"** as the application type
   - **Name**: `Electric Sheep Android`
   - **Package name**: `com.electricsheep.app`
   - **SHA-1 certificate fingerprint**: 
     - For debug builds, get it with:
       ```bash
       keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
       ```
     - Copy the SHA-1 value (without colons)
   - Click **"CREATE"**

### 5. Configure Supabase Dashboard

1. **Go to Supabase Dashboard**:
   - Visit: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd

2. **Navigate to Authentication**:
   - Click **"Authentication"** in the left sidebar
   - Click **"Providers"** tab

3. **Enable Google Provider**:
   - Find **"Google"** in the list
   - Toggle it **ON**

4. **Enter OAuth Credentials**:
   - **Client ID (for OAuth)**: Paste the Client ID from step 4.2
   - **Client Secret (for OAuth)**: Paste the Client Secret from step 4.2
   - **Redirect URL**: Should already be set to:
     ```
     https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback
     ```

5. **Save**:
   - Click **"Save"** at the bottom

### 6. Add Redirect URL to Google Console (if not already done)

1. **Go back to Google Cloud Console**
2. **Navigate to**: APIs & Services → Credentials
3. **Click on your Web Application OAuth client**
4. **Under "Authorized redirect URIs"**, ensure you have:
   ```
   https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback
   ```
5. **Click "Save"**

## Verification

### Test the Setup

1. **Run your app**:
   ```bash
   ./scripts/dev-reload.sh
   ```

2. **Navigate to Mood Management screen**

3. **Click "Sign in with Google"**

4. **Expected flow**:
   - Browser opens with Google sign-in
   - You sign in with your Google account
   - Browser redirects back to app
   - App authenticates and shows you as logged in

### Troubleshooting

**"redirect_uri_mismatch" error**:
- Verify the redirect URI in Google Console matches exactly:
  `https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback`
- Check for trailing slashes or typos

**"access_denied" error**:
- If using External app type, ensure your email is in the test users list
- Check that the app is published (for production use)

**"invalid_client" error**:
- Verify Client ID and Client Secret are correct in Supabase dashboard
- Check for extra spaces when copying/pasting

## Important Notes

- **Client Secret**: Keep this secure! Never commit it to git.
- **Test Users**: For External apps, only test users can sign in until the app is published.
- **Publishing**: To allow any Google user to sign in, you'll need to publish your app in Google Cloud Console (requires verification for sensitive scopes).

## Next Steps

After completing the setup:
1. Test Google sign-in in your app
2. Verify user data is created in Supabase Auth
3. Test that mood entries are scoped to the authenticated user

