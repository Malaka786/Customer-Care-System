# Admin User Management & Analytics Feature

## Overview
Implemented comprehensive admin functionality for managing users/agents and viewing system analytics for tickets and live chat sessions.

## Features Implemented

### 1. User Management (`/admin/users`)
**Capabilities:**
- View all users categorized by role (Users, Agents, Admins)
- Display user statistics (total counts for each role)
- Delete users and agents
- Protection against deleting admin accounts
- Real-time UI updates after deletion

**Backend Endpoint:**
- `GET /admin/users` - Display user management page
- `DELETE /admin/user/{id}` - Delete a user/agent (admin accounts protected)

**Security:**
- Admin accounts cannot be deleted
- Deletion requires confirmation
- Returns appropriate error messages

### 2. Analytics Dashboard (`/admin/analytics`)
**Metrics Displayed:**

#### User Statistics
- Total Users (ROLE_USER)
- Total Agents (ROLE_AGENT)
- Total Administrators (ROLE_ADMIN)

#### Ticket Analytics
- **Status Breakdown:**
  - Total Tickets
  - Open Tickets
  - In Progress Tickets
  - Resolved Tickets
  - Closed Tickets

- **Category Distribution:**
  - Dynamic list of all ticket categories
  - Count per category

#### Live Chat Analytics
- **Session Status:**
  - Total Chat Sessions
  - Active Sessions
  - Waiting for Agent Sessions
  - Closed Sessions

#### System Overview
- Total System Users (all roles combined)
- All Tickets
- All Chat Sessions

## Files Modified

### Backend
**File:** `src/main/java/lk/sliit/customer_care/controller/AdminController.java`

Added methods:
```java
// User management with role filtering
@GetMapping("/users")
public String getAllUsers(Model model)

// Delete user/agent (protected for admins)
@DeleteMapping("/user/{id}")
@ResponseBody
public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id)

// Analytics dashboard with all statistics
@GetMapping("/analytics")
public String getAnalytics(Model model)
```

**Dependencies Added:**
- `ChatSessionRepository` - For chat statistics
- `TicketRepository` - For ticket statistics
- Java Streams for data aggregation
- Collectors for grouping by category

### Frontend Templates

#### 1. `admin-users.html`
**Features:**
- Three separate tables for Users, Agents, and Admins
- Color-coded role badges
- Delete buttons with confirmation
- Disabled delete for admin accounts
- Statistics cards showing counts
- Responsive design

**JavaScript:**
- `deleteUser()` function with AJAX DELETE request
- Confirmation dialog before deletion
- Success/error alerts
- Auto page reload after successful deletion

#### 2. `admin-analytics.html`
**Sections:**
- User Statistics (3-column grid)
- Ticket Analytics
  - Status summary (5-card grid)
  - Category breakdown (dynamic list)
- Live Chat Analytics (4-card grid)
- System Overview (3-card summary)

**Styling:**
- Gradient stat cards with hover effects
- Color-coded statistics (success, warning, info, danger)
- Responsive grid layouts
- Modern card-based design

#### 3. `admin-dashboard.html` (Updated)
**Change:**
- Added "📊 Analytics" link to navigation menu

## Navigation Structure

Admin menu now includes:
```
🏠 Home
⚡ Dashboard (Create Agents)
👥 Manage Users (View/Delete Users & Agents)
📊 Analytics (View Statistics)
💬 Feedback (Manage Feedback)
🚪 Logout
```

## Usage

### Access User Management
1. Login as admin
2. Click "👥 Manage Users" in the navbar
3. View users organized by role
4. Click "🗑️ Delete" to remove a user/agent
5. Confirm deletion in the dialog

### Access Analytics Dashboard
1. Login as admin
2. Click "📊 Analytics" in the navbar
3. View comprehensive statistics:
   - User distribution
   - Ticket status and categories
   - Chat session metrics
   - System overview

## Security Considerations

1. **Admin Protection:**
   - Admin accounts cannot be deleted via UI
   - Backend validates role before deletion
   - Returns error if attempting to delete admin

2. **Authorization:**
   - All endpoints under `/admin/*` require ROLE_ADMIN
   - Spring Security handles access control

3. **Data Integrity:**
   - User deletion cascades to related tickets (via JPA)
   - Confirmation dialog prevents accidental deletions

## Technical Details

### Data Aggregation
```java
// User counts by role
long totalUsers = allUsers.stream()
    .filter(u -> "ROLE_USER".equals(u.getRole()))
    .count();

// Ticket status counts
long openTickets = allTickets.stream()
    .filter(t -> "OPEN".equals(t.getStatus()))
    .count();

// Tickets grouped by category
Map<String, Long> ticketsByCategory = allTickets.stream()
    .collect(Collectors.groupingBy(
        Ticket::getCategory, 
        Collectors.counting()
    ));

// Chat session status counts
long activeChatSessions = allSessions.stream()
    .filter(s -> s.getStatus() == ChatSession.ChatStatus.ACTIVE)
    .count();
```

### Frontend AJAX Delete
```javascript
fetch(`/admin/user/${userId}`, {
    method: 'DELETE'
})
.then(res => res.json())
.then(data => {
    if (data.success) {
        alert('✅ User deleted successfully!');
        location.reload();
    } else {
        alert('❌ Error: ' + data.message);
    }
})
```

## Future Enhancements

Potential improvements:
1. **Export functionality** - Export analytics to PDF/CSV
2. **Date range filters** - View analytics for specific periods
3. **Chart visualizations** - Add graphs using Chart.js
4. **User activity logs** - Track user actions
5. **Bulk operations** - Delete multiple users at once
6. **Advanced filtering** - Search and filter user lists
7. **Email notifications** - Alert admins on critical metrics

## Testing Checklist

- [x] Admin can view all users separated by role
- [x] Admin can delete regular users
- [x] Admin can delete agents
- [x] Admin cannot delete other admins
- [x] Delete requires confirmation
- [x] Page reloads after successful deletion
- [x] Analytics shows correct user counts
- [x] Analytics displays ticket statistics
- [x] Analytics shows chat session metrics
- [x] All tickets grouped by category correctly
- [x] Responsive design works on mobile
- [x] Navigation links work correctly

---
**Date:** 2025-10-22  
**Status:** ✅ Complete and Ready for Production
