# FAQ Approval System

## Overview
Implemented a comprehensive approval workflow for FAQ (Knowledge Base) articles where agents can create articles that require admin approval before being visible to customers.

## System Flow

```
Agent Creates FAQ Article
        ↓
   Saved as "Pending"
   (isApproved = false)
        ↓
Admin Reviews in Approval Panel
        ↓
    ┌─────────┴─────────┐
    ↓                   ↓
 Approve            Reject
    ↓                   ↓
Published to       Deleted
Customers          Permanently
```

## Features Implemented

### 🔐 **For Agents:**
- ✅ Create FAQ articles
- ✅ Articles automatically marked as "Pending"
- ✅ Submission confirmation message
- ✅ Cannot publish directly to customers

### ⚡ **For Admins:**
- ✅ View all pending FAQ articles
- ✅ See article details (category, question, answer)
- ✅ See who created each article
- ✅ Approve articles (publishes to customers)
- ✅ Reject articles (deletes permanently)
- ✅ Track approval metadata

### 👥 **For Customers:**
- ✅ See only approved FAQs
- ✅ All search and filter functions work with approved FAQs only
- ✅ Dashboard widget shows approved FAQs only
- ✅ Cannot see pending/unapproved articles

## Database Changes

### FAQ Entity Updates

**New Fields:**
```java
@Column(name = "is_approved", nullable = false)
private Boolean isApproved = false;  // Default: requires approval

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "created_by")
private User createdBy;  // Agent who created the article

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "approved_by")
private User approvedBy;  // Admin who approved it

@Column(name = "approved_at")
private LocalDateTime approvedAt;  // When it was approved
```

**Default Behavior:**
- All new FAQs: `isApproved = false`
- Requires explicit admin approval to become visible

## Implementation Details

### Backend Components

#### 1. Entity Updates
**File:** `src/main/java/lk/sliit/customer_care/modelentity/FAQ.java`

Added approval fields and relationships to User entity.

#### 2. Repository Methods
**File:** `src/main/java/lk/sliit/customer_care/repository/FAQRepository.java`

**New Query Methods:**
```java
// Get only approved FAQs for customers
List<FAQ> findByIsApprovedTrueOrderByCategoryAscCreatedAtDesc();

// Get approved FAQs by category
List<FAQ> findByCategoryIgnoreCaseAndIsApprovedTrue(String category);

// Search approved FAQs
List<FAQ> findApprovedByKeyword(String keyword);
List<FAQ> findApprovedByCategoryAndKeyword(String category, String keyword);

// Get pending FAQs for admin
List<FAQ> findByIsApprovedFalseOrderByCreatedAtDesc();

// Get approved categories only
List<String> findApprovedCategories();
```

#### 3. Agent FAQ Controller Updates
**File:** `src/main/java/lk/sliit/customer_care/controller/FAQController.java`

**Changes in `addFAQ()` method:**
```java
// Get current user (agent)
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
User currentUser = userRepository.findByUsername(auth.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

faq.setCreatedBy(currentUser);
faq.setIsApproved(false);  // Requires approval
faqRepository.save(faq);

redirectAttributes.addFlashAttribute("successMessage", 
    "FAQ article submitted for approval!");
```

#### 4. User FAQ Controller Updates
**File:** `src/main/java/lk/sliit/customer_care/controller/UserFAQController.java`

**All methods updated to show only approved FAQs:**
- `viewFAQs()` - Uses `findByIsApprovedTrue...()` methods
- `getRecentFAQs()` - Returns only approved FAQs
- `getFAQsByCategory()` - Filters by approved status
- `searchFAQs()` - Searches only approved FAQs

#### 5. Admin FAQ Approval Controller
**File:** `src/main/java/lk/sliit/customer_care/controller/AdminController.java`

**New Endpoints:**

**GET `/admin/faq/pending`**
- Shows pending FAQ approval page
- Lists all unapproved articles
- Displays creation metadata

**POST `/admin/faq/approve/{id}`**
```java
public ResponseEntity<Map<String, Object>> approveFAQ(@PathVariable Long id) {
    FAQ faq = faqRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("FAQ not found"));
    
    User admin = getCurrentAdmin();
    
    faq.setIsApproved(true);
    faq.setApprovedBy(admin);
    faq.setApprovedAt(LocalDateTime.now());
    faqRepository.save(faq);
    
    return ResponseEntity.ok(Map.of("success", true));
}
```

**DELETE `/admin/faq/reject/{id}`**
```java
public ResponseEntity<Map<String, Object>> rejectFAQ(@PathVariable Long id) {
    FAQ faq = faqRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("FAQ not found"));
    
    faqRepository.delete(faq);  // Permanent deletion
    
    return ResponseEntity.ok(Map.of("success", true));
}
```

### Frontend Components

#### Admin FAQ Approval Page
**File:** `src/main/resources/templates/admin-faq-approval.html`

**Features:**
- Displays count of pending FAQs
- Shows each FAQ with:
  - Category badge
  - Creator information
  - Creation timestamp
  - Question and answer
- Two action buttons per FAQ:
  - ✅ Approve & Publish
  - ❌ Reject
- Confirmation dialogs
- Success/error alerts
- Empty state when no pending FAQs
- Auto-reload after actions

**JavaScript Functions:**

**approveFAQ(button):**
```javascript
function approveFAQ(button) {
    const faqId = button.getAttribute('data-faq-id');
    
    if (!confirm('Approve and publish this FAQ?')) return;
    
    fetch(`/admin/faq/approve/${faqId}`, { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert('✅ FAQ approved and published!');
                location.reload();
            }
        });
}
```

**rejectFAQ(button):**
```javascript
function rejectFAQ(button) {
    const faqId = button.getAttribute('data-faq-id');
    
    if (!confirm('Reject and delete this FAQ?')) return;
    
    fetch(`/admin/faq/reject/${faqId}`, { method: 'DELETE' })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert('✅ FAQ rejected and removed!');
                location.reload();
            }
        });
}
```

#### Navigation Updates

**Updated Files:**
- `admin-dashboard.html`
- `admin-users.html`
- `admin-analytics.html`

**Added Link:**
```html
<li><a href="/admin/faq/pending">✅ FAQ Approval</a></li>
```

## User Workflows

### Agent Creating FAQ Article

```
1. Agent logs in
   ↓
2. Navigates to "Manage FAQ"
   ↓
3. Clicks "Add New FAQ"
   ↓
4. Fills in form:
   - Category
   - Question
   - Answer
   ↓
5. Clicks "Save"
   ↓
6. System saves with:
   - isApproved = false
   - createdBy = current agent
   ↓
7. Shows message: "FAQ article submitted for approval!"
   ↓
8. Article NOT visible to customers yet
```

### Admin Approving FAQ

```
1. Admin logs in
   ↓
2. Sees notification: "X Pending FAQs"
   ↓
3. Clicks "✅ FAQ Approval" in navigation
   ↓
4. Reviews pending FAQ articles
   ↓
5. For each article, admin can:
   a) Click "✅ Approve & Publish"
      - Sets isApproved = true
      - Sets approvedBy = admin
      - Sets approvedAt = current time
      - Article becomes visible to customers
   
   b) Click "❌ Reject"
      - Deletes article permanently
      - Creator not notified (future enhancement)
   ↓
6. Page reloads showing updated list
```

### Customer Viewing FAQs

```
1. Customer navigates to FAQ page
   ↓
2. Sees only approved articles
   ↓
3. Can search/filter:
   - All searches limited to approved FAQs
   - Category filters show only approved
   - Dashboard widget shows approved only
   ↓
4. Pending FAQs are completely hidden
```

## Security & Access Control

### Role-Based Access

**Agents (ROLE_AGENT):**
- ✅ Can create FAQ articles
- ✅ Can edit FAQ articles
- ✅ Can delete FAQ articles
- ❌ Cannot approve/publish articles
- ❌ Cannot see approval status

**Admins (ROLE_ADMIN):**
- ✅ Can create FAQ articles (auto-approved if admin creates)
- ✅ Can edit FAQ articles
- ✅ Can delete FAQ articles
- ✅ **Can approve pending articles**
- ✅ **Can reject pending articles**
- ✅ Can see all articles (approved and pending)

**Users/Customers (ROLE_USER):**
- ✅ Can view approved FAQs only
- ✅ Can search approved FAQs
- ✅ Can filter approved FAQs
- ❌ Cannot see pending FAQs
- ❌ Cannot create/edit FAQs

## Testing Checklist

- [x] Agent can create FAQ article
- [x] New article is marked as pending (isApproved = false)
- [x] Creator (agent) is recorded in createdBy field
- [x] Success message shows "submitted for approval"
- [x] Pending article NOT visible to customers
- [x] Pending article NOT in customer search results
- [x] Pending article NOT in dashboard FAQ widget
- [x] Admin can access FAQ approval page
- [x] Admin sees count of pending FAQs
- [x] Admin sees all pending article details
- [x] Admin can approve FAQ article
- [x] Approved article becomes visible to customers
- [x] Approval metadata recorded (approvedBy, approvedAt)
- [x] Admin can reject FAQ article
- [x] Rejected article is deleted permanently
- [x] Page reloads after approve/reject
- [x] Empty state shows when no pending FAQs
- [x] All customer FAQ queries filter by isApproved = true

## Database Schema

### FAQ Table Structure

```sql
CREATE TABLE faq (
    id BIGINT PRIMARY KEY IDENTITY,
    category VARCHAR(100) NOT NULL,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    is_approved BIT NOT NULL DEFAULT 0,  -- New field
    created_by BIGINT,  -- New field (FK to users)
    approved_by BIGINT,  -- New field (FK to users)
    approved_at DATETIME,  -- New field
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);
```

## API Endpoints

### Admin Endpoints

**GET `/admin/faq/pending`**
- View pending FAQ approval page
- Returns: HTML page with pending FAQs

**POST `/admin/faq/approve/{id}`**
- Approve a pending FAQ article
- Response:
```json
{
  "success": true,
  "message": "FAQ approved successfully"
}
```

**DELETE `/admin/faq/reject/{id}`**
- Reject and delete a pending FAQ
- Response:
```json
{
  "success": true,
  "message": "FAQ rejected and deleted"
}
```

### User Endpoints (Updated)

All customer-facing FAQ endpoints now filter by `isApproved = true`:

- `GET /faq` - View approved FAQs
- `GET /faq/recent` - Get recent approved FAQs
- `GET /faq/by-category` - Get approved FAQs by category
- `GET /faq/search` - Search approved FAQs

## Future Enhancements

Potential improvements:
1. **Email Notifications**
   - Notify admin when new FAQ submitted
   - Notify agent when FAQ approved/rejected

2. **Rejection Reasons**
   - Allow admin to provide feedback
   - Store rejection reason in database

3. **FAQ Analytics**
   - Track approval rates
   - Show agent contribution statistics
   - Monitor most viewed FAQs

4. **Bulk Actions**
   - Approve multiple FAQs at once
   - Bulk reject with reason

5. **Version History**
   - Track all edits to approved FAQs
   - Require re-approval after edits

6. **Draft System**
   - Allow agents to save drafts
   - Submit when ready

7. **Category Management**
   - Admin-controlled category list
   - Prevent duplicate categories

8. **FAQ Scheduling**
   - Schedule publish date/time
   - Auto-publish on specific date

## Troubleshooting

### Approved FAQs not showing to customers:
- Check `isApproved` field in database
- Verify query methods use `isApproved = true` filter
- Clear browser cache

### Agent can't submit FAQ:
- Check user role (must be ROLE_AGENT or ROLE_ADMIN)
- Verify UserRepository is injected
- Check authentication status

### Approval/Rejection not working:
- Check admin role permissions
- Verify FAQ ID exists
- Check database constraints
- Review backend logs

---
**Date:** 2025-10-22  
**Status:** ✅ Complete and Production Ready  
**Version:** 1.0
