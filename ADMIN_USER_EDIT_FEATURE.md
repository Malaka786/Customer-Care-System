# Admin User Edit Feature

## Overview
Enhanced the admin user management page with the ability to edit user/agent details including phone number, address, and password.

## Features Added

### ✏️ Edit User Functionality

**What Can Be Edited:**
- ✅ Phone Number
- ✅ Address  
- ✅ Password (optional - only if provided)

**What Cannot Be Edited:**
- ❌ Username (read-only in the modal)
- ❌ User Role (preserved automatically)
- ❌ User ID (system-managed)

## Implementation Details

### Backend Endpoints

**File:** `src/main/java/lk/sliit/customer_care/controller/AdminController.java`

#### 1. Get User by ID
```java
@GetMapping("/user/{id}")
@ResponseBody
public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id)
```
- Fetches user details for editing
- Returns user data: id, username, phoneNumber, address, role
- Used to populate the edit modal

#### 2. Update User
```java
@PutMapping("/user/{id}")
@ResponseBody
public ResponseEntity<Map<String, Object>> updateUser(
    @PathVariable Long id,
    @RequestBody Map<String, Object> requestBody)
```
- Updates user details
- Fields updated:
  - `phoneNumber` (required)
  - `address` (required)
  - `password` (optional - only if provided)
- Password is encrypted using `PasswordEncoder` before saving
- Returns success/error response

**Request Body Example:**
```json
{
  "phoneNumber": "0771234567",
  "address": "New Address",
  "password": "newpassword123"  // optional
}
```

### Frontend Components

**File:** `src/main/resources/templates/admin-users.html`

#### 1. Edit Buttons
Added to all three user tables (Users, Agents, Admins):
```html
<button class="edit-btn" th:attr="data-user-id=${user.id}" onclick="editUser(this)">
    ✏️ Edit
</button>
```

#### 2. Edit Modal
Beautiful modal dialog with form fields:
- **Username:** Disabled (read-only display)
- **Phone Number:** Text input with pattern validation (10 digits)
- **Address:** Text input
- **New Password:** Password input (optional, minimum 6 characters)

**Modal Features:**
- Smooth slide-in animation
- Backdrop blur effect
- Click outside to close
- Close button (×)
- Cancel button
- Save button with gradient styling

#### 3. CSS Styling
```css
.edit-btn {
    background: #4299e1;
    color: white;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    transition: all 0.3s ease;
}

.modal {
    position: fixed;
    background: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(5px);
    z-index: 2000;
}

.modal-content {
    background: white;
    border-radius: 24px;
    padding: 2.5rem;
    animation: modalSlideIn 0.3s ease;
}
```

#### 4. JavaScript Functions

**editUser(button):**
- Fetches user details via GET `/admin/user/{id}`
- Populates the modal form fields
- Shows the modal with `.active` class

**closeEditModal():**
- Hides the modal
- Resets the form

**Form Submit Handler:**
- Prevents default form submission
- Collects form data
- Only includes password if it's not empty
- Sends PUT request to `/admin/user/{id}`
- Shows success/error alerts
- Reloads page on success

**Click Outside Handler:**
- Closes modal when clicking on the backdrop

## User Flow

### Edit User Workflow:

```
1. Admin clicks "✏️ Edit" button
   ↓
2. System fetches user details (GET /admin/user/{id})
   ↓
3. Modal opens with pre-filled form
   ↓
4. Admin edits phone number, address, or password
   ↓
5. Admin clicks "✔️ Save Changes"
   ↓
6. System validates and updates user (PUT /admin/user/{id})
   ↓
7. Success message shown
   ↓
8. Page reloads with updated data
```

## Security Features

1. **Password Encryption:**
   - New passwords are encrypted using `PasswordEncoder`
   - Stored securely in the database

2. **Username Protection:**
   - Username field is disabled in the modal
   - Cannot be changed to prevent identity issues

3. **Role Preservation:**
   - User role is not included in the update request
   - Automatically preserved in the database

4. **Validation:**
   - Phone number pattern: exactly 10 digits
   - Password minimum length: 6 characters
   - Required fields enforced

## UI/UX Enhancements

### Visual Design:
- ✨ Blue edit buttons (distinct from red delete buttons)
- 🎨 Gradient save button
- 🌊 Smooth modal animations
- 💫 Backdrop blur effect
- 🎯 Hover effects on buttons

### User Experience:
- ⚡ Fast modal loading
- 📝 Clear form labels
- ✅ Inline validation
- 🚀 Auto-reload after save
- 💬 Clear success/error messages

## Testing Checklist

- [x] Edit button displays for all users
- [x] Edit button displays for all agents
- [x] Edit button displays for admins
- [x] Modal opens when clicking edit
- [x] User details load correctly in modal
- [x] Username is read-only
- [x] Phone number validation works
- [x] Address can be updated
- [x] Password is optional
- [x] Password gets encrypted
- [x] Empty password doesn't update password
- [x] Save button submits form
- [x] Cancel button closes modal
- [x] Click outside closes modal
- [x] Success message shows
- [x] Page reloads after save
- [x] Updated data displays correctly
- [x] Error handling works

## Code Examples

### Editing a User (Frontend):
```javascript
function editUser(button) {
    const userId = button.getAttribute('data-user-id');
    
    fetch(`/admin/user/${userId}`)
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                // Populate form
                document.getElementById('editUserId').value = data.user.id;
                document.getElementById('editUsername').value = data.user.username;
                document.getElementById('editPhoneNumber').value = data.user.phoneNumber;
                document.getElementById('editAddress').value = data.user.address;
                
                // Show modal
                document.getElementById('editModal').classList.add('active');
            }
        });
}
```

### Saving Changes (Frontend):
```javascript
const updateData = {
    phoneNumber: phoneNumber,
    address: address
};

if (password && password.trim() !== '') {
    updateData.password = password;
}

fetch(`/admin/user/${userId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updateData)
})
.then(res => res.json())
.then(data => {
    if (data.success) {
        alert('✅ User updated successfully!');
        location.reload();
    }
});
```

### Backend Update (Java):
```java
if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
    user.setPhoneNumber(phoneNumber);
}

if (address != null && !address.trim().isEmpty()) {
    user.setAddress(address);
}

// Only update password if provided
if (password != null && !password.trim().isEmpty()) {
    user.setPassword(passwordEncoder.encode(password));
}

userRepository.save(user);
```

## Future Enhancements

Potential improvements:
1. **Bulk edit** - Edit multiple users at once
2. **Profile picture** - Upload and manage user avatars
3. **Email field** - Add email address support
4. **Role change** - Allow changing user roles
5. **Activity log** - Track who edited what and when
6. **Undo changes** - Restore previous values
7. **Real-time validation** - Check username/phone availability
8. **Two-factor auth** - Reset 2FA for users

## Troubleshooting

### Modal doesn't open:
- Check browser console for JavaScript errors
- Verify GET endpoint returns user data
- Ensure modal HTML exists in the page

### Changes don't save:
- Check network tab for PUT request
- Verify request body format
- Check backend logs for errors
- Ensure user ID is valid

### Password not updating:
- Verify password is not empty string
- Check password encoding in backend
- Confirm passwordEncoder bean is available

---
**Date:** 2025-10-22  
**Status:** ✅ Complete and Production Ready
**Version:** 1.0
