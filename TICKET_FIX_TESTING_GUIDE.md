# Ticket Submission Fix - Testing Guide 🧪

## ✅ Compilation Status
**BUILD SUCCESS** - All files compiled without errors!

## Quick Test Steps

### 1. Start the Application
```bash
cd "c:\Users\senal\Downloads\Quick Share\Customer_Care"
mvn spring-boot:run
```

### 2. Test Ticket Submission

#### A. Login as Customer
1. Open browser: `http://localhost:8080/login`
2. Login with customer credentials
   - Username: `[your customer username]`
   - Password: `[your customer password]`

#### B. Navigate to Submit Ticket
1. Click "Submit Ticket" or go to `http://localhost:8080/tickets/submit`
2. Verify the form loads without errors

#### C. Fill and Submit Form
1. **Title**: Enter at least 5 characters (e.g., "Cannot access my account")
2. **Category**: Select from dropdown (Technical/Billing/Account/General/Other)
3. **Description**: Enter at least 10 characters describing the issue

#### D. Submit and Verify
1. Click "Submit Ticket" button
2. **Expected Result**: 
   - Redirects to success page showing:
     - Ticket ID
     - Title
     - Category
     - Status (should be "Open")
     - Your username
     - Created timestamp

### 3. Verify Ticket in Database

#### Option 1: Check via Application
- Go to "My Tickets" page
- Verify your newly created ticket appears in the list

#### Option 2: Check via SQL Server
```sql
USE Customer_Care_DB;
SELECT * FROM ticket ORDER BY created_at DESC;
```

## Expected Behavior

### ✅ What Should Work Now

1. **Form Loads**: Submit ticket page loads without errors
2. **CSRF Protection**: Form includes hidden CSRF token field
3. **Validation**: 
   - Client-side validation shows errors immediately
   - Server-side validation prevents invalid submissions
4. **Submission**: Form submits successfully
5. **Success Page**: Shows ticket details after submission
6. **Database**: Ticket saved with correct data:
   - Title, Description, Category ✅
   - Status = "Open" ✅
   - User reference ✅
   - Created timestamp ✅

### 🚫 Error Scenarios Handled

1. **Missing CSRF Token**: Won't happen (token auto-included)
2. **Expired Session**: Redirects to login with message
3. **Invalid Data**: Shows validation errors
4. **Database Error**: Shows user-friendly error message
5. **Null Authentication**: Redirects to login

## Test Cases

### Test Case 1: Valid Submission ✅
```
Title: "Login not working"
Category: "Technical"
Description: "I cannot login to my account. Getting error message."

Expected: Success page with ticket details
```

### Test Case 2: Title Too Short ❌
```
Title: "Bug"
Category: "Technical"
Description: "There is a bug in the system"

Expected: Error message "Title must be at least 5 characters"
```

### Test Case 3: Missing Category ❌
```
Title: "Payment Issue"
Category: [Not selected]
Description: "Cannot process payment"

Expected: Error message "Please select a category"
```

### Test Case 4: Description Too Short ❌
```
Title: "Support Request"
Category: "General"
Description: "Help"

Expected: Error message "Description must be at least 10 characters"
```

### Test Case 5: Not Logged In ❌
```
Action: Try to access /tickets/submit without login

Expected: Redirect to /login
```

## Troubleshooting

### Issue: 403 Forbidden Error
**Cause**: CSRF token missing or invalid  
**Solution**: 
- Clear browser cache
- Check if form has: `<input type="hidden" th:name="${_csrf.parameterName}" ...>`
- Restart application

### Issue: "User not found" Error
**Cause**: Session expired or user doesn't exist  
**Solution**: 
- Re-login
- Verify user exists in database
- Check authentication in browser dev tools

### Issue: Validation Error on Status
**Cause**: Status field validation (should be fixed)  
**Solution**: 
- Verify `Ticket.java` has `private String status = "Open";`
- Check controller sets status before validation

### Issue: 404 on Success Page
**Cause**: Missing controller endpoint  
**Solution**: 
- Verify `TicketController` has `@GetMapping("/success")` method
- Check redirect URL is `/tickets/success?id=...`

## Database Verification Queries

```sql
-- Check total tickets
SELECT COUNT(*) as total_tickets FROM ticket;

-- Check recent tickets
SELECT TOP 10 
    id, 
    title, 
    category, 
    status, 
    user_id, 
    created_at 
FROM ticket 
ORDER BY created_at DESC;

-- Check tickets by status
SELECT status, COUNT(*) as count 
FROM ticket 
GROUP BY status;

-- Check user's tickets
SELECT t.*, u.username 
FROM ticket t 
JOIN users u ON t.user_id = u.id 
WHERE u.username = 'your_username';
```

## Files Changed Summary

1. ✅ `submit-ticket.html` - Added CSRF token and Thymeleaf namespace
2. ✅ `ticket-success.html` - Added Thymeleaf namespace
3. ✅ `Ticket.java` - Default status value and validation
4. ✅ `TicketController.java` - Enhanced error handling and success endpoint
5. ✅ `SecurityConfig.java` - Updated access rules

## Success Indicators

- [ ] Form loads without errors
- [ ] Validation works (both client and server)
- [ ] Submission succeeds
- [ ] Success page displays correct ticket details
- [ ] Ticket visible in "My Tickets"
- [ ] Ticket saved in database
- [ ] No console errors
- [ ] No 403/404/500 errors

## Next Steps After Testing

If all tests pass:
1. Test with different user roles (agent, admin)
2. Test edge cases (special characters, max length)
3. Test concurrent submissions
4. Test with different browsers

If issues found:
1. Check browser console for errors
2. Check application logs
3. Verify database connection
4. Check user session/authentication
5. Review the fix documentation: `TICKET_SUBMISSION_FIX.md`

---

**Note**: Make sure SQL Server is running before testing!
