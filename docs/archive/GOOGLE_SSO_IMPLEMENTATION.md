# Google SSO Implementation Guide

This document outlines the implementation of Google Single Sign-On (SSO) for the Electric Sheep mood app.

## Implementation Status

### ✅ Completed
- [x] Added Supabase Auth dependency (`gotrue-kt`)
- [x] Created `SupabaseAuthProvider` with Google OAuth support
- [x] Updated `AuthModule` to use `SupabaseAuthProvider`
- [x] Updated `ElectricSheepApplication` initialization
- [x] Added OAuth methods to `UserManager`
- [x] Updated `MoodManagementViewModel` with sign up and Google OAuth support

### 🚧 In Progress
- [ ] Update `MoodManagementScreen` UI with:
  - Password input field
  - Toggle between sign in/sign up
  - Google SSO button
  - Handle OAuth URL opening

### ⏳ Pending
- [ ] Handle OAuth callback in `MainActivity`
- [ ] Add deep link intent filter to `AndroidManifest.xml`
- [ ] Configure Google OAuth in Supabase dashboard
- [ ] Add tests for `SupabaseAuthProvider`
- [ ] Update documentation

## Next Steps

### 1. Update UI (MoodManagementScreen.kt)

Add to the login UI section:
- Password `OutlinedTextField`
- Toggle button to switch between "Sign In" and "Create Account"
- Google SSO button that calls `viewModel.signInWithGoogle()` and opens the URL
- Update button text/action based on `isSignUpMode`

### 2. Handle OAuth Callback (MainActivity.kt)

Add deep link handling:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Handle OAuth callback
    intent?.data?.let { uri ->
        if (uri.scheme == "com.electricsheep.app" && uri.host == "auth-callback") {
            handleOAuthCallback(uri)
        }
    }
    
    // ... rest of onCreate
}

private fun handleOAuthCallback(uri: Uri) {
    val application = applicationContext as ElectricSheepApplication
    val userManager = application.getUserManager()
    
    lifecycleScope.launch {
        userManager.handleOAuthCallback(uri)
            .onSuccess { user ->
                Logger.info("MainActivity", "OAuth callback successful: ${user.id}")
                // UI will automatically update via StateFlow
            }
            .onFailure { error ->
                Logger.error("MainActivity", "OAuth callback failed", error)
                // Show error to user
            }
    }
}
```

### 3. Add Deep Link to AndroidManifest.xml

Add intent filter to `MainActivity`:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:windowSoftInputMode="adjustResize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    
    <!-- OAuth callback deep link -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="com.electricsheep.app"
            android:host="auth-callback" />
    </intent-filter>
</activity>
```

### 4. Configure Google OAuth in Supabase

1. Go to: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd
2. Navigate to: **Authentication** → **Providers**
3. Enable **Google** provider
4. Add OAuth credentials:
   - Client ID (from Google Cloud Console)
   - Client Secret (from Google Cloud Console)
5. Add redirect URL: `com.electricsheep.app://auth-callback`

### 5. Set Up Google Cloud Console

1. Create project in [Google Cloud Console](https://console.cloud.google.com/)
2. Enable Google+ API
3. Create OAuth 2.0 credentials:
   - Application type: Web application
   - Authorized redirect URIs: `https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback`
4. Copy Client ID and Client Secret to Supabase

## Testing

1. Test email/password sign up
2. Test email/password sign in
3. Test Google SSO flow:
   - Click "Sign in with Google"
   - Complete OAuth in browser
   - Verify callback handled correctly
   - Verify user authenticated

## Files Modified

- `app/build.gradle.kts` - Added `gotrue-kt` dependency
- `app/src/main/java/com/electricsheep/app/auth/SupabaseAuthProvider.kt` - New file
- `app/src/main/java/com/electricsheep/app/auth/AuthModule.kt` - Updated
- `app/src/main/java/com/electricsheep/app/auth/UserManager.kt` - Added OAuth methods
- `app/src/main/java/com/electricsheep/app/data/DataModule.kt` - Added Auth module
- `app/src/main/java/com/electricsheep/app/ElectricSheepApplication.kt` - Updated initialization
- `app/src/main/java/com/electricsheep/app/ui/screens/mood/MoodManagementViewModel.kt` - Added sign up and OAuth

## Files To Update

- `app/src/main/java/com/electricsheep/app/ui/screens/mood/MoodManagementScreen.kt` - Update UI
- `app/src/main/java/com/electricsheep/app/MainActivity.kt` - Handle OAuth callback
- `app/src/main/AndroidManifest.xml` - Add deep link intent filter

