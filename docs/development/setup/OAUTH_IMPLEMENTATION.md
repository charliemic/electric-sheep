# OAuth Implementation Guide

This document explains the OAuth implementation pattern used in Electric Sheep and why Chrome Custom Tabs is the recommended approach.

**Last Updated**: 2024-11-18  
**Status**: ✅ Using Supabase SDK-native OAuth methods

## Current Implementation

We use **Chrome Custom Tabs** for OAuth flows, which is the Android best practice recommended by Google. The OAuth implementation uses Supabase Kotlin SDK's native methods with automatic PKCE support.

### Why Chrome Custom Tabs?

1. **Better User Experience**:
   - Custom Tabs look like part of your app (customizable toolbar color)
   - Faster loading (Chrome is already running in the background)
   - Smooth animations and transitions
   - Share cookies with Chrome (users stay logged in)

2. **Security**:
   - Uses the user's existing Chrome browser
   - No need to handle cookies or sessions manually
   - Follows Android security best practices

3. **Reliability**:
   - Works on all Android devices with Chrome installed
   - Graceful fallback to regular browser if Custom Tabs unavailable
   - Better error handling

### Implementation Details

The OAuth flow works as follows:

1. **User clicks "Sign in with Google"**:
   - `MoodManagementViewModel.signInWithGoogle()` is called
   - Gets OAuth URL from `SupabaseAuthProvider.getGoogleOAuthUrl()`
   - SDK's `getOAuthUrl()` generates URL with PKCE parameters
   - URL path is corrected from `/auth/v1/` to `/auth/v1/authorize` (SDK workaround)
   - API key is added as query parameter (SDK doesn't include it)
   - URL is set in ViewModel state

2. **Open Custom Tab**:
   - `MoodManagementScreen` observes the OAuth URL
   - Uses `CustomTabsClient.getPackageName()` to find Chrome
   - Opens URL in Custom Tab with custom toolbar color
   - Falls back to regular browser if Custom Tabs unavailable

3. **OAuth Callback**:
   - User completes authentication in Custom Tab
   - Supabase redirects to deep link: `com.electricsheep.app://auth-callback?code=...`
   - `MainActivity.handleOAuthCallback()` receives the deep link
   - SDK's `handleDeeplinks()` processes the callback (PKCE verification, code exchange)
   - `SupabaseAuthProvider.handleOAuthCallback()` retrieves session from SDK
   - User is authenticated and UI updates automatically

## SDK-Native Implementation

The OAuth flow uses Supabase Kotlin SDK's native methods:

1. **`getOAuthUrl()`**: Generates OAuth URL with PKCE parameters
   - Automatically generates `code_challenge` and `code_challenge_method`
   - Includes `state` parameter for CSRF protection
   - Note: Requires path correction (`/auth/v1/` → `/auth/v1/authorize`) and API key addition

2. **`handleDeeplinks()`**: Processes OAuth callback
   - Validates `state` parameter
   - Exchanges authorization code for tokens (with PKCE verification)
   - Creates and stores session automatically

3. **`FlowType.PKCE`**: Configured in `DataModule.kt`
   - Enables automatic PKCE handling
   - No manual PKCE code generation needed

## Workarounds Required

Due to SDK limitations, we need to apply workarounds:

1. **Path Correction**: SDK generates `/auth/v1/` but Supabase requires `/auth/v1/authorize`
2. **API Key Addition**: SDK doesn't include `apikey` query parameter

These are applied in `SupabaseAuthProvider.getGoogleOAuthUrl()`.

### Code Structure

```kotlin
// In MoodManagementScreen.kt
LaunchedEffect(googleOAuthUrl) {
    googleOAuthUrl?.let { url ->
        // Check if Chrome Custom Tabs is available
        val packageName = CustomTabsClient.getPackageName(context, null)
        
        if (packageName != null) {
            // Use Custom Tabs (recommended)
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(0xFF6200EE.toInt())
            val customTabsIntent = builder.build()
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.launchUrl(context, uri)
        } else {
            // Fallback to regular browser
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }
    }
}
```

## Comparison with Other Approaches

### ❌ Opening Full Browser (Previous Implementation)

**Problems**:
- Opens a separate browser app (breaks app flow)
- Slower (browser needs to start)
- Doesn't share cookies (user may need to log in again)
- Poor user experience

### ❌ WebView (Not Recommended)

**Problems**:
- Security concerns (OAuth providers discourage WebView)
- Doesn't share cookies with browser
- Users may not trust WebView for authentication
- Google explicitly recommends against WebView for OAuth

### ✅ Chrome Custom Tabs (Current Implementation)

**Benefits**:
- ✅ Recommended by Google for OAuth flows
- ✅ Better UX (feels like part of the app)
- ✅ Shares cookies with Chrome
- ✅ Faster and more secure
- ✅ Graceful fallback if unavailable

## Dependencies

```kotlin
// In build.gradle.kts
implementation("androidx.browser:browser:1.7.0")
```

## Testing

### On Emulator

1. Ensure emulator has Chrome installed (most modern emulators include it)
2. Test the OAuth flow:
   - Click "Sign in with Google"
   - Custom Tab should open (not full browser)
   - Complete authentication
   - Should redirect back to app

### On Physical Device

1. Ensure Chrome is installed (usually pre-installed)
2. Test the OAuth flow
3. Verify Custom Tab opens with custom toolbar color

### Fallback Testing

To test the fallback behavior:
1. Temporarily disable Chrome on device
2. OAuth should fall back to regular browser
3. Flow should still work correctly

## Troubleshooting

### Custom Tabs Not Opening

**Symptoms**: OAuth opens in full browser instead of Custom Tab

**Possible Causes**:
- Chrome not installed
- Custom Tabs not supported on device
- Package name resolution failing

**Solution**: The code automatically falls back to regular browser, so OAuth should still work.

### OAuth Callback Not Received

**Symptoms**: User completes authentication but app doesn't receive callback

**Check**:
1. Deep link is configured in `AndroidManifest.xml`:
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.VIEW" />
       <category android:name="android.intent.category.DEFAULT" />
       <category android:name="android.intent.category.BROWSABLE" />
       <data
           android:scheme="com.electricsheep.app"
           android:host="auth-callback" />
   </intent-filter>
   ```

2. Supabase redirect URL matches: `com.electricsheep.app://auth-callback`

3. Check logs for OAuth callback errors

## Best Practices

1. **Always use Custom Tabs for OAuth**: It's the recommended Android pattern
2. **Provide fallback**: Always fall back to regular browser if Custom Tabs unavailable
3. **Customize toolbar**: Set toolbar color to match app theme
4. **Handle errors gracefully**: Show user-friendly error messages
5. **Test on multiple devices**: Ensure OAuth works across different Android versions

## References

- [Chrome Custom Tabs Documentation](https://developer.chrome.com/docs/android/custom-tabs/)
- [Android OAuth Best Practices](https://developer.android.com/training/sign-in/oauth)
- [Supabase Auth Documentation](https://supabase.com/docs/guides/auth)

