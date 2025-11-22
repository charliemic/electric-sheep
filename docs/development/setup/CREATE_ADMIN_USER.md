# Creating Admin Users

**Purpose**: Create or promote users to admin role for dashboard access

## Quick Start

```bash
# Set environment variables
export SUPABASE_URL="https://xxx.supabase.co"
export SUPABASE_SECRET_KEY="sb_secret_xxx"  # Or SUPABASE_SERVICE_ROLE_KEY

# Create new admin user
./scripts/create-admin-user.sh admin@example.com "Admin User"

# Or promote existing user to admin
./scripts/create-admin-user.sh existing@example.com "Existing User"
```

## Prerequisites

1. **Supabase Credentials:**
   - `SUPABASE_URL` - Your Supabase project URL
   - `SUPABASE_SECRET_KEY` - Service role key (starts with `sb_secret_...`)
   - Or `SUPABASE_SERVICE_ROLE_KEY` (for compatibility)

2. **Get Your Credentials:**
   - Go to Supabase Dashboard → Settings → API
   - Copy the **secret** key (not the anon key)

## Usage

### Create New Admin User

```bash
./scripts/create-admin-user.sh admin@example.com "Admin User"
```

**What it does:**
- Creates a new user with the specified email
- Generates a secure random password (displays it)
- Sets role to `admin` in user metadata
- Email is automatically confirmed

**Output:**
```
Checking if user exists...
User does not exist. Creating new admin user...
Generated password: xyz123abc456
⚠️  Save this password securely!
User created: admin@example.com
Setting role to admin...
✓ Admin user created successfully!

Credentials:
  Email: admin@example.com
  Password: xyz123abc456
  Role: admin

⚠️  Save these credentials securely!
```

### Promote Existing User to Admin

```bash
./scripts/create-admin-user.sh existing@example.com
```

**What it does:**
- Finds existing user by email
- Updates user metadata to set `role: "admin"`
- Does not change password

**Output:**
```
Checking if user exists...
User exists: existing@example.com
Setting role to admin...
✓ User existing@example.com is now an admin
```

### Create Admin User with Custom Password

```bash
./scripts/create-admin-user.sh admin@example.com "Admin User" "my-secure-password"
```

**What it does:**
- Creates user with specified password
- Sets role to `admin`
- Uses provided password instead of generating one

## Script Features

- ✅ **Idempotent** - Safe to run multiple times
- ✅ **Auto-detection** - Creates new user or promotes existing
- ✅ **Password generation** - Auto-generates secure password if not provided
- ✅ **Role management** - Sets `role: "admin"` in user metadata
- ✅ **Error handling** - Clear error messages

## How It Works

1. **Check if user exists** - Queries Supabase by email
2. **If exists:**
   - Updates `user_metadata.role` to `"admin"`
3. **If not exists:**
   - Creates new user via Admin API
   - Sets `user_metadata.role` to `"admin"`
   - Generates password (if not provided)

## Role Storage

**Location:** Supabase `user_metadata.role`

**Values:**
- `"user"` - Default role (regular user)
- `"admin"` - Admin role (can access dashboard authoring)

**Access:**
- Available in JWT token claims
- Extracted server-side in auth middleware
- Defaults to `"user"` if not set

## Verification

**Check user role in Supabase Dashboard:**
1. Go to Authentication → Users
2. Find user by email
3. Check `user_metadata.role` field

**Or via API:**
```bash
curl -X GET "${SUPABASE_URL}/auth/v1/admin/users?email=eq.admin@example.com" \
  -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
  -H "apikey: ${SUPABASE_SECRET_KEY}" | jq '.users[0].user_metadata.role'
```

## Testing

**After creating admin user:**

1. **Start dashboard:**
   ```bash
   cd scripts/metrics
   npm start
   ```

2. **Login:**
   - Navigate to `http://localhost:8080/login`
   - Sign in with admin credentials
   - Should redirect to dashboard

3. **Test admin access:**
   - Navigate to `http://localhost:8080/author`
   - Should see authoring interface (admin only)

4. **Test regular user:**
   - Sign in with non-admin user
   - Try to access `/author`
   - Should get 403 Forbidden or redirect

## Troubleshooting

### "SUPABASE_SECRET_KEY not set"
- Set environment variable: `export SUPABASE_SECRET_KEY=sb_secret_xxx`
- Or use: `export SUPABASE_SERVICE_ROLE_KEY=sb_secret_xxx`

### "User not found" (when promoting)
- Verify email is correct
- Check user exists in Supabase Dashboard

### "Failed to set admin role"
- Check Supabase API is accessible
- Verify secret key has admin permissions
- Check user metadata format in Supabase

## Related Documentation

- `docs/architecture/SHARED_AUTH_ARCHITECTURE.md` - Auth architecture
- `scripts/lib/supabase-auth-admin.sh` - Auth admin library
- `docs/development/analysis/AUTH_IMPLEMENTATION_STATUS.md` - Implementation status

