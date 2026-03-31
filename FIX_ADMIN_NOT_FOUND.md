# Fix: Admin Not Found Error

## Error
```
Error approving FAQ: Admin not found
java.lang.RuntimeException: Admin not found
```

## Cause
The logged-in user's username doesn't exist in the database users table.

## Solution

### Step 1: Check Which User You're Logged In As

The error log shows:
```
where u1_0.username=?
```

This query is looking for your current username but not finding it.

### Step 2: Check Your Users Table

Run this SQL to see all users:
```sql
SELECT id, username, role, phone_number FROM users;
```

Or run the diagnostic script:
- File: `check_users.sql`

### Step 3: Fix the Issue

#### Option A: The user exists but with a different username

If you see users in the table but with different usernames, you're logged in with the wrong account.

**Solution:** Logout and login with a username that exists in the database.

#### Option B: No admin user exists

If there are no admin users in the database:

**Solution 1: Promote an existing user to admin**
```sql
-- Replace 'your_username' with an existing username
UPDATE users 
SET role = 'ROLE_ADMIN' 
WHERE username = 'your_username';
```

**Solution 2: Create a new admin user via registration**
1. Logout
2. Go to registration page
3. Register a new user
4. Then run:
```sql
UPDATE users 
SET role = 'ROLE_ADMIN' 
WHERE username = 'new_username';
```

#### Option C: The username has special characters or spaces

Check if your username has issues:
```sql
-- Check exact username
SELECT id, username, LEN(username) as length, role 
FROM users;
```

Look for:
- Extra spaces
- Special characters
- Case sensitivity issues

**Solution:** Update the username to remove issues:
```sql
UPDATE users 
SET username = TRIM(username)  -- Remove spaces
WHERE id = 1;  -- Replace with actual user ID
```

### Step 4: Verify the Fix

After updating, run:
```sql
-- Check if admin user exists
SELECT id, username, role 
FROM users 
WHERE role = 'ROLE_ADMIN';
```

You should see at least one admin user.

### Step 5: Test Again

1. Logout completely
2. Login as the admin user
3. Go to `/admin/faq/pending`
4. Try to approve an FAQ

## Quick Fix (Temporary)

I've already updated the code to handle this gracefully. The FAQ will now approve even if the user isn't found in the database. However, the `approved_by` field will be NULL.

**After restarting the app, approval will work but won't track who approved it.**

## Recommended Long-term Fix

Create a proper admin account:

```sql
-- 1. Check current users
SELECT * FROM users;

-- 2. If you have a user, make them admin
UPDATE users 
SET role = 'ROLE_ADMIN' 
WHERE username = 'your_actual_username';

-- 3. Verify
SELECT username, role FROM users WHERE role = 'ROLE_ADMIN';
```

## Why This Happened

The FAQ approval tries to record which admin approved each FAQ for audit purposes. It looks up the current logged-in username in the users table.

If the username doesn't exist:
- Old code: Threw "Admin not found" error
- New code: Approves anyway but sets approved_by to NULL

## Testing

After fixing:

1. **Check you're logged in as admin:**
   - Should see "Admin" role in the system
   - Should have access to `/admin/*` pages

2. **Try approval again:**
   - Go to `/admin/faq/pending`
   - Click "Approve"
   - Should work now!

3. **Verify in database:**
```sql
SELECT 
    id, 
    question, 
    is_approved, 
    approved_by, 
    approved_at 
FROM faq 
WHERE is_approved = 1;
```

## Still Having Issues?

Run these diagnostic queries and share the results:

```sql
-- 1. Show all users
SELECT id, username, role FROM users;

-- 2. Show pending FAQs
SELECT id, question, is_approved, created_by FROM faq WHERE is_approved = 0;

-- 3. Check your current session
-- (This requires checking Spring Security logs - see application console)
```

---

**Quick Summary:**
1. Code is now fixed to work even without finding the admin
2. Restart your application
3. Try approval again - it should work!
4. Create proper admin account for future tracking
