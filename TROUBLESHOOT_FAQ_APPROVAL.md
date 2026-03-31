# Troubleshooting: Admin Can't Approve FAQ

## Issue
Admin clicks "Approve" button but FAQ doesn't get approved.

## Diagnostic Steps

### Step 1: Check Browser Console

1. Open the FAQ approval page: `http://localhost:8080/admin/faq/pending`
2. Press **F12** to open Developer Tools
3. Go to **Console** tab
4. Click "Approve" button
5. Look for error messages

**Expected Console Output:**
```
Approve button clicked for FAQ ID: 1
Sending approval request...
Response status: 200
Response data: {success: true, message: "FAQ approved successfully"}
```

**If you see errors:**
- Note the error message
- Check the Network tab for the request details

### Step 2: Check Network Tab

1. In Developer Tools, go to **Network** tab
2. Click "Approve" button
3. Look for request to `/admin/faq/approve/{id}`

**Check:**
- ✓ Request method: POST
- ✓ Status code: 200 (success) or 4xx/5xx (error)
- ✓ Response body

**Common Issues:**

| Status Code | Meaning | Solution |
|-------------|---------|----------|
| 403 | Forbidden - CSRF token missing | See CSRF fix below |
| 404 | Not Found - Wrong URL | Check endpoint mapping |
| 500 | Server Error | Check backend logs |

### Step 3: Check Backend Logs

Look in your application console for these messages:

**Expected Output:**
```
Approve FAQ called for ID: 1
FAQ found: How to reset password?
Current user: admin
Admin found: admin
FAQ approved successfully!
```

**If you see errors:**
- Copy the full error stack trace
- Check which line is failing

## Common Fixes

### Fix 1: Database Columns Missing

**Symptom:** Error in logs about missing columns

**Solution:** Run the database migration:
```sql
-- Run check_and_fix_faq_table.sql
```

### Fix 2: CSRF Token Issue

**Symptom:** 403 Forbidden error in Network tab

**Solution:** Check if CSRF is enabled. Add meta tag to HTML:

In `admin-faq-approval.html`, add to `<head>`:
```html
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>
```

And update JavaScript:
```javascript
const token = document.querySelector('meta[name="_csrf"]').content;
const header = document.querySelector('meta[name="_csrf_header"]').content;

fetch(`/admin/faq/approve/${faqId}`, {
    method: 'POST',
    headers: {
        [header]: token,
        'Content-Type': 'application/json'
    }
})
```

### Fix 3: Security Configuration

**Symptom:** 403 or 401 error

**Check:** `SecurityConfig.java`

Ensure these endpoints are accessible to admins:
```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

### Fix 4: User Not Found

**Symptom:** "Admin not found" in logs

**Check:** Current logged-in user
```sql
-- Check users table
SELECT id, username, role FROM users WHERE role LIKE '%ADMIN%';
```

**Solution:** Ensure you're logged in as an admin user.

### Fix 5: FAQ Not Found

**Symptom:** "FAQ not found" error

**Check:** FAQ exists in database
```sql
-- Check FAQ table
SELECT id, question, is_approved FROM faq WHERE is_approved = 0;
```

### Fix 6: Foreign Key Constraint

**Symptom:** Error saving FAQ with approved_by

**Solution:** Ensure foreign keys exist:
```sql
-- Check foreign keys
SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('faq');
```

If missing, add them:
```sql
ALTER TABLE faq ADD CONSTRAINT FK_faq_approved_by_users 
FOREIGN KEY (approved_by) REFERENCES users(id);
```

## Testing Procedure

### 1. Verify Database Migration

```sql
-- Check table structure
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'faq'
ORDER BY ORDINAL_POSITION;
```

Expected columns:
- ✓ is_approved (bit)
- ✓ created_by (bigint)
- ✓ approved_by (bigint)
- ✓ approved_at (datetime)

### 2. Create Test FAQ

As an agent:
1. Go to `/agent/faq/add`
2. Fill in form
3. Submit
4. Should see: "FAQ article submitted for approval!"

### 3. Check Pending FAQs

```sql
-- Check pending FAQs
SELECT id, question, is_approved, created_by 
FROM faq 
WHERE is_approved = 0;
```

Should show at least one pending FAQ.

### 4. Test Approval

As admin:
1. Go to `/admin/faq/pending`
2. Should see pending FAQ
3. Click "Approve"
4. Check console logs (both browser and backend)
5. Should reload and FAQ should disappear from list

### 5. Verify Approval

```sql
-- Check if FAQ was approved
SELECT id, question, is_approved, approved_by, approved_at 
FROM faq 
WHERE id = 1;  -- Replace with your FAQ ID
```

Expected:
- is_approved = 1
- approved_by = (admin user ID)
- approved_at = (current timestamp)

### 6. Test Customer View

As customer or public:
1. Go to `/faq`
2. Should see the approved FAQ
3. Should NOT see unapproved FAQs

## Debug Mode

### Enable Detailed Logging

Add to `application.properties`:
```properties
# Enable SQL logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Enable debug logging
logging.level.lk.sliit.customer_care=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Check Request Details

In browser console:
```javascript
// Test the endpoint directly
fetch('/admin/faq/approve/1', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'}
})
.then(res => res.json())
.then(data => console.log('Result:', data))
.catch(err => console.error('Error:', err));
```

## Quick Test Script

Run this in browser console on the approval page:
```javascript
// Test if jQuery is available (if you're using it)
console.log('jQuery available:', typeof $ !== 'undefined');

// Test if FAQ ID is available
const firstButton = document.querySelector('.btn-approve');
console.log('First FAQ ID:', firstButton?.getAttribute('data-faq-id'));

// Test fetch directly
if (firstButton) {
    const faqId = firstButton.getAttribute('data-faq-id');
    console.log('Testing approval for FAQ:', faqId);
    
    fetch(`/admin/faq/approve/${faqId}`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'}
    })
    .then(res => {
        console.log('Status:', res.status);
        return res.json();
    })
    .then(data => console.log('Response:', data))
    .catch(err => console.error('Error:', err));
}
```

## Still Not Working?

### Collect This Information:

1. **Browser Console Output** (F12 → Console)
2. **Network Request Details** (F12 → Network)
3. **Backend Logs** (from terminal running the app)
4. **Database Query Results**:
   ```sql
   SELECT * FROM faq WHERE id = [failing FAQ ID];
   SELECT * FROM users WHERE role LIKE '%ADMIN%';
   ```

### Check These Files:

1. Is `AdminController.java` updated with logging?
2. Is `admin-faq-approval.html` updated with console.log?
3. Did you restart the application after changes?
4. Did you clear browser cache?

---

**Most Common Solution:** 
Run `check_and_fix_faq_table.sql` and restart the application!
