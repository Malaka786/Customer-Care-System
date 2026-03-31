# User & Agent Profile Feature

## Overview
Implemented a comprehensive profile management system allowing both customers (users) and agents to view and edit their personal information, including the ability to change their password securely.

## Features Implemented

### 🎯 Profile Management
Users and agents can:
- ✅ View their profile information
- ✅ Edit phone number
- ✅ Edit address
- ✅ Change password (with current password verification)
- ✅ See their username and role (read-only)

### 🔐 Security Features
- **Current Password Verification:** Required to change password
- **Password Encryption:** All passwords encrypted using PasswordEncoder
- **Password Confirmation:** Client-side validation for password match
- **Session-based Authentication:** Uses Spring Security context

## Implementation Details

### Backend

**File:** `src/main/java/lk/sliit/customer_care/controller/ProfileController.java`

#### Endpoints:

**1. GET `/profile`**
- Shows the profile page
- Loads current user data from session
- Redirects to login if not authenticated

**2. GET `/profile/data`** (AJAX)
- Returns current user data as JSON
- Used for dynamic loading if needed

**3. PUT `/profile/update`** (AJAX)
- Updates user profile information
- Validates current password if changing password
- Encrypts new password before saving

**Request Body:**
```json
{
  "phoneNumber": "0771234567",
  "address": "New Address",
  "currentPassword": "oldpass",  // Required only if changing password
  "newPassword": "newpass123"     // Optional
}
```

**Response:**
```json
{
  "success": true,
  "message": "Profile updated successfully"
}
```

### Frontend

**File:** `src/main/resources/templates/profile.html`

#### Features:

**1. Profile Display**
- Large profile icon with gradient background
- Username display
- Role badge (User/Agent with color coding)
- Organized sections:
  - Account Information (read-only)
  - Personal Information (editable)
  - Change Password (optional)

**2. Form Validation**
- Phone number pattern: exactly 10 digits
- Password minimum length: 6 characters
- Password confirmation matching
- Required field validation

**3. User Experience**
- Success/error alerts with icons
- Auto-scroll to alerts
- Auto-hide success messages after 5 seconds
- Clear password fields after update
- Auto-reload after successful update
- Smooth animations

**4. JavaScript Functions**

**showAlert(message, isSuccess):**
- Displays success or error messages
- Scrolls to top to show alert
- Auto-hides success messages

**Password Validation:**
- Real-time password match validation
- Custom validity messages

**Form Submission:**
- Validates all fields
- Checks password match
- Verifies current password if changing
- Sends PUT request
- Handles response and updates UI

### Navigation Updates

**User Dashboard** (`user-dashboard.html`)
- Added "👤 Profile" link before Logout

**Agent Dashboard** (`agent-dashboard.html`)
- Added "👤 Profile" link before Logout

Both dashboards now have easy access to the profile page.

## User Flow

### View Profile:
```
1. User/Agent logged in
   ↓
2. Clicks "👤 Profile" in navigation
   ↓
3. Profile page loads with current information
   ↓
4. User views username, role, phone, address
```

### Edit Profile (Without Password Change):
```
1. User opens profile page
   ↓
2. Edits phone number and/or address
   ↓
3. Leaves password fields empty
   ↓
4. Clicks "💾 Save Changes"
   ↓
5. System updates phone/address
   ↓
6. Success message shown
   ↓
7. Page reloads with updated data
```

### Change Password:
```
1. User opens profile page
   ↓
2. Optionally edits phone/address
   ↓
3. Enters current password
   ↓
4. Enters new password (min 6 characters)
   ↓
5. Confirms new password
   ↓
6. Clicks "💾 Save Changes"
   ↓
7. System verifies current password
   ↓
8. If correct, encrypts and saves new password
   ↓
9. Success message shown
   ↓
10. Password fields cleared
   ↓
11. Page reloads
```

## Visual Design

### Profile Page Layout:
```
┌─────────────────────────────────────┐
│           Header & Navigation        │
├─────────────────────────────────────┤
│          👤 My Profile              │
├─────────────────────────────────────┤
│                                      │
│        ┌─────────────┐              │
│        │   👤 Icon   │              │
│        │  (Gradient) │              │
│        └─────────────┘              │
│         john_doe                     │
│         [User Badge]                 │
│                                      │
│  📋 Account Information              │
│  ├─ Username: john_doe (disabled)   │
│  └─ Role: User (disabled)            │
│                                      │
│  ─────────────────────────          │
│                                      │
│  📝 Personal Information             │
│  ├─ Phone: [0771234567]             │
│  └─ Address: [Colombo]               │
│                                      │
│  ─────────────────────────          │
│                                      │
│  🔐 Change Password                  │
│  ├─ Current: [•••••]                │
│  ├─ New: [•••••]                    │
│  └─ Confirm: [•••••]                │
│                                      │
│      [💾 Save Changes]               │
│                                      │
└─────────────────────────────────────┘
```

### Color Scheme:
- **User Badge:** Blue (#bee3f8 background, #2c5282 text)
- **Agent Badge:** Green (#c6f6d5 background, #22543d text)
- **Primary Gradient:** #667eea → #764ba2
- **Success Alert:** Green background with dark green text
- **Error Alert:** Red background with dark red text

## Security Implementation

### Password Change Security:

**1. Current Password Verification:**
```java
if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
    return ResponseEntity.badRequest()
        .body(Map.of("success", false, 
                     "message", "Current password is incorrect"));
}
```

**2. Password Encryption:**
```java
user.setPassword(passwordEncoder.encode(newPassword));
```

**3. Frontend Validation:**
```javascript
if (newPassword || confirmPassword) {
    if (!currentPassword) {
        showAlert('Please enter your current password to change password', false);
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showAlert('New passwords do not match', false);
        return;
    }
}
```

## Error Handling

### Backend Errors:
- User not found
- User not authenticated
- Current password incorrect
- Database save errors

### Frontend Validation:
- Empty required fields
- Phone number pattern mismatch
- Password length < 6 characters
- Password mismatch
- Current password not provided when changing password

### Error Messages:
- ❌ "Current password is required to change password"
- ❌ "Current password is incorrect"
- ❌ "New passwords do not match"
- ❌ "Please enter your current password to change password"
- ✅ "Profile updated successfully!"

## Testing Checklist

- [x] User can access profile page from dashboard
- [x] Agent can access profile page from dashboard
- [x] Profile displays current user information
- [x] Username field is disabled
- [x] Role field is disabled
- [x] Phone number can be edited
- [x] Address can be edited
- [x] Phone validation works (10 digits)
- [x] Password change requires current password
- [x] Incorrect current password shows error
- [x] New password must match confirmation
- [x] Password minimum 6 characters enforced
- [x] Success message shows after update
- [x] Error messages display correctly
- [x] Page reloads after successful update
- [x] Password fields clear after update
- [x] Updated data persists in database
- [x] Password gets encrypted
- [x] Navigation links work correctly

## Code Examples

### Update Profile (Frontend):
```javascript
const updateData = {
    phoneNumber: phoneNumber,
    address: address
};

if (newPassword && currentPassword) {
    updateData.currentPassword = currentPassword;
    updateData.newPassword = newPassword;
}

fetch('/profile/update', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updateData)
})
.then(res => res.json())
.then(data => {
    if (data.success) {
        showAlert('Profile updated successfully!', true);
        setTimeout(() => location.reload(), 2000);
    }
});
```

### Password Verification (Backend):
```java
if (newPassword != null && !newPassword.trim().isEmpty()) {
    if (currentPassword == null || currentPassword.trim().isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, 
                         "message", "Current password is required"));
    }
    
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
        return ResponseEntity.badRequest()
            .body(Map.of("success", false, 
                         "message", "Current password is incorrect"));
    }
    
    user.setPassword(passwordEncoder.encode(newPassword));
}
```

## Files Created/Modified

### Created:
1. `src/main/java/lk/sliit/customer_care/controller/ProfileController.java` - Backend controller
2. `src/main/resources/templates/profile.html` - Profile page template
3. `PROFILE_FEATURE.md` - This documentation

### Modified:
1. `src/main/resources/templates/user-dashboard.html` - Added profile link
2. `src/main/resources/templates/agent-dashboard.html` - Added profile link

## Access URLs

- **Profile Page:** `http://localhost:8080/profile`
- **Update Profile:** `PUT http://localhost:8080/profile/update`
- **Get Profile Data:** `GET http://localhost:8080/profile/data`

## Future Enhancements

Potential improvements:
1. **Profile Picture Upload** - Allow users to upload avatar
2. **Email Field** - Add email address management
3. **Two-Factor Authentication** - Enable 2FA for accounts
4. **Activity Log** - Show recent account activity
5. **Password Strength Meter** - Visual password strength indicator
6. **Email Verification** - Verify email addresses
7. **Account Deletion** - Allow users to delete their own account
8. **Export Data** - GDPR compliance - export user data
9. **Session Management** - View and revoke active sessions
10. **Notification Preferences** - Manage email/SMS notifications

## Troubleshooting

### Profile page doesn't load:
- Check if user is logged in
- Verify ProfileController is registered
- Check Spring Security configuration

### Updates don't save:
- Check network tab for PUT request
- Verify request body format
- Check backend logs for errors
- Ensure passwordEncoder bean exists

### Password change fails:
- Verify current password is correct
- Check password meets minimum length (6)
- Ensure passwords match
- Check password encoding in database

---
**Date:** 2025-10-22  
**Status:** ✅ Complete and Production Ready  
**Version:** 1.0  
**Author:** AI Assistant
