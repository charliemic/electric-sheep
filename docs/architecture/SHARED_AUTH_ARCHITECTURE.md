# Shared Authentication Architecture

**Date**: 2025-01-20  
**Purpose**: Unified authentication and authorization system for Android app and Dashboard

## Overview

**Shared Authentication System** that provides:
- ✅ **Unified Auth** - Same user accounts for Android app and Dashboard
- ✅ **User Roles** - Regular users and Admin users
- ✅ **Role-Based Access Control** - Admin-only features gated behind roles
- ✅ **Consistent Architecture** - Same patterns across platforms

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Supabase Auth                            │
│  (Single Source of Truth for All Users)                     │
│                                                              │
│  - User Accounts (email/password, Google OAuth)             │
│  - JWT Tokens (access_token, refresh_token)                 │
│  - User Metadata (roles, display_name, etc.)                │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
               │                              │
    ┌──────────▼──────────┐      ┌───────────▼──────────┐
    │   Android App       │      │   Dashboard Server    │
    │   (Kotlin)          │      │   (Node.js/Fastify)   │
    │                     │      │                       │
    │  UserManager        │      │  Auth Middleware      │
    │  SupabaseAuthProvider│      │  Token Verification   │
    │  User (with role)   │      │  User (with role)     │
    └─────────────────────┘      └───────────────────────┘
```

## User Roles

### Role Types

1. **Regular User** (`user`)
   - Default role for all new users
   - Can use Android app features
   - Can view public dashboard pages
   - Cannot access authoring features
   - Cannot access admin features

2. **Admin User** (`admin`)
   - Elevated permissions
   - Can access dashboard authoring
   - Can access admin features
   - Can manage other users (future)
   - Can view all metrics/data

### Role Storage

**Supabase User Metadata:**
```json
{
  "display_name": "John Doe",
  "role": "admin"  // or "user" (default)
}
```

**Storage Location:**
- Supabase Auth `user_metadata` field
- Accessible via JWT token claims
- Can be updated via Admin API

## User Model (Extended)

### Android App (`User.kt`)

```kotlin
@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val createdAt: Long? = null,
    val role: UserRole = UserRole.USER  // NEW
) {
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN
}

enum class UserRole {
    USER,   // Regular user
    ADMIN   // Admin user
}
```

### Dashboard (`user.js`)

```javascript
export class User {
    constructor(id, email, displayName, role = 'user') {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }
    
    get isAdmin() {
        return this.role === 'admin';
    }
}
```

## Role Extraction

### From Supabase JWT Token

**JWT Payload Structure:**
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "user_metadata": {
    "display_name": "John Doe",
    "role": "admin"
  },
  "app_metadata": {
    "provider": "email"
  }
}
```

**Extraction Logic:**
```javascript
// Dashboard (Node.js)
function extractUserFromToken(token) {
  const payload = decodeJWT(token);
  return {
    id: payload.sub,
    email: payload.email,
    displayName: payload.user_metadata?.display_name,
    role: payload.user_metadata?.role || 'user'
  };
}
```

```kotlin
// Android App (Kotlin)
private fun extractRole(metadata: JsonObject?): UserRole {
    return try {
        val roleString = metadata?.get("role")?.toString()?.trim('"')
        when (roleString) {
            "admin" -> UserRole.ADMIN
            else -> UserRole.USER
        }
    } catch (e: Exception) {
        UserRole.USER  // Default to USER if role not found
    }
}
```

## Access Control

### Android App

**Role-Based Feature Gating:**
```kotlin
// In ViewModel or Screen
val currentUser by userManager.currentUser.collectAsState()

if (currentUser?.isAdmin == true) {
    // Show admin features
    AdminSettingsButton()
} else {
    // Hide admin features
}

// Or check before operations
fun performAdminAction() {
    val user = userManager.requireUserId()
    if (!user.isAdmin) {
        throw UnauthorizedException("Admin access required")
    }
    // Perform admin action
}
```

### Dashboard

**Role-Based Route Protection:**
```javascript
// Auth middleware
export function requireAdmin(request, reply) {
  const user = request.user;
  
  if (!user || !user.isAdmin) {
    reply.code(403);
    return { error: 'Admin access required' };
  }
  
  return; // Continue
}

// Protect admin routes
fastify.get('/admin/*', async (request, reply) => {
  await requireAuth(request, reply);
  await requireAdmin(request, reply);
  // Admin-only code
});
```

## Feature Gating

### Dashboard Features

**Public Features (No Auth):**
- View metrics dashboard (`/`)
- View public pages (`/pages/:id` if `isPublic: true`)
- View API status (`/api/status`)

**Authenticated Features (Any User):**
- View own pages (`/pages/:id` if owner)
- View own metrics

**Admin-Only Features:**
- Content authoring (`/author/*`)
- Admin dashboard (`/admin/*`)
- User management (future)
- System configuration (future)

### Android App Features

**Regular User Features:**
- Mood tracking
- Data sync
- View own data

**Admin-Only Features:**
- Admin settings (future)
- System diagnostics (future)
- User management (future)

## Implementation Plan

### Phase 1: Extend User Model

**Android App:**
1. Add `UserRole` enum
2. Add `role` field to `User` data class
3. Extract role from Supabase `user_metadata`
4. Add `isAdmin` property

**Dashboard:**
1. Create `User` class with role
2. Extract role from JWT token
3. Add `isAdmin` property

### Phase 2: Role Storage in Supabase

**Option A: User Metadata (Recommended)**
- Store role in `user_metadata.role`
- Accessible via JWT token
- Can be updated via Admin API
- No database migration needed

**Option B: Separate Roles Table**
- Create `user_roles` table
- More flexible (future: permissions, multiple roles)
- Requires database migration
- More complex queries

**Recommendation: Start with Option A (user_metadata), migrate to Option B if needed**

### Phase 3: Admin User Creation

**Via Admin API:**
```bash
# Create admin user
source scripts/lib/supabase-auth-admin.sh
auth_admin_create_user "" "admin@example.com" "password" "Admin User"

# Set admin role via metadata update
curl -X PUT "${SUPABASE_URL}/auth/v1/admin/users/${USER_ID}" \
  -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"user_metadata": {"role": "admin"}}'
```

### Phase 4: Protect Dashboard Routes

**Update Fastify routes:**
```javascript
// Public routes (no auth)
fastify.get('/', ...);  // Metrics dashboard
fastify.get('/pages/:id', ...);  // Public pages

// Authenticated routes (any user)
fastify.get('/api/metrics', requireAuth, ...);

// Admin-only routes
fastify.get('/author/*', requireAuth, requireAdmin, ...);
fastify.get('/admin/*', requireAuth, requireAdmin, ...);
```

### Phase 5: Update Android App

**Add role checks:**
```kotlin
// In SupabaseAuthProvider
override suspend fun getCurrentUser(): User? {
    val session = supabaseClient.auth.currentSessionOrNull()
    val userInfo = session?.user
    
    if (userInfo != null) {
        val role = extractRole(userInfo.userMetadata)
        return User(
            id = userInfo.id,
            email = userInfo.email ?: "",
            displayName = extractDisplayName(userInfo.userMetadata, userInfo.email),
            createdAt = userInfo.createdAt?.toEpochMilliseconds(),
            role = role  // NEW
        )
    }
    return null
}
```

## Security Considerations

### Role Assignment

**Who Can Assign Roles:**
- ✅ **Admin users** (via Admin API, future feature)
- ✅ **Service role** (via scripts/CI/CD)
- ❌ **Regular users** (cannot self-promote)

### Role Verification

**Server-Side (Dashboard):**
- Always verify role from JWT token
- Never trust client-provided role
- Verify token with Supabase on each request

**Client-Side (Android):**
- Role is for UI only (show/hide features)
- Server-side verification still required
- Never trust client role for security

### Token Security

**JWT Token Claims:**
- Role stored in `user_metadata` (part of token)
- Token signed by Supabase (cannot be forged)
- Token expiration handled by Supabase
- Refresh tokens for session management

## Migration Strategy

### Existing Users

**Default Role Assignment:**
- All existing users default to `USER` role
- No migration needed (role is optional in metadata)
- Admin users assigned manually via Admin API

### New Users

**Sign-Up Flow:**
- New users default to `USER` role
- Role set automatically in `user_metadata`
- Can be upgraded to `ADMIN` by existing admin

## Admin User Management

### Creating Admin Users

**Via Script:**
```bash
# Create admin user script
./scripts/create-admin-user.sh admin@example.com "Admin Name"
```

**Via Supabase Dashboard:**
1. Go to Authentication → Users
2. Create or select user
3. Edit user metadata
4. Add `role: "admin"`

**Via Admin API:**
```bash
# Update user role
curl -X PUT "${SUPABASE_URL}/auth/v1/admin/users/${USER_ID}" \
  -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"user_metadata": {"role": "admin"}}'
```

## Access Control Matrix

| Feature | Public | Authenticated User | Admin |
|---------|--------|-------------------|-------|
| View metrics dashboard | ✅ | ✅ | ✅ |
| View public pages | ✅ | ✅ | ✅ |
| View own pages | ❌ | ✅ | ✅ |
| Create/edit pages | ❌ | ❌ | ✅ |
| Admin dashboard | ❌ | ❌ | ✅ |
| User management | ❌ | ❌ | ✅ |
| System config | ❌ | ❌ | ✅ |

## Future Enhancements

### Additional Roles (Future)

**Potential Roles:**
- `editor` - Can create/edit pages but not admin features
- `viewer` - Can view all pages but not edit
- `moderator` - Can moderate content

### Permissions System (Future)

**Granular Permissions:**
- `pages.create`
- `pages.edit`
- `pages.delete`
- `admin.access`
- `users.manage`

**Role-Permission Mapping:**
- `admin` → All permissions
- `editor` → `pages.*` permissions
- `user` → No special permissions

## Benefits

1. **Unified Auth**
   - Same user accounts across platforms
   - Single source of truth (Supabase)
   - Consistent user experience

2. **Security**
   - Role-based access control
   - Server-side verification
   - Proper authorization

3. **Scalability**
   - Easy to add new roles
   - Can migrate to permissions system
   - Flexible architecture

4. **Consistency**
   - Same patterns across platforms
   - Reuses existing Supabase Auth
   - No duplicate auth systems

## Related Documentation

- `docs/architecture/AUTHENTICATION.md` - Android app authentication
- `docs/development/analysis/DASHBOARD_AUTH_SECURITY_PROPOSAL.md` - Dashboard security
- `app/src/main/java/com/electricsheep/app/auth/User.kt` - User model
- `app/src/main/java/com/electricsheep/app/auth/SupabaseAuthProvider.kt` - Auth provider

