# Authentication Architecture

This document describes the authentication system for the Electric Sheep Android app.

## Overview

The authentication system provides:
- User identity management
- Data scoping to individual users
- Multi-device support (data sync across devices)
- Extensible authentication provider abstraction

## Architecture

```
┌─────────────────────────────────────┐
│         UserManager                  │
│  (Reactive user state)               │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      AuthProvider                    │
│      (Interface)                     │
└──────┬──────────────────┬───────────┘
       │                  │
┌──────▼──────┐   ┌──────▼───────────┐
│ Placeholder │   │  Real Providers  │
│  Provider   │   │  (Future)        │
│ (Dev Only)  │   │  - Supabase Auth │
│             │   │  - Auth0         │
│             │   │  - Firebase Auth │
└─────────────┘   └──────────────────┘
```

## Components

### 1. User (`auth/User.kt`)

Represents a user in the system:
- `id`: Unique user identifier (from auth provider)
- `email`: User's email address
- `displayName`: Optional display name
- `createdAt`: Account creation timestamp

### 2. AuthProvider (`auth/AuthProvider.kt`)

Interface for authentication providers:
- `getCurrentUser()`: Get authenticated user
- `signIn(email, password)`: Sign in user
- `signUp(email, password, displayName)`: Create new user
- `signOut()`: Sign out current user
- `refreshSession()`: Refresh authentication token
- `resetPassword(email)`: Request password reset

### 3. PlaceholderAuthProvider (`auth/PlaceholderAuthProvider.kt`)

Development-only placeholder:
- Stores user in memory (lost on app restart)
- No actual authentication (accepts any credentials)
- Generates fake user IDs
- **Not suitable for production**

### 4. UserManager (`auth/UserManager.kt`)

Manages current user session:
- Reactive `StateFlow<User?>` for current user
- Wraps `AuthProvider` with reactive updates
- Provides `requireUserId()` for data operations
- Handles sign in/out with state updates

### 5. AuthModule (`auth/AuthModule.kt`)

Dependency injection for authentication:
- Creates `AuthProvider` instances
- Creates `UserManager` instances
- Central point for switching auth providers

## Data Scoping

All data operations are scoped to the current authenticated user:

### Mood Model
- `Mood.userId`: Links mood to user who created it
- Validation ensures `userId` is not blank

### Repository
- All queries filter by `userId`
- Security checks verify `userId` matches current user
- Prevents users from accessing other users' data

### Database
- All queries include `userId` filter
- Index on `userId` for performance
- Migration adds `userId` column to existing data

## Usage

### Signing In/Up

```kotlin
// In ViewModel or similar
val userManager: UserManager = // injected

// Sign up
userManager.signUp("user@example.com", "password", "Display Name")
    .onSuccess { user ->
        // User created and signed in
    }
    .onFailure { error ->
        // Handle error
    }

// Sign in
userManager.signIn("user@example.com", "password")
    .onSuccess { user ->
        // User signed in
    }
```

### Observing Current User

```kotlin
// In Composable or ViewModel
val currentUser by userManager.currentUser.collectAsState()

if (currentUser == null) {
    // Show sign in screen
} else {
    // Show app content
}
```

### Accessing User ID

```kotlin
// Throws exception if not authenticated
val userId = userManager.requireUserId()

// Returns null if not authenticated
val userId = userManager.getUserIdOrNull()
```

## Future: Real Authentication Providers

### Supabase Auth

```kotlin
class SupabaseAuthProvider(
    private val supabaseClient: SupabaseClient
) : AuthProvider {
    override suspend fun signIn(email: String, password: String): Result<User> {
        val response = supabaseClient.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return Result.success(mapToUser(response.user))
    }
    // ... implement other methods
}
```

### Auth0

```kotlin
class Auth0Provider(
    private val auth0Client: Auth0
) : AuthProvider {
    override suspend fun signIn(email: String, password: String): Result<User> {
        val credentials = auth0Client.authentication.login(
            email,
            password,
            "your-connection"
        ).await()
        return Result.success(mapToUser(credentials))
    }
    // ... implement other methods
}
```

## Migration Strategy

When migrating from placeholder to real auth:

1. **Implement real AuthProvider**:
   - Create concrete implementation (e.g., `SupabaseAuthProvider`)
   - Implement all interface methods
   - Handle token storage/refresh

2. **Update AuthModule**:
   ```kotlin
   fun createAuthProvider(): AuthProvider {
       return if (useRealAuth) {
           SupabaseAuthProvider(supabaseClient)
       } else {
           PlaceholderAuthProvider()
       }
   }
   ```

3. **Handle existing data**:
   - Existing moods have `userId = 'placeholder_user'`
   - Prompt user to sign in on first launch
   - Migrate placeholder data to real user account

4. **Update Supabase schema**:
   - Add Row Level Security (RLS) policies
   - Ensure `userId` matches authenticated user
   - Add foreign key constraints if needed

## Security Considerations

1. **User ID Validation**: Repository verifies `userId` matches current user
2. **Query Filtering**: All queries automatically filter by `userId`
3. **Remote Security**: Supabase RLS policies enforce user scoping
4. **Token Storage**: Real auth providers should use secure storage (EncryptedSharedPreferences)

## Testing

### Unit Tests

- `UserManagerTest`: Test user state management
- `PlaceholderAuthProviderTest`: Test placeholder provider
- `MoodRepositoryUserScopingTest`: Test data scoping

### Manual Testing

1. Sign up with placeholder provider
2. Create mood entries
3. Sign out and sign in with different user
4. Verify moods are scoped to correct user
5. Verify cannot access other user's moods

## Current Limitations

1. **Placeholder Provider**: No persistence, no real authentication
2. **No Token Refresh**: Placeholder doesn't handle token expiration
3. **No Password Reset**: Placeholder doesn't implement password reset
4. **No Multi-Factor Auth**: Not yet supported

## Next Steps

1. Implement real authentication provider (Supabase Auth recommended)
2. Add secure token storage
3. Implement session refresh
4. Add password reset flow
5. Add multi-factor authentication support
6. Add biometric authentication (fingerprint/face unlock)

