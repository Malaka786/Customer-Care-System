# Sender Name Display Fix

## Problem
Error 500 when loading chat history, preventing customer names from displaying in messages.

## Root Cause
The `ChatMessage.sender` relationship uses `@ManyToOne(fetch = FetchType.LAZY)`, which means the User entity is not loaded by default. When the DTO tries to access `message.getSender().getUsername()`, it causes a LazyInitializationException because the Hibernate session has already closed.

## Solution Applied

### 1. Fixed ChatMessageRepository Query
**File**: `src/main/java/lk/sliit/customer_care/repository/ChatMessageRepository.java`

Added `JOIN FETCH` to eagerly load the sender:

```java
@Query("SELECT cm FROM ChatMessage cm JOIN FETCH cm.sender WHERE cm.chatSessionId = :sessionId AND cm.isDeleted = false ORDER BY cm.createdAt ASC")
List<ChatMessage> findByChatSessionIdAndIsDeletedFalseOrderByCreatedAtAsc(@Param("sessionId") String sessionId);
```

**What this does:**
- `JOIN FETCH cm.sender` - Tells JPA to load the User entity along with the ChatMessage in a single query
- Prevents LazyInitializationException
- Improves performance by reducing database queries

### 2. Enhanced ChatMessageDTO Constructor
**File**: `src/main/java/lk/sliit/customer_care/dto/ChatMessageDTO.java`

Improved null safety:

```java
public ChatMessageDTO(ChatMessage message) {
    this.content = message.getContent();
    // Safely get username with null checks
    if (message.getSender() != null) {
        this.senderUsername = message.getSender().getUsername();
    } else {
        this.senderUsername = "System";
    }
    this.senderType = message.getSenderType();
    this.sessionId = message.getChatSessionId();
    this.createdAt = message.getCreatedAt();
}
```

## Expected Behavior After Fix

### In Agent Chat Interface:
1. ✅ **Error 500 resolved** - Chat history loads successfully
2. ✅ **Customer names display correctly** - Shows actual customer username (e.g., "chanithi", "nalani")
3. ✅ **Agent names display correctly** - Shows actual agent username
4. ✅ **Message alignment correct**:
   - Customer messages on the LEFT with customer name
   - Agent messages on the RIGHT with agent name

### Example Message Display:
```
┌─────────────────────┐
│ chanithi            │  ← Customer name (left side)
│ Hello, I need help  │
└─────────────────────┘

                    ┌─────────────────────┐
                    │ Agent Name          │  ← Agent name (right side)
                    │ How can I help you? │
                    └─────────────────────┘
```

## Testing Steps

1. **Restart the application**:
   ```bash
   mvn spring-boot:run
   ```

2. **Create test messages**:
   - Login as customer → Send messages in live chat
   - Verify messages are saved to database

3. **Test agent view**:
   - Login as agent
   - Navigate to `/agent/chat`
   - Open browser console (F12)
   - Click on a customer session

4. **Verify in console**:
   ```
   📖 Loading chat history for session: session-xxxxx
   📡 Response status: 200 OK  ← Should be 200, not 500
   📨 Received messages: 3 messages
   ✅ Displaying 3 messages
   💬 Displaying message: {senderUsername: "chanithi", ...}
     → Customer message (left)
     → Sender: chanithi  ← Actual customer name
     → Content: Hello
   ```

5. **Verify in chat window**:
   - Messages load without "Error 500"
   - Customer names appear above their messages
   - Messages are on the correct sides

## Technical Details

### Before Fix:
```
ChatMessage (loaded)
  ↓
  sender (LAZY - not loaded)
  ↓
  User (❌ throws LazyInitializationException)
```

### After Fix:
```
ChatMessage + User (both loaded with JOIN FETCH)
  ↓
  sender (✅ already loaded)
  ↓
  User.username (✅ accessible)
```

### Database Query Generated:

**Before** (2 queries - N+1 problem):
```sql
SELECT * FROM chat_messages WHERE chat_session_id = ?;
-- Then for each message:
SELECT * FROM User WHERE id = ?;
```

**After** (1 query - optimized):
```sql
SELECT cm.*, u.* 
FROM chat_messages cm 
INNER JOIN User u ON cm.sender_id = u.id
WHERE cm.chat_session_id = ?
ORDER BY cm.created_at ASC;
```

## Benefits

1. **Performance**: Single query instead of N+1 queries
2. **Reliability**: No LazyInitializationException
3. **Correctness**: Always shows actual sender username
4. **Maintainability**: Clearer code with explicit fetching strategy

## Rollback (If Needed)

If you need to revert:

1. Remove `JOIN FETCH` from the query
2. Change fetch type to EAGER in ChatMessage entity:
   ```java
   @ManyToOne(fetch = FetchType.EAGER)
   private User sender;
   ```

Note: EAGER loading is less optimal than JOIN FETCH because it always loads the relationship, even when not needed.

## Related Files Changed

1. ✅ `ChatMessageRepository.java` - Added JOIN FETCH
2. ✅ `ChatMessageDTO.java` - Improved null handling

## Known Issues Resolved

- ❌ Error 500 when loading chat history
- ❌ LazyInitializationException
- ❌ Customer names not displaying
- ❌ N+1 query problem

All issues are now ✅ FIXED!
