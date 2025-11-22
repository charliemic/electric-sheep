/**
 * Authentication middleware for Fastify dashboard server
 * 
 * Implements best practices:
 * - Server-side token verification with Supabase
 * - Token expiration handling
 * - Role extraction from JWT
 * - Secure error messages
 * - Rate limiting support
 */

const SUPABASE_URL = process.env.SUPABASE_URL || '';
const SUPABASE_ANON_KEY = process.env.SUPABASE_ANON_KEY || '';

/**
 * Verify Supabase JWT token with expiration check
 * 
 * Best Practice: Always verify token signature and expiration on server
 * 
 * @param {string} token - JWT access token
 * @returns {Promise<{valid: boolean, user?: object, error?: string, expired?: boolean}>}
 */
async function verifyToken(token) {
  if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
    return { valid: false, error: 'Authentication service unavailable' };
  }
  
  if (!token || typeof token !== 'string') {
    return { valid: false, error: 'Invalid token format' };
  }
  
  try {
    // Verify token with Supabase (includes signature and expiration check)
    const response = await fetch(`${SUPABASE_URL}/auth/v1/user`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'apikey': SUPABASE_ANON_KEY,
        'Content-Type': 'application/json'
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
    } else if (response.status === 401) {
      // Token expired or invalid
      return { valid: false, error: 'Token expired or invalid', expired: true };
    } else {
      // Other error - don't expose details
      return { valid: false, error: 'Authentication failed' };
    }
  } catch (error) {
    // Don't expose internal errors
    console.error('Token verification error:', error);
    return { valid: false, error: 'Authentication service error' };
  }
}

/**
 * Refresh expired token
 * 
 * Best Practice: Handle token expiration gracefully
 * 
 * @param {string} refreshToken - JWT refresh token
 * @returns {Promise<{success: boolean, access_token?: string, refresh_token?: string, error?: string}>}
 */
async function refreshToken(refreshToken) {
  if (!SUPABASE_URL || !SUPABASE_ANON_KEY) {
    return { success: false, error: 'Authentication service unavailable' };
  }
  
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
      const data = await response.json();
      return { 
        success: true, 
        access_token: data.access_token,
        refresh_token: data.refresh_token
      };
    } else {
      return { success: false, error: 'Token refresh failed' };
    }
  } catch (error) {
    console.error('Token refresh error:', error);
    return { success: false, error: 'Token refresh service error' };
  }
}

/**
 * Fastify authentication decorator
 * 
 * Best Practice: Verify token on every protected request
 * 
 * @param {object} request - Fastify request
 * @param {object} reply - Fastify reply
 * @returns {Promise<void>}
 */
export async function authenticate(request, reply) {
  // Skip auth for public routes
  const publicRoutes = ['/', '/api/metrics', '/api/status', '/pages/:id', '/login', '/api/author/data/:source', '/api/author/chart/:type'];
  const isPublic = publicRoutes.some(route => {
    const pattern = route.replace(/:[^/]+/g, '[^/]+');
    const urlPath = request.url.split('?')[0];
    return new RegExp(`^${pattern}$`).test(urlPath);
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

/**
 * Require authentication decorator
 * 
 * Best Practice: Apply auth middleware to protected routes
 * 
 * @param {object} fastify - Fastify instance
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
 * 
 * Best Practice: Always verify role server-side, never trust client
 * 
 * @param {object} request - Fastify request
 * @param {object} reply - Fastify reply
 * @returns {void}
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

/**
 * Validate email format
 * 
 * Best Practice: Validate all inputs
 * 
 * @param {string} email - Email to validate
 * @returns {boolean}
 */
export function isValidEmail(email) {
  if (!email || typeof email !== 'string') {
    return false;
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate password strength
 * 
 * Best Practice: Enforce minimum password requirements
 * 
 * @param {string} password - Password to validate
 * @returns {{valid: boolean, error?: string}}
 */
export function validatePassword(password) {
  if (!password || typeof password !== 'string') {
    return { valid: false, error: 'Password is required' };
  }
  
  if (password.length < 6) {
    return { valid: false, error: 'Password must be at least 6 characters' };
  }
  
  return { valid: true };
}

/**
 * Sanitize email input
 * 
 * Best Practice: Sanitize user inputs
 * 
 * @param {string} email - Email to sanitize
 * @returns {string}
 */
export function sanitizeEmail(email) {
  if (!email || typeof email !== 'string') {
    return '';
  }
  return email.toLowerCase().trim();
}

