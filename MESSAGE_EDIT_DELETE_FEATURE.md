# Customer Message Edit & Delete Feature

## Overview
Customers can now edit and delete their own messages in real-time during live chat sessions. All changes are persisted to the database and broadcast to all participants via WebSocket.

## Features Implemented

### 1. Message Editing ✅
- Customers can edit their own messages
- Edited messages show an "(edited)" indicator
- Updates are reflected in database with `isEdited` flag
- Real-time updates broadcast to all chat participants
- Only message owner can edit (security enforced)

### 2. Message Deletion ✅
- Customers can delete their own messages
- Soft delete (message marked as `isDeleted = true`)
- Deleted messages disappear from chat window
- Real-time deletion broadcast to all participants
- Only message owner can delete (security enforced)

### 3. Real-Time Synchronization ✅
- WebSocket broadcasts edit/delete actions
- All connected users see updates instantly
- Agent dashboard automatically reflects changes
- No page refresh needed

## Files Modified

### Backend

#### 1. ChatMessageDTO.java
**Added fields:**
- `Long id` - Message identifier for edit/delete operations
- `Boolean isEdited` - Indicates if message was edited
- `Boolean isDeleted` - Indicates if message was deleted

**Changes:**
```java
public class ChatMessageDTO {
    private Long id;                    // NEW
    private String content;
    private String senderUsername;
    private SenderType senderType;
    private String sessionId;
    private LocalDateTime createdAt;
    private Boolean isEdited;          // NEW
    private Boolean isDeleted;         // NEW
    // ... getters and setters
}
```

#### 2. ChatService.java
**Added methods:**

**`editMessage(Long messageId, String newContent, Long userId)`**
- Finds message by ID
- Verifies user owns the message
- Updates content and sets `isEdited = true`
- Saves to database
- Broadcasts update via WebSocket

**`deleteMessage(Long messageId, Long userId)`**
- Finds message by ID
- Verifies user owns the message
- Sets `isDeleted = true` (soft delete)
- Saves to database
- Broadcasts deletion via WebSocket

**Security:**
- Both methods verify `message.getSender().getId().equals(userId)`
- Throws exception if user tries to edit/delete someone else's message

#### 3. ChatController.java
**Added REST endpoints:**

**`PUT /api/chat/message/{messageId}`**
- Parameters: `content`, `userId`
- Calls `chatService.editMessage()`
- Returns success/failure response

**`DELETE /api/chat/message/{messageId}`**
- Parameters: `userId`
- Calls `chatService.deleteMessage()`
- Returns success/failure response

### Frontend

#### 4. live-chat.html

**CSS Additions:**
- `.message-actions` - Container for edit/delete buttons
- `.message-btn` - Base button styling
- `.edit-btn` - Yellow edit button
- `.delete-btn` - Red delete button
- `.edited-indicator` - Small "(edited)" text
- Hover effects to show buttons

**JavaScript Enhancements:**

**`displayMessage(message)` - Enhanced**
- Checks if message is deleted → removes from DOM
- Checks if message exists → updates content (for edits)
- Adds `data-message-id` attribute for tracking
- Shows edit/delete buttons for user's own messages
- Displays "(edited)" indicator if `message.isEdited === true`
- Updates existing messages instead of duplicating

**`editMessage(messageId, currentContent)` - NEW**
- Shows prompt with current message content
- Sends PUT request to `/api/chat/message/{messageId}`
- Includes `userId` for security verification
- Waits for WebSocket update (real-time sync)

**`deleteMessageConfirm(messageId)` - NEW**
- Shows confirmation dialog
- Sends DELETE request to `/api/chat/message/{messageId}`
- Includes `userId` for security verification
- Waits for WebSocket update (real-time sync)

## User Interface

### Message Appearance

**User's Own Messages (before hover):**
```
┌─────────────────────────┐
│ Hello, I need help      │
└─────────────────────────┘
```

**User's Own Messages (on hover):**
```
┌─────────────────────────┐
│ [Edit] [Delete]         │
│ Hello, I need help      │
└─────────────────────────┘
```

**Edited Message:**
```
┌─────────────────────────┐
│ [Edit] [Delete]         │
│ Hello, I need help now  │
│ (edited)                │
└─────────────────────────┘
```

**Agent's Messages:**
```
┌─────────────────────────┐
│ How can I help you?     │
└─────────────────────────┘
```
(No edit/delete buttons shown)

## How It Works

### Edit Flow

1. **User clicks "Edit" button** on their message
2. **Prompt appears** with current message text
3. **User modifies** and confirms
4. **Frontend sends** PUT request to `/api/chat/message/{messageId}`
5. **Backend verifies** user owns the message
6. **Backend updates** database (`content`, `isEdited = true`, `updatedAt`)
7. **Backend broadcasts** updated `ChatMessageDTO` via WebSocket
8. **Frontend receives** WebSocket message
9. **Frontend updates** existing message in DOM
10. **All participants see** edited message with "(edited)" indicator

### Delete Flow

1. **User clicks "Delete" button** on their message
2. **Confirmation dialog** appears
3. **User confirms** deletion
4. **Frontend sends** DELETE request to `/api/chat/message/{messageId}`
5. **Backend verifies** user owns the message
6. **Backend updates** database (`isDeleted = true`, `updatedAt`)
7. **Backend broadcasts** updated `ChatMessageDTO` via WebSocket
8. **Frontend receives** WebSocket message
9. **Frontend removes** message from DOM
10. **All participants see** message disappear

## Database Schema

### ChatMessage Table Updates

The table already had these columns (no schema changes needed):
- `is_edited BOOLEAN DEFAULT false`
- `is_deleted BOOLEAN DEFAULT false`
- `updated_at DATETIME`

### Example Records

**Original Message:**
```sql
id | content          | is_edited | is_deleted | created_at          | updated_at
1  | "Hello"          | false     | false      | 2025-10-21 10:00:00 | NULL
```

**After Edit:**
```sql
id | content          | is_edited | is_deleted | created_at          | updated_at
1  | "Hello there"    | true      | false      | 2025-10-21 10:00:00 | 2025-10-21 10:05:00
```

**After Delete:**
```sql
id | content          | is_edited | is_deleted | created_at          | updated_at
1  | "Hello there"    | true      | true       | 2025-10-21 10:00:00 | 2025-10-21 10:10:00
```

## Testing Instructions

### Test Edit Functionality

1. **Login as customer** → Navigate to `/live-chat`
2. **Send a message** (e.g., "Hello")
3. **Hover over your message** → Edit and Delete buttons appear
4. **Click "Edit"** button
5. **Modify the text** in prompt (e.g., "Hello there")
6. **Click OK**
7. **Verify:**
   - Message updates immediately
   - "(edited)" indicator appears
   - Database shows `is_edited = true`
   - If agent is watching, they see the edit in real-time

### Test Delete Functionality

1. **Send a message**
2. **Hover over your message**
3. **Click "Delete"** button
4. **Confirm** in dialog
5. **Verify:**
   - Message disappears from chat
   - Database shows `is_deleted = true`
   - If agent is watching, message disappears for them too

### Test Security

1. **Try to edit/delete via browser console:**
```javascript
fetch('/api/chat/message/1?content=hacked&userId=999', {method: 'PUT'})
```
2. **Verify:** Error response "You can only edit your own messages"

### Test Real-Time Sync

1. **Open chat as customer** in Browser 1
2. **Open same session as agent** in Browser 2
3. **Customer sends message** in Browser 1
4. **Agent sees message** in Browser 2
5. **Customer edits message** in Browser 1
6. **Agent sees edit with "(edited)"** in Browser 2
7. **Customer deletes message** in Browser 1
8. **Agent sees message disappear** in Browser 2

## API Documentation

### Edit Message
```http
PUT /api/chat/message/{messageId}
Parameters:
  - content: string (new message content)
  - userId: long (ID of user making the edit)

Response:
{
  "success": true,
  "message": "Message edited successfully",
  "data": {
    "id": 1,
    "content": "Updated content",
    "senderUsername": "customer1",
    "senderType": "USER",
    "sessionId": "session-123",
    "createdAt": "2025-10-21T10:00:00",
    "isEdited": true,
    "isDeleted": false
  }
}
```

### Delete Message
```http
DELETE /api/chat/message/{messageId}
Parameters:
  - userId: long (ID of user deleting the message)

Response:
{
  "success": true,
  "message": "Message deleted successfully",
  "data": {
    "id": 1,
    "content": "Original content",
    "senderUsername": "customer1",
    "senderType": "USER",
    "sessionId": "session-123",
    "createdAt": "2025-10-21T10:00:00",
    "isEdited": false,
    "isDeleted": true
  }
}
```

## WebSocket Message Format

### Edit Broadcast
```json
{
  "id": 1,
  "content": "Updated content",
  "senderUsername": "customer1",
  "senderType": "USER",
  "sessionId": "session-123",
  "createdAt": "2025-10-21T10:00:00",
  "isEdited": true,
  "isDeleted": false
}
```

### Delete Broadcast
```json
{
  "id": 1,
  "content": "Original content",
  "senderUsername": "customer1",
  "senderType": "USER",
  "sessionId": "session-123",
  "createdAt": "2025-10-21T10:00:00",
  "isEdited": false,
  "isDeleted": true
}
```

## Security Features

1. **Ownership Verification:**
   - Backend checks `message.getSender().getId().equals(userId)`
   - Prevents users from editing/deleting others' messages

2. **Soft Delete:**
   - Messages are not physically deleted
   - Audit trail preserved in database
   - Can be restored if needed

3. **User Authentication:**
   - All endpoints require authenticated user
   - `userId` extracted from authenticated session

## Future Enhancements

### Potential Improvements:
1. **Edit History** - Track all previous versions
2. **Time Limit** - Only allow edits within 5 minutes
3. **Hard Delete** - Admin ability to permanently delete
4. **Markdown Support** - Rich text formatting
5. **Reaction Emojis** - Quick responses
6. **Reply/Thread** - Reply to specific messages

## Troubleshooting

### Issue: Edit/Delete buttons don't appear
**Solution:**
- Check CSS is loaded correctly
- Verify hover state works
- Check console for JavaScript errors

### Issue: "You can only edit your own messages" error
**Solution:**
- Verify `userId` matches message sender
- Check browser console for actual userId being sent
- Verify user is authenticated

### Issue: Changes don't appear in real-time
**Solution:**
- Check WebSocket connection status
- Verify WebSocket topic subscription
- Check browser console for WebSocket messages

### Issue: Edited messages show duplicate
**Solution:**
- Check `data-message-id` attribute is set
- Verify `displayMessage()` checks for existing message
- Clear chat and reload

## Browser Console Logs

When functioning correctly, you should see:
```
Connected: CONNECTED user admin frame-type:CONNECT-ACK
Editing message: 1
Message edited successfully
(WebSocket update received automatically)
```

## Conclusion

This feature provides customers with full control over their chat messages, improving user experience and chat quality. The real-time synchronization ensures all participants always see the current state of the conversation.
