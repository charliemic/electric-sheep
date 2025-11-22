# Shared Authentication Architecture v2 (Best Practices Aligned)

**Date**: 2025-01-20  
**Status**: ✅ Best Practices Evaluated & Improved  
**Purpose**: Unified authentication and authorization system aligned with industry best practices

## Best Practices Evaluation

### ✅ What We're Doing Right

1. **Server-Side Token Verification** - Always verify JWT with Supabase on server
2. **Role-Based Access Control** - Clear admin/user distinction
3. **User Scoping** - All data scoped to userId
4. **Shared Auth** - Single source of truth (Supabase)
5. **Principle of Least Privilege** - Users only get minimum necessary access

### ⚠️ Improvements Needed (Based on Best Practices)

1. **Token Refresh Handling** - Need to handle expired tokens gracefully
2. **Rate Limiting** - Protect auth endpoints from brute force
3. **Error Messages** - Don't expose sensitive information
4. **Token Expiration Checks** - Verify token hasn't expired
5. **RLS Policies** - If storing pages in database, add RLS
6. **Token Storage** - localStorage is OK for MVP, consider httpOnly cookies for production

## Improved Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Supabase Auth                            │
│  (Single Source of Truth - Shared by Android & Dashboard)   │
│                                                              │
│  - User Accounts (email/password, Google OAuth)             │
│  - JWT Tokens (access_token, refresh_token)                 │
│  - User Metadata (role: "user" | "admin")                   │
│  - Custom Claims (via Auth Hook - optional enhancement)      │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
    ┌──────────▼──────────┐      ┌───────────▼──────────┐
    │   Android App       │      │   Dashboard Server    │
    │   (Kotlin)          │      │   (Node.js/Fastify)   │
    │                     │      │                       │
    │  UserManager        │      │  Auth Middleware      │
    │  User (with role)   │      │  - Token Verification  │
    │  Role checks        │      │  - Expiration Check    │
    │  Token refresh      │      │  - Rate Limiting       │
    └─────────────────────┘      └───────────────────────┘
```

## Implementation Improvements

### 1. Enhanced Token Verification

**Best Practice**: Verify token signature AND expiration

```javascript
/**
 * Verify Supabase JWT token with expiration check
 */
async function verifyToken(token) {
  if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
    return { valid: false, error: 'Authentication service unavailable' };
  }
  
  try {
    // Verify token with Supabase (includes signature and expiration check)
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
        role: userData.user_metadata?.role || 'user'
      };
      
      return { valid: true, user };
    } else if (response.status === 401) {
      // Token expired or invalid
      return { valid: false, error: 'Token expired', expired: true };
    } else {
      return { valid: false, error: 'Invalid token' };
    }
  } catch (error) {
    // Don't expose internal errors
    return { valid: false, error: 'Authentication service error' };
  }
}
```

### 2. Token Refresh Handling

**Best Practice**: Handle token expiration gracefully

```javascript
/**
 * Refresh expired token
 */
async function refreshToken(refreshToken) {
  try {
    const response = await fetch(`${SUPABASE_URL}/auth/v1/token?grant_type=refresh_token`, {
      method: 'POST',
      headers: {
        'apikey': SUPABASE_ANON_KEY,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ refresh_token: refreshToken })
    });
    
    if (response.ok) {
      const { access_token, refresh_token } = await response.json();
      return { success: true, access_token, refresh_token };
    } else {
      return { success: false, error: 'Token refresh failed' };
    }
  } catch (error) {
    return { success: false, error: 'Token refresh service error' };
  }
}
```

### 3. Rate Limiting

**Best Practice**: Protect auth endpoints from brute force

```javascript
import { rateLimit } from '@fastify/rate-limit';

// Rate limiting for auth endpoints
await fastify.register(rateLimit, {
  max: 5, // 5 requests
  timeWindow: '15 minutes', // per 15 minutes
  skipOnError: false
});

fastify.post('/api/auth/login', {
  config: {
    rateLimit: {
      max: 5,
      timeWindow: '15 minutes'
    }
  }
}, async (request, reply) => {
  // Login logic
});
```

### 4. Secure Error Messages

**Best Practice**: Don't expose sensitive information

```javascript
// ❌ BAD: Exposes internal details
return { error: `Database error: ${dbError.message}` };

// ✅ GOOD: Generic error message
return { error: 'Authentication failed' };

// ✅ GOOD: Log details server-side, return generic message
Logger.error('Auth', 'Login failed', { email, error: dbError });
return { error: 'Invalid email or password' };
```

### 5. Enhanced Auth Middleware

**Best Practice**: Comprehensive token verification

```javascript
/**
 * Enhanced authentication middleware with expiration handling
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
    
    // Include refresh hint if token expired
    if (verification.expired) {
      reply.header('X-Token-Expired', 'true');
    }
    
    return { error: 'Invalid or expired token' };
  }
  
  // Attach user to request
  request.user = verification.user;
  return;
}
```

### 6. Role Verification

**Best Practice**: Always verify role server-side

```javascript
/**
 * Require admin role (enhanced)
 */
export function requireAdmin(request, reply) {
  const user = request.user;
  
  if (!user) {
    reply.code(401);
    return { error: 'Authentication required' };
  }
  
  // Always verify role from token (never trust client)
  if (user.role !== 'admin') {
    reply.code(403);
    // Don't expose what role they have
    return { error: 'Access denied' };
  }
  
  return; // Continue
}
```

## Security Enhancements

### 1. Input Validation

**Best Practice**: Validate all inputs

```javascript
fastify.post('/api/auth/login', async (request, reply) => {
  const { email, password } = request.body;
  
  // Validate input
  if (!email || typeof email !== 'string' || !email.includes('@')) {
    reply.code(400);
    return { error: 'Invalid email format' };
  }
  
  if (!password || typeof password !== 'string' || password.length < 6) {
    reply.code(400);
    return { error: 'Invalid password' };
  }
  
  // Sanitize email (lowercase, trim)
  const sanitizedEmail = email.toLowerCase().trim();
  
  // Continue with login...
});
```

### 2. Logging (Security Events)

**Best Practice**: Log security events without sensitive data

```javascript
// Log authentication attempts
Logger.info('Auth', 'Login attempt', { 
  email: sanitizedEmail, 
  success: false,
  ip: request.ip 
});

// Don't log passwords or tokens
// ❌ BAD: Logger.info('Auth', 'Login', { password });
```

### 3. HTTPS Enforcement

**Best Practice**: Require HTTPS in production

```javascript
// In production, enforce HTTPS
if (process.env.NODE_ENV === 'production') {
  fastify.addHook('onRequest', async (request, reply) => {
    if (request.headers['x-forwarded-proto'] !== 'https') {
      reply.code(403);
      return { error: 'HTTPS required' };
    }
  });
}
```

## Token Storage Best Practices

### Current: localStorage (MVP)

**Pros:**
- ✅ Simple to implement
- ✅ Works immediately
- ✅ No cookie handling needed

**Cons:**
- ⚠️ Vulnerable to XSS attacks
- ⚠️ Accessible to JavaScript

**Mitigation:**
- ✅ Content Security Policy (CSP) headers
- ✅ Input sanitization
- ✅ XSS prevention in code

### Future: httpOnly Cookies (Production)

**Pros:**
- ✅ Not accessible to JavaScript (XSS protection)
- ✅ Automatic cookie handling
- ✅ CSRF protection with SameSite

**Cons:**
- ⚠️ More complex setup
- ⚠️ Requires cookie handling
- ⚠️ CSRF token needed

**Migration Path:**
- Start with localStorage for MVP
- Add httpOnly cookies for production
- Support both during transition

## Database Security (If Storing Pages in DB)

### Row-Level Security (RLS)

**Best Practice**: If we move pages to Supabase database, add RLS

```sql
-- Enable RLS on authored_pages table
ALTER TABLE authored_pages ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read their own pages + public pages
CREATE POLICY "Users can read own and public pages"
ON authored_pages
FOR SELECT
TO authenticated
USING (
  user_id = auth.uid() OR is_public = true
);

-- Policy: Only admins can create pages
CREATE POLICY "Admins can create pages"
ON authored_pages
FOR INSERT
TO authenticated
WITH CHECK (
  (auth.jwt() -> 'user_metadata' ->> 'role') = 'admin'
);

-- Policy: Users can update their own pages (if admin)
CREATE POLICY "Admins can update own pages"
ON authored_pages
FOR UPDATE
TO authenticated
USING (
  (auth.jwt() -> 'user_metadata' ->> 'role') = 'admin' AND
  user_id = auth.uid()
);
```

## Implementation Checklist

### Phase 1: Core Auth (MVP)
- [x] Token verification with Supabase
- [x] Role extraction from user_metadata
- [x] Admin middleware
- [ ] Token expiration handling
- [ ] Rate limiting on auth endpoints
- [ ] Secure error messages
- [ ] Input validation

### Phase 2: Enhanced Security
- [ ] Token refresh handling
- [ ] HTTPS enforcement
- [ ] Security event logging
- [ ] CSP headers
- [ ] XSS prevention

### Phase 3: Production Hardening
- [ ] httpOnly cookies
- [ ] CSRF protection
- [ ] RLS policies (if DB storage)
- [ ] Security monitoring
- [ ] Audit logging

## Best Practices Summary

### ✅ Following Best Practices

1. **Server-Side Verification** - Always verify tokens on server
2. **Role-Based Access** - Clear role hierarchy
3. **Least Privilege** - Users get minimum access
4. **User Scoping** - Data isolated by userId
5. **Shared Auth** - Single source of truth

### 🔄 Improvements Added

1. **Token Expiration** - Check and handle expired tokens
2. **Rate Limiting** - Protect against brute force
3. **Error Messages** - Generic, non-revealing errors
4. **Input Validation** - Validate all inputs
5. **Security Logging** - Log security events

### 📋 Future Enhancements

1. **httpOnly Cookies** - Better token storage
2. **RLS Policies** - Database-level security
3. **CSRF Protection** - Cookie-based auth security
4. **Security Monitoring** - Track auth events
5. **Audit Logging** - Compliance and security

## Related Documentation

- `docs/architecture/SHARED_AUTH_ARCHITECTURE.md` - Original architecture
- `docs/development/analysis/DASHBOARD_AUTH_SECURITY_PROPOSAL.md` - Security proposal
- `docs/security/SECURITY_PRINCIPLES.md` - Security principles

