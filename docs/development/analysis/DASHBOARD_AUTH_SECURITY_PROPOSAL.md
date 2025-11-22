# Dashboard Authoring Security Proposal

**Date**: 2025-01-20  
**Issue**: Content authoring feature needs authentication/authorization with role-based access  
**Goal**: Integrate Supabase Auth with dashboard, implement user roles (admin/user), gate admin features

## Current State

### Authentication Architecture (Android App)

**Existing System:**
- ✅ **Supabase Auth** - Fully implemented and working
- ✅ **UserManager** - Manages user sessions
- ✅ **SupabaseAuthProvider** - Handles email/password and Google OAuth
- ✅ **Session Management** - JWT tokens stored by Supabase SDK
- ✅ **User Scoping** - All data operations scoped to authenticated user
- ⚠️ **No Roles** - All users have same permissions

**Key Components:**
- `UserManager` - Reactive user state management
- `SupabaseAuthProvider` - Supabase Auth implementation
- `User` - User model (id, email, displayName) - *to be extended with role*
- Session tokens - JWT stored by Supabase SDK

### Dashboard Authoring (Current - No Auth)

**Current Implementation:**
- ❌ **No authentication** - Anyone can create/edit pages
- ❌ **No authorization** - No user scoping
- ❌ **No access control** - All pages are public
- ❌ **No role-based access** - No admin/user distinction
- ⚠️ **Security Risk** - Unauthorized access to authoring features

## Security Requirements

### Critical Security Needs

1. **Authentication Required**
   - Users must sign in to create/edit pages
   - Use existing Supabase Auth (already in ecosystem)
   - Reuse same user accounts as Android app

2. **Authorization & Roles**
   - **User Roles**: Regular users (`user`) and Admin users (`admin`)
   - **Admin-Only Features**: Content authoring, admin dashboard gated behind admin role
   - **User Scoping**: Users can only edit their own pages
   - **Future**: Shared pages with permissions

3. **Access Control**
   - Public pages: Anyone can view
   - Private pages: Only creator can view
   - Admin features: Only admin users can access

4. **Data Scoping**
   - Pages linked to user ID (like moods in Android app)
   - Queries filter by userId
   - Prevents cross-user access

## Proposed Solution: Shared Supabase Auth with Role-Based Access

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Supabase Auth                            │
│  (Single Source of Truth - Shared by Android & Dashboard)   │
│                                                              │
│  - User Accounts (email/password, Google OAuth)             │
│  - JWT Tokens (access_token, refresh_token)                 │
│  - User Metadata (role: "user" | "admin")                   │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
    ┌──────────▼──────────┐      ┌───────────▼──────────┐
    │   Android App       │      │   Dashboard Server    │
    │   (Kotlin)          │      │   (Node.js/Fastify)   │
    │                     │      │                       │
    │  UserManager        │      │  Auth Middleware      │
    │  User (with role)   │      │  Token Verification   │
    │  Role checks        │      │  Role checks          │
    └─────────────────────┘      └───────────────────────┘
```

### User Roles

**Role Types:**
1. **Regular User** (`user`) - Default
   - Can use Android app
   - Can view public dashboard pages
   - Cannot access authoring/admin features

2. **Admin User** (`admin`)
   - Can access dashboard authoring
   - Can access admin features
   - Can manage users (future)

**Role Storage:**
- Stored in Supabase `user_metadata.role`
- Accessible via JWT token claims
- Default: `"user"` if not set

## Implementation Plan

### Phase 1: Extend User Model with Roles

**Android App:**
1. Add `UserRole` enum
2. Add `role` field to `User` data class
3. Extract role from Supabase `user_metadata`
4. Add `isAdmin` property

**Dashboard:**
1. Create `User` class with role
2. Extract role from JWT token
3. Add `isAdmin` property

### Phase 2: Add Authentication Middleware

**Create `scripts/metrics/auth-middleware.js`:**

```javascript
import { readFileSync } from 'fs';
import { join } from 'path';

const SUPABASE_URL = process.env.SUPABASE_URL || '';
const SUPABASE_ANON_KEY = process.env.SUPABASE_ANON_KEY || '';

/**
 * Verify Supabase JWT token and extract user with role
 */
async function verifyToken(token) {
  if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
    return { valid: false, error: 'Supabase not configured' };
  }
  
  try {
    // Verify token with Supabase
    const response = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'apikey': SUPABASE_ANON_KEY
      }
    });
    
    if (response.ok) {
      const userData = await response.json();
      
      // Extract user with role
      const user = {
        id: userData.id,
        email: userData.email,
        displayName: userData.user_metadata?.display_name,
        role: userData.user_metadata?.role || 'user'  // Default to 'user'
      };
      
      return { valid: true, user };
    } else {
      return { valid: false, error: 'Invalid token' };
    }
  } catch (error) {
    return { valid: false, error: error.message };
  }
}

/**
 * Fastify authentication decorator
 */
export async function authenticate(request, reply) {
  // Skip auth for public routes
  const publicRoutes = ['/', '/api/metrics', '/api/status', '/pages/:id', '/login'];
  const isPublic = publicRoutes.some(route => {
    const pattern = route.replace(/:[^/]+/g, '[^/]+');
    return new RegExp(`^${pattern}$`).test(request.url.split('?')[0]);
  });
  
  if (isPublic) {
    return; // No auth required
  }
  
  // Require auth for protected routes
  const authHeader = request.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    reply.code(401);
    return { error: 'Authentication required' };
  }
  
  const token = authHeader.substring(7);
  const verification = await verifyToken(token);
  
  if (!verification.valid) {
    reply.code(401);
    return { error: verification.error || 'Invalid token' };
  }
  
  // Attach user to request
  request.user = verification.user;
  return;
}

/**
 * Require authentication decorator
 */
export function requireAuth(fastify) {
  fastify.addHook('onRequest', async (request, reply) => {
    const protectedRoutes = ['/author', '/api/author', '/admin'];
    const requiresAuth = protectedRoutes.some(route => 
      request.url.startsWith(route)
    );
    
    if (requiresAuth) {
      await authenticate(request, reply);
      if (reply.statusCode === 401) {
        return; // Already sent error response
      }
    }
  });
}

/**
 * Require admin role
 */
export function requireAdmin(request, reply) {
  const user = request.user;
  
  if (!user) {
    reply.code(401);
    return { error: 'Authentication required' };
  }
  
  if (user.role !== 'admin') {
    reply.code(403);
    return { error: 'Admin access required' };
  }
  
  return; // Continue
}
```

### Phase 3: Update Content Authoring Module

**Update `content-author.js` to include userId:**

```javascript
/**
 * Save authored page (with user scoping)
 */
export function savePage(pageId, content, metadata = {}, userId) {
  const pageData = {
    id: pageId,
    content,
    userId, // Add user ID
    metadata: {
      title: metadata.title || 'Untitled Page',
      createdAt: metadata.createdAt || new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      theme: metadata.theme || 'light',
      includeTOC: metadata.includeTOC !== false,
      isPublic: metadata.isPublic || false, // Public/private flag
      ...metadata
    }
  };
  
  // ... rest of save logic
}

/**
 * Load authored page (with user verification)
 */
export function loadPage(pageId, userId = null) {
  const page = loadPageFromFile(pageId);
  
  if (!page) {
    return null;
  }
  
  // Check access permissions
  if (userId && page.userId !== userId && !page.metadata.isPublic) {
    return null; // User doesn't have access
  }
  
  return page;
}

/**
 * List pages (filtered by user)
 */
export function listPages(userId = null) {
  const allPages = loadAllPages();
  
  if (userId) {
    // Return user's pages + public pages
    return allPages.filter(page => 
      page.userId === userId || page.metadata.isPublic
    );
  } else {
    // Return only public pages
    return allPages.filter(page => page.metadata.isPublic);
  }
}
```

### Phase 4: Update Fastify Routes

**Add authentication and admin checks to routes:**

```javascript
import { requireAuth, requireAdmin } from './auth-middleware.js';

// Add auth middleware
requireAuth(fastify);

// Content authoring routes (ADMIN ONLY)
fastify.get('/author', async (request, reply) => {
    await requireAuth(request, reply);
    if (reply.statusCode === 401) return;
    
    await requireAdmin(request, reply);
    if (reply.statusCode === 403) return;
    
    reply.type('text/html');
    return getAuthoringInterfaceHTML();
});

fastify.get('/author/new', async (request, reply) => {
    await requireAuth(request, reply);
    if (reply.statusCode === 401) return;
    
    await requireAdmin(request, reply);
    if (reply.statusCode === 403) return;
    
    reply.type('text/html');
    return getNewPageEditorHTML();
});

fastify.get('/author/edit/:id', async (request, reply) => {
    await requireAuth(request, reply);
    if (reply.statusCode === 401) return;
    
    await requireAdmin(request, reply);
    if (reply.statusCode === 403) return;
    
    const { id } = request.params;
    const userId = request.user.id;
    const page = loadPage(id, userId);
    
    if (!page) {
        reply.code(404);
        return { error: 'Page not found or access denied' };
    }
    
    reply.type('text/html');
    return getEditPageEditorHTML(page);
});

fastify.post('/api/author/save', async (request, reply) => {
    await requireAuth(request, reply);
    if (reply.statusCode === 401) return;
    
    await requireAdmin(request, reply);
    if (reply.statusCode === 403) return;
    
    const { id, content, metadata } = request.body;
    const userId = request.user.id;
    
    try {
        const pageId = id || `page-${Date.now()}`;
        const page = savePage(pageId, content, metadata, userId);
        return { success: true, page };
    } catch (error) {
        reply.code(400);
        return { success: false, error: error.message };
    }
});

fastify.get('/api/author/pages', async (request, reply) => {
    await requireAuth(request, reply);
    if (reply.statusCode === 401) return;
    
    await requireAdmin(request, reply);
    if (reply.statusCode === 403) return;
    
    const userId = request.user.id;
    return { pages: listPages(userId) };
});

fastify.get('/pages/:id', async (request, reply) => {
    const { id } = request.params;
    const userId = request.user?.id; // Optional - for private pages
    
    const page = loadPage(id, userId);
    
    if (!page) {
        reply.code(404);
        reply.type('text/html');
        return '<h1>Page not found</h1><p>The requested page does not exist or you do not have access.</p>';
    }
    
    const html = generatePageHTML(page, {
        theme: page.metadata.theme || 'light',
        includeTOC: page.metadata.includeTOC !== false
    });
    reply.type('text/html');
    return html;
});

// API endpoint for live data in pages
fastify.get('/api/author/data/:source', async (request, reply) => {
    // Public endpoint - no auth required (data is public)
    const { source } = request.params;
    
    try {
        let data;
        if (source === 'metrics:latest') {
            data = getAllMetrics();
        } else if (source === 'tests:latest') {
            const testMetric = getLatestMetric('tests', 'test_.*\\.json');
            data = testMetric;
        } else if (source.startsWith('logs:')) {
            // Log loading would be implemented here
            reply.code(501);
            return { success: false, error: 'Log loading not yet implemented' };
        } else {
            reply.code(404);
            return { success: false, error: 'Unknown data source' };
        }
        
        return { success: true, data };
    } catch (error) {
        reply.code(500);
        return { success: false, error: error.message };
    }
});

// API endpoint for chart data
fastify.get('/api/author/chart/:type', async (request, reply) => {
    // Public endpoint - no auth required
    const { type } = request.params;
    
    try {
        // Generate chart config based on type
        let config;
        if (type === 'complexity-trend') {
            // Would generate complexity trend chart config
            config = {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: 'Complexity',
                        data: []
                    }]
                }
            };
        } else {
            reply.code(404);
            return { success: false, error: 'Unknown chart type' };
        }
        
        return { success: true, config };
    } catch (error) {
        reply.code(500);
        return { success: false, error: error.message };
    }
});
```

### Phase 5: Add Login UI to Dashboard

**Create login page:**

```javascript
fastify.get('/login', async (request, reply) => {
    reply.type('text/html');
    return getLoginPageHTML();
});

fastify.post('/api/auth/login', async (request, reply) => {
    const { email, password } = request.body;
    
    try {
        // Sign in via Supabase Auth API
        const response = await fetch(`${SUPABASE_URL}/auth/v1/token?grant_type=password`, {
            method: 'POST',
            headers: {
                'apikey': SUPABASE_ANON_KEY,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const { access_token, user } = await response.json();
            
            // Extract role from user metadata
            const role = user.user_metadata?.role || 'user';
            
            return { 
                success: true, 
                token: access_token,
                user: {
                    id: user.id,
                    email: user.email,
                    displayName: user.user_metadata?.display_name,
                    role: role
                }
            };
        } else {
            reply.code(401);
            return { success: false, error: 'Invalid credentials' };
        }
    } catch (error) {
        reply.code(500);
        return { success: false, error: error.message };
    }
});
```

### Phase 6: Client-Side Auth Handling

**Update authoring interface to handle auth:**

```javascript
// In authoring interface HTML
<script>
    // Check if user is authenticated and is admin
    const token = localStorage.getItem('auth_token');
    const userRole = localStorage.getItem('user_role');
    
    if (!token && window.location.pathname.startsWith('/author')) {
        // Redirect to login
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    } else if (userRole !== 'admin' && window.location.pathname.startsWith('/author')) {
        // Redirect to dashboard (not admin)
        window.location.href = '/?error=admin_required';
    }
    
    // Store token after login
    function handleLogin(token, user) {
        localStorage.setItem('auth_token', token);
        localStorage.setItem('user_role', user.role);
        // Include token in all API requests
    }
    
    // Include token in API requests
    async function apiRequest(url, options = {}) {
        const token = localStorage.getItem('auth_token');
        const headers = {
            ...options.headers,
            'Authorization': `Bearer ${token}`
        };
        
        return fetch(url, { ...options, headers });
    }
</script>
```

## Security Considerations

### Token Storage

**Client-Side (Browser):**
- Store JWT in `localStorage` (simple, but vulnerable to XSS)
- Or use `httpOnly` cookies (more secure, requires cookie handling)
- **Recommendation**: Start with `localStorage`, upgrade to cookies later

### Token Verification

**Server-Side:**
- Verify JWT with Supabase on every request
- Cache verification results (optional, for performance)
- Handle token expiration gracefully

### Access Control

**Page Permissions:**
- `isPublic: true` - Anyone can view
- `isPublic: false` - Only creator can view
- Future: Shared pages with specific user permissions

**Role-Based Access:**
- Admin-only routes require `role === 'admin'`
- Regular users cannot access authoring features
- Server-side verification (never trust client)

### Data Scoping

**User Isolation:**
- All pages linked to `userId`
- Queries filter by `userId`
- Prevents users from accessing other users' pages
- Same pattern as Android app (moods scoped to user)

## Integration with Existing Auth

### Shared Supabase Auth

**Benefits:**
- ✅ Same user accounts as Android app
- ✅ Single source of truth
- ✅ No separate auth system needed
- ✅ Already configured and working
- ✅ **Role-based access control** - Admin features gated

**Implementation:**
- Use Supabase Auth API (`/auth/v1/token`)
- Verify tokens via Supabase (`/auth/v1/user`)
- Extract user info and **role** from JWT payload
- Store role in `user_metadata.role` (Supabase)

### Role Management

**Setting Admin Role:**
```bash
# Via Admin API
curl -X PUT "${SUPABASE_URL}/auth/v1/admin/users/${USER_ID}" \
  -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
  -H "Content-Type: application/json" \
  -d '{"user_metadata": {"role": "admin"}}'
```

**Default Role:**
- New users default to `"user"` role
- Role stored in `user_metadata.role`
- Accessible via JWT token claims

### User Model Alignment

**Android App User (Extended):**
```kotlin
data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val createdAt: Long,
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

**Dashboard User (from Supabase):**
```javascript
{
  id: "uuid",
  email: "user@example.com",
  user_metadata: { 
    display_name: "Name",
    role: "admin"  // NEW
  },
  created_at: "2024-01-01T00:00:00Z"
}
```

**Mapping:**
- `id` → `id`
- `email` → `email`
- `user_metadata.display_name` → `displayName`
- `user_metadata.role` → `role` (default: `"user"`)
- `created_at` → `createdAt`

## Migration Path

### Step 1: Extend User Model with Roles

**Android App:**
1. Add `UserRole` enum
2. Add `role` field to `User` data class
3. Extract role from Supabase `user_metadata`
4. Add `isAdmin` property

**Dashboard:**
1. Create `User` class with role
2. Extract role from JWT token
3. Add `isAdmin` property

### Step 2: Add Auth Middleware (No Breaking Changes)

1. Add `auth-middleware.js` with role extraction
2. Add `requireAdmin` middleware
3. Add auth routes (`/login`, `/api/auth/login`)
4. Make auth optional (existing routes still work)
5. Test authentication flow

### Step 3: Protect Authoring Routes (Admin Only)

1. Add `requireAuth` + `requireAdmin` middleware to authoring routes
2. Update `content-author.js` to include `userId`
3. Add user scoping to page operations
4. Test with authenticated admin users
5. Verify regular users cannot access authoring

### Step 4: Add Login UI

1. Create login page (`/login`)
2. Add login form (email/password)
3. Handle OAuth (optional, for Google sign-in)
4. Store token in localStorage
5. Redirect after login

### Step 5: Update Client-Side

1. Add token to API requests
2. Handle 401 errors (redirect to login)
3. Handle 403 errors (show "admin required" message)
4. Show user info in dashboard
5. Add logout functionality

## Benefits of This Approach

1. **Unified Auth**
   - Same user accounts as Android app
   - Single sign-on (if implemented)
   - Consistent user experience

2. **Security**
   - Authentication required for authoring
   - Role-based access control (admin/user)
   - User-scoped pages
   - Access control (public/private)

3. **Consistency**
   - Same auth patterns as Android app
   - Reuses existing Supabase Auth
   - No new auth system needed

4. **Scalability**
   - Can add more roles later
   - Can add permissions system later
   - Can add shared pages later
   - Can add admin users later

## Access Control Matrix

| Feature | Public | Authenticated User | Admin |
|---------|--------|-------------------|-------|
| View metrics dashboard | ✅ | ✅ | ✅ |
| View public pages | ✅ | ✅ | ✅ |
| View own pages | ❌ | ✅ | ✅ |
| **Create/edit pages** | ❌ | ❌ | ✅ |
| **Admin dashboard** | ❌ | ❌ | ✅ |
| User management | ❌ | ❌ | ✅ |
| System config | ❌ | ❌ | ✅ |

## Decision

**Recommended**: ✅ **Shared Supabase Auth with Role-Based Access Control**

**Rationale:**
- Already have Supabase Auth working
- Same user accounts as Android app
- Proper security (JWT tokens, user scoping)
- Role-based access control (admin/user)
- Admin features properly gated
- Consistent architecture across platforms
- Scalable (can add more roles/permissions later)

## Next Steps

1. Review this proposal
2. Approve approach
3. Implement Phase 1 (extend user model with roles)
4. Implement Phase 2 (auth middleware)
5. Implement Phase 3 (protect routes)
6. Add login UI
7. Update client-side auth handling
8. Create admin user for testing

## Related Documentation

- `docs/architecture/SHARED_AUTH_ARCHITECTURE.md` - Complete shared auth architecture
- `docs/architecture/AUTHENTICATION.md` - Android app authentication
- `app/src/main/java/com/electricsheep/app/auth/User.kt` - User model (to be extended)
